/*
 * The MIT License
 *
 * Copyright (c) 2010 Simon Westcott
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

package org.jvnet.hudson.plugins.bulkbuilder.model;

import hudson.Plugin;
import java.util.LinkedList;
import java.util.List;

/**
 * @author simon
 */
public class BuildHistory extends Plugin {

    /**
     * Size of the history to maintain
     */
    public static final Integer SIZE = 10;

    /**
     * History list
     */
    private LinkedList<BuildHistoryItem> items = new LinkedList<BuildHistoryItem>();

    /**
     * Add a new pattern, over-writing any previous occurrences
     * @param pattern
     */
    public final void add(BuildHistoryItem pattern) {
        items.addFirst(pattern);

        if (items.size() > SIZE) {
            items.removeLast();
        }
    }

    /**
     * Return list of build patterns
     *
     * @return
     */
    public final List<BuildHistoryItem> getAll() {
        return items;
    }

    /**
     * Return the size of the build history
     *
     * @return
     */
    public final int size() {
        return items.size();
    }

    /**
     * Remove all items from build history
     */
    public final void clear() {
        items.clear();
    }
}
