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

package org.biouno.unochoice.issue28735;

import hudson.cli.CLICommandInvoker;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Item;
import hudson.model.Job;
import hudson.model.ParameterDefinition;
import hudson.model.ParametersAction;
import hudson.model.ParametersDefinitionProperty;
import hudson.model.PasswordParameterDefinition;
import hudson.model.RootAction;
import hudson.model.Run;
import hudson.model.StringParameterDefinition;
import hudson.model.StringParameterValue;
import jenkins.model.Jenkins;
import org.biouno.unochoice.AbstractUnoChoiceParameter;
import org.biouno.unochoice.CascadeChoiceParameter;
import org.biouno.unochoice.ChoiceParameter;
import org.biouno.unochoice.DynamicReferenceParameter;
import org.biouno.unochoice.model.GroovyScript;
import org.htmlunit.HttpMethod;
import org.htmlunit.Page;
import org.htmlunit.WebRequest;
import org.kohsuke.stapler.StaplerRequest2;
import org.kohsuke.stapler.StaplerResponse2;
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
import org.jvnet.hudson.test.MockFolder;
import org.jvnet.hudson.test.TestExtension;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * With {@code computeDefaultFromReferences} on, cascade parameters get a value computed from their referenced
 * parameters when a build is started without the UI, e.g. by a timer or via {@code buildWithParameters}.
 */
@Issue("JENKINS-28735")
@WithJenkins
class TestCascadeDefaultsWithoutUi {

    private static final String PARENT_SCRIPT = "return ['a', 'b']";
    private static final String CHILD_SCRIPT = "return [PARENT + '-child']";
    private static final String GRANDCHILD_SCRIPT = "return [CHILD + '-grandchild']";
    private static final String CYCLE_A_SCRIPT = "return ['A[' + A + B + ']']";
    private static final String CYCLE_B_SCRIPT = "return ['B[' + A + ']']";
    private static final String SECRET_CHILD_SCRIPT = "return ['[' + PARENT + ']']";
    private static final String DIAMOND_B_SCRIPT = "return [A + '-b']";
    private static final String DIAMOND_C_SCRIPT = "return [A + '-c']";
    private static final String DIAMOND_D_SCRIPT = "return [B + '+' + C]";
    private static final String CHAIN_C_SCRIPT = "return [B + '-c']";
    private static final String CHAIN_D_SCRIPT = "return [C + '-d']";
    private static final String Z_SCRIPT = "return ['z']";
    private static final String FALLBACK_SCRIPT = "return ['FALLBACK']";

    private static final String D_SCRIPT = "return ['d']";
    private static final String REVERSE_B_SCRIPT = "return [D + '-b']";
    private static final String REVERSE_C_SCRIPT = "return [A + '+' + B]";
    private static final String PIPELINE_SCRIPT = "properties([parameters([\n"
            + "  [$class: 'ChoiceParameter', choiceType: 'PT_SINGLE_SELECT', name: 'PARENT',\n"
            + "   randomName: 'properties-parent', script: [$class: 'GroovyScript',\n"
            + "   script: [classpath: [], sandbox: true, script: \"return ['a', 'b']\"],\n"
            + "   fallbackScript: [classpath: [], sandbox: true, script: \"return ['FALLBACK']\"]]],\n"
            + "  [$class: 'CascadeChoiceParameter', choiceType: 'PT_SINGLE_SELECT', name: 'CHILD',\n"
            + "   randomName: 'properties-child', referencedParameters: 'PARENT', computeDefaultFromReferences: true,\n"
            + "   script: [$class: 'GroovyScript',\n"
            + "   script: [classpath: [], sandbox: true, script: \"return [PARENT + '-child']\"],\n"
            + "   fallbackScript: [classpath: [], sandbox: true, script: \"return ['FALLBACK']\"]]]\n"
            + "])])";

    private JenkinsRule j;

    @BeforeEach
    void setUp(JenkinsRule j) {
        this.j = j;
        for (String script : new String[] {PARENT_SCRIPT, CHILD_SCRIPT, GRANDCHILD_SCRIPT, CYCLE_A_SCRIPT,
                CYCLE_B_SCRIPT, SECRET_CHILD_SCRIPT, DIAMOND_B_SCRIPT, DIAMOND_C_SCRIPT, DIAMOND_D_SCRIPT,
                CHAIN_C_SCRIPT, CHAIN_D_SCRIPT, Z_SCRIPT, D_SCRIPT, REVERSE_B_SCRIPT, REVERSE_C_SCRIPT,
                FALLBACK_SCRIPT}) {
            ScriptApproval.get().preapprove(script, GroovyLanguage.get());
        }
    }

