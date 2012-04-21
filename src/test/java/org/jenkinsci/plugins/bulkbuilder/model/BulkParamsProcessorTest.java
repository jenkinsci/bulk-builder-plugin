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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;

/**
 * @author simon
 */
public class BulkParamsProcessorTest {

    @Test
    public void testGetProjectParamsWithInvalidParam() {
        BulkParamProcessor params = new BulkParamProcessor("param1+value1");
        Map<String, String> projectParams = params.getProjectParams();

        assertNull(projectParams);
    }

    @Test
    public void testGetProjectParamsSimple() {
        BulkParamProcessor params = new BulkParamProcessor("param1=value1");
        Map<String, String> projectParams = params.getProjectParams();

        Map<String, String> expected = new HashMap<String, String>(1);
        expected.put("param1", "value1");

        assertEquals(expected, projectParams);
    }

    @Test
    public void testGetProjectParamsAdvanced() {
        BulkParamProcessor params = new BulkParamProcessor("param2=value2&param3=value3&param4=value4");
        Map<String, String> projectParams = params.getProjectParams();

        Map<String, String> expected = new HashMap<String, String>(3);
        expected.put("param2", "value2");
        expected.put("param3", "value3");
        expected.put("param4", "value4");

        assertEquals(expected, projectParams);
    }

    @Test
    public void testGetProjectParamsMixedValidAndInvalidParams() {
        BulkParamProcessor params = new BulkParamProcessor("param5=value5&param6*value6");
        Map<String, String> projectParams = params.getProjectParams();

        Map<String, String> expected = new HashMap<String, String>(1);
        expected.put("param5", "value5");

        assertEquals(expected, projectParams);
    }
}