/*
 * This file is part of the Strinput distribution (https://github.com/CocoTheOwner/Strinput).
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
package tests;

import nl.codevs.strinput.system.util.NGram;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * NGram string matching tests.
 * @author Sjoerd van de Goor
 * @since v0.1
 */
public class TestNGram {

    @Test
    public void testNGram() {
        String input = "applepie";

        String[] options = {
                "applepie",
                "apple",
                "ap",
                "pplp",
                "pie"
        };

        double[] results = NGram.ngramMatching(input, List.of(options));

        for (int i = 0; i < results.length; i++) {
            for (int j = i + 1; j < results.length; j++) {
                System.out.println(options[i] + " (" + results[i] + ") > " + options[j] + " (" + results[j] + ")");
                assertTrue(results[i] >= results[j]);
            }
        }
    }

    @Test
    public void testSingle() {
        String i = "applepie";
        String j = "apple";
        double sj = (double) NGram.nGramMatch(i, j) / NGram.nGramMatch(i, i);
        System.out.println(j + " (" + sj + ") should be over 0.5");
        assertTrue(sj > 0.5);
    }

    /**
     * Testing.
     * @param args not used
     */
    public static void main(String... args) {
        String input = "dungeoneer1";

        String[] options = {
                "gussie842",
                "cocodef9",
                "engineer",
                "killer",
                "reeeee",
                "dungeon",
                "dung",
                "deng",
                "doongeon",
                "ungeon",
                "ungeoneer",
                "dungeoneer1",
                "d"
        };

        double[] results = NGram.ngramMatching(input, List.of(options));
        System.out.println("Input: " + input);
        for (int i = 0; i < results.length; i++) {
            System.out.println(options[i] + " -> " + results[i]);
        }
    }
}
