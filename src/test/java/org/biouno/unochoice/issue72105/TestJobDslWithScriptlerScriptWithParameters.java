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
package org.biouno.unochoice.issue72105;

import hudson.model.FileParameterValue;
import hudson.model.FreeStyleProject;
import hudson.model.JobProperty;
import hudson.model.JobPropertyDescriptor;
import hudson.model.ParameterDefinition;
import hudson.model.ParametersDefinitionProperty;
import hudson.model.Result;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FileUtils;
import org.biouno.unochoice.AbstractScriptableParameter;
import org.biouno.unochoice.CascadeChoiceParameter;
import org.biouno.unochoice.model.ScriptlerScript;
import org.jenkinsci.plugins.scriptler.ScriptlerHelper;
import org.jenkinsci.plugins.scriptler.ScriptlerManagement;
import org.jenkinsci.plugins.scriptler.config.Parameter;
import org.jenkinsci.plugins.scriptsecurity.scripts.ScriptApproval;
import org.jenkinsci.plugins.scriptsecurity.scripts.languages.GroovyLanguage;
import org.jenkinsci.plugins.workflow.flow.FlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.Issue;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.recipes.LocalData;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * In JENKINS-72105 the plug-in did not persist the user option to use
 * parameters in their scriptler scripts, when the script was created
 * by a Job DSL build.
 */
@Issue("JENKINS-72105")
public class TestJobDslWithScriptlerScriptWithParameters {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    private ScriptlerManagement scriptler;
    private File scriptFile = null;

    private static final String GROOVY_SCRIPT = "return [P1, P2]";
    private static final String PARAMETER_WITH_SCRIPTLER_AND_PARAMETERS = "P3";

    /**
     * Create the script in Scriptler.
     *
     * @throws Exception I/O?
     */
    @Before
    public void setUp() throws Exception {
        ScriptApproval.get().preapprove(GROOVY_SCRIPT, new GroovyLanguage());
        // scriptler setup
        scriptler = j.getInstance().getExtensionList(ScriptlerManagement.class).get(0);
        ScriptlerHelper scriptlerHelper = new ScriptlerHelper(scriptler);
        scriptFile = Files.createTempFile("uno-choice", "Cascade.groovy").toFile();
        FileUtils.writeStringToFile(scriptFile, GROOVY_SCRIPT, Charset.defaultCharset(), false);
        FileItem fi = new FileParameterValue.FileItemImpl(scriptFile);
        scriptlerHelper.saveScript(fi, true, "Cascade.groovy");

        scriptler.getConfiguration()
                .getScriptById("Cascade.groovy")
                .setParameters(Arrays.asList(
                        new Parameter("P1", ""),
                        new Parameter("P2", "")));
    }

    @Test
    @LocalData("test")
    public void test() throws Exception {
        assertNull(j.jenkins.getItem("scriptler-test"));

        final FreeStyleProject job = (FreeStyleProject) j.jenkins.getItem("job");
        j.buildAndAssertStatus(Result.SUCCESS, job);

        final WorkflowJob jobDSLCreated = (WorkflowJob) j.jenkins.getItem("scriptler-test");
        assertNotNull(jobDSLCreated);

        // This will be set to true if we are able to confirm P1 and P2 were persisted with the JobDSL created job
        boolean parametersDefined = false;
        loop1:
        for (Map.Entry<JobPropertyDescriptor, JobProperty<? super WorkflowJob>> entry : jobDSLCreated.getProperties()
                .entrySet()) {
            JobProperty<? super WorkflowJob> jobProperty = entry.getValue();
            if (jobProperty instanceof ParametersDefinitionProperty) {
                ParametersDefinitionProperty paramDef = (ParametersDefinitionProperty) jobProperty;
                List<ParameterDefinition> parameters = paramDef.getParameterDefinitions();
                for (ParameterDefinition parameter : parameters) {
                    if (PARAMETER_WITH_SCRIPTLER_AND_PARAMETERS.equals(parameter.getName()) &&
                            parameter instanceof CascadeChoiceParameter) {
                        final CascadeChoiceParameter p3 = (CascadeChoiceParameter) parameter;
                        ScriptlerScript script = (ScriptlerScript) p3.getScript();
                        assertEquals(2, script.getParameters().size());
                        assertTrue(script.getParameters().containsKey("P1"));
                        assertTrue(script.getParameters().containsKey("P2"));
                        parametersDefined = true;
                        break loop1;
                    }
                }
            }
        }

        assertTrue("The P3 parameter MUST have a scriptler parameter with two parameters (persisted when saving it). Regression detected!", parametersDefined);
    }
}
