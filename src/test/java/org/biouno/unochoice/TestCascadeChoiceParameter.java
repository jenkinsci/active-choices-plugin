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

package org.biouno.unochoice;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import hudson.model.Descriptor;
import org.biouno.unochoice.model.GroovyScript;
import org.jenkinsci.plugins.scriptsecurity.sandbox.groovy.SecureGroovyScript;
import org.jenkinsci.plugins.scriptsecurity.scripts.ScriptApproval;
import org.jenkinsci.plugins.scriptsecurity.scripts.languages.GroovyLanguage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;
import org.kohsuke.stapler.HttpResponses;

@WithJenkins
class TestCascadeChoiceParameter {

    private static final String SCRIPT = "return ['a', 'b']";
    private static final String FALLBACK_SCRIPT = "return ['EMPTY!']";

    @BeforeEach
    void setUp(JenkinsRule j) {
        ScriptApproval.get().preapprove(SCRIPT, GroovyLanguage.get());
        ScriptApproval.get().preapprove(FALLBACK_SCRIPT, GroovyLanguage.get());
    }

    @Test
    void testConstructor() throws Descriptor.FormException {
        GroovyScript script = new GroovyScript(new SecureGroovyScript(SCRIPT, Boolean.FALSE, null),
                new SecureGroovyScript(FALLBACK_SCRIPT, Boolean.FALSE, null));
        CascadeChoiceParameter param = new CascadeChoiceParameter("param000", "description", "some-random-name", script,
                CascadeChoiceParameter.ELEMENT_TYPE_FORMATTED_HIDDEN_HTML, "param001, param002", true, 5);

        assertEquals("param000", param.getName());
        assertEquals("description", param.getDescription());
        assertEquals("some-random-name", param.getRandomName());
        assertEquals(script, param.getScript());
        assertEquals("ET_FORMATTED_HIDDEN_HTML", param.getChoiceType());
        assertEquals("param001, param002", param.getReferencedParameters());
        assertTrue(param.getFilterable());
        assertEquals(Integer.valueOf(5), param.getFilterLength());
    }

    @Test
    void testParameters() throws Descriptor.FormException {
        GroovyScript script = new GroovyScript(new SecureGroovyScript(SCRIPT, Boolean.FALSE, null),
                new SecureGroovyScript(FALLBACK_SCRIPT, Boolean.FALSE, null));
        CascadeChoiceParameter param = new CascadeChoiceParameter("param000", "description", "some-random-name", script,
                CascadeChoiceParameter.ELEMENT_TYPE_FORMATTED_HIDDEN_HTML, "param001, param002", true, 0);
        assertTrue(param.getParameters().isEmpty());

        try {
            param.doUpdate("param001=A__LESEP__param002=B__LESEP__param003=");
        } catch (HttpResponses.HttpResponseException response) {
            // ignore
        }

        Map<String, String> expected = new LinkedHashMap<>();
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
    void testNullFilterable() throws Descriptor.FormException {
        GroovyScript script = new GroovyScript(new SecureGroovyScript(SCRIPT, Boolean.FALSE, null),
                new SecureGroovyScript(FALLBACK_SCRIPT, Boolean.FALSE, null));
        CascadeChoiceParameter param = new CascadeChoiceParameter("param000", "description", "some-random-name", script,
                CascadeChoiceParameter.ELEMENT_TYPE_FORMATTED_HIDDEN_HTML, "param001, param002", null, 5);

        assertEquals("param000", param.getName());
        assertEquals("description", param.getDescription());
        assertEquals("some-random-name", param.getRandomName());
        assertEquals(script, param.getScript());
        assertEquals("ET_FORMATTED_HIDDEN_HTML", param.getChoiceType());
        assertEquals("param001, param002", param.getReferencedParameters());
        assertFalse(param.getFilterable());
        assertEquals(Integer.valueOf(5), param.getFilterLength());
    }

}
