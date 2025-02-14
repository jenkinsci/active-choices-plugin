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
package org.biouno.unochoice.issue74967;

import org.biouno.unochoice.BaseUiTest;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.Issue;
import org.jvnet.hudson.test.recipes.LocalData;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * In JENKINS-74967 a JS exception when the user entered anything in the filter field.
 * The JavaScript code is not very well-designed (mea-culpa) so it is not easy to test
 * it. For that reason, the config.xml attached in this issue was used here to write a
 * complete e2e test (i.e. yes, we are killing the ant with an elephant).
 */
@Issue("JENKINS-74967")
class TestFilter extends BaseUiTest {

    /**
     * Tests that the page loads with paramA=AAA, filter empty, and paramB showing AAA-1, AAA-2, and AAA-3.
     * Then, it enters the filter text BB, paramA is updated to BBB (matches filter), which cascades to
     * changing paramB to BBB-1, BBB-2, BBB-3.
     *
     * @throws Exception resolving the test web pages
     */
    @Test
    @LocalData("test")
    void test() throws Exception {
        driver.get(j.getURL().toString() + "job/test/build");

        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".jenkins-spinner")));

        WebElement paramA = findSelect("paramA");
        assertTrue(paramA.isDisplayed());
        assertTrue(paramA.isEnabled());
        assertEquals("AAA", new Select(paramA).getFirstSelectedOption().getText());

        List<WebElement> paramB = findRadios("paramB");
        for (int i = 0; i < paramB.size(); i++) {
            WebElement param = paramB.get(i);
            assertTrue(getLabel(param).isDisplayed());
            assertTrue(param.isEnabled());
            assertEquals("AAA-" + (i + 1), param.getDomAttribute("value"));
        }

        WebElement filterElement = driver.findElement(By.cssSelector("div.active-choice[name='parameter'] > input.uno_choice_filter"));
        filterElement.sendKeys("BB");

        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".jenkins-spinner")));

        paramA = findSelect("paramA");
        assertTrue(paramA.isDisplayed());
        assertTrue(paramA.isEnabled());
        assertEquals("BBB", new Select(paramA).getFirstSelectedOption().getText());

        paramB = findRadios("paramB");
        for (int i = 0; i < paramB.size(); i++) {
            WebElement param = paramB.get(i);
            assertTrue(getLabel(param).isDisplayed());
            assertTrue(param.isEnabled());
            assertEquals("BBB-" + (i + 1), param.getDomAttribute("value"));
        }

    }
}
