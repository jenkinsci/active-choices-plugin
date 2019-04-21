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

package org.biouno.unochoice;

import org.biouno.unochoice.model.GroovyScript;
import org.biouno.unochoice.util.Utils;
import org.jenkinsci.plugins.scriptsecurity.sandbox.groovy.SecureGroovyScript;
import org.jenkinsci.plugins.scriptsecurity.scripts.ScriptApproval;
import org.jenkinsci.plugins.scriptsecurity.scripts.languages.GroovyLanguage;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.kohsuke.stapler.HttpResponses;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestDynamicReferenceParameter {

    private final static String SCRIPT = "return ['D', 'C', 'B', 'A']";
    private final static String FALLBACK_SCRIPT = "";
    private final static String VISIBILITY_SCRIPT = "return true";
    private final static String VISIBILITY_FALLBACK_SCRIPT = "return false";
    private final static String RANDOM_NAME = Utils.createRandomParameterName("choice-parameter", "test");

    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Before
    public void setUp() {
        ScriptApproval.get().preapprove(SCRIPT, GroovyLanguage.get());
        ScriptApproval.get().preapprove(FALLBACK_SCRIPT, GroovyLanguage.get());
    }

    @Test
    public void testConstructor() {
        GroovyScript script = new GroovyScript(new SecureGroovyScript(SCRIPT, Boolean.FALSE, null),
                new SecureGroovyScript(FALLBACK_SCRIPT, Boolean.FALSE, null));
        GroovyScript visibilityScript = new GroovyScript(new SecureGroovyScript(VISIBILITY_SCRIPT, Boolean.FALSE, null),
                new SecureGroovyScript(VISIBILITY_FALLBACK_SCRIPT, Boolean.FALSE, null));
        DynamicReferenceParameter param = new DynamicReferenceParameter(
                "param000", "description", RANDOM_NAME,
                script, visibilityScript, DynamicReferenceParameter.ELEMENT_TYPE_FORMATTED_HIDDEN_HTML,
                "param001, param002", true);

        assertEquals("param000", param.getName());
        assertEquals("description", param.getDescription());
        assertEquals(script, param.getScript());
        assertEquals(visibilityScript, param.getVisibilityScript());
        assertEquals(RANDOM_NAME, param.getRandomName());
        assertEquals("ET_FORMATTED_HIDDEN_HTML", param.getChoiceType());
        assertEquals("param001, param002", param.getReferencedParameters());
        assertTrue(param.getOmitValueField());
    }

    @Test
    public void testParameters() {
        GroovyScript script = new GroovyScript(
                new SecureGroovyScript(SCRIPT, Boolean.FALSE, null),
                new SecureGroovyScript(FALLBACK_SCRIPT, Boolean.FALSE, null));
        GroovyScript visibilityScript = new GroovyScript(
                new SecureGroovyScript(VISIBILITY_SCRIPT, Boolean.FALSE, null),
                new SecureGroovyScript(VISIBILITY_FALLBACK_SCRIPT, Boolean.FALSE, null));
        DynamicReferenceParameter param = new DynamicReferenceParameter(
                "param000", "description", RANDOM_NAME,
                script, visibilityScript, DynamicReferenceParameter.ELEMENT_TYPE_FORMATTED_HIDDEN_HTML,
                "param001, param002", true);
        assertTrue(param.getParameters().isEmpty());

        try {
            param.doUpdate("param001=A__LESEP__param002=B__LESEP__param003=");
        } catch (HttpResponses.HttpResponseException response) {
            // ignore
        }

        Map<String, String> expected = new LinkedHashMap<String, String>();
        expected.put("param001", "A");
        expected.put("param002", "B");
        expected.put("param003", "");
        assertEquals(expected, param.getParameters());

        try {
            param.doUpdate("");
        } catch (HttpResponses.HttpResponseException response) {
            // ignore
        }
        expected.clear();
        assertEquals(expected, param.getParameters());

        assertEquals(Arrays.asList("param001", "param002"), Arrays.asList(param.getReferencedParametersAsArray()));
    }

    @Test
    public void testVisibility() {
        GroovyScript script = new GroovyScript(
                new SecureGroovyScript(SCRIPT, Boolean.FALSE, null),
                new SecureGroovyScript(FALLBACK_SCRIPT, Boolean.FALSE, null));
        GroovyScript visibilityScript = new GroovyScript(
                new SecureGroovyScript(VISIBILITY_SCRIPT, Boolean.FALSE, null),
                new SecureGroovyScript(VISIBILITY_FALLBACK_SCRIPT, Boolean.FALSE, null));
        DynamicReferenceParameter param = new DynamicReferenceParameter(
                "param000", "description", RANDOM_NAME,
                script, visibilityScript, DynamicReferenceParameter.ELEMENT_TYPE_FORMATTED_HIDDEN_HTML,
                "param001, param002", true);

        assertTrue(param.isVisible());
    }

    @Test
    public void testDefaultVisibility() {
        GroovyScript script = new GroovyScript(
                new SecureGroovyScript(SCRIPT, Boolean.FALSE, null),
                new SecureGroovyScript(FALLBACK_SCRIPT, Boolean.FALSE, null));
        GroovyScript visibilityScript = new GroovyScript(
                new SecureGroovyScript("", Boolean.FALSE, null),
                new SecureGroovyScript(VISIBILITY_FALLBACK_SCRIPT, Boolean.FALSE, null));
        DynamicReferenceParameter param = new DynamicReferenceParameter(
                "param000", "description", RANDOM_NAME,
                script, visibilityScript, DynamicReferenceParameter.ELEMENT_TYPE_FORMATTED_HIDDEN_HTML,
                "param001, param002", true);

        assertTrue(param.isVisible());
    }

    @Test
    public void testInvisibility() {
        GroovyScript script = new GroovyScript(
                new SecureGroovyScript(SCRIPT, Boolean.FALSE, null),
                new SecureGroovyScript(FALLBACK_SCRIPT, Boolean.FALSE, null));
        GroovyScript visibilityScript = new GroovyScript(
                new SecureGroovyScript(VISIBILITY_FALLBACK_SCRIPT, Boolean.FALSE, null),
                new SecureGroovyScript(VISIBILITY_FALLBACK_SCRIPT, Boolean.FALSE, null));
        DynamicReferenceParameter param = new DynamicReferenceParameter(
                "param000", "description", RANDOM_NAME,
                script, visibilityScript, DynamicReferenceParameter.ELEMENT_TYPE_FORMATTED_HIDDEN_HTML,
                "param001, param002", true);

        assertFalse(param.isVisible());
    }

    @Test
    public void testCascadedVisibility() {
        GroovyScript script = new GroovyScript(new SecureGroovyScript(SCRIPT, Boolean.FALSE, null),
                new SecureGroovyScript(FALLBACK_SCRIPT, Boolean.FALSE, null));
        GroovyScript visibilityScript = new GroovyScript(
                new SecureGroovyScript("return param001=='A' && param002=='B'", Boolean.FALSE, null),
                new SecureGroovyScript(VISIBILITY_FALLBACK_SCRIPT, Boolean.FALSE, null));
        DynamicReferenceParameter param = new DynamicReferenceParameter(
                "param000", "description", RANDOM_NAME,
                script, visibilityScript, DynamicReferenceParameter.ELEMENT_TYPE_FORMATTED_HIDDEN_HTML,
                "param001, param002", true);

        try {
            param.doUpdate("param001=A__LESEP__param002=B");
        } catch (HttpResponses.HttpResponseException response) {
            // ignore
        }

        assertTrue(param.isVisible());
    }

    @Test
    public void testCascadedInvisibility() {
        GroovyScript script = new GroovyScript(new SecureGroovyScript(SCRIPT, Boolean.FALSE, null),
                new SecureGroovyScript(FALLBACK_SCRIPT, Boolean.FALSE, null));
        GroovyScript visibilityScript = new GroovyScript(
                new SecureGroovyScript("return param001=='A' && param002=='A'", Boolean.FALSE, null),
                new SecureGroovyScript(VISIBILITY_FALLBACK_SCRIPT, Boolean.FALSE, null));
        DynamicReferenceParameter param = new DynamicReferenceParameter(
                "param000", "description", RANDOM_NAME,
                script, visibilityScript, DynamicReferenceParameter.ELEMENT_TYPE_FORMATTED_HIDDEN_HTML,
                "param001, param002", true);

        try {
            param.doUpdate("param001=A__LESEP__param002=B");
        } catch (HttpResponses.HttpResponseException response) {
            // ignore
        }

        assertFalse(param.isVisible());
    }
}
