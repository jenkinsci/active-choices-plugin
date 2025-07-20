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
package org.biouno.unochoice.issue63963;

import hudson.model.Descriptor;
import hudson.model.FreeStyleProject;
import hudson.model.ParametersDefinitionProperty;
import hudson.model.StringParameterDefinition;
import org.biouno.unochoice.BaseUiTest;
import org.biouno.unochoice.CascadeChoiceParameter;
import org.biouno.unochoice.model.GroovyScript;
import org.jenkinsci.plugins.scriptsecurity.sandbox.groovy.SecureGroovyScript;
import org.jenkinsci.plugins.scriptsecurity.scripts.ScriptApproval;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.Issue;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test that verifies that the order of parameters is correct
 * when using a {@code Map}.
 *
 * @since 2.5.1
 */
@Issue("JENKINS-63963")
@WithJenkins
class TestGroovyScriptParametersOrder extends BaseUiTest {

    /**
     * Using test kindly provided by Jason Antman in JENKINS-63963. See issue in JIRA for more context
     * about the problem, analysis, and solution (mostly done by Jason).
     *
     * @throws IOException if it fails to load the script
     */
    @Test
    void testGroovyScriptParametersOrder() throws IOException, Descriptor.FormException {
        FreeStyleProject project = j.createFreeStyleProject();

        StringParameterDefinition teamUid = new StringParameterDefinition("TEAM_UID", "foo", "Team name (UID)");
        StringParameterDefinition environment = new StringParameterDefinition("ENVIRONMENT", "bar", "The environment name to use for this stack (default: dev)");

        String scriptText = """
                Map getOptions(String teamId, String env) {
                    def found_snapshots = ["":"Select a backup file or snapshot to restore for " + teamId + " env " + env]; // value => selection text
                    found_snapshots["one"] = "snapshot e";
                    found_snapshots["two"] = "snapshot d";
                    found_snapshots["three"] = "snapshot c";
                    found_snapshots["four"] = "snapshot b";
                    found_snapshots["five"] = "snapshot a";
                    return found_snapshots;
                }
                
                // get the choice parameters
                if (binding.variables.get("TEAM_UID") == null) { return [''] }
                def team_uid = binding.variables.get("TEAM_UID")
                if (binding.variables.get("ENVIRONMENT") == null) { return [''] }
                def environment = binding.variables.get("ENVIRONMENT")
                if(environment == '' || team_uid == '') { return [''] }
                def snapshots;
                snapshots = getOptions(team_uid, environment);
                return snapshots;""";
        ScriptApproval.get().approveSignature("method groovy.lang.Binding getVariables");
        SecureGroovyScript secureScript = new SecureGroovyScript(scriptText, true, null);

        GroovyScript script = new GroovyScript(secureScript, secureScript);
        CascadeChoiceParameter backupFileName = new CascadeChoiceParameter(
                "BACKUP_FILENAME",
                "Backup filename parameter",
                "random-name",
                script,
                CascadeChoiceParameter.PARAMETER_TYPE_SINGLE_SELECT,
                "TEAM_UID,ENVIRONMENT",
                true,
                1);

        project.addProperty(new ParametersDefinitionProperty(Arrays.asList(teamUid, environment, backupFileName)));
        project.save();

        driver.get(j.getURL().toString() + "job/" + project.getName() + "/build?delay=0sec");

        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".jenkins-spinner")));

        By selectOptions = By.cssSelector("div.active-choice:has([name='name'][value='BACKUP_FILENAME']) > div > select > option");
        wait.until(ExpectedConditions.numberOfElementsToBe(selectOptions, 6));
        WebElement renderedParameterElement = findSelect("BACKUP_FILENAME");
        Select select = new Select(renderedParameterElement);
        List<WebElement> htmlOptions = select.getOptions();
        final List<String> expected = new LinkedList<>();
        expected.add("Select a backup file or snapshot to restore for foo env bar");
        expected.add("snapshot e");
        expected.add("snapshot d");
        expected.add("snapshot c");
        expected.add("snapshot b");
        expected.add("snapshot a");
        assertEquals(expected.size(), htmlOptions.size(), "Wrong number of HTML options rendered");
        for (int i = 0; i < htmlOptions.size(); i++) {
            assertEquals(expected.get(i), htmlOptions.get(i).getText(), "Wrong HTML options rendered (or out of order)");
        }
    }
}
