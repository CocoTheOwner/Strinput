/*
 * This file is part of the Strinput distribution.
 * (https://github.com/CocoTheOwner/Strinput)
 * Copyright (c) 2021 Sjoerd van de Goor.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package nl.codevs.strinput.system.virtual;

import nl.codevs.strinput.system.StrInput;
import nl.codevs.strinput.system.StrCategory;
import nl.codevs.strinput.system.Env;
import nl.codevs.strinput.system.StrUser;
import nl.codevs.strinput.system.text.C;
import nl.codevs.strinput.system.text.Str;
import nl.codevs.strinput.system.util.NGram;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A {@link StrInput} annotated method's virtual representation.
 *
 * @author Sjoerd van de Goor
 * @since v0.1
 */
public final class StrVirtualCategory implements StrVirtual {

    /**
     * Parent category.
     */
    private final StrVirtualCategory parent;
    /**
     * Commands in this category.
     */
    private final List<StrVirtualCommand> commands;
    /**
     * Subcategories.
     */
    private final List<StrVirtualCategory> subCats;
    /**
     * Annotation on this category.
     */
    private final StrInput annotation;
    /**
     * Instance of this category.
     */
    private final StrCategory instance;

    /**
     * Get commands.
     * @return the commands
     */
    @Contract(" -> new")
    public @NotNull List<StrVirtualCommand> getCommands() {
        return new ArrayList<>(commands);
    }

    /**
     * Get subcats.
     * @return the subcats
     */
    @Contract(" -> new")
    public @NotNull List<StrVirtualCategory> getSubCats() {
        return new ArrayList<>(subCats);
    }

    /**
     * Get the parent virtual.
     *
     * @return the parent virtual
     */
    @Override
    public @Nullable StrVirtualCategory getParent() {
        return parent;
    }

    /**
     * Create a new virtual category.<br>
     * Assumes the {@code instance} is annotated by @{@link StrInput}
     * @param previous the parent category (null if root)
     * @param object an instance of the underlying class
     */
    public StrVirtualCategory(
            @Nullable final StrVirtualCategory previous,
            @NotNull final StrCategory object
    ) {
        this.parent = previous;
        this.annotation = object.getClass().getAnnotation(StrInput.class);
        this.instance = object;
        this.commands = setupCommands();
        this.subCats = setupSubCats();
    }

    /**
     * Get the default virtual name
     * when the annotation was not given a specific name.
     *
     * @return the name
     */
    @Override
    public @NotNull String getDefaultName() {
        return instance.getClass().getSimpleName();
    }

    /**
     * Get the annotation on the class/method.
     *
     * @return the annotation
     */
    @Override
    public @NotNull StrInput getAnnotation() {
        return annotation;
    }

    /**
     * Get the class instance this virtual category manages.
     * @return the class instance of this virtual
     */
    public @NotNull StrCategory getInstance() {
        return instance;
    }

    /**
     * Run the virtual.
     *
     * @param arguments the remaining arguments
     * @return true if this virtual ran successfully
     */
    @Override
    public boolean run(@NotNull final List<String> arguments) {
        if (arguments.size() == 0) {
            debug(new Str("Sending help to user"));
            help(user());
            return true;
        }
        List<StrVirtual> options = new ArrayList<>();
        options.addAll(subCats);
        options.addAll(commands);
        int n = options.size();
        options = options.stream().filter(
                o -> o.doesMatchUser(user())
        ).collect(Collectors.toList());
        if (n - options.size() != 0) {
            debug(new Str(C.B).a(
                    "Virtual" + getName() + " filtered out "
                            + (n - options.size()) + " options!"
            ));
        }

        String next = arguments.remove(0);

        List<StrVirtual> opt = NGram.sortByNGram(
                next,
                options,
                Env.settings().getMatchThreshold()
        );

        debug(new Str("Options: ", C.G)
                .a(new Str(opt
                        .stream()
                        .map(o -> String.join("/", o.getNames()))
                        .collect(Collectors.joining(", ")),
                        C.B)
                )
        );

        debug(new Str(C.G).a(
                "Attempting to find a match in "
                        + options.size() + " options with input: " + next)
        );
        for (StrVirtual option : opt) {
            if (option.run(new ArrayList<>(arguments))) {
                return true;
            } else {
                debug(new Str(C.R).a(
                        "Virtual " + option.getName()
                                + " matched with "
                                + next + " but failed to run!"
                ));
            }
        }
        debug(new Str(C.R).a(
                "Virtual " + getName()
                        + " failed to find a matching option for "
                        + next + " and returns false"
        ));
        return false;
    }

    /**
     * Send help for this virtual to a user.
     *
     * @param user the user to send help to
     */
    @Override
    public void help(final @NotNull StrUser user) {
        List<Str> helpMessages = new ArrayList<>();

        user.sendMessage(helpMessages);
    }