    @Test
    void buildWithoutParametersUsesReferencedDefaults() throws Exception {
        final FreeStyleProject project = createChainProject();

        final FreeStyleBuild build = j.buildAndAssertSuccess(project);

        assertEquals("a", parameterValue(build, "PARENT"));
        assertEquals("a-child", parameterValue(build, "CHILD"));
        assertEquals("a-child-grandchild", parameterValue(build, "GRANDCHILD"));
    }

    @Test
    void buildWithParametersUsesSuppliedReferencedValues() throws Exception {
        final FreeStyleProject project = createChainProject();
        final CascadeChoiceParameter child = (CascadeChoiceParameter) project
                .getProperty(ParametersDefinitionProperty.class).getParameterDefinition("CHILD");
        child.setCacheDefaultValue(true);
        assertEquals("a-child", parameterValue(j.buildAndAssertSuccess(project), "CHILD"));

        try (JenkinsRule.WebClient client = j.createWebClient()) {
            buildWithParameters(client, project, "PARENT=b", HttpMethod.POST);
        }
        final FreeStyleBuild build = project.getLastBuild();
        assertEquals(2, build.getNumber());
        assertEquals("b", parameterValue(build, "PARENT"));
        assertEquals("b-child", parameterValue(build, "CHILD"));
        assertEquals("b-child-grandchild", parameterValue(build, "GRANDCHILD"));

        assertEquals("a-child", parameterValue(j.buildAndAssertSuccess(project), "CHILD"));
    }

    @Test
    void requestToAnotherUrlCannotPassValues() throws Exception {
        final FreeStyleProject project = createChainProject();
        j.jenkins.setSecurityRealm(j.createDummySecurityRealm());
        j.jenkins.setAuthorizationStrategy(new MockAuthorizationStrategy()
                .grant(Jenkins.READ, Item.READ).everywhere().to("reader"));

        try (JenkinsRule.WebClient client = j.createWebClient().login("reader")) {
            final String json = client.getJSON(project.getUrl()
                    + "api/json?PARENT=INJECTED&tree=property[parameterDefinitions[name,defaultParameterValue[value]]]")
                    .getContentAsString();
            assertTrue(json.contains("\"a-child\""), json);
            assertFalse(json.contains("INJECTED"), json);
        }
    }

    @Test
    void passwordValuesAreNotPassedToScripts() throws Exception {
        final FreeStyleProject project = j.createFreeStyleProject("password");
        project.addProperty(new ParametersDefinitionProperty(
                new PasswordParameterDefinition("PARENT", "s3cret", ""),
                cascade("CHILD", "password-child", SECRET_CHILD_SCRIPT, "PARENT")));

        assertEquals("[]", parameterValue(j.buildAndAssertSuccess(project), "CHILD"));

        try (JenkinsRule.WebClient client = j.createWebClient()) {
            buildWithParameters(client, project, "PARENT=posted", HttpMethod.POST);
        }
        assertEquals("[]", parameterValue(project.getLastBuild(), "CHILD"));
    }

    @Test
    void refusedRequestValuesGiveTheOldValue() throws Exception {
        final FreeStyleProject project = createChainProject();
        final String config = project.getConfigFile().asString().replace("</project>", "<authToken>T</authToken></project>");
        project.updateByXml((Source) new StreamSource(new StringReader(config)));
        j.jenkins.setSecurityRealm(j.createDummySecurityRealm());
        j.jenkins.setAuthorizationStrategy(new MockAuthorizationStrategy()
                .grant(Jenkins.READ, Item.READ).everywhere().toEveryone()
                .grant(Item.BUILD).everywhere().to("builder"));

        // anonymous POST: the token lets Jenkins core start the build, but the user cannot build the job
        try (JenkinsRule.WebClient client = j.createWebClient()) {
            buildWithParameters(client, project, "token=T&PARENT=b", HttpMethod.POST);
        }
        assertEquals("b", parameterValue(project.getLastBuild(), "PARENT"));
        assertEquals("FALLBACK", parameterValue(project.getLastBuild(), "CHILD"));

        // GET by a user who can build the job: allowed by the token, but not a POST
        try (JenkinsRule.WebClient client = j.createWebClient().login("builder")) {
            buildWithParameters(client, project, "token=T&PARENT=b", HttpMethod.GET);
        }
        assertEquals(2, project.getLastBuild().getNumber());
        assertEquals("b", parameterValue(project.getLastBuild(), "PARENT"));
        assertEquals("FALLBACK", parameterValue(project.getLastBuild(), "CHILD"));
    }

