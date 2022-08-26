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

package org.biouno.unochoice.issue69448;

import static org.junit.Assert.assertEquals;

import org.biouno.unochoice.ChoiceParameter;
import org.biouno.unochoice.model.GroovyScript;
import org.jenkinsci.plugins.scriptsecurity.sandbox.groovy.SecureGroovyScript;
import org.jenkinsci.plugins.scriptsecurity.scripts.ScriptApproval;
import org.jenkinsci.plugins.scriptsecurity.scripts.languages.GroovyLanguage;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.Issue;
import org.jvnet.hudson.test.JenkinsRule;

import hudson.model.ParameterValue;

/**
 * See JENKINS-69448.
 *
 * @since 2.6.3
 */
@Issue("JENKINS-69448")
public class TestDefaultValuesWithScriptReturningMap {

    private static final String DEFAULT_FALLBACK_SCRIPT = "return ['EMPTY!']";
    private static final String PARAMETER_NAME = "my-parameter-name";

    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Test
    public void testReturnMap() {
        ChoiceParameter parameter = createChoiceParameter(
                ChoiceParameter.PARAMETER_TYPE_CHECK_BOX,
                "return ['A':'Description for A:selected:disabled', 'B':'Description for B:disabled:selected', 'C':'Description for C', 'D':'Description for D:selected']"
        );

        ParameterValue parameterValue = parameter.getDefaultParameterValue();

        assertEquals("Invalid parameter name!", PARAMETER_NAME, parameterValue.getName());
        assertEquals("Invalid parameter value!", "A,B,D", parameterValue.getValue());
    }

    private static final ChoiceParameter createChoiceParameter(String type, String script) {
        ScriptApproval.get().preapprove(script, GroovyLanguage.get());
        ScriptApproval.get().preapprove(DEFAULT_FALLBACK_SCRIPT, GroovyLanguage.get());

        GroovyScript groovyScript = new GroovyScript(
                new SecureGroovyScript(script, Boolean.FALSE, null),
                new SecureGroovyScript(DEFAULT_FALLBACK_SCRIPT, Boolean.FALSE, null)
        );
        return new ChoiceParameter(PARAMETER_NAME, "description", "random-name1", groovyScript, type, true, 1);
    }
}
