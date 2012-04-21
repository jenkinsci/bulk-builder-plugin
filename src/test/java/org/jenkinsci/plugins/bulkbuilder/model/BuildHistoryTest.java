/*
 * The MIT License
 *
 * Copyright (c) 2010-2011 Simon Westcott
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.jenkinsci.plugins.bulkbuilder.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import org.junit.Before;
import org.junit.Test;

/**
 * @author simon
 */
public class BuildHistoryTest {

    private String pattern = "test pattern";
    private BuildHistoryItem item;
    private BuildHistory history = new BuildHistory();

    @Before
    public void setUp() {
        item = new BuildHistoryItem(pattern);
        history.add(item);
    }

    /**
     * Test size
     */
    @Test
    public void testAddAndSize() {
        assertEquals(1, history.size());
        history.add(new BuildHistoryItem("second"));
        assertEquals(2, history.size());
        history.add(new BuildHistoryItem("third"));
        assertEquals(3, history.size());
    }

    /**
     * Test clear
     */
    @Test
    public void testClear() {
        assertNotSame(0, history.size());
        history.clear();
        assertEquals(0, history.size());
    }

    /**
     * Test history list does not exceed the max size
     */
    @Test
    public void testHistoryListDoesNotExceedMaxSize() {

        while (history.size() < BuildHistory.SIZE) {
            history.add(new BuildHistoryItem("test"));
        }

        assertEquals(BuildHistory.SIZE, history.size());

        history.add(new BuildHistoryItem("one more"));
        assertEquals(BuildHistory.SIZE, history.size());
    }
}
