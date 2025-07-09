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

package org.biouno.unochoice.issue49260;

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
import org.jvnet.hudson.test.MockFolder;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

import hudson.model.FreeStyleProject;
import hudson.model.ParametersDefinitionProperty;

/**
 * Tests for different folders having same Project name. See JENKINS-49260.
 *
 * @since 2.2
 */
@Issue("JENKINS-49260")
@WithJenkins
class TestProjectDifferenceAcrossFolders {
    private JenkinsRule j;

    // LIST script
    private static final String SCRIPT_LIST = "return ['Test', jenkinsProject.getName(), jenkinsProject.getFullName()]";
    private static final String FALLBACK_SCRIPT_LIST = "return ['EMPTY!']";

    private static final String PARAMETER_NAME = "my-parameter-name";

    private static final String PROJECT_NAME = "MyJenkinsJob";
    private static final String FOLDER_NAME_A = "AAA";
    private static final String FOLDER_NAME_B = "BBB";

    @BeforeEach
    void setUp(JenkinsRule j) {
        this.j = j;
        ScriptApproval.get()
                .preapprove(SCRIPT_LIST, GroovyLanguage.get());
        ScriptApproval.get()
                .preapprove(FALLBACK_SCRIPT_LIST, GroovyLanguage.get());
    }

    @Test
    void testProjectsAreDifferent() throws IOException, Descriptor.FormException {
        GroovyScript listScript = new GroovyScript(new SecureGroovyScript(SCRIPT_LIST, Boolean.FALSE, null),
                new SecureGroovyScript(FALLBACK_SCRIPT_LIST, Boolean.FALSE, null));

        MockFolder folderA = j.createFolder(FOLDER_NAME_A);
        MockFolder folderB = j.createFolder(FOLDER_NAME_B);

        FreeStyleProject projectA = folderA.createProject(FreeStyleProject.class, PROJECT_NAME);
        ChoiceParameter listParamA = new ChoiceParameter(PARAMETER_NAME, "description...", "random-name-A", listScript,
              CascadeChoiceParameter.PARAMETER_TYPE_SINGLE_SELECT, false, 1);
        projectA.addProperty(new ParametersDefinitionProperty(listParamA));

        FreeStyleProject projectB = folderB.createProject(FreeStyleProject.class, PROJECT_NAME);
        ChoiceParameter listParamB = new ChoiceParameter(PARAMETER_NAME, "description...", "random-name-B", listScript,
              CascadeChoiceParameter.PARAMETER_TYPE_SINGLE_SELECT, false, 1);
        projectB.addProperty(new ParametersDefinitionProperty(listParamB));

        Map<Object, Object> listSelectionValueA = listParamA.getChoices();
        Map<Object, Object> listSelectionValueB = listParamB.getChoices();

        // keys and values have the same content when the parameter returns an array...
        assertTrue(listSelectionValueA.containsKey(PROJECT_NAME), "Missing project name for A!");
        assertTrue(listSelectionValueB.containsKey(PROJECT_NAME), "Missing project name for B!");

        // Now, check full name!
        assertTrue(listSelectionValueA.containsKey(FOLDER_NAME_A+"/"+PROJECT_NAME), "Missing project full name for A!");
        assertTrue(listSelectionValueB.containsKey(FOLDER_NAME_B+"/"+PROJECT_NAME), "Missing project full name for B!");
    }
}