    @Test
    void otherBuildWithParametersUrlGivesTheOldValue() throws Exception {
        createChainProject();

        // a buildWithParameters URL without a job in its path, like the Build Authorization Token Root plugin's
        try (JenkinsRule.WebClient client = j.createWebClient()) {
            final String value = client.goTo("fakeByToken/buildWithParameters?job=chain&PARENT=b", "text/plain")
                    .getWebResponse().getContentAsString();
            assertEquals("FALLBACK", value);
        }
    }

    @Test
    void sensitiveValuesAreNotPassedToScripts() throws Exception {
        final FreeStyleProject project = j.createFreeStyleProject("sensitive");
        final ParameterDefinition child = cascade("CHILD", "sensitive-child", SECRET_CHILD_SCRIPT, "PARENT");
        project.addProperty(new ParametersDefinitionProperty(
                new SensitiveStringParameterDefinition("PARENT", "s3cret"),
                child));

        assertEquals("[]", ((StringParameterValue) child.getDefaultParameterValue()).getValue());
    }

    @Test
    void referencedCascadeGetsItsRecordedValueInAnyOrder() throws Exception {
        final FreeStyleProject project = j.createFreeStyleProject("reverse");
        final CountingCascadeParameter b = new CountingCascadeParameter("B", "reverse-b", REVERSE_B_SCRIPT, "D");
        b.setCacheDefaultValue(true);
        final ParameterDefinition c = cascade("C", "reverse-c", REVERSE_C_SCRIPT, "A,B");
        project.addProperty(new ParametersDefinitionProperty(
                new StringParameterDefinition("A", "a"), choice("D", "reverse-d", Z_SCRIPT), c, b));
        final FreeStyleBuild first = j.buildAndAssertSuccess(project);
        assertEquals("a+z-b", parameterValue(first, "C"));
        assertEquals("z-b", parameterValue(first, "B"));

        // a new D default; B keeps its cached value, as nothing it depends on is posted
        project.removeProperty(ParametersDefinitionProperty.class);
        project.addProperty(new ParametersDefinitionProperty(
                new StringParameterDefinition("A", "a"), choice("D", "reverse-d2", D_SCRIPT), c, b));
        try (JenkinsRule.WebClient client = j.createWebClient()) {
            buildWithParameters(client, project, "A=x", HttpMethod.POST);
        }
        assertEquals("x+z-b", parameterValue(project.getLastBuild(), "C"));
        assertEquals("z-b", parameterValue(project.getLastBuild(), "B"));
    }

    @Test
    void referencedCascadeIsComputedOncePerRequestInAnyOrder() throws Exception {
        final FreeStyleProject project = j.createFreeStyleProject("reverse");
        final CountingCascadeParameter b = new CountingCascadeParameter("B", "reverse-b", REVERSE_B_SCRIPT, "D");
        project.addProperty(new ParametersDefinitionProperty(
                new StringParameterDefinition("A", "a"), choice("D", "reverse-d", Z_SCRIPT),
                cascade("C", "reverse-c", REVERSE_C_SCRIPT, "A,B"), b));

        try (JenkinsRule.WebClient client = j.createWebClient()) {
            buildWithParameters(client, project, "A=x", HttpMethod.POST);
        }

        assertEquals("x+z-b", parameterValue(project.getLastBuild(), "C"));
        assertEquals("z-b", parameterValue(project.getLastBuild(), "B"));
        assertEquals(1, b.getChoicesEvalCount());
    }

    @Test
    void pipelinePropertiesStepCanTurnTheOptionOn() throws Exception {
        final WorkflowJob job = j.createProject(WorkflowJob.class, "properties");
        job.setDefinition(new CpsFlowDefinition(PIPELINE_SCRIPT, true));
        j.buildAndAssertSuccess(job);
        final CascadeChoiceParameter child = (CascadeChoiceParameter) job
                .getProperty(ParametersDefinitionProperty.class).getParameterDefinition("CHILD");
        assertTrue(child.getComputeDefaultFromReferences());

        final Run<?, ?> build = j.buildAndAssertSuccess(job);

        assertEquals("a", parameterValue(build, "PARENT"));
        assertEquals("a-child", parameterValue(build, "CHILD"));
    }

