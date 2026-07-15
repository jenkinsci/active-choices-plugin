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

package org.biouno.unochoice.issue_performance;

import hudson.model.Descriptor;
import hudson.model.FreeStyleProject;
import hudson.model.ParametersDefinitionProperty;
import org.biouno.unochoice.AbstractScriptableParameter;
import org.biouno.unochoice.AbstractUnoChoiceParameter;
import org.biouno.unochoice.ChoiceParameter;
import org.biouno.unochoice.model.GroovyScript;
import org.jenkinsci.plugins.scriptsecurity.sandbox.groovy.SecureGroovyScript;
import org.jenkinsci.plugins.scriptsecurity.scripts.ScriptApproval;
import org.jenkinsci.plugins.scriptsecurity.scripts.languages.GroovyLanguage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

import java.io.IOException;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@WithJenkins
class TestProjectResolutionCaching {

    private JenkinsRule j;

    private static final String SCRIPT_LIST = "return [jenkinsProject.getFullName()]";
    private static final String FALLBACK_SCRIPT_LIST = "return ['EMPTY!']";

    @BeforeEach
    void setUp(JenkinsRule j) {
        this.j = j;
        ScriptApproval.get().preapprove(SCRIPT_LIST, GroovyLanguage.get());
        ScriptApproval.get().preapprove(FALLBACK_SCRIPT_LIST, GroovyLanguage.get());
    }

    @Test
    void cachesResolvedProjectNameAfterUuidLookup() throws IOException, Descriptor.FormException, ReflectiveOperationException {
        FreeStyleProject project = j.createProject(FreeStyleProject.class, "performance-test");
        ChoiceParameter parameter = new ChoiceParameter(
                "param",
                "description",
                "random-name",
                new GroovyScript(
                        new SecureGroovyScript(SCRIPT_LIST, false, null),
                        new SecureGroovyScript(FALLBACK_SCRIPT_LIST, false, null)
                ),
                AbstractUnoChoiceParameter.PARAMETER_TYPE_SINGLE_SELECT,
                false,
                1
        );
        project.addProperty(new ParametersDefinitionProperty(parameter));

        assertNull(getField(parameter, "projectFullName"));
        assertNull(getField(parameter, "projectFullNameCache"));

        parameter.getChoices();

        assertNull(getField(parameter, "projectFullName"));
        assertNull(getField(parameter, "projectName"));
        assertEquals(project.getFullName(), getField(parameter, "projectFullNameCache"));
        assertEquals(project.getName(), getField(parameter, "projectNameCache"));
    }

    private static Object getField(Object target, String fieldName) throws ReflectiveOperationException {
        Field field = AbstractScriptableParameter.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(target);
    }
}
