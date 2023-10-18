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
package org.biouno.unochoice;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.biouno.unochoice.model.GroovyScript;
import org.biouno.unochoice.model.Script;
import org.biouno.unochoice.model.ScriptlerScript;
import org.jenkinsci.plugins.scriptler.ScriptlerHelper;
import org.jenkinsci.plugins.scriptler.ScriptlerManagement;
import org.jenkinsci.plugins.scriptler.builder.ScriptlerBuilder;
import org.jenkinsci.plugins.scriptler.config.Parameter;
import org.jenkinsci.plugins.scriptsecurity.sandbox.groovy.SecureGroovyScript;
import org.jenkinsci.plugins.scriptsecurity.scripts.ScriptApproval;
import org.jenkinsci.plugins.scriptsecurity.scripts.languages.GroovyLanguage;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import hudson.XmlFile;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.JobProperty;
import hudson.model.JobPropertyDescriptor;
import hudson.model.ParameterDefinition;
import hudson.model.ParametersDefinitionProperty;
import hudson.model.Result;
import hudson.model.FileParameterValue.FileItemImpl;
import hudson.model.queue.QueueTaskFuture;

/**
 * Regression tests, to confirm we are able to:
 *
 * <ul>
 * <li>Save a job with parameters</li>
 * <li>Confirm the values are persisted to the job configuration</li>
 * <li>Confirm we can also see parameters of Scriptler parameters</li>
 * </ul>
 *
 * <p>
 * These tests have been created after regressions in 1.5.0 and 1.5.1.
 * </p>
 *
 * @since 1.5.3
 */
public class TestPersistingParameters {

    private final static String SCRIPT_PARAM001 = "return 1..10";
    private final static String SCRIPT_FALLBACK_PARAM001 = "return []";
    private final static String SCRIPT_PARAM002 = "return [PARAM001]";
    private final static String SCRIPT_FALLBACK_PARAM002 = "return []";

    @Rule
    public JenkinsRule j = new JenkinsRule();

    private ScriptlerManagement scriptler;

    private File scriptFile = null;

    @Before
    public void setUp() throws Exception {
        ScriptApproval.get().preapprove(SCRIPT_PARAM001, new GroovyLanguage());
        ScriptApproval.get().preapprove(SCRIPT_FALLBACK_PARAM001, new GroovyLanguage());
        ScriptApproval.get().preapprove(SCRIPT_PARAM002, new GroovyLanguage());
        ScriptApproval.get().preapprove(SCRIPT_FALLBACK_PARAM002, new GroovyLanguage());

        // scriptler setup
        scriptler = j.getInstance().getExtensionList(ScriptlerManagement.class).get(0);
        ScriptlerHelper scriptlerHelper = new ScriptlerHelper(scriptler);
        scriptFile = Files.createTempFile("uno-choice", "dummy.groovy").toFile();
        FileUtils.writeStringToFile(scriptFile, SCRIPT_PARAM001, Charset.defaultCharset(), false);
        FileItem fi = new FileItemImpl(scriptFile);
        scriptlerHelper.saveScript(fi, true, "dummy.groovy");

        scriptler.getConfiguration().getScriptById("dummy.groovy")
                .setParameters(Arrays.asList(new Parameter("arg1", "v1")));
    }

    /**
     * Test persisting jobs with parameters.
     *
     * @throws Exception in Jenkins rule
     */
    @Test
    public void testSaveParameters() throws Exception {
        FreeStyleProject project = j.createFreeStyleProject();
        GroovyScript scriptParam001 = new GroovyScript(new SecureGroovyScript(SCRIPT_PARAM001, false, null),
                new SecureGroovyScript(SCRIPT_FALLBACK_PARAM001, false, null));
        ChoiceParameter param001 = new ChoiceParameter("param001", "param001 description", "random-name",
                scriptParam001, AbstractUnoChoiceParameter.PARAMETER_TYPE_SINGLE_SELECT, true, 1);
        GroovyScript scriptParam002 = new GroovyScript(new SecureGroovyScript(SCRIPT_PARAM002, false, null),
                new SecureGroovyScript(SCRIPT_FALLBACK_PARAM002, false, null));
        CascadeChoiceParameter param002 = new CascadeChoiceParameter("param002", "param002 description", "random-name",
                scriptParam002, AbstractUnoChoiceParameter.PARAMETER_TYPE_SINGLE_SELECT, "param001", true, 1);
        ParametersDefinitionProperty param001Def = new ParametersDefinitionProperty(
                Arrays.asList(param001, param002));
        project.addProperty(param001Def);
        QueueTaskFuture<FreeStyleBuild> future = project.scheduleBuild2(0);
        FreeStyleBuild build = future.get();
        // even though the cascaded parameter will fail to evaluate, we should
        // still get a success here.
        assertEquals(Result.SUCCESS, build.getResult());
        XmlFile configXml = project.getConfigFile();
        FreeStyleProject reReadProject = (FreeStyleProject) configXml.read();
        int found = 0;
        for (Entry<JobPropertyDescriptor, JobProperty<? super FreeStyleProject>> entry : reReadProject.getProperties()
                .entrySet()) {
            JobProperty<? super FreeStyleProject> jobProperty = entry.getValue();
            if (jobProperty instanceof ParametersDefinitionProperty) {
                ParametersDefinitionProperty paramDef = (ParametersDefinitionProperty) jobProperty;
                List<ParameterDefinition> parameters = paramDef.getParameterDefinitions();
                for (ParameterDefinition parameter : parameters) {
                    if (parameter instanceof AbstractScriptableParameter) {
                        found++;
                        AbstractScriptableParameter choiceParam = (AbstractScriptableParameter) parameter;
                        String scriptText = ((GroovyScript) choiceParam.getScript()).getScript().getScript();
                        String fallbackScriptText = ((GroovyScript) choiceParam.getScript()).getFallbackScript()
                                .getScript();
                        assertTrue("Found an empty script!", StringUtils.isNotBlank(scriptText));
                        assertTrue("Found an empty fallback script!", StringUtils.isNotBlank(fallbackScriptText));
                        if (parameter.getName().equals("param001")) {
                            assertEquals(SCRIPT_PARAM001, scriptText);
                            assertEquals(SCRIPT_FALLBACK_PARAM001, fallbackScriptText);
                        } else {
                            assertEquals(SCRIPT_PARAM002, scriptText);
                            assertEquals(SCRIPT_FALLBACK_PARAM002, fallbackScriptText);
                        }
                    }
                }
            }
        }
        // We have two parameters before saving. We must have two now.
        assertEquals("Didn't find all parameters after persisting xml", 2, found);
    }

    /**
     * Use a parameter with Scriptler script, with parameters, persist it, and
     * confirm the XML configuration gets correctly persisted.
     *
     * @throws Exception in Jenkins rule
     */
    @Test
    public void testSaveScriptlerParameters() throws Exception {
        FreeStyleProject project = j.createFreeStyleProject();

        final ScriptlerBuilder scriptlerBuilder = new ScriptlerBuilder("", "dummy.groovy", false, new Parameter[] {new Parameter("arg1", "bla")});
        ScriptlerScript scriptParam001 = new ScriptlerScript(scriptlerBuilder, Boolean.TRUE);
        ChoiceParameter param001 = new ChoiceParameter("param001", "param001 description", "random-name",
                scriptParam001, AbstractUnoChoiceParameter.PARAMETER_TYPE_SINGLE_SELECT, true, 1);
        GroovyScript scriptParam002 = new GroovyScript(new SecureGroovyScript(SCRIPT_PARAM002, false, null),
                new SecureGroovyScript(SCRIPT_FALLBACK_PARAM002, false, null));
        CascadeChoiceParameter param002 = new CascadeChoiceParameter("param002", "param002 description", "random-name",
                scriptParam002, AbstractUnoChoiceParameter.PARAMETER_TYPE_SINGLE_SELECT, "param001", true, 1);
        ParametersDefinitionProperty param001Def = new ParametersDefinitionProperty(
                Arrays.asList(param001, param002));
        project.addProperty(param001Def);
        QueueTaskFuture<FreeStyleBuild> future = project.scheduleBuild2(0);
        FreeStyleBuild build = future.get();
        // even though the cascaded parameter will fail to evaluate, we should
        // still get a success here.
        assertEquals(Result.SUCCESS, build.getResult());
        XmlFile configXml = project.getConfigFile();
        FreeStyleProject reReadProject = (FreeStyleProject) configXml.read();
        int found = 0;
        for (Entry<JobPropertyDescriptor, JobProperty<? super FreeStyleProject>> entry : reReadProject.getProperties()
                .entrySet()) {
            JobProperty<? super FreeStyleProject> jobProperty = entry.getValue();
            if (jobProperty instanceof ParametersDefinitionProperty) {
                ParametersDefinitionProperty paramDef = (ParametersDefinitionProperty) jobProperty;
                List<ParameterDefinition> parameters = paramDef.getParameterDefinitions();
                for (ParameterDefinition parameter : parameters) {
                    if (parameter instanceof AbstractScriptableParameter) {
                        found++;
                        AbstractScriptableParameter choiceParam = (AbstractScriptableParameter) parameter;
                        Script leScript = choiceParam.getScript();
                        if (leScript instanceof GroovyScript) {
                            String scriptText = ((GroovyScript) leScript).getScript().getScript();
                            String fallbackScriptText = ((GroovyScript) choiceParam.getScript()).getFallbackScript()
                                    .getScript();
                            assertTrue("Found an empty script!", StringUtils.isNotBlank(scriptText));
                            assertTrue("Found an empty fallback script!", StringUtils.isNotBlank(fallbackScriptText));
                            assertEquals(SCRIPT_PARAM002, scriptText);
                            assertEquals(SCRIPT_FALLBACK_PARAM002, fallbackScriptText);
                        } else {
                            String scriptText = FileUtils.readFileToString(scriptFile, Charset.defaultCharset());
                            assertTrue("Found an empty script!", StringUtils.isNotBlank(scriptText));
                            assertEquals(SCRIPT_PARAM001, scriptText);
                            assertEquals("Wrong number of parameters for scriptler parameter!", 1, ((ScriptlerScript) leScript).getParameters().size());
                            assertEquals("Wrong scriptler parameter name!", "arg1", ((ScriptlerScript) leScript).getParameters()
                                    .keySet().iterator().next());
                        }
                    }
                }
            }
        }
        // We have two parameters before saving. We must have two now.
        assertEquals("Didn't find all parameters after persisting xml", 2, found);
    }

    @Test
    public void testSaveScriptlerParametersFromPipeline() throws Exception {
        WorkflowJob job = j.createProject(WorkflowJob.class);

        URL pipelinePath = TestPersistingParameters.class.getResource("PipelineWithScriptlerParameters.groovy");
        assert pipelinePath != null;
        byte[] contentBytes = Files.readAllBytes(Paths.get(pipelinePath.toURI()));
        String pipeline = new String(contentBytes, StandardCharsets.UTF_8);
        job.setDefinition(new CpsFlowDefinition(pipeline, true));

        QueueTaskFuture<WorkflowRun> future = job.scheduleBuild2(0);
        assert future != null;
        WorkflowRun run = future.get();
        assertEquals(Result.SUCCESS, run.getResult());

        XmlFile configXml = job.getConfigFile();
        WorkflowJob reReadJob = (WorkflowJob) configXml.read();
        int found = 0;
        for (JobProperty<? super WorkflowJob> jobProperty : reReadJob.getProperties().values()) {
            if (jobProperty instanceof ParametersDefinitionProperty) {
                ParametersDefinitionProperty paramDef = (ParametersDefinitionProperty) jobProperty;
                List<ParameterDefinition> parameters = paramDef.getParameterDefinitions();
                for (ParameterDefinition parameter : parameters) {
                    if (parameter instanceof AbstractScriptableParameter) {
                        found++;
                        AbstractScriptableParameter choiceParam = (AbstractScriptableParameter) parameter;
                        Script leScript = choiceParam.getScript();
                        if (leScript instanceof GroovyScript) {
                            String scriptText = ((GroovyScript) leScript).getScript().getScript();
                            String fallbackScriptText = ((GroovyScript) leScript).getFallbackScript().getScript();
                            assertTrue("Found an empty script!", StringUtils.isNotBlank(scriptText));
                            assertTrue("Found an empty fallback script!", StringUtils.isNotBlank(fallbackScriptText));
                            assertEquals(SCRIPT_PARAM002, scriptText);
                            assertEquals(SCRIPT_FALLBACK_PARAM002, fallbackScriptText);
                        } else {
                            String scriptText = FileUtils.readFileToString(scriptFile, Charset.defaultCharset());
                            assertTrue("Found an empty script!", StringUtils.isNotBlank(scriptText));
                            assertEquals(SCRIPT_PARAM001, scriptText);
                            assertEquals("Wrong number of parameters for scriptler parameter!", 1, ((ScriptlerScript) leScript).getParameters().size());
                            assertEquals("Wrong scriptler parameter name!", "arg1", ((ScriptlerScript) leScript).getParameters()
                                    .keySet().iterator().next());
                        }
                    }
                }
            }
        }
        // We have two parameters before saving. We must have two now.
        assertEquals("Didn't find all parameters after persisting xml", 2, found);
    }
}
