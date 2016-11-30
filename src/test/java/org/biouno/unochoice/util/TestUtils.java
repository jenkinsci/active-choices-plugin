/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2016 Ioannis Moutsatsos, Bruno P. Kinoshita
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jenkinsci.plugins.scriptler.config.Script;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jvnet.hudson.test.JenkinsRule;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import hudson.slaves.EnvironmentVariablesNodeProperty;

/**
 * Test the {@link Utils} utility class.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ Utils.class })
@PowerMockIgnore({"javax.crypto.*" })
public class TestUtils {

    @Rule
    JenkinsRule j = new JenkinsRule();

    @Test
    public void testGetAllScriptlerScripts() {
        Set<Script> fakeScripts = new HashSet<Script>();
        fakeScripts.add(new Script("id", "name", "comment", true, "originCatalog", "originScript", "originDate", true,
                null, true));
        PowerMockito.mockStatic(Utils.class);
        PowerMockito.when(Utils.getAllScriptlerScripts()).thenReturn(fakeScripts);
        Set<Script> scripts = Utils.getAllScriptlerScripts();
        assertTrue(scripts.size() == 1);
    }

    @Test
    public void testIsSelected() {
        assertTrue(Utils.isSelected("a:selected"));
        assertFalse(Utils.isSelected(null));
        assertFalse(Utils.isSelected(""));
        assertFalse(Utils.isSelected("a"));
    }

    @Test
    public void testEscapeSelected() {
        String escaped = Utils.escapeSelected(null);
        assertEquals("", escaped);

        escaped = Utils.escapeSelected("");
        assertEquals("", escaped);

        escaped = Utils.escapeSelected("a:selected");
        assertEquals("a", escaped);

        escaped = Utils.escapeSelected("a");
        assertEquals("a", escaped);
    }

    @Test
    public void testRandomParameterName() {
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
    public void testGetGlobalNodeProperties() {
        Map<?, ?> map = Utils.getGlobalNodeProperties();
        Map<String, String> testMap = new HashMap<String, String>();
        testMap.put("time", "20:13:13");
        EnvironmentVariablesNodeProperty.Entry entry = new EnvironmentVariablesNodeProperty.Entry("time",
                testMap.get("time"));
        EnvironmentVariablesNodeProperty envVarsNodeProp = new EnvironmentVariablesNodeProperty(entry);
        j.jenkins.getGlobalNodeProperties().add(envVarsNodeProp);
        map = Utils.getGlobalNodeProperties();
        assertEquals("20:13:13", map.values().iterator().next());
    }

}
