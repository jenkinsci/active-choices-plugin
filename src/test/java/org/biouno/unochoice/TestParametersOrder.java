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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import hudson.model.Descriptor;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import org.biouno.unochoice.model.GroovyScript;
import org.jenkinsci.plugins.scriptsecurity.sandbox.groovy.SecureGroovyScript;
import org.jenkinsci.plugins.scriptsecurity.scripts.ScriptApproval;
import org.jenkinsci.plugins.scriptsecurity.scripts.languages.GroovyLanguage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class TestParametersOrder {

    private static final String SCRIPT = "return ['D', 'C', 'B', 'A']";
    private static final String FALLBACK_SCRIPT = "";

    @BeforeEach
    void setUp(JenkinsRule j) {
        ScriptApproval.get().preapprove(SCRIPT, GroovyLanguage.get());
        ScriptApproval.get().preapprove(FALLBACK_SCRIPT, GroovyLanguage.get());
    }

    @Test
    void testParametersOrder() throws Descriptor.FormException {
        Map<Object, Object> parameters = new LinkedHashMap<>();
        parameters.put("D", "D");
        parameters.put("C", "C");
        parameters.put("B", "B");
        parameters.put("A", "A");

        ChoiceParameter parameter = new ChoiceParameter("script001", "description", "random name",
                new GroovyScript(new SecureGroovyScript(SCRIPT, Boolean.FALSE, null),
                        new SecureGroovyScript(FALLBACK_SCRIPT, Boolean.FALSE, null)),
                ChoiceParameter.PARAMETER_TYPE_MULTI_SELECT, true, 0);
        Map<Object, Object> result = parameter.getChoices(Collections.emptyMap());
        assertArrayEquals(parameters.keySet().toArray(), result.keySet().toArray());
    }

}
