/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2021 Ioannis Moutsatsos, Bruno P. Kinoshita
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
package org.biouno.unochoice.issue63983;

import hudson.model.FileParameterValue;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FileUtils;
import org.biouno.unochoice.model.ScriptlerScript;
import org.jenkinsci.plugins.scriptler.ScriptlerHelper;
import org.jenkinsci.plugins.scriptler.ScriptlerManagement;
import org.jenkinsci.plugins.scriptler.builder.ScriptlerBuilder;
import org.jenkinsci.plugins.scriptler.config.Parameter;
import org.jenkinsci.plugins.scriptler.config.Script;
import org.jenkinsci.plugins.scriptler.config.ScriptlerConfiguration;
import org.jenkinsci.plugins.scriptsecurity.sandbox.RejectedAccessException;
import org.jenkinsci.plugins.scriptsecurity.scripts.ScriptApproval;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.Issue;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.recipes.LocalData;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Collections;

@Issue("63983")
public class TestSandboxAndApproval {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Test
    @LocalData
    public void testGroovyScriptApprovedNoSandboxOK() throws Exception {
        final ScriptlerManagement scriptler = j.getInstance().getExtensionList(ScriptlerManagement.class).get(0);
        final ScriptlerHelper scriptlerHelper = new ScriptlerHelper(scriptler);
        // This method is sandboxed: https://github.com/jenkinsci/script-security-plugin/blob/ddce7c7653f6e15fefc081cd45e600805ae20fec/src/main/resources/org/jenkinsci/plugins/scriptsecurity/sandbox/whitelists/blacklist#L12-L13
        final String scriptText = "import hudson.model.Hudson;\n" +
                "Hudson.getInstance();\n" +
                "return \"Got instance!\";";
        final File scriptFile = Files.createTempFile("uno-choice", "63983").toFile();
        FileUtils.writeStringToFile(scriptFile, scriptText, Charset.defaultCharset(), false);
        final FileItem fi = new FileParameterValue.FileItemImpl(scriptFile);
        scriptlerHelper.saveScript(fi, true, "63983");
        Script script = new Script("63983.groovy", "63983.groovy", "A comment.", false, Collections.emptyList(), false);
        ScriptlerConfiguration.getConfiguration().addOrReplace(script);
        ScriptlerBuilder scriptlerBuilder = new ScriptlerBuilder("", script.getId(), false, new Parameter[0]);
        ScriptlerScript scriptParam001 = new ScriptlerScript(scriptlerBuilder, Boolean.FALSE);
        // Now let's pre-approve all scripts. The script will run since it's approved, and won't use the
        // sandbox since we specified so.
        ScriptApproval.get().preapproveAll();
        Object output = scriptParam001.eval();
        Assert.assertNotNull(output);
        Assert.assertEquals("Got instance!", output);
    }

    @Test
    @LocalData
    public void testGroovyScriptPendingSandboxRejected() throws Exception {
        final ScriptlerManagement scriptler = j.getInstance().getExtensionList(ScriptlerManagement.class).get(0);
        final ScriptlerHelper scriptlerHelper = new ScriptlerHelper(scriptler);
        final String scriptText = "import hudson.model.Hudson;\n" +
                "Hudson.getInstance();\n" +
                "return \"Got instance!\";";
        final File scriptFile = Files.createTempFile("uno-choice", "63983").toFile();
        FileUtils.writeStringToFile(scriptFile, scriptText, Charset.defaultCharset(), false);
        final FileItem fi = new FileParameterValue.FileItemImpl(scriptFile);
        scriptlerHelper.saveScript(fi, true, "63983");
        Script script = new Script("63983.groovy", "63983.groovy", "A comment.", false, Collections.emptyList(), false);
        ScriptlerConfiguration.getConfiguration().addOrReplace(script);
        ScriptlerBuilder scriptlerBuilder = new ScriptlerBuilder("", script.getId(), false, new Parameter[0]);
        ScriptlerScript scriptParam001 = new ScriptlerScript(scriptlerBuilder, Boolean.TRUE);
        // Now let's deny all scripts
        ScriptApproval.get().clearApprovedScripts();
        try {
            scriptParam001.eval();
            Assert.fail("Not supposed to evaluate when using Security, and GroovyScript not approved");
        } catch (RuntimeException re) {
            Assert.assertTrue(re.getCause() instanceof RejectedAccessException);
        }
    }

    @Test
    @LocalData
    public void testGroovyScriptPendingNoSandboxRejected() throws Exception {
        final ScriptlerManagement scriptler = j.getInstance().getExtensionList(ScriptlerManagement.class).get(0);
        final ScriptlerHelper scriptlerHelper = new ScriptlerHelper(scriptler);
        final String scriptText = "import hudson.model.Hudson;\n" +
                "Hudson.getInstance();\n" +
                "return \"Got instance!\";";
        final File scriptFile = Files.createTempFile("uno-choice", "63983").toFile();
        FileUtils.writeStringToFile(scriptFile, scriptText, Charset.defaultCharset(), false);
        final FileItem fi = new FileParameterValue.FileItemImpl(scriptFile);
        scriptlerHelper.saveScript(fi, true, "63983");
        Script script = new Script("63983.groovy", "63983.groovy", "A comment.", false, Collections.emptyList(), false);
        ScriptlerConfiguration.getConfiguration().addOrReplace(script);
        ScriptlerBuilder scriptlerBuilder = new ScriptlerBuilder("", script.getId(), false, new Parameter[0]);
        ScriptlerScript scriptParam001 = new ScriptlerScript(scriptlerBuilder, Boolean.FALSE);
        // Now let's deny all scripts
        ScriptApproval.get().clearApprovedScripts();
        try {
            scriptParam001.eval();
            Assert.fail("Not supposed to evaluate when using Security, and GroovyScript not approved");
        } catch (RuntimeException re) {
            Assert.assertTrue(re.getCause() instanceof RuntimeException && re.getMessage().contains("not yet approved"));
        }
    }

    @Test
    @LocalData
    public void testGroovyScriptApprovedSandboxRejected() throws Exception {
        final ScriptlerManagement scriptler = j.getInstance().getExtensionList(ScriptlerManagement.class).get(0);
        final ScriptlerHelper scriptlerHelper = new ScriptlerHelper(scriptler);
        final String scriptText = "import hudson.model.Hudson;\n" +
                "Hudson.getInstance();\n" +
                "return \"Got instance!\";";
        final File scriptFile = Files.createTempFile("uno-choice", "63983").toFile();
        FileUtils.writeStringToFile(scriptFile, scriptText, Charset.defaultCharset(), false);
        final FileItem fi = new FileParameterValue.FileItemImpl(scriptFile);
        scriptlerHelper.saveScript(fi, true, "63983");
        Script script = new Script("63983.groovy", "63983.groovy", "A comment.", false, Collections.emptyList(), false);
        ScriptlerConfiguration.getConfiguration().addOrReplace(script);
        ScriptlerBuilder scriptlerBuilder = new ScriptlerBuilder("", script.getId(), false, new Parameter[0]);
        ScriptlerScript scriptParam001 = new ScriptlerScript(scriptlerBuilder, Boolean.TRUE);
        // We are approving all scripts here. However, we also have sandbox enabled. The Hudson.getInstance() will
        // trigger the sandbox alert, and the script is supposed to fail evaluation.
        ScriptApproval.get().preapproveAll();
        try {
            scriptParam001.eval();
            Assert.fail("Not supposed to evaluate when using Security, and GroovyScript not approved");
        } catch (RuntimeException re) {
            Assert.assertTrue(re.getCause() instanceof RejectedAccessException);
        }
    }
}
