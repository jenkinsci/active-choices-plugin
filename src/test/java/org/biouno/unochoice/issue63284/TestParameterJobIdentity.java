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

package org.biouno.unochoice.issue63284;

import hudson.model.FreeStyleProject;
import hudson.model.Item;
import hudson.model.ParametersAction;
import hudson.model.ParametersDefinitionProperty;
import hudson.model.Run;
import hudson.model.TopLevelItem;
import hudson.model.User;
import hudson.security.ACL;
import hudson.security.ACLContext;
import jenkins.model.Jenkins;
import org.biouno.unochoice.AbstractUnoChoiceParameter;
import org.biouno.unochoice.CascadeChoiceParameter;
import org.biouno.unochoice.ChoiceParameter;
import org.biouno.unochoice.model.GroovyScript;
import org.htmlunit.HttpMethod;
import org.htmlunit.WebRequest;
import org.jenkinsci.plugins.scriptsecurity.sandbox.groovy.SecureGroovyScript;
import org.jenkinsci.plugins.scriptsecurity.scripts.ScriptApproval;
import org.jenkinsci.plugins.scriptsecurity.scripts.languages.GroovyLanguage;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.Issue;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.MockAuthorizationStrategy;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A parameter finds the job that holds it, even when another job has the same name it saved, or the same UUID.
 */
@Issue("JENKINS-63284")
@WithJenkins
class TestParameterJobIdentity {

    private static final String JOB_NAME_SCRIPT = "return [jenkinsProject.fullName]";
    private static final String PARENT_SCRIPT = "return ['a', 'b']";
    private static final String ONE_SCRIPT = "return ['one']";
    private static final String TWO_SCRIPT = "return ['two']";
    private static final String CHILD_SCRIPT = "return [PARENT + '-child']";
    private static final String PEEK_SCRIPT = "return [jenkins.model.Jenkins.get().getItemByFullName('chain')"
            + ".getProperty(hudson.model.ParametersDefinitionProperty).getParameterDefinition('CHILD')"
            + ".getDefaultParameterValue().value]";
    private static final String FALLBACK_SCRIPT = "return ['FALLBACK']";

    private JenkinsRule j;

    @BeforeEach
    void setUp(JenkinsRule j) {
        this.j = j;
        for (String script : new String[] {JOB_NAME_SCRIPT, PARENT_SCRIPT, ONE_SCRIPT, TWO_SCRIPT, CHILD_SCRIPT,
                PEEK_SCRIPT, FALLBACK_SCRIPT}) {
            ScriptApproval.get().preapprove(script, GroovyLanguage.get());
        }
    }

    @Test
    void copiedJobFindsItself() throws Exception {
        final FreeStyleProject original = j.createFreeStyleProject("original");
        final ChoiceParameter parameter = choice("JOB", "job-name", JOB_NAME_SCRIPT);
        original.addProperty(new ParametersDefinitionProperty(parameter));
        assertEquals(List.of("original"), keys(parameter.getChoices()));
        original.save();

        final FreeStyleProject copy = (FreeStyleProject) j.jenkins.copy((TopLevelItem) original, "copy");
        final ChoiceParameter copied = (ChoiceParameter) copy.getProperty(ParametersDefinitionProperty.class)
                .getParameterDefinition("JOB");

        assertEquals(List.of("copy"), keys(copied.getChoices()));
        assertEquals(List.of("original"), keys(parameter.getChoices()));
    }

    @Test
    void renamedJobFindsItselfWhenAnotherJobTakesItsOldName() throws Exception {
        final FreeStyleProject project = j.createFreeStyleProject("old-name");
        final ChoiceParameter parameter = choice("JOB", "job-name", JOB_NAME_SCRIPT);
        project.addProperty(new ParametersDefinitionProperty(parameter));
        assertEquals(List.of("old-name"), keys(parameter.getChoices()));

        project.renameTo("new-name");
        j.createFreeStyleProject("old-name");

        assertEquals(List.of("new-name"), keys(parameter.getChoices()));
    }

    @Test
    void parameterNoLongerInItsJobKeepsTheJob() throws Exception {
        final FreeStyleProject project = j.createFreeStyleProject("replaced");
        final CascadeChoiceParameter old = cascade("JOB", "job-name", JOB_NAME_SCRIPT, "");
        project.addProperty(new ParametersDefinitionProperty(old));
        assertEquals(List.of("replaced"), keys(old.getChoices()));

        // a build page opened before this save still uses the old instance
        project.removeProperty(ParametersDefinitionProperty.class);
        project.addProperty(new ParametersDefinitionProperty(cascade("JOB", "job-name", JOB_NAME_SCRIPT, "")));

        final List<Object> choices = old.getChoicesForUI();
        assertTrue(((Collection<?>) choices.get(1)).contains("replaced"), choices.toString());
    }

