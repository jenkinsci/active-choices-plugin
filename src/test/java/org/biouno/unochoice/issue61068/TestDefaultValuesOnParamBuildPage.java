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

package org.biouno.unochoice.issue61068;

import static org.junit.jupiter.api.Assertions.assertEquals;

import hudson.model.Descriptor;
import org.biouno.unochoice.ChoiceParameter;
import org.biouno.unochoice.model.GroovyScript;
import org.jenkinsci.plugins.scriptsecurity.sandbox.groovy.SecureGroovyScript;
import org.jenkinsci.plugins.scriptsecurity.scripts.ScriptApproval;
import org.jenkinsci.plugins.scriptsecurity.scripts.languages.GroovyLanguage;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.Issue;
import org.jvnet.hudson.test.JenkinsRule;

import hudson.model.ParameterValue;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

/**
 * Tests for radio parameter has incorrect default value on parambuild URL. See
 * JENKINS-61068.
 *
 * @since 2.2.3
 */
@Issue("JENKINS-61068")
@WithJenkins
class TestDefaultValuesOnParamBuildPage {

    private static final String DEFAULT_FALLBACK_SCRIPT = "return ['EMPTY!']";
    private static final String PARAMETER_NAME = "my-parameter-name";

    @Test
    void testReturnEmptyWhenNoElementsAreAvailable(JenkinsRule j) throws Descriptor.FormException {
        ChoiceParameter parameter = createChoiceParameter(
                ChoiceParameter.PARAMETER_TYPE_RADIO,
                "return []"
        );

        ParameterValue parameterValue = parameter.getDefaultParameterValue();

        assertEquals(PARAMETER_NAME, parameterValue.getName(), "Invalid parameter name!");
        assertEquals("", parameterValue.getValue(), "Invalid parameter value!");
    }

    @Test
    void testReturnEmptyWhenOnlyEmptyElementIsDefined(JenkinsRule j) throws Descriptor.FormException {
        ChoiceParameter parameter = createChoiceParameter(
                ChoiceParameter.PARAMETER_TYPE_RADIO,
                "return ['']"
        );

        ParameterValue parameterValue = parameter.getDefaultParameterValue();

        assertEquals(PARAMETER_NAME, parameterValue.getName(), "Invalid parameter name!");
        assertEquals("", parameterValue.getValue(), "Invalid parameter value!");
    }

    @Test
    void testReturnFirstElementWhenSelectedIsNotSet(JenkinsRule j) throws Descriptor.FormException {
        ChoiceParameter parameter = createChoiceParameter(
                ChoiceParameter.PARAMETER_TYPE_RADIO,
                "return ['A', 'B', 'C', 'D']"
        );

        ParameterValue parameterValue = parameter.getDefaultParameterValue();

        assertEquals(PARAMETER_NAME, parameterValue.getName(), "Invalid parameter name!");
        assertEquals("A", parameterValue.getValue(), "Invalid parameter value!");
    }

    @Test
    void testReturnSelectedElement(JenkinsRule j) throws Descriptor.FormException {
        ChoiceParameter parameter = createChoiceParameter(
                ChoiceParameter.PARAMETER_TYPE_RADIO,
                "return ['A', 'B', 'C:selected', 'D', 'E:disabled']"
        );

        ParameterValue parameterValue = parameter.getDefaultParameterValue();

        assertEquals(PARAMETER_NAME, parameterValue.getName(), "Invalid parameter name!");
        assertEquals("C", parameterValue.getValue(), "Invalid parameter value!");
    }

    @Test
    void testReturnSelectedElements(JenkinsRule j) throws Descriptor.FormException {
        ChoiceParameter parameter = createChoiceParameter(
                ChoiceParameter.PARAMETER_TYPE_CHECK_BOX,
                "return ['A', 'B:selected', 'C', 'D:selected', 'E:disabled']"
        );

        ParameterValue parameterValue = parameter.getDefaultParameterValue();

        assertEquals(PARAMETER_NAME, parameterValue.getName(), "Invalid parameter name!");
        assertEquals("B,D", parameterValue.getValue(), "Invalid parameter value!");
    }

    @Test
    void testReturnSelectedEmptyElement(JenkinsRule j) throws Descriptor.FormException {
        ChoiceParameter parameter = createChoiceParameter(
                ChoiceParameter.PARAMETER_TYPE_RADIO,
                "return ['A', 'B', ':selected', 'D', 'E:disabled']"
        );

        ParameterValue parameterValue = parameter.getDefaultParameterValue();

        assertEquals(PARAMETER_NAME, parameterValue.getName(), "Invalid parameter name!");
        assertEquals("", parameterValue.getValue(), "Invalid parameter value!");
    }

    @Test
    void testAllSuffixesAreTrimmed(JenkinsRule j) throws Descriptor.FormException {
        ChoiceParameter parameter = createChoiceParameter(
                ChoiceParameter.PARAMETER_TYPE_CHECK_BOX,
                "return ['A:selected:disabled', 'B:disabled:selected', 'C', 'D:selected']"
        );

        ParameterValue parameterValue = parameter.getDefaultParameterValue();

        assertEquals(PARAMETER_NAME, parameterValue.getName(), "Invalid parameter name!");
        assertEquals("A,B,D", parameterValue.getValue(), "Invalid parameter value!");
    }

    private static ChoiceParameter createChoiceParameter(String type, String script) throws Descriptor.FormException {
        ScriptApproval.get().preapprove(script, GroovyLanguage.get());
        ScriptApproval.get().preapprove(DEFAULT_FALLBACK_SCRIPT, GroovyLanguage.get());

        GroovyScript groovyScript = new GroovyScript(
                new SecureGroovyScript(script, Boolean.FALSE, null),
                new SecureGroovyScript(DEFAULT_FALLBACK_SCRIPT, Boolean.FALSE, null)
        );
        return new ChoiceParameter(PARAMETER_NAME, "description", "random-name1", groovyScript, type, true, 1);
    }
}
