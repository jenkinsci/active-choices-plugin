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
package org.biouno.unochoice.issue74963;

import hudson.model.FileParameterValue;
import org.apache.commons.fileupload2.core.FileItem;
import org.apache.commons.io.FileUtils;
import org.biouno.unochoice.BaseUiTest;
import org.jenkinsci.plugins.scriptler.ScriptlerHelper;
import org.jenkinsci.plugins.scriptler.ScriptlerManagement;
import org.jenkinsci.plugins.scriptsecurity.scripts.ScriptApproval;
import org.jenkinsci.plugins.scriptsecurity.scripts.languages.GroovyLanguage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.Issue;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.recipes.LocalData;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * In JENKINS-74963 an exception was raised and the spinning animation never disappeared.
 * That caused the plug-in to fail to locate the HTML SELECT element.
 *
 * <p>The test in this code was shared by JIRA user "barel" on JENKINS-75194.</p>
 */
@Issue("JENKINS-74963")
class TestSelectWithSpinningWheelMayBeNull extends BaseUiTest {

    private static final String SCRIPTLER_DEPLOYMENT_REGIONS = """
            // Simplified: Return a static mapping for deployment regions
            def regions = [
                DEV: ["us-east-1", "us-west-1"],
                PRODUCTION: ["eu-central-1", "eu-west-1"]
            ]
            // Return the regions based on the ENVIRONMENT parameter (or an empty list if not defined)
            return (ENVIRONMENT && regions.containsKey(ENVIRONMENT)) ? regions[ENVIRONMENT] : []\s
            """;
    private static final String SCRIPTLER_ECR_IMAGES = """
            // Simplified: Return a static list of image tags
            return ["v1.0.0", "v1.1.0", "v2.0.0"]\s
            """;

    /**
     * Create the script in Scriptler.
     */
    @BeforeEach
    public void setUp(JenkinsRule j) {
        super.setUp(j);
        this.j = j;
        ScriptApproval.get().preapprove(SCRIPTLER_DEPLOYMENT_REGIONS, new GroovyLanguage());
        ScriptApproval.get().preapprove(SCRIPTLER_ECR_IMAGES, new GroovyLanguage());
        // scriptler setup
        final ScriptlerManagement scriptler = j.getInstance().getExtensionList(ScriptlerManagement.class).get(0);
        final ScriptlerHelper scriptlerHelper = new ScriptlerHelper(scriptler);

        try {
            // get_deployment_regions.groovy
            final File scriptFile1 = Files.createTempFile("uno-choice", "Cascade1.groovy").toFile();
            FileUtils.writeStringToFile(scriptFile1, SCRIPTLER_DEPLOYMENT_REGIONS, Charset.defaultCharset(), false);
            final FileItem<?> fi1 = new FileParameterValue.FileItemImpl2(scriptFile1);
            scriptlerHelper.saveScript(fi1, true, "get_deployment_regions.groovy");

            // aws_ecr_images_tags.groovy
            final File scriptFile2 = Files.createTempFile("uno-choice", "Cascade2.groovy").toFile();
            FileUtils.writeStringToFile(scriptFile2, SCRIPTLER_ECR_IMAGES, Charset.defaultCharset(), false);
            FileItem<?> fi2 = new FileParameterValue.FileItemImpl2(scriptFile2);
            scriptlerHelper.saveScript(fi2, true, "aws_ecr_images_tags.groovy");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @LocalData("test")
    void test() throws Exception {
        driver.get(j.getURL().toString() + "job/test/build");

        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".jenkins-spinner")));

        WebElement environmentSelect = driver.findElement(By.cssSelector("input[value='ENVIRONMENT'] + select"));
        assertTrue(environmentSelect.isDisplayed());
        assertTrue(environmentSelect.isEnabled());
        assertEquals("DEV", new Select(environmentSelect).getFirstSelectedOption().getText());

        WebElement awsRegionSelect = findSelect("AWS_REGION");
        assertTrue(awsRegionSelect.isDisplayed());
        assertTrue(awsRegionSelect.isEnabled());
        assertEquals("us-east-1", new Select(awsRegionSelect).getFirstSelectedOption().getText());

        WebElement ecrImageTagSelect = findSelect("ECR_IMAGE_TAG");
        assertTrue(ecrImageTagSelect.isDisplayed());
        assertTrue(ecrImageTagSelect.isEnabled());
        assertEquals("v1.0.0", new Select(ecrImageTagSelect).getFirstSelectedOption().getText());

    }
}
