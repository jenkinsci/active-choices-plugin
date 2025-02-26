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
package org.biouno.unochoice.issue38532;

import hudson.model.Descriptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.biouno.unochoice.CascadeChoiceParameter;
import org.biouno.unochoice.ChoiceParameter;
import org.biouno.unochoice.DynamicReferenceParameter;
import org.biouno.unochoice.model.GroovyScript;
import org.jenkinsci.plugins.scriptsecurity.sandbox.groovy.SecureGroovyScript;
import org.jenkinsci.plugins.scriptsecurity.scripts.ScriptApproval;
import org.jenkinsci.plugins.scriptsecurity.scripts.languages.GroovyLanguage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.Issue;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;
import org.kohsuke.stapler.HttpResponses;

/**
 * Test that scripts can access global node properties.
 *
 * @since 1.5.x
 */
@Issue("JENKINS-38532")
@WithJenkins
class TestParameterValuesWithEquals {

    // LIST script
    private static final String SCRIPT_LIST = "return ['A=1', 'B=2', 'C=3']";
    private static final String FALLBACK_SCRIPT_LIST = "return ['EMPTY!']";

    // LIST_SELECTION script
    private static final String SCRIPT_LIST_SELECTION = "value = LIST.toString()\nreturn \"${value}\"";
    private static final String FALLBACK_SCRIPT_LIST_SELECTION = "return ['EMPTY!']";

    @BeforeEach
    void setUp(JenkinsRule j) {
        ScriptApproval.get().preapprove(SCRIPT_LIST, GroovyLanguage.get());
        ScriptApproval.get().preapprove(FALLBACK_SCRIPT_LIST, GroovyLanguage.get());
        ScriptApproval.get().preapprove(SCRIPT_LIST_SELECTION, GroovyLanguage.get());
        ScriptApproval.get().preapprove(FALLBACK_SCRIPT_LIST_SELECTION, GroovyLanguage.get());
    }

    @Test
    void testEvaluationWorksEvenThoughWeUsedEqualsInParameterValues() throws Descriptor.FormException {
        GroovyScript listScript = new GroovyScript(new SecureGroovyScript(SCRIPT_LIST, Boolean.FALSE, null),
                new SecureGroovyScript(FALLBACK_SCRIPT_LIST, Boolean.FALSE, null));
        GroovyScript listSelectionScript = new GroovyScript(
                new SecureGroovyScript(SCRIPT_LIST_SELECTION, Boolean.FALSE, null),
                new SecureGroovyScript(FALLBACK_SCRIPT_LIST_SELECTION, Boolean.FALSE, null));
        ChoiceParameter listParam = new ChoiceParameter("LIST", "description...", "random-name1", listScript,
                CascadeChoiceParameter.PARAMETER_TYPE_SINGLE_SELECT, true, 1);
        DynamicReferenceParameter listSelectionParam = new DynamicReferenceParameter("LIST_SELECTION", "description...",
                "random-name2", listSelectionScript, CascadeChoiceParameter.ELEMENT_TYPE_FORMATTED_HIDDEN_HTML, "LIST",
                true);
        // should be a String
        String listValue = listParam.getDefaultParameterValue().getValue().toString();
        try {
            listSelectionParam.doUpdate(String.format("%s=%s", listParam.getName(), listValue));
        } catch (HttpResponses.HttpResponseException response) {
            // ignore
        }
        // as this is a formatted hidden HTML...
        String listSelectionValue = listSelectionParam.getChoicesAsString();

        // by default, the plug-in returns the first element in the list...
        assertEquals("A=1",
                listSelectionValue,
                "Value returned from selection list doesn't match first element in list parameter");
    }

}
