/*
 * The MIT License (MIT)
 * 
 * Copyright (c) <2014> <Ioannis Moutsatsos, Bruno P. Kinoshita>
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import org.biouno.unochoice.model.GroovyScript;
import org.biouno.unochoice.util.Utils;
import org.jenkinsci.plugins.scriptsecurity.scripts.ScriptApproval;
import org.jenkinsci.plugins.scriptsecurity.scripts.languages.GroovyLanguage;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class TestCascadeChoiceParameter {

    private final static String SCRIPT = "return ['a', 'b']";
    private final static String FALLBACK_SCRIPT = "return ['EMPTY!']";
    private final static String VISIBILITY_SCRIPT = "return true";
    private final static String VISIBILITY_FALLBACK_SCRIPT = "return false";
    private final static String RANDOM_NAME = Utils.createRandomParameterName("choice-parameter", "test");

    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Before
    public void setUp() throws Exception {
        ScriptApproval.get().preapprove(SCRIPT, GroovyLanguage.get());
        ScriptApproval.get().preapprove(FALLBACK_SCRIPT, GroovyLanguage.get());
    }

    @Test
    public void testConstructor() {
        GroovyScript script = new GroovyScript(SCRIPT, FALLBACK_SCRIPT);
        GroovyScript visibilityScript = new GroovyScript(VISIBILITY_SCRIPT, VISIBILITY_FALLBACK_SCRIPT);
        CascadeChoiceParameter param = new CascadeChoiceParameter(
            "param000", "description", RANDOM_NAME,
            script, visibilityScript, CascadeChoiceParameter.ELEMENT_TYPE_FORMATTED_HIDDEN_HTML,
            "param001, param002", true);

        assertEquals("param000", param.getName());
        assertEquals("description", param.getDescription());
        assertEquals(script, param.getScript());
        assertEquals(visibilityScript, param.getVisibilityScript());
        assertEquals("ET_FORMATTED_HIDDEN_HTML", param.getChoiceType());
        assertEquals("param001, param002", param.getReferencedParameters());
        assertTrue(param.getFilterable());
    }

    @Test
    public void testParameters() {
        GroovyScript script = new GroovyScript(SCRIPT, FALLBACK_SCRIPT);
        GroovyScript visibilityScript = new GroovyScript(VISIBILITY_SCRIPT, VISIBILITY_FALLBACK_SCRIPT);
        CascadeChoiceParameter param = new CascadeChoiceParameter(
            "param000", "description", RANDOM_NAME,
            script, visibilityScript, CascadeChoiceParameter.ELEMENT_TYPE_FORMATTED_HIDDEN_HTML,
            "param001, param002", true);
        assertTrue(param.getParameters().isEmpty());

        param.doUpdate("param001=A__LESEP__param002=B__LESEP__param003=");

        Map<String, String> expected = new LinkedHashMap<String, String>();
        expected.put("param001", "A");
        expected.put("param002", "B");
        expected.put("param003", "");
        assertEquals(expected, param.getParameters());

        param.doUpdate("");
        expected.clear();
        assertEquals(expected, param.getParameters());

        assertEquals(Arrays.asList("param001", "param002"), Arrays.asList(param.getReferencedParametersAsArray()));
    }

    @Test
    public void testVisibility() {
        GroovyScript script = new GroovyScript(SCRIPT, FALLBACK_SCRIPT);
        GroovyScript visibilityScript = new GroovyScript(VISIBILITY_SCRIPT, VISIBILITY_FALLBACK_SCRIPT);
        CascadeChoiceParameter param = new CascadeChoiceParameter(
            "param000", "description", RANDOM_NAME,
            script, visibilityScript, CascadeChoiceParameter.ELEMENT_TYPE_FORMATTED_HIDDEN_HTML,
            "", true);

        assertTrue(param.isVisible());
    }
    
    @Test
    public void testDefaultVisibility() {
        GroovyScript script = new GroovyScript(SCRIPT, FALLBACK_SCRIPT);
        GroovyScript visibilityScript = new GroovyScript("", VISIBILITY_FALLBACK_SCRIPT);
        CascadeChoiceParameter param = new CascadeChoiceParameter(
            "param000", "description", RANDOM_NAME,
            script, visibilityScript, CascadeChoiceParameter.ELEMENT_TYPE_FORMATTED_HIDDEN_HTML,
            "", true);

        assertTrue(param.isVisible());
    }

    @Test
    public void testCascadedVisibility() {
        GroovyScript script = new GroovyScript(SCRIPT, FALLBACK_SCRIPT);
        GroovyScript visibilityScript = new GroovyScript("return param001=='A' && param002=='B'", VISIBILITY_FALLBACK_SCRIPT);
        CascadeChoiceParameter param = new CascadeChoiceParameter(
            "param000", "description", RANDOM_NAME,
            script, visibilityScript, CascadeChoiceParameter.ELEMENT_TYPE_FORMATTED_HIDDEN_HTML,
            "param001, param002", true);
        param.doUpdate("param001=A__LESEP__param002=B");

        assertTrue(param.isVisible());
    }

}
