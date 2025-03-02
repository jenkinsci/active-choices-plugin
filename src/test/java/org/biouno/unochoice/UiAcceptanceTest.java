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
package org.biouno.unochoice;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;
import org.jvnet.hudson.test.recipes.LocalData;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@WithJenkins
class UiAcceptanceTest extends BaseUiTest {

    @LocalData("test")
    @Test
    void testHelpFiles() throws Exception {
        // Load the page
        driver.get(j.getURL().toString() + "job/test/configure");

        // Get the help container div of PARAM1.
        final WebElement param1ParamDiv = findParamDiv("PARAM1");

        final WebElement helpTextDiv = param1ParamDiv.findElement(By.cssSelector("div.help-area > div.help"));
        assertFalse(helpTextDiv.isDisplayed());

        final WebElement helpIcon = param1ParamDiv.findElement(By.cssSelector("a.jenkins-help-button"));
        wait.until(ExpectedConditions.elementToBeClickable(helpIcon));
        Actions actions = new Actions(driver);
        actions
                .moveToElement(helpIcon)
                .click()
                .perform();

        wait.withMessage(() -> "The help text should have been displayed").until(d -> helpTextDiv.isDisplayed());

        assertTrue(helpTextDiv.getText().startsWith("This is a simple parameter"));
    }

    @LocalData
    @Test
    void test() throws Exception {
        // Load the page
        driver.get(j.getURL().toString() + "job/test/build");

        /*
         PARAM1 and PARAM1A are SINGLE_SELECT based
         */
        WebElement param1 = findSelect("PARAM1");

        assertTrue(param1.isDisplayed());
        assertTrue(param1.isEnabled());

        checkOptions(() -> findSelect("PARAM1"), "A", "B", "C");

        assertEquals("A", new Select(param1).getFirstSelectedOption().getText());
        checkOptions(() -> findSelect("PARAM1A"), "A1", "A2", "A3");

        new Select(param1).selectByValue("B");
        checkOptions(() -> findSelect("PARAM1A"), "B1", "B2", "B3");

        new Select(param1).selectByValue("C");
        checkOptions(() -> findSelect("PARAM1A"), "C1", "C2", "C3");

        new Select(param1).selectByValue("A");
        checkOptions(() -> findSelect("PARAM1A"), "A1", "A2", "A3");

        /*
         PARAM2 and PARAM2A are RADIO based
         */
        List<WebElement> param2Choices = findRadios("PARAM2");

        checkRadios(() -> param2Choices, "A", "B", "C");
        // this is until something is selected
        checkRadios(() -> findRadios("PARAM2A"), "1", "2", "3");

        param2Choices.get(0).click();
        checkRadios(() -> findRadios("PARAM2A"), "A1", "A2", "A3");

        param2Choices.get(1).click();
        checkRadios(() -> findRadios("PARAM2A"), "B1", "B2", "B3");

        param2Choices.get(2).click();
        checkRadios(() -> findRadios("PARAM2A"), "C1", "C2", "C3");

        param2Choices.get(0).click();
        checkRadios(() -> findRadios("PARAM2A"), "A1", "A2", "A3");

        /*
         PARAM3 and PARAM3A are CHECKBOX based
         */
        List<WebElement> param3Choices = findCheckboxes("PARAM3");

        checkRadios(() -> param3Choices, "A", "B", "C");
        // this is until something is selected
        checkRadios(() -> findCheckboxes("PARAM3A"));

        param3Choices.get(0).click();
        checkRadios(() -> findCheckboxes("PARAM3A"), "A1", "A2", "A3");

        param3Choices.get(1).click();
        checkRadios(() -> findCheckboxes("PARAM3A"), "A1", "B1", "A2", "B2", "A3", "B3");

        param3Choices.get(2).click();
        checkRadios(() -> findCheckboxes("PARAM3A"), "A1", "B1", "C1", "A2", "B2", "C2", "A3", "B3", "C3");

        param3Choices.get(0).click();
        checkRadios(() -> findCheckboxes("PARAM3A"), "B1", "C1", "B2", "C2", "B3", "C3");

        param3Choices.get(1).click();
        checkRadios(() -> findCheckboxes("PARAM3A"), "C1", "C2", "C3");

        param3Choices.get(0).click();
        checkRadios(() -> findCheckboxes("PARAM3A"), "A1", "C1", "A2", "C2", "A3", "C3");

        param3Choices.get(2).click();
        checkRadios(() -> findCheckboxes("PARAM3A"), "A1", "A2", "A3");

        param3Choices.get(0).click();
        checkRadios(() -> findCheckboxes("PARAM3A"));

        /*
         PARAM4 and PARAM4A are MULTI_SELECT based
         */
        WebElement param4Input = findSelect("PARAM4");

        checkOptions(() -> param4Input, "A", "B", "C");
        // this is until something is selected
        checkOptions(() -> findSelect("PARAM4A"));

        new Select(param4Input).selectByVisibleText("A");
        checkOptions(() -> findSelect("PARAM4A"), "A1", "A2", "A3");

        new Select(param4Input).selectByVisibleText("B");
        checkOptions(() -> findSelect("PARAM4A"), "A1", "B1", "A2", "B2", "A3", "B3");

        new Select(param4Input).selectByVisibleText("C");
        checkOptions(() -> findSelect("PARAM4A"), "A1", "B1", "C1", "A2", "B2", "C2", "A3", "B3", "C3");

        new Select(param4Input).deselectByVisibleText("A");
        checkOptions(() -> findSelect("PARAM4A"), "B1", "C1", "B2", "C2", "B3", "C3");

        new Select(param4Input).deselectByVisibleText("B");
        checkOptions(() -> findSelect("PARAM4A"), "C1", "C2", "C3");

        new Select(param4Input).selectByValue("A");
        checkOptions(() -> findSelect("PARAM4A"), "A1", "C1", "A2", "C2", "A3", "C3");

        new Select(param4Input).deselectByValue("C");
        checkOptions(() -> findSelect("PARAM4A"), "A1", "A2", "A3");

        new Select(param4Input).deselectByValue("A");
        checkOptions(() -> findSelect("PARAM4A"));

    }
}