    @Test
    void cliBuildUsesReferencedDefaults() throws Exception {
        final FreeStyleProject project = createChainProject();

        final CLICommandInvoker.Result result = new CLICommandInvoker(j, "build")
                .invokeWithArgs("chain", "-p", "PARENT=b", "-s");

        assertEquals(0, result.returnCode(), result.stderr());
        // the CLI value of PARENT cannot be seen by the script of CHILD
        assertEquals("b", parameterValue(project.getLastBuild(), "PARENT"));
        assertEquals("a-child", parameterValue(project.getLastBuild(), "CHILD"));
    }

    @Test
    void onlyBuildWithParametersCanPassValues() throws Exception {
        final FreeStyleProject project = createChainProject();
        j.jenkins.setSecurityRealm(j.createDummySecurityRealm());
        j.jenkins.setAuthorizationStrategy(new MockAuthorizationStrategy()
                .grant(Jenkins.READ, Item.READ, Item.BUILD).everywhere().to("builder"));

        try (JenkinsRule.WebClient client = j.createWebClient().login("builder")) {
            final WebRequest request = new WebRequest(new URL(j.getURL(), project.getUrl()
                    + "api/json?PARENT=b&tree=property[parameterDefinitions[name,defaultParameterValue[value]]]"),
                    HttpMethod.POST);
            client.addCrumb(request);
            final String json = client.getPage(request).getWebResponse().getContentAsString();
            assertTrue(json.contains("\"a-child\""), json);
            assertFalse(json.contains("b-child"), json);
        }
    }

    @Test
    void optionOffKeepsTheOldValues() throws Exception {
        final FreeStyleProject project = j.createFreeStyleProject("off");
        final CountingChoiceParameter parent = new CountingChoiceParameter("PARENT", "off-parent",
                script(PARENT_SCRIPT));
        project.addProperty(new ParametersDefinitionProperty(
                parent,
                new CascadeChoiceParameter("CHILD", "", "off-child", script(CHILD_SCRIPT),
                        AbstractUnoChoiceParameter.PARAMETER_TYPE_SINGLE_SELECT, "PARENT", false, 1)));

        final FreeStyleBuild build = j.buildAndAssertSuccess(project);
        assertEquals("a", parameterValue(build, "PARENT"));
        assertEquals("FALLBACK", parameterValue(build, "CHILD"));
        assertEquals(1, parent.getChoicesEvalCount());

        // the REST API runs no script of PARENT for CHILD either
        try (JenkinsRule.WebClient client = j.createWebClient()) {
            final String json = client.getJSON(project.getUrl()
                    + "api/json?tree=property[parameterDefinitions[name,defaultParameterValue[value]]]")
                    .getContentAsString();
            assertTrue(json.contains("\"FALLBACK\""), json);
        }
        assertEquals(2, parent.getChoicesEvalCount());
    }

    @Test
    void eachValueIsComputedOncePerRequest() throws Exception {
        final FreeStyleProject project = j.createFreeStyleProject("long-chain");
        final CountingChoiceParameter a = new CountingChoiceParameter("A", "long-chain-a", script(PARENT_SCRIPT));
        project.addProperty(new ParametersDefinitionProperty(
                a,
                cascade("B", "long-chain-b", DIAMOND_B_SCRIPT, "A"),
                cascade("C", "long-chain-c", CHAIN_C_SCRIPT, "B"),
                cascade("D", "long-chain-d", CHAIN_D_SCRIPT, "C")));

        try (JenkinsRule.WebClient client = j.createWebClient()) {
            final String json = client.getJSON(project.getUrl()
                    + "api/json?tree=property[parameterDefinitions[name,defaultParameterValue[value]]]")
                    .getContentAsString();
            assertTrue(json.contains("\"a-b-c-d\""), json);
        }
        // once for A itself, once for B
        assertEquals(2, a.getChoicesEvalCount());
    }

