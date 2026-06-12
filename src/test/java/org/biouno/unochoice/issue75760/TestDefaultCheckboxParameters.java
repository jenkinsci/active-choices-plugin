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
package org.biouno.unochoice.issue75760;

import hudson.model.queue.QueueTaskFuture;
import jenkins.plugins.git.junit.jupiter.WithGitSampleRepo;
import jenkins.plugins.git.traits.BranchDiscoveryTrait;
import jenkins.scm.impl.trait.WildcardSCMHeadFilterTrait;
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
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.*;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

import java.util.List;

import static org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProjectTest.scheduleAndFindBranchProject;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test that a build with no parameter set result on an empty value and not the first choice item.
 *
 * @since 2.6.5
 */
@Issue("JENKINS-75760")
@WithJenkins
@WithGitSampleRepo
class TestDefaultCheckboxParameters {
    private JenkinsRule j;
    private GitSampleRepoRule sampleRepo;

    // LIST script
    private static final String SCRIPT_TRUSTED = "return ['envA', 'envB', 'envC']";
    private static final String SCRIPT_NOT_TRUSTED = "return ['env1', 'env2', 'env3']";
    private static final String SCRIPT_FALLBACK = "return ['error']";

    @BeforeEach
    void setUp(JenkinsRule j, GitSampleRepoRule sampleRepo) throws Exception {
        this.j = j;
        j.jenkins.setSecurityRealm(j.createDummySecurityRealm());
        j.jenkins.setAuthorizationStrategy(new MockAuthorizationStrategy()
                .grant(Jenkins.ADMINISTER).everywhere().toEveryone()
        );
        ScriptApproval.get().preapprove(SCRIPT_TRUSTED, new GroovyLanguage());
        ScriptApproval.get().preapprove(SCRIPT_FALLBACK, new GroovyLanguage());

        // create a git repo
        this.sampleRepo = sampleRepo;
        sampleRepo.init();
        sampleRepo.write("Jenkinsfile", "echo 'params=' + params");
        sampleRepo.git("add", "Jenkinsfile");
        sampleRepo.git("commit", "--all", "--message=flow");
    }

    @Test
    void testCheckboxParamSandbox() throws Exception {
        // create a multibranch workflow from git repo
        WorkflowMultiBranchProject mp = j.createProject(WorkflowMultiBranchProject.class, "job1");
        GitSCMSource gitSCMSource = new GitSCMSource(sampleRepo.toString());
        gitSCMSource.setTraits(List.of(new BranchDiscoveryTrait(), new WildcardSCMHeadFilterTrait("*", "")));
        mp.getSourcesList().add(new BranchSource(gitSCMSource));
        WorkflowJob job = scheduleAndFindBranchProject(mp, "master");
        assertEquals(new GitBranchSCMHead("master"), SCMHead.HeadByItem.findHead(job));
        assertEquals(1, mp.getItems().size());
        j.waitUntilNoActivity();

        // configure groovy scripts
        GroovyScript script = new GroovyScript(new SecureGroovyScript(SCRIPT_TRUSTED, true, null), new SecureGroovyScript(SCRIPT_FALLBACK, false, null));
        ChoiceParameter param = new ChoiceParameter("environments", "description", "random-name",
                script, AbstractUnoChoiceParameter.PARAMETER_TYPE_CHECK_BOX, false, 1);
        job.addProperty(new ParametersDefinitionProperty(
                List.of(param)));
        job.save();
        j.waitUntilNoActivity();

        QueueTaskFuture<WorkflowRun> future = job.scheduleBuild2(0);
        WorkflowRun build = j.assertBuildStatusSuccess(future);
        // check that the build works
        j.assertLogContains("params=[environments:]", build);
    }

    @Test
    void testCheckboxParamTrusted() throws Exception {
        // create a multibranch workflow from git repo
        WorkflowMultiBranchProject mp = j.createProject(WorkflowMultiBranchProject.class, "job2");
        GitSCMSource gitSCMSource = new GitSCMSource(sampleRepo.toString());
        gitSCMSource.setTraits(List.of(new BranchDiscoveryTrait(), new WildcardSCMHeadFilterTrait("*", "")));
        mp.getSourcesList().add(new BranchSource(gitSCMSource));
        WorkflowJob job = scheduleAndFindBranchProject(mp, "master");
        assertEquals(new GitBranchSCMHead("master"), SCMHead.HeadByItem.findHead(job));
        assertEquals(1, mp.getItems().size());
        j.waitUntilNoActivity();

        // configure groovy scripts
        GroovyScript script = new GroovyScript(new SecureGroovyScript(SCRIPT_TRUSTED, false, null), new SecureGroovyScript(SCRIPT_FALLBACK, false, null));
        ChoiceParameter param = new ChoiceParameter("environments", "description", "random-name",
                script, AbstractUnoChoiceParameter.PARAMETER_TYPE_CHECK_BOX, false, 1);
        job.addProperty(new ParametersDefinitionProperty(
                List.of(param)));
        job.save();
        j.waitUntilNoActivity();

        QueueTaskFuture<WorkflowRun> future = job.scheduleBuild2(0);
        WorkflowRun build = j.assertBuildStatusSuccess(future);
        // check that the build works
        j.assertLogContains("params=[environments:]", build);
    }

    @Test
    void testCheckboxParamNotTrusted() throws Exception {
        // create a multibranch workflow from git repo
        WorkflowMultiBranchProject mp = j.createProject(WorkflowMultiBranchProject.class, "job3");
        GitSCMSource gitSCMSource = new GitSCMSource(sampleRepo.toString());
        gitSCMSource.setTraits(List.of(new BranchDiscoveryTrait(), new WildcardSCMHeadFilterTrait("*", "")));
        mp.getSourcesList().add(new BranchSource(gitSCMSource));
        WorkflowJob job = scheduleAndFindBranchProject(mp, "master");
        assertEquals(new GitBranchSCMHead("master"), SCMHead.HeadByItem.findHead(job));
        assertEquals(1, mp.getItems().size());
        j.waitUntilNoActivity();

        // configure groovy scripts
        GroovyScript script = new GroovyScript(new SecureGroovyScript(SCRIPT_NOT_TRUSTED, false, null), new SecureGroovyScript(SCRIPT_FALLBACK, false, null));
        ChoiceParameter param = new ChoiceParameter("environments", "description", "random-name",
                script, AbstractUnoChoiceParameter.PARAMETER_TYPE_CHECK_BOX, false, 1);
        job.addProperty(new ParametersDefinitionProperty(
                List.of(param)));
        job.save();
        j.waitUntilNoActivity();

        QueueTaskFuture<WorkflowRun> future = job.scheduleBuild2(0);
        WorkflowRun build = j.assertBuildStatusSuccess(future);
        // check that the build works
        j.assertLogContains("params=[environments:]", build);
    }

}
