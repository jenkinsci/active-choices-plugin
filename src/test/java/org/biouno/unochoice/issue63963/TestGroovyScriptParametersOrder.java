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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import hudson.model.Descriptor;
import org.biouno.unochoice.CascadeChoiceParameter;
import org.biouno.unochoice.model.GroovyScript;
import org.jenkinsci.plugins.scriptsecurity.sandbox.groovy.SecureGroovyScript;
import org.jenkinsci.plugins.scriptsecurity.scripts.ScriptApproval;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.JenkinsRule.WebClient;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;
import org.xml.sax.SAXException;

import org.htmlunit.html.DomElement;
import org.htmlunit.html.DomNode;
import org.htmlunit.html.HtmlOption;
import org.htmlunit.html.HtmlPage;
import org.htmlunit.html.HtmlSelect;

import hudson.model.FreeStyleProject;
import hudson.model.ParametersDefinitionProperty;
import hudson.model.StringParameterDefinition;

/**
 * Test that verifies that the order of parameters is correct
 * when using a {@code Map}.
 *
 * @since 2.5.1
 */
@WithJenkins
class TestGroovyScriptParametersOrder {

    /**
     * Using test kindly provided by Jason Antman in JENKINS-63963. See issue in JIRA for more context
     * about the problem, analysis, and solution (mostly done by Jason).
     *
     * @throws IOException if it fails to load the script
     * @throws SAXException if the XML is malformed
     */
    @Test
    void testGroovyScriptParametersOrder(JenkinsRule j) throws IOException, SAXException, Descriptor.FormException {
        FreeStyleProject project = j.createFreeStyleProject();

        StringParameterDefinition teamUid = new StringParameterDefinition("TEAM_UID", "foo", "Team name (UID)");
        StringParameterDefinition environment = new StringParameterDefinition("ENVIRONMENT", "bar", "The environment name to use for this stack (default: dev)");

        String scriptText = """
                Map getOptions(String teamId, String enviro) {
                    def found_snapshots = ["":"Select a backup file or snapshot to restore for " + teamId + " enviro " + enviro]; // value => selection text
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


        WebClient wc = j.createWebClient();
        wc.setThrowExceptionOnFailingStatusCode(false);
        HtmlPage configPage = wc.goTo("job/" + project.getName() + "/build?delay=0sec");
        DomElement renderedParameterElement = configPage.getElementById("random-name");
        HtmlSelect select = null;
        for (DomNode node: renderedParameterElement.getChildren()) {
            if (node instanceof HtmlSelect) {
                select = (HtmlSelect) node;
                break;
            }
        }

        assertNotNull(select, "Missing cascade parameter select HTML node element!");

        List<HtmlOption> htmlOptions = select.getOptions();
        final List<String> options = htmlOptions
                .stream()
                .map(HtmlOption::getText)
                .toList();
        final List<String> expected = new LinkedList<>();
        {
            expected.add("Select a backup file or snapshot to restore for foo enviro bar");
            expected.add("snapshot e");
            expected.add("snapshot d");
            expected.add("snapshot c");
            expected.add("snapshot b");
            expected.add("snapshot a");
        }
        assertEquals(expected.size(), options.size(), "Wrong number of HTML options rendered");
        assertEquals(expected, options, "Wrong HTML options rendered (or out of order)");
    }
}