    @Test
    void jobInAFolderUsesReferencedValues() throws Exception {
        final MockFolder folder = j.createFolder("folder");
        final FreeStyleProject project = folder.createProject(FreeStyleProject.class, "inner");
        project.addProperty(new ParametersDefinitionProperty(
                choice("PARENT", "folder-parent", PARENT_SCRIPT),
                cascade("CHILD", "folder-child", CHILD_SCRIPT, "PARENT")));

        assertEquals("a-child", parameterValue(j.buildAndAssertSuccess(project), "CHILD"));

        try (JenkinsRule.WebClient client = j.createWebClient()) {
            buildWithParameters(client, project, "PARENT=b", HttpMethod.POST);
        }
        assertEquals("b-child", parameterValue(project.getLastBuild(), "CHILD"));
    }

    @Test
    void reactiveReferenceParameterUsesReferencedValues() throws Exception {
        final FreeStyleProject project = j.createFreeStyleProject("reference");
        final DynamicReferenceParameter child = new DynamicReferenceParameter("CHILD", "", "reference-child",
                script(CHILD_SCRIPT), AbstractUnoChoiceParameter.ELEMENT_TYPE_FORMATTED_HIDDEN_HTML, "PARENT", false);
        child.setComputeDefaultFromReferences(true);
        project.addProperty(new ParametersDefinitionProperty(
                choice("PARENT", "reference-parent", PARENT_SCRIPT),
                child));

        assertEquals("a-child", parameterValue(j.buildAndAssertSuccess(project), "CHILD"));

        try (JenkinsRule.WebClient client = j.createWebClient()) {
            buildWithParameters(client, project, "PARENT=b", HttpMethod.POST);
        }
        assertEquals("b-child", parameterValue(project.getLastBuild(), "CHILD"));
    }

    @Test
    void referencedCascadeUsesItsCachedDefault() throws Exception {
        final FreeStyleProject project = createChainProject();
        final CascadeChoiceParameter child = (CascadeChoiceParameter) project
                .getProperty(ParametersDefinitionProperty.class).getParameterDefinition("CHILD");
        child.setCacheDefaultValue(true);
        assertEquals("a-child", parameterValue(j.buildAndAssertSuccess(project), "CHILD"));

        // a new PARENT default; CHILD keeps its cached value, and GRANDCHILD must agree with it
        final ParameterDefinition grandchild = project.getProperty(ParametersDefinitionProperty.class)
                .getParameterDefinition("GRANDCHILD");
        project.removeProperty(ParametersDefinitionProperty.class);
        project.addProperty(new ParametersDefinitionProperty(choice("PARENT", "chain-parent-z", Z_SCRIPT), child,
                grandchild));
        final FreeStyleBuild build = j.buildAndAssertSuccess(project);

        assertEquals("z", parameterValue(build, "PARENT"));
        assertEquals("a-child", parameterValue(build, "CHILD"));
        assertEquals("a-child-grandchild", parameterValue(build, "GRANDCHILD"));
    }

    @Test
    void sharedReferencedParameterIsComputedOnce() throws Exception {
        final FreeStyleProject project = j.createFreeStyleProject("diamond");
        final CountingChoiceParameter a = new CountingChoiceParameter("A", "diamond-a", script(PARENT_SCRIPT));
        final ParameterDefinition d = cascade("D", "diamond-d", DIAMOND_D_SCRIPT, "B,C");
        project.addProperty(new ParametersDefinitionProperty(
                a,
                cascade("B", "diamond-b", DIAMOND_B_SCRIPT, "A"),
                cascade("C", "diamond-c", DIAMOND_C_SCRIPT, "A"),
                d));

        assertEquals("a-b+a-c", ((StringParameterValue) d.getDefaultParameterValue()).getValue());
        assertEquals(1, a.getChoicesEvalCount());
    }

    @Issue("JENKINS-63284")
    @Test
    void pipelineBuildWithoutParametersUsesReferencedDefaults() throws Exception {
        final WorkflowJob job = j.createProject(WorkflowJob.class, "pipeline");
        job.setDefinition(new CpsFlowDefinition("echo 'hello'", true));
        job.addProperty(new ParametersDefinitionProperty(
                choice("PARENT", "pipeline-parent", PARENT_SCRIPT),
                cascade("CHILD", "pipeline-child", CHILD_SCRIPT, "PARENT")));

        final Run<?, ?> build = j.buildAndAssertSuccess(job);

        assertEquals("a", parameterValue(build, "PARENT"));
        assertEquals("a-child", parameterValue(build, "CHILD"));
    }

