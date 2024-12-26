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
package org.biouno.unochoice.issue48448;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Map;

import hudson.model.Descriptor;
import org.biouno.unochoice.CascadeChoiceParameter;
import org.biouno.unochoice.ChoiceParameter;
import org.biouno.unochoice.model.GroovyScript;
import org.jenkinsci.plugins.scriptsecurity.sandbox.groovy.SecureGroovyScript;
import org.jenkinsci.plugins.scriptsecurity.scripts.ScriptApproval;
import org.jenkinsci.plugins.scriptsecurity.scripts.languages.GroovyLanguage;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.Issue;
import org.jvnet.hudson.test.JenkinsRule;

/**
 * Test that scripts can access the parameter object.
 *
 * @since 1.5.x
 */
@Issue("JENKINS-48448")
public class TestParameterObjectIsPresent {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    // LIST script
    private final String SCRIPT_LIST = "return ['A', 'B', jenkinsParameter.getName(), 'C']";
    private final String FALLBACK_SCRIPT_LIST = "return ['EMPTY!']";

    private final String PARAMETER_NAME = "my-parameter-name";

    @Before
    public void setUp() throws Exception {
        ScriptApproval.get()
                .preapprove(SCRIPT_LIST, GroovyLanguage.get());
        ScriptApproval.get()
                .preapprove(FALLBACK_SCRIPT_LIST, GroovyLanguage.get());
    }

    @Test
    public void testParameterObjectIsPresent() throws IOException, Descriptor.FormException {
        GroovyScript listScript = new GroovyScript(new SecureGroovyScript(SCRIPT_LIST, Boolean.FALSE, null),
                new SecureGroovyScript(FALLBACK_SCRIPT_LIST, Boolean.FALSE, null));
        ChoiceParameter listParam = new ChoiceParameter(PARAMETER_NAME, "description...", "random-name1", listScript,
                CascadeChoiceParameter.PARAMETER_TYPE_MULTI_SELECT, true, 1);
        Map<Object, Object> listSelectionValue = listParam.getChoices();

        // keys and values have the same content when the parameter returns an array...
        assertTrue("Missing parameter name!", listSelectionValue.containsKey(PARAMETER_NAME));
    }

}
