/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2026 Ioannis Moutsatsos, Bruno P. Kinoshita
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

package org.biouno.unochoice.issue76298;

import hudson.model.ParameterValue;
import hudson.model.StringParameterValue;
import org.biouno.unochoice.ChoiceParameter;
import org.biouno.unochoice.model.GroovyScript;
import org.jenkinsci.plugins.scriptsecurity.sandbox.groovy.SecureGroovyScript;
import org.jenkinsci.plugins.scriptsecurity.scripts.ScriptApproval;
import org.jenkinsci.plugins.scriptsecurity.scripts.languages.GroovyLanguage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.Issue;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * See JENKINS-76298, GH-911.
 *
 * @since 2.8.9
 */
@Issue("JENKINS-76298")
@WithJenkins
class TestAbstractScriptableParameterDefaultValueCache {

    /**
     * Returns a list of values where the first element
     * is used as default by {@code AbstractScriptableParameter}.
     */
    private static final String SCRIPT = "return ['first', 'second']";
    private static final String FALLBACK_SCRIPT = "return ['FALLBACK']";

    @BeforeEach
    void setUp(JenkinsRule j) {
        ScriptApproval.get().preapprove(SCRIPT, GroovyLanguage.get());
        ScriptApproval.get().preapprove(FALLBACK_SCRIPT, GroovyLanguage.get());
    }

    /**
     * Verifies that {@code getDefaultParameterValue()} uses an internal cache:
     * - the default value is computed once from the choices
     * - subsequent calls reuse the cached value without recomputing choices.
     */
    @Test
    void testDefaultValueIsCached() throws Exception {
        GroovyScript script = new GroovyScript(
            new SecureGroovyScript(SCRIPT, false, null),
            new SecureGroovyScript(FALLBACK_SCRIPT, false, null)
        );

        CountingChoiceParameter param = new CountingChoiceParameter(
            "param000",
            "description",
            "some-random-name",
            script,
            "choiceType",
            true,
            0
        );

        // First call: choices are evaluated and default value is computed
        ParameterValue first = param.getDefaultParameterValue();
        assertEquals(StringParameterValue.class, first.getClass());
        assertEquals("first", ((StringParameterValue) first).getValue());

        // Second call: should return same value but without recomputing choices
        ParameterValue second = param.getDefaultParameterValue();
        assertEquals("first", ((StringParameterValue) second).getValue());

        // Choices must have been computed exactly once
        assertEquals(1, param.getChoicesEvalCount());
    }

    /**
     * Counts how many times choices are evaluated by overriding {@code getChoices(Map)}.
     */
    private static class CountingChoiceParameter extends ChoiceParameter {

        private int choicesEvalCount = 0;

        CountingChoiceParameter(
            String name,
            String description,
            String randomName,
            GroovyScript script,
            String choiceType,
            Boolean filterable,
            Integer filterLength
        ) {
            super(name, description, randomName, script, choiceType, filterable, filterLength);
        }

        int getChoicesEvalCount() {
            return choicesEvalCount;
        }

        @Override
        public Map<Object, Object> getChoices(Map<Object, Object> parameters) {
            choicesEvalCount++;
            return super.getChoices(parameters);
        }
    }
}