    @Test
    void cyclicReferencesResolveToEmptyValues() throws Exception {
        final FreeStyleProject project = j.createFreeStyleProject("cycle");
        project.addProperty(new ParametersDefinitionProperty(
                cascade("A", "cycle-a", CYCLE_A_SCRIPT, "A,B"),
                cascade("B", "cycle-b", CYCLE_B_SCRIPT, "A")));

        final FreeStyleBuild build = j.buildAndAssertSuccess(project);

        assertEquals("A[B[]]", parameterValue(build, "A"));
        assertEquals("B[A[]]", parameterValue(build, "B"));
    }

    private FreeStyleProject createChainProject() throws Exception {
        final FreeStyleProject project = j.createFreeStyleProject("chain");
        project.addProperty(new ParametersDefinitionProperty(
                choice("PARENT", "chain-parent", PARENT_SCRIPT),
                cascade("CHILD", "chain-child", CHILD_SCRIPT, "PARENT"),
                cascade("GRANDCHILD", "chain-grandchild", GRANDCHILD_SCRIPT, "CHILD")));
        return project;
    }

    private Page buildWithParameters(JenkinsRule.WebClient client, Job<?, ?> job, String query,
            HttpMethod method) throws Exception {
        final WebRequest request = new WebRequest(
                new URL(j.getURL(), job.getUrl() + "buildWithParameters?" + query), method);
        if (method == HttpMethod.POST) {
            client.addCrumb(request);
        }
        final Page page = client.getPage(request);
        j.waitUntilNoActivity();
        return page;
    }

    private static ParameterDefinition choice(String name, String randomName, String script) throws Exception {
        return new ChoiceParameter(name, "", randomName, script(script),
                AbstractUnoChoiceParameter.PARAMETER_TYPE_SINGLE_SELECT, false, 1);
    }

    private static ParameterDefinition cascade(String name, String randomName, String script,
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

    /**
     * A {@code buildWithParameters} URL that has no job in its path, as the Build Authorization Token Root plugin
     * has. It returns the value Jenkins creates for CHILD of the given job.
     */
    @TestExtension("otherBuildWithParametersUrlGivesTheOldValue")
    public static class FakeBuildByToken implements RootAction {

        @Override
        public String getIconFileName() {
            return null;
        }

        @Override
        public String getDisplayName() {
            return null;
        }

        @Override
        public String getUrlName() {
            return "fakeByToken";
        }

        public void doBuildWithParameters(StaplerRequest2 req, StaplerResponse2 rsp) throws IOException {
            final Job<?, ?> job = Jenkins.get().getItemByFullName(req.getParameter("job"), Job.class);
            final Object value = job.getProperty(ParametersDefinitionProperty.class).getParameterDefinition("CHILD")
                    .createValue(req).getValue();
            rsp.setContentType("text/plain;charset=UTF-8");
            rsp.getWriter().print(value);
        }
    }

    /**
     * A string parameter whose values are sensitive.
     */
    private static class SensitiveStringParameterDefinition extends StringParameterDefinition {

        SensitiveStringParameterDefinition(String name, String defaultValue) {
            super(name, defaultValue);
        }

        @Override
        public StringParameterValue getDefaultParameterValue() {
            return new StringParameterValue(getName(), getDefaultValue()) {
                @Override
                public boolean isSensitive() {
                    return true;
                }
            };
        }
    }

    /**
     * A cascade parameter, with {@code computeDefaultFromReferences} on, that counts how many times choices are
     * evaluated.
     */
    private static class CountingCascadeParameter extends CascadeChoiceParameter {

        private int choicesEvalCount = 0;

        CountingCascadeParameter(String name, String randomName, String script, String referencedParameters)
                throws Exception {
            super(name, "", randomName, script(script), AbstractUnoChoiceParameter.PARAMETER_TYPE_SINGLE_SELECT,
                    referencedParameters, false, 1);
            setComputeDefaultFromReferences(true);
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

    /**
     * Counts how many times choices are evaluated.
     */
    private static class CountingChoiceParameter extends ChoiceParameter {

        private int choicesEvalCount = 0;

        CountingChoiceParameter(String name, String randomName, GroovyScript script) {
            super(name, "", randomName, script, AbstractUnoChoiceParameter.PARAMETER_TYPE_SINGLE_SELECT, false, 1);
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
