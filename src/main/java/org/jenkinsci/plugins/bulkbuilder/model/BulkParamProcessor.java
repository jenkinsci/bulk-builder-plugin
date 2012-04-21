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

package org.jenkinsci.plugins.bulkbuilder.model;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author simon
 */
public class BulkParamProcessor {

    private static final Logger LOGGER = Logger
	    .getLogger(BulkParamProcessor.class.getName());

    private String rawParams;

    public BulkParamProcessor(String params) {
	this.rawParams = params;
    }

    /**
     * Process and return input parameter string
     *
     * @return
     */
    public Map<String, String> getProjectParams() {
	if (rawParams == null) {
	    return null;
	}

	StringTokenizer tokeniser = new StringTokenizer(rawParams, "&");

	Map<String, String> values = new HashMap<String, String>(
		tokeniser.countTokens());

	while (tokeniser.hasMoreTokens()) {
	    String rawParam = tokeniser.nextToken();

	    String[] split = rawParam.split("=");
	    if (split.length == 2) {
		values.put(split[0], split[1]);

		if (LOGGER.isLoggable(Level.INFO)) {
		    LOGGER.log(Level.INFO, "Added {0}", split);
		}
	    }
	}

	if (values.isEmpty()) {
	    return null;
	}

	return values;
    }
}
