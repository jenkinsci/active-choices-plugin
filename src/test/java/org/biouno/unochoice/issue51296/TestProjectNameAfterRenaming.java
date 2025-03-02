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

package org.biouno.unochoice.issue51296;

import java.io.IOException;
import java.util.Map;

import hudson.model.Descriptor;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.biouno.unochoice.CascadeChoiceParameter;
import org.biouno.unochoice.ChoiceParameter;
import org.biouno.unochoice.model.GroovyScript;
import org.jenkinsci.plugins.scriptsecurity.sandbox.groovy.SecureGroovyScript;
import org.jenkinsci.plugins.scriptsecurity.scripts.ScriptApproval;
import org.jenkinsci.plugins.scriptsecurity.scripts.languages.GroovyLanguage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.Issue;
import org.jvnet.hudson.test.JenkinsRule;

import hudson.model.FreeStyleProject;
import hudson.model.ParametersDefinitionProperty;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

/**
 * Tests for projectName being correct after renaming project. See JENKINS-51296.
 *
 * @since 2.2
 */
@Issue("JENKINS-51296")
@WithJenkins
class TestProjectNameAfterRenaming {
    private JenkinsRule j;

    // LIST script
    private static final String SCRIPT_LIST = "return ['Test', jenkinsProject.getName()]";
    private static final String FALLBACK_SCRIPT_LIST = "return ['EMPTY!']";

    private static final String PARAMETER_NAME = "my-parameter-name";

    private static final String PROJECT_NAME_BEFORE = "MyOldJenkinsJob";
    private static final String PROJECT_NAME_AFTER = "MyRealJenkinsJob";

    @BeforeEach
    void setUp(JenkinsRule j) {
        this.j = j;
        ScriptApproval.get()
                .preapprove(SCRIPT_LIST, GroovyLanguage.get());
        ScriptApproval.get()
                .preapprove(FALLBACK_SCRIPT_LIST, GroovyLanguage.get());
    }

    @Test
    void testProjectAreDifferent() throws IOException, Descriptor.FormException {

        FreeStyleProject project = j.createProject(FreeStyleProject.class, PROJECT_NAME_BEFORE);

        GroovyScript listScript = new GroovyScript(new SecureGroovyScript(SCRIPT_LIST, Boolean.FALSE, null),
                                                    new SecureGroovyScript(FALLBACK_SCRIPT_LIST, Boolean.FALSE, null));

        ChoiceParameter listParam = new ChoiceParameter(PARAMETER_NAME, "description...", "random-name", listScript,
                CascadeChoiceParameter.PARAMETER_TYPE_SINGLE_SELECT, false, 1);

        ParametersDefinitionProperty paramsDef = new ParametersDefinitionProperty(listParam);

        project.addProperty(paramsDef);

        Map<Object, Object> listSelectionValue = listParam.getChoices();

        String choicesStatus = listParam.getChoicesAsString() ;

        // keys and values have the same content when the parameter returns an array...
        assertTrue(listSelectionValue.containsKey(PROJECT_NAME_BEFORE), "Wrong project name from the begging ["+choicesStatus+"] test broken?!");

        // --- After renaming, the real test ---
        project.renameTo(PROJECT_NAME_AFTER);
        Map<Object, Object> listSelectionValueAfter = listParam.getChoices();

        choicesStatus = listParam.getChoicesAsString() ;

        // Now, check full name!
        assertTrue(listSelectionValueAfter.containsKey(PROJECT_NAME_AFTER), "Wrong project name after renaming: "+choicesStatus);
    }
}
