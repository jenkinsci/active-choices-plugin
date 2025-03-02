/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2020 Ioannis Moutsatsos, Bruno P. Kinoshita
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

package org.biouno.unochoice.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;

import hudson.slaves.EnvironmentVariablesNodeProperty;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

/**
 * Test the {@link Utils} utility class.
 */
@WithJenkins
class TestUtils {

    @Test
    void testIsSelected(JenkinsRule j) {
        assertTrue(Utils.isSelected("a:selected"));
        assertTrue(Utils.isSelected("a:selected:disabled"));
        assertTrue(Utils.isSelected("a:disabled:selected"));
        assertFalse(Utils.isSelected(null));
        assertFalse(Utils.isSelected(""));
        assertFalse(Utils.isSelected("a"));
        assertFalse(Utils.isSelected("a:selected example"));
    }

    @Test
    void testEscapeSelected(JenkinsRule j) {
        String escaped = Utils.escapeSelected(null);
        assertEquals("", escaped);

        escaped = Utils.escapeSelected("");
        assertEquals("", escaped);

        escaped = Utils.escapeSelected("a:selected");
        assertEquals("a", escaped);

        escaped = Utils.escapeSelected("a:selected:disabled");
        assertEquals("a:disabled", escaped);

        escaped = Utils.escapeSelected("a:disabled:selected");
        assertEquals("a:disabled", escaped);

        escaped = Utils.escapeSelected("a:selected example");
        assertEquals("a:selected example", escaped);

        escaped = Utils.escapeSelected("a");
        assertEquals("a", escaped);
    }

    @Test
    void testIsDisabled(JenkinsRule j) {
        assertTrue(Utils.isDisabled("a:disabled"));
        assertTrue(Utils.isDisabled("a:selected:disabled"));
        assertTrue(Utils.isDisabled("a:disabled:selected"));
        assertFalse(Utils.isDisabled(null));
        assertFalse(Utils.isDisabled(""));
        assertFalse(Utils.isDisabled("a"));
        assertFalse(Utils.isDisabled("a:disabled example"));
    }

    @Test
    void testEscapeDisabled(JenkinsRule j) {
        String escaped = Utils.escapeDisabled(null);
        assertEquals("", escaped);

        escaped = Utils.escapeDisabled("");
        assertEquals("", escaped);

        escaped = Utils.escapeDisabled("a:disabled");
        assertEquals("a", escaped);

        escaped = Utils.escapeDisabled("a:selected:disabled");
        assertEquals("a:selected", escaped);

        escaped = Utils.escapeDisabled("a:disabled:selected");
        assertEquals("a:selected", escaped);

        escaped = Utils.escapeDisabled("a:disabled example");
        assertEquals("a:disabled example", escaped);

        escaped = Utils.escapeDisabled("a");
        assertEquals("a", escaped);
    }

    @Test
    void testEscapeSelectedAndDisabled(JenkinsRule j) {
        String escaped = Utils.escapeSelectedAndDisabled(null);
        assertEquals("", escaped);

        escaped = Utils.escapeSelectedAndDisabled("");
        assertEquals("", escaped);

        escaped = Utils.escapeSelectedAndDisabled("a:disabled");
        assertEquals("a", escaped);

        escaped = Utils.escapeSelectedAndDisabled("a:selected:disabled");
        assertEquals("a", escaped);

        escaped = Utils.escapeSelectedAndDisabled("a:disabled:selected");
        assertEquals("a", escaped);

        escaped = Utils.escapeSelectedAndDisabled("a:selected example");
        assertEquals("a:selected example", escaped);

        escaped = Utils.escapeSelectedAndDisabled("a:disabled example");
        assertEquals("a:disabled example", escaped);

        escaped = Utils.escapeSelectedAndDisabled("a:selected:disabled example");
        assertEquals("a:selected:disabled example", escaped);

        escaped = Utils.escapeSelectedAndDisabled("a:disabled:selected example");
        assertEquals("a:disabled:selected example", escaped);

        escaped = Utils.escapeSelectedAndDisabled("a");
        assertEquals("a", escaped);
    }

    @Test
    void testRandomParameterName(JenkinsRule j) {
        String paramName = Utils.createRandomParameterName("test", "param");
        assertNotNull(paramName);
        assertTrue(paramName.startsWith("test"));
        assertTrue(paramName.endsWith("param"));

        paramName = Utils.createRandomParameterName(null, "param");
        assertNotNull(paramName);
        assertFalse(paramName.startsWith("test"));
        assertTrue(paramName.endsWith("param"));

        paramName = Utils.createRandomParameterName("test", null);
        assertNotNull(paramName);
        assertTrue(paramName.startsWith("test"));
        assertFalse(paramName.endsWith("param"));
    }

    @Test
    void testGetGlobalNodeProperties(JenkinsRule j) {
        Map<String, String> testMap = new HashMap<>();
        testMap.put("time", "20:13:13");
        EnvironmentVariablesNodeProperty.Entry entry = new EnvironmentVariablesNodeProperty.Entry("time",
                testMap.get("time"));
        EnvironmentVariablesNodeProperty envVarsNodeProp = new EnvironmentVariablesNodeProperty(entry);
        j.jenkins.getGlobalNodeProperties().add(envVarsNodeProp);
        Map<?, ?> map = Utils.getGlobalNodeProperties();
        assertEquals("20:13:13", map.values().iterator().next());
    }

}
