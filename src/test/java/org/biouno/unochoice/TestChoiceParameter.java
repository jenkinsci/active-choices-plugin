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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestChoiceParameter {

    private final static String SCRIPT = "return ['a', 'b']";
    private final static String FALLBACK_SCRIPT = "return ['EMPTY!']";
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
        GroovyScript script = new GroovyScript(
                new SecureGroovyScript(SCRIPT, Boolean.FALSE, null),
                new SecureGroovyScript(FALLBACK_SCRIPT, Boolean.FALSE, null));
        GroovyScript visibilityScript = new GroovyScript(
                new SecureGroovyScript(VISIBILITY_SCRIPT, Boolean.FALSE, null),
                new SecureGroovyScript(VISIBILITY_FALLBACK_SCRIPT, Boolean.FALSE, null));
        ChoiceParameter param = new ChoiceParameter(
                "param000", "description", RANDOM_NAME,
                script, visibilityScript, CascadeChoiceParameter.ELEMENT_TYPE_FORMATTED_HIDDEN_HTML, true, 5);

        assertEquals("param000", param.getName());
        assertEquals("description", param.getDescription());
        assertEquals(script, param.getScript());
        assertEquals(visibilityScript, param.getVisibilityScript());
        assertEquals("ET_FORMATTED_HIDDEN_HTML", param.getChoiceType());
        assertTrue(param.getFilterable());
        assertEquals(Integer.valueOf(5), param.getFilterLength());
    }

    @Test
    public void testDefaultVisibility() {
        GroovyScript script = new GroovyScript(
                new SecureGroovyScript(SCRIPT, Boolean.FALSE, null),
                new SecureGroovyScript(FALLBACK_SCRIPT, Boolean.FALSE, null));
        GroovyScript visibilityScript = new GroovyScript(
                new SecureGroovyScript("", Boolean.FALSE, null),
                new SecureGroovyScript(VISIBILITY_FALLBACK_SCRIPT, Boolean.FALSE, null));
        ChoiceParameter param = new ChoiceParameter(
                "param000", "description", RANDOM_NAME,
                script, visibilityScript, "", true, 0);

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
        ChoiceParameter param = new ChoiceParameter(
                "param000", "description", RANDOM_NAME,
                script, visibilityScript, "", true, 0);

        assertFalse(param.isVisible());
    }
}