    @Test
    void jobTheUserCannotReadIsNotReplacedByAnotherJob() throws Exception {
        final FreeStyleProject original = j.createFreeStyleProject("original");
        final ChoiceParameter parameter = choice("JOB", "job-name", JOB_NAME_SCRIPT);
        original.addProperty(new ParametersDefinitionProperty(parameter));
        assertEquals(List.of("original"), keys(parameter.getChoices()));
        original.save();
        final FreeStyleProject secret = (FreeStyleProject) j.jenkins.copy((TopLevelItem) original, "secret");
        final ChoiceParameter copied = (ChoiceParameter) secret.getProperty(ParametersDefinitionProperty.class)
                .getParameterDefinition("JOB");
        j.jenkins.setSecurityRealm(j.createDummySecurityRealm());
        j.jenkins.setAuthorizationStrategy(new MockAuthorizationStrategy()
                .grant(Jenkins.READ).everywhere().to("reader")
                .grant(Item.READ).onItems(original).to("reader"));

        // the reader cannot see the job holding the parameter, and does not get the job it saved the name of
        try (ACLContext ignored = ACL.as2(User.getById("reader", true).impersonate2())) {
            assertEquals(List.of("FALLBACK"), keys(copied.getChoices()));
        }
        assertEquals(List.of("secret"), keys(copied.getChoices()));
    }

    @Test
    void sharedRandomNameUsesTheRightPipelineJob() throws Exception {
        final WorkflowJob first = j.createProject(WorkflowJob.class, "first");
        first.setDefinition(new CpsFlowDefinition("echo 'hello'", true));
        first.addProperty(new ParametersDefinitionProperty(
                choice("PARENT", "first-parent", ONE_SCRIPT),
                cascade("CHILD", "shared-child", CHILD_SCRIPT, "PARENT")));
        final WorkflowJob second = j.createProject(WorkflowJob.class, "second");
        second.setDefinition(new CpsFlowDefinition("echo 'hello'", true));
        second.addProperty(new ParametersDefinitionProperty(
                choice("PARENT", "second-parent", TWO_SCRIPT),
                cascade("CHILD", "shared-child", CHILD_SCRIPT, "PARENT")));

        assertEquals("two-child", parameterValue(j.buildAndAssertSuccess(second), "CHILD"));
        assertEquals("one-child", parameterValue(j.buildAndAssertSuccess(first), "CHILD"));
    }

    @Test
    void requestForAnotherJobDoesNotPassValues() throws Exception {
        final FreeStyleProject chain = j.createFreeStyleProject("chain");
        chain.addProperty(new ParametersDefinitionProperty(
                choice("PARENT", "chain-parent", PARENT_SCRIPT),
                cascade("CHILD", "chain-child", CHILD_SCRIPT, "PARENT")));
        final FreeStyleProject other = j.createFreeStyleProject("other");
        other.addProperty(new ParametersDefinitionProperty(
                choice("PARENT", "other-parent", PARENT_SCRIPT),
                choice("PEEK", "other-peek", PEEK_SCRIPT)));

        try (JenkinsRule.WebClient client = j.createWebClient()) {
            final WebRequest request = new WebRequest(
                    new URL(j.getURL(), other.getUrl() + "buildWithParameters?PARENT=b"), HttpMethod.POST);
            client.addCrumb(request);
            client.getPage(request);
        }
        j.waitUntilNoActivity();

        // the posted PARENT is for "other": CHILD of "chain" neither uses it nor mixes in its default
        assertEquals("b", parameterValue(other.getLastBuild(), "PARENT"));
        assertEquals("FALLBACK", parameterValue(other.getLastBuild(), "PEEK"));
    }

    private static List<Object> keys(Map<Object, Object> choices) {
        return new ArrayList<>(choices.keySet());
    }

    private static ChoiceParameter choice(String name, String randomName, String script) throws Exception {
        return new ChoiceParameter(name, "", randomName, script(script),
                AbstractUnoChoiceParameter.PARAMETER_TYPE_SINGLE_SELECT, false, 1);
    }

    private static CascadeChoiceParameter cascade(String name, String randomName, String script,
            String referencedParameters) throws Exception {
        final CascadeChoiceParameter parameter = new CascadeChoiceParameter(name, "", randomName, script(script),
                AbstractUnoChoiceParameter.PARAMETER_TYPE_SINGLE_SELECT, referencedParameters, false, 1);
        parameter.setComputeDefaultFromReferences(true);
        return parameter;
    }

    private static GroovyScript script(String script) throws Exception {
        return new GroovyScript(new SecureGroovyScript(script, false, null),
                new SecureGroovyScript(FALLBACK_SCRIPT, false, null));
    }

    private static Object parameterValue(Run<?, ?> build, String name) {
        return build.getAction(ParametersAction.class).getParameter(name).getValue();
    }
}
