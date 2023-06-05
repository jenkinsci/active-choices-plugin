/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2023 Ioannis Moutsatsos, Bruno P. Kinoshita
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
package org.biouno.unochoice.issue65235;

import org.htmlunit.html.HtmlSelect;
import hudson.model.ParametersDefinitionProperty;
import jenkins.branch.BranchSource;
import jenkins.model.Jenkins;
import jenkins.plugins.git.GitBranchSCMHead;
import jenkins.plugins.git.GitSCMSource;
import jenkins.plugins.git.GitSampleRepoRule;
import jenkins.scm.api.SCMHead;
import org.biouno.unochoice.AbstractUnoChoiceParameter;
import org.biouno.unochoice.ChoiceParameter;
import org.biouno.unochoice.model.GroovyScript;
import org.jenkinsci.plugins.scriptsecurity.sandbox.groovy.SecureGroovyScript;
import org.jenkinsci.plugins.scriptsecurity.scripts.ScriptApproval;
import org.jenkinsci.plugins.scriptsecurity.scripts.languages.GroovyLanguage;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import org.jvnet.hudson.test.*;
import java.util.Arrays;

import static org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProjectTest.scheduleAndFindBranchProject;
import static org.junit.Assert.*;

/**
 * Test that scripts can access the jenkinsProject object.
 *
 * @since 2.6.5
 */
@Issue("JENKINS-65235")
public class TestProjectObjectIsPresentOnMultibranchWorkflow {

    @Rule
    public JenkinsRule j = new JenkinsRule();
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    @ClassRule public static BuildWatcher buildWatcher = new BuildWatcher();
    @Rule public GitSampleRepoRule sampleRepo = new GitSampleRepoRule();

    // LIST script
    private final String SCRIPT_LIST = "return ['A', 'B', jenkinsProject.fullName, 'C']";
    private final String FALLBACK_SCRIPT_LIST = "return ['EMPTY!']";

    @Before
    public void setUp() throws Exception {

        ScriptApproval.get()
                .preapprove(SCRIPT_LIST, GroovyLanguage.get());
        ScriptApproval.get()
                .preapprove(FALLBACK_SCRIPT_LIST, GroovyLanguage.get());
    }

    @Test
    public void testProjectObjectIsAvailable() throws Exception {
        // create a multibranch workflow from git repo
        j.jenkins.setSecurityRealm(j.createDummySecurityRealm());
        j.jenkins.setAuthorizationStrategy(new MockAuthorizationStrategy()
                .grant(Jenkins.ADMINISTER).everywhere().toEveryone()
        );
        // create a git repo
        sampleRepo.init();
        sampleRepo.write("Jenkinsfile", "echo 'it works'");
        sampleRepo.git("add", "Jenkinsfile");
        sampleRepo.git("commit", "--all", "--message=flow");
        // create a multibranch workflow from git repo
        WorkflowMultiBranchProject mp = j.createProject(WorkflowMultiBranchProject.class, "gitprj");
        mp.getSourcesList().add(new BranchSource(new GitSCMSource(null, sampleRepo.toString(), "", "*", "", false)));
        WorkflowJob gitprj = scheduleAndFindBranchProject(mp, "master");
        assertEquals(new GitBranchSCMHead("master"), SCMHead.HeadByItem.findHead(gitprj));
        assertEquals(1, mp.getItems().size());
        j.waitUntilNoActivity();

        // configure groovy scripts
        GroovyScript scriptParam001 = new GroovyScript(new SecureGroovyScript(SCRIPT_LIST, false, null),
                new SecureGroovyScript(FALLBACK_SCRIPT_LIST, false, null));
        ChoiceParameter param001 = new ChoiceParameter("param001", "param001 description", "random-name",
                scriptParam001, AbstractUnoChoiceParameter.PARAMETER_TYPE_SINGLE_SELECT, true, 1);
        gitprj.addProperty(new ParametersDefinitionProperty(
                Arrays.asList(param001)));
        gitprj.save();
        j.waitUntilNoActivity();

        // check that the build works
        j.assertLogContains("it works", j.assertBuildStatusSuccess(gitprj.scheduleBuild2(0)));

        JenkinsRule.WebClient wc = j.createWebClient();
        // Accessing build page without parameters causes 405 Method not allowed.
        // So suppress the exception.
        wc.setThrowExceptionOnFailingStatusCode(false);
        // load the page with params related UI
        HtmlSelect htmlSelect = wc.getPage(gitprj.asItem(), "build").getFormByName("parameters").getSelectByName("value");

        assertNotEquals("fallback script execution detected", "EMPTY!", htmlSelect.getOption(0).getText());
        assertEquals("currentProject not evaluated as expected", "gitprj/master", htmlSelect.getOption(2).getText());

    }

}
