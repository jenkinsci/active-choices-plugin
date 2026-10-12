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

package org.biouno.unochoice.github_1017;

import hudson.model.FreeStyleProject;
import hudson.model.ParametersDefinitionProperty;
import org.biouno.unochoice.BaseUiTest;
import org.biouno.unochoice.CascadeChoiceParameter;
import org.biouno.unochoice.model.GroovyScript;
import org.jenkinsci.plugins.scriptsecurity.sandbox.groovy.SecureGroovyScript;
import org.jenkinsci.plugins.scriptsecurity.scripts.ScriptApproval;
import org.jenkinsci.plugins.scriptsecurity.scripts.languages.GroovyLanguage;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.Issue;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test that a reference parameter, when referencing itself, does not
 * result in an infinite loop.
 */
@Issue("GH-1017")
@WithJenkins
public class TestReferenceParameterSelfReference extends BaseUiTest {

    private static final String PARAMETER_NAME = "SKIP";

    private static final String SCRIPT = """
            Map<String, String> aRtn = [
            		'DRYRUN': 'Perform dryrun, i.e. no commits, packaging, deployment and JIRA update',
            		'MAIL': 'Skip "Sending notification mails"' + (SKIP.toString().find(/^(.*,)*MAIL(,.+)*$/) ? ':selected' : ''),
            		'DEPLOY':'Skip "Deploy build package"',
            		'PACKAGE':'Skip "Build update package"',
            		'DEPLOYPKG':'Skip "Deploy update package"',
            		'JIRA':'Skip "Update JIRA"',
            		'CLEAN':'Skip "Cleanup"' + (SKIP.toString().find(/^(.*,)*CLEAN(,.+)*$/) ? ':selected' : ''),
            		'GET':'Skip "Get sources"' + (SKIP.toString().find(/^(.*,)*GET(,.+)*$/) ? ':selected' : ''),
            		'GENDIFF':'Skip "Evaluate differences"' + (SKIP.toString().find(/^(.*,)*GENDIFF(,.+)*$/) ? ':selected' : ''),
            		'ADJPROJ':'Skip "Adjust profilefiles"' + (SKIP.toString().find(/^(.*,)*ADJPROJ(,.+)*$/) ? ':selected' : ''),
            		'STARTLABEL':'Skip "Prepare startlabel"' + (SKIP.toString().find(/^(.*,)*STARTLABEL(,.+)*$/) ? ':selected' : ''),
            		'COMPILE':'Skip "First compile"' + (SKIP.toString().find(/^(.*,)*COMPILE(,.+)*$/) ? ':selected' : ''),
            		'STORE':'Skip "Storing"' + (SKIP.toString().find(/^(.*,)*STORE(,.+)*$/) ? ':selected' : ''),
            		'ENDLABEL':'Skip "Prepare endlabel"' + (SKIP.toString().find(/^(.*,)*ENDLABEL(,.+)*$/) ? ':selected' : ''),
            		'APPDIFF':'Skip "Apply differences"' + (SKIP.toString().find(/^(.*,)*APPDIFF(,.+)*$/) ? ':selected' : ''),
            		'ADJPROJ2':'Skip "Adjust profilefiles 2nd time"' + (SKIP.toString().find(/^(.*,)*ADJPROJ2(,.+)*$/) ? ':selected' : ''),
            		'COMPILE2':'Skip "Second compile"' + (SKIP.toString().find(/^(.*,)*COMPILE2(,.+)*$/) ? ':selected' : ''),
            		'DELTA':'Skip "Generate delta"' + (SKIP.toString().find(/^(.*,)*DELTA(,.+)*$/) ? ':selected' : ''),
            		'OBFUSCATE':'Skip "Obfuscate assemblies"' + (SKIP.toString().find(/^(.*,)*OBFUSCATE(,.+)*$/) ? ':selected' : ''),
            		'SIGN':'Skip "Sign binaries"' + (SKIP.toString().find(/^(.*,)*SIGN(,.+)*$/) ? ':selected' : ''),
            		'ARCHIVE':'Skip "Archiving"' + (SKIP.toString().find(/^(.*,)*ARCHIVE(,.+)*$/) ? ':selected' : '')
            	]
            
            	if (SKIP.toString().find(/^(.*,)*DRYRUN(,.+)*$/)) {
            		aRtn['DRYRUN'] += (aRtn['DRYRUN'].find(/^.*:selected.*$/) ? '' : ':selected')
            	}
            	if (aRtn['DRYRUN'].find(/^.*:selected.*$/)) { // DRYRUN selected, disable DEPLOYMENT and JIRA
            		aRtn.remove('DEPLOY')
            		aRtn.remove('PACKAGE')
            		aRtn.remove('DEPLOYPKG')
            		aRtn.remove('JIRA')
            	}
            
            return aRtn
            """;

    @Test
    void testSelfReferencingParameterRendersWithoutInfiniteLoop() throws Exception {

        ScriptApproval.get().preapprove(SCRIPT, GroovyLanguage.get());

        GroovyScript groovyScript = new GroovyScript(
                new SecureGroovyScript(SCRIPT, false, null),
                new SecureGroovyScript("return ''", false, null)
        );

        // Recursion here, as the PARAMETER_NAME is used for name and for reference!
        CascadeChoiceParameter parameter =
                new CascadeChoiceParameter(
                        PARAMETER_NAME,
                        "Self-referencing parameter",
                        UUID.randomUUID().toString(),
                        groovyScript,
                        "PT_CHECKBOX",
                        PARAMETER_NAME,
                        false,
                        0
                );

        FreeStyleProject project =
                j.createFreeStyleProject("self-reference");

        project.addProperty(
                new ParametersDefinitionProperty(parameter)
        );

        driver.manage().timeouts()
                .pageLoadTimeout(Duration.ofSeconds(20));

        driver.get(
                j.getURL().toExternalForm()
                        + project.getUrl()
                        + "build?delay=0sec"
        );

        waitForCheckboxes(PARAMETER_NAME, 21);
        assertEquals(21, findCheckboxes(PARAMETER_NAME).size(), "The self-referencing checkbox parameter should render its choices");
    }

}
