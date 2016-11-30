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

import static org.junit.Assert.assertEquals;

import org.biouno.unochoice.model.GroovyScript;
import org.jenkinsci.plugins.scriptsecurity.sandbox.groovy.SecureGroovyScript;
import org.jenkinsci.plugins.scriptsecurity.scripts.ScriptApproval;
import org.jenkinsci.plugins.scriptsecurity.scripts.languages.GroovyLanguage;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jvnet.hudson.test.JenkinsRule;
import org.kohsuke.stapler.StaplerRequest;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import hudson.model.ParameterValue;
import hudson.model.StringParameterValue;
import net.sf.json.JSONObject;

/**
 * Test the behavior of the {@link AbstractUnoChoiceParameter}.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({StaplerRequest.class})
@PowerMockIgnore({"javax.crypto.*" })
public class TestAbstractUnoChoiceParameter {

    private final String SCRIPT = "return ['a', 'b']";
    private final String FALLBACK_SCRIPT = "return ['EMPTY!']";

    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Before
    public void setUp() throws Exception {
        ScriptApproval.get().preapprove(SCRIPT, GroovyLanguage.get());
        ScriptApproval.get().preapprove(FALLBACK_SCRIPT, GroovyLanguage.get());
    }

    @Test
    public void testCreateValue() {
        GroovyScript script = new GroovyScript(new SecureGroovyScript(SCRIPT, Boolean.FALSE, null),
                new SecureGroovyScript(FALLBACK_SCRIPT, Boolean.FALSE, null));
        ChoiceParameter param = new ChoiceParameter("name", "description", "some-random-name", script, "choiceType",
                true, 0);
        ParameterValue value = param.createValue("value");

        assertEquals("value", value.getValue().toString());

        JSONObject json = new JSONObject();
        json.put("name", "name");
        json.put("value", "value");

        StaplerRequest request = PowerMockito.mock(StaplerRequest.class);
        PowerMockito.when(request.bindJSON(StringParameterValue.class, json)).thenReturn((StringParameterValue) value);

        value = param.createValue(request, json);

        assertEquals("value", value.getValue().toString());
    }

}
