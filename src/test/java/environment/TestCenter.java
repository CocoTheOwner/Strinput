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
package environment;

import nl.codevs.strinput.system.*;
import nl.codevs.strinput.system.text.Str;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * StrCenter test implementation.
 * @author Sjoerd van de Goor
 * @since v0.1
 */
public class TestCenter extends StrCenter {

    public static TestCenter SUT = new TestCenter();

    /**
     * Create a new command center.<br>
     * Make sure to point command calls to {@link #onCommand(List, StrUser)}
     */
    public TestCenter() {
        super(
                new File("testSettings"),
                new StrUser() {
                    @Override
                    public @NotNull String getName() {
                        return "Console";
                    }

                    @Override
                    public void sendMessage(@NotNull Str message) {
                        System.out.println(message.toHumanReadable());
                    }

                    @Override
                    public boolean supportsClickables() {
                        return false;
                    }

                    @Override
                    public void playSound(@NotNull StrSoundEffect sfx) {

                    }

                    @Override
                    public boolean supportsContext() {
                        return false;
                    }

                    @Override
                    public boolean hasPermission(@NotNull String permission) {
                        return true;
                    }
                },
                new TestRoot()
        );

        // Set async to false so we can run tests sync
        Env.touch(getConsole());
        Env.touch(this);
        getSettings().setAsync(false);
        getSettings().setSettingsCommands(false);
    }

    public static void main(String[] args) {
        System.out.println(
                String.join(
                        "\n",
                        SUT.getListing(
                                "  ",
                                new ArrayList<>(List.of(
                                        "test",
                                        "mult",
                                        "1",
                                        "2"
                                ))
                        )
                )
        );
    }

    /**
     * Run a function sync (will run if {@link StrSettings#isAsync()} is false or when {@link StrInput#sync()} is true).
     *
     * @param runnable the runnable to run
     */
    @Override
    public void runSync(@NotNull Runnable runnable) {
        runnable.run();
    }
}
