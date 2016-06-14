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

import static org.junit.Assert.assertArrayEquals;

import java.util.Collections;
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

public class TestParametersOrder {

    private final static String SCRIPT = "return ['D', 'C', 'B', 'A']";
    private final static String FALLBACK_SCRIPT = "";
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
    public void testParametersOrder() {
        Map<Object, Object> parameters = new LinkedHashMap<Object, Object>();
        parameters.put("D", "D");
        parameters.put("C", "C");
        parameters.put("B", "B");
        parameters.put("A", "A");

        GroovyScript script = new GroovyScript(SCRIPT, FALLBACK_SCRIPT);
        GroovyScript visibilityScript = new GroovyScript(VISIBILITY_SCRIPT, VISIBILITY_FALLBACK_SCRIPT);
        ChoiceParameter parameter = new ChoiceParameter(
                "script001", "description", RANDOM_NAME, script,visibilityScript,
                ChoiceParameter.PARAMETER_TYPE_MULTI_SELECT, true);

        Map<Object, Object> result = parameter.getChoices(Collections.<Object, Object>emptyMap());
        assertArrayEquals(parameters.keySet().toArray(), result.keySet().toArray());
    }

}