    /**
     * Calculate {@link StrVirtualCommand}s in this category.
     * @return the list of setup virtual commands
     */
    private @NotNull List<StrVirtualCommand> setupCommands() {
        List<StrVirtualCommand> setupCommands = new ArrayList<>();

        for (Method command : instance.getClass().getDeclaredMethods()) {
            if (Modifier.isStatic(command.getModifiers())
                    || Modifier.isFinal(command.getModifiers())
                    || Modifier.isPrivate(command.getModifiers())
            ) {
                continue;
            }

            if (!command.isAnnotationPresent(StrInput.class)) {
                continue;
            }

            setupCommands.add(new StrVirtualCommand(this, command));
        }

        return setupCommands;
    }

    /**
     * Calculate all {@link StrVirtualCategory}s in this category.
     * @return the list of setup virtual categories
     */
    private @NotNull List<StrVirtualCategory> setupSubCats() {
        List<StrVirtualCategory> setupCategories = new ArrayList<>();

        // Get all previous categories in the chain

        for (Field subCat : instance.getClass().getDeclaredFields()) {
            if (Modifier.isStatic(subCat.getModifiers())
                    || Modifier.isFinal(subCat.getModifiers())
                    || Modifier.isTransient(subCat.getModifiers())
                    || Modifier.isVolatile(subCat.getModifiers())
            ) {
                continue;
            }

            // Class must be annotated by StrInput
            if (!subCat.getType().isAnnotationPresent(StrInput.class)) {
                continue;
            }

            try {
                new FileWriter(
                        subCat.getClass().getSimpleName() + ".txt"
                ).write(subCat.toGenericString());
            } catch (IOException e) {
                e.printStackTrace();
            }

            subCat.setAccessible(true);
            Object childRoot;
            try {
                childRoot = subCat.get(instance);
            } catch (IllegalAccessException e) {
                debug(new Str("Could not get child \""
                        + subCat.getName() + "\" from instance: \""
                        + instance.getClass().getSimpleName() + "\""
                ));
                debug(new Str("Because of: " + e.getMessage()));
                continue;
            }
            if (childRoot == null) {
                try {
                    childRoot = subCat.getType().getConstructor().newInstance();
                    subCat.set(instance, childRoot);
                } catch (NoSuchMethodException e) {
                    debug(new Str("Method \"" + subCat.getName()
                            + "\" does not exist in instance: \""
                            + instance.getClass().getSimpleName() + "\""
                    ));
                    debug(new Str("Because of: " + e.getMessage()));
                } catch (IllegalAccessException e) {
                    debug(new Str("Could get, but not access child \""
                            + subCat.getName() + "\" from instance: \""
                            + instance.getClass().getSimpleName() + "\""
                    ));
                    debug(new Str("Because of: " + e.getMessage()));
                } catch (InstantiationException e) {
                    debug(new Str("Could not instantiate \"" + subCat.getName()
                            + "\" from instance: \""
                            + instance.getClass().getSimpleName() + "\""
                    ));
                    debug(new Str("Because of: " + e.getMessage()));
                } catch (InvocationTargetException e) {
                    debug(new Str("Invocation exception on \""
                            + subCat.getName() + "\" from instance: \""
                            + instance.getClass().getSimpleName() + "\""
                    ));
                    debug(new Str("Because of: " + e.getMessage()));
                    debug(new Str("Underlying exception: "
                            + e.getTargetException().getMessage()));
                }
            }

            if (childRoot == null) {
                continue;
            }

            setupCategories.add(
                    new StrVirtualCategory(this, (StrCategory) childRoot)
            );
        }

        return setupCategories;
    }

    /**
     * List this node and any sub-virtuals
     * to form a string-based graph representation in {@code current}.
     * @param prefix prefix all substrings with this prefix,
     *              so it aligns with previous nodes.
     * @param spacing the space to append to the prefix
     *               for subsequent sub-virtuals
     * @param current the current graph
     * @param exampleInput an example input for NGram matching
     */
    @Contract(mutates = "param3")
    public void getListing(
            @NotNull final String prefix,
            @NotNull final String spacing,
            @NotNull final List<String> current,
            @NotNull final List<String> exampleInput
    ) {
        current.add(prefix
                + getName()
                + (getAliases().isEmpty() ? "" : " (" + getAliases() + ")")
                + " cmds: " + getCommands().size()
                + " / subcs: " + getSubCats().size()
                + " matches with " + exampleInput.get(0)
                + " @ " + ((double) NGram.nGramMatch(
                        exampleInput.get(0), getName())
                / NGram.nGramMatch(getName(), getName())));
        for (StrVirtualCategory subCat : getSubCats()) {
            subCat.getListing(
                    prefix + spacing,
                    spacing, current,
                    new ArrayList<>(exampleInput.subList(
                            1,
                            exampleInput.size()
                    ))
            );
        }
        for (StrVirtualCommand command : getCommands()) {
            command.getListing(
                    prefix + spacing,
                    spacing, current,
                    new ArrayList<>(exampleInput.subList(
                            1,
                            exampleInput.size()
                    ))
            );
        }
    }
}
