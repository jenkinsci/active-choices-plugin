/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2024 Ioannis Moutsatsos, Bruno P. Kinoshita
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
package org.biouno.unochoice.issue71909;

import org.biouno.unochoice.BaseUiTest;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.Issue;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;
import org.jvnet.hudson.test.recipes.LocalData;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * See JENKINS-71909.
 *
 * <p>JENKINS-71365 made Stapler proxy asynchronous and used JavaScript promises. This
 * caused a regression where users could no longer render parameters in a deterministic
 * way.</p>
 *
 * <p>To have proper asynchronous reactivity, we would have to either implement something
 * akin to Vue or React's reactivity engine, since users would have to be able to declare
 * how parameters react based on more elaborated constraints.</p>
 *
 * <p>This test uses the example from JENKINS-71909 to reproduce the bug.</p>
 *
 * @since 2.8.4
 */
@Issue("JENKINS-71909")
@WithJenkins
class TestRevertingAsynchronousProxy extends BaseUiTest {

    @LocalData("test")
    @Test
    void test() throws Exception {
        // Load the page
        driver.get(j.getURL().toString() + "job/test/build");

        // From OP:
        //
        // The issue is when "Item2" is selected, then the 4) active-choice
        // elements returns as selected "buster" instead of "bullseye", this
        // because, when first called the wrong server list is returned from
        // 3), because 3) is called with "a1" as parameter instead of "a2"
        // as should be after the 2) gets executed.

        // From: https://bugbug.io/blog/software-testing/StaleElementReferenceException/
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("return document.readyState").equals("complete");

        waitLoadingMessage();

        WebElement targetParam = findSelect("TARGET");

        wait.until(ExpectedConditions.elementToBeClickable(targetParam));

        assertTrue(targetParam.isDisplayed());
        assertTrue(targetParam.isEnabled());
        new Select(targetParam).selectByValue("Item3");

        waitLoadingMessage();

        try {
            wait.until(ExpectedConditions.elementToBeClickable(findRadios("DOCKER_BASE_IMAGE").get(0)));
            assertEquals(2, findRadios("DOCKER_BASE_IMAGE").size());
        } catch (StaleElementReferenceException e) {
            List<WebElement> dockerBaseImageParam = findRadios("DOCKER_BASE_IMAGE");
            wait.until(ExpectedConditions.elementToBeClickable(dockerBaseImageParam.get(0)));
            assertEquals(2, dockerBaseImageParam.size());
        }

        checkRadios(radios("DOCKER_BASE_IMAGE"), "buster", "bullseye");

        assertEquals("buster", findRadios("DOCKER_BASE_IMAGE").get(0).getDomAttribute("value"));
        assertEquals("true", findRadios("DOCKER_BASE_IMAGE").get(0).getDomAttribute("checked"));

        wait.until(ExpectedConditions.elementToBeClickable(findCheckboxes("MACHINES").get(0)));

        try {
            assertEquals("server2", findCheckboxes("MACHINES").get(1).getDomAttribute("value"));
            findCheckboxes("MACHINES").get(1).click();
        } catch (StaleElementReferenceException e) {
            assertEquals("server2", findCheckboxes("MACHINES").get(1).getDomAttribute("value"));
            findCheckboxes("MACHINES").get(1).click();
        }

        waitLoadingMessage();

        checkRadios(radios("DOCKER_BASE_IMAGE"), "buster", "bullseye");

        assertEquals("bullseye", findRadios("DOCKER_BASE_IMAGE").get(1).getDomAttribute("value"));
        assertEquals("true", findRadios("DOCKER_BASE_IMAGE").get(1).getDomAttribute("checked"));
    }
}
