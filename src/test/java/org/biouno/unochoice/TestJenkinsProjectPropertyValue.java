package org.biouno.unochoice;

import hudson.model.FreeStyleProject;
import hudson.model.Job;
import hudson.model.ParametersDefinitionProperty;
import org.biouno.unochoice.model.GroovyScript;
import org.jenkinsci.plugins.scriptsecurity.sandbox.groovy.SecureGroovyScript;
import org.jenkinsci.plugins.scriptsecurity.scripts.ScriptApproval;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.IOException;
import java.util.Map;

import static org.biouno.unochoice.AbstractScriptableParameter.JENKINS_PROJECT_VARIABLE_NAME;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestJenkinsProjectPropertyValue {
    @Rule
    public JenkinsRule j = new JenkinsRule();

    private final String PROJECT_NAME = "MyJenkinsJob";
    private final String PARAMETER_NAME = "MY_PARAMETER_NAME";

    private final int MAX_INDEX = 6;
    // Popular ways to get property value.
    private final String SCRIPT_LIST = "try {\n" +
            "    return ['Test',\n" +
            "           \"1-${" + JENKINS_PROJECT_VARIABLE_NAME + ".getName()}\",\n" +
            "           \"2-${binding." + JENKINS_PROJECT_VARIABLE_NAME + ".getName()}\",\n" +
            "           \"3-${this.binding." + JENKINS_PROJECT_VARIABLE_NAME + ".getName()}\",\n" +
            "           \"4-${binding.getProperty('" + JENKINS_PROJECT_VARIABLE_NAME + "').getName()}\",\n" +
            "           \"5-${this.binding.getProperty('" + JENKINS_PROJECT_VARIABLE_NAME + "').getName()}\",\n" +
            "           \"" + MAX_INDEX + "-${this.getProperty('" + JENKINS_PROJECT_VARIABLE_NAME + "').getName()}\",\n" +
            "    ]\n" +
            "}\n" +
            " catch (e) {\n" +
            "    return [e.toString(),]\n" +
            "}\n";
    private final String FALLBACK_SCRIPT_LIST = "return ['ERROR!',]";

    private final String EXPECTED_VALUE = PROJECT_NAME;

    @Before
    public void setUp() throws Exception {
        // .getName() prerequisite
        ScriptApproval.get().approveSignature("method hudson.model.Item getName");
        // binding.jenkinsProject/binding.getProperty()/this.getProperty() prerequisite
        ScriptApproval.get().approveSignature("method groovy.lang.GroovyObject getProperty java.lang.String");
    }

    @Test
    public void testFreestyleJob() throws IOException {
        FreeStyleProject project = j.createProject(FreeStyleProject.class, PROJECT_NAME);
        testPropertyValueForJob(project);
    }

    @Test
    public void testPipelineJob() throws IOException {
        WorkflowJob project = j.createProject(WorkflowJob.class, PROJECT_NAME);
        testPropertyValueForJob(project);
    }

    private void testPropertyValueForJob(Job<?, ?> project) throws IOException {
        GroovyScript listScript = new GroovyScript(new SecureGroovyScript(SCRIPT_LIST, true, null),
                new SecureGroovyScript(FALLBACK_SCRIPT_LIST, true, null));

        ChoiceParameter listParam = new ChoiceParameter(PARAMETER_NAME, "description...", "random-name", listScript,
                CascadeChoiceParameter.PARAMETER_TYPE_SINGLE_SELECT, false, 1);

        ParametersDefinitionProperty paramsDef = new ParametersDefinitionProperty(listParam);

        project.addProperty(paramsDef);

        Map<Object, Object> listSelectionValue = listParam.getChoices();
        assertFalse("Choices map is empty.", listSelectionValue.isEmpty());

        String choicesStatus = listParam.getChoicesAsString();
        assertFalse(
                "Something went wrong. Exception: " + choicesStatus,
                choicesStatus.contains("Exception")
        );

        assertTrue("Value 'Test' is not presented in choices: " + choicesStatus + ".", listSelectionValue.containsKey("Test"));
        for (int i = 1; i < MAX_INDEX; i++) {
            assertTrue("Wrong project name value with index #" + i + ": " + choicesStatus + ".", listSelectionValue.containsKey(i + "-" + EXPECTED_VALUE));
        }
    }
}
