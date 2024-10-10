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

import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.commons.lang3.StringUtils;
import org.htmlunit.ElementNotFoundException;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.jvnet.hudson.test.JenkinsRule;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.text.MessageFormat;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public abstract class BaseUiTest {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    protected WebDriver driver;
    protected WebDriverWait wait;

    protected static boolean isCi() {
        return StringUtils.isNotBlank(System.getenv("CI"));
    }

    protected static final Duration MAX_WAIT = Duration.parse(System.getProperty("ui.loading.timeout", "PT300S"));

    @BeforeClass
    public static void setUpClass() {
        if (isCi()) {
            // The browserVersion needs to match what is provided by the Jenkins Infrastructure
            // If you see an exception like this:
            //
            // org.openqa.selenium.SessionNotCreatedException: Could not start a new session. Response code 500. Message: session not created: This version of ChromeDriver only supports Chrome version 114
            // Current browser version is 112.0.5615.49 with binary path /usr/bin/chromium-browser
            //
            // Then that means you need to update the version here to match the current browser version.
            WebDriverManager.chromedriver().browserVersion("112").setup();
        } else {
            WebDriverManager.chromedriver().setup();
        }
    }

    @Before
    public void setUp() throws Exception {
        if (isCi()) {
            driver = new ChromeDriver(new ChromeOptions().addArguments("--headless", "--disable-dev-shm-usage", "--no-sandbox"));
        } else {
            driver = new ChromeDriver(new ChromeOptions());
        }
        wait = new WebDriverWait(driver, MAX_WAIT);
        driver.manage().window().setSize(new Dimension(2560, 1440));
    }

    @After
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    protected static By radios(String paramName) {
        return By.cssSelector("div.active-choice:has([name='name'][value='" + paramName + "']) input[type='radio']");
    }

    protected List<WebElement> findRadios(String paramName) {
        return findRadios(paramName, null);
    }

    protected List<WebElement> findRadios(String paramName, Map<String, String> attributes) {
        return driver.findElements(radios(paramName));
    }

    protected static By checkboxes(String paramName) {
        return By.cssSelector("div.active-choice:has([name='name'][value='" + paramName + "']) input[type='checkbox']");
    }

    protected List<WebElement> findCheckboxes(String paramName) {
        return driver.findElements(checkboxes(paramName));
    }

    protected static By selects(String paramName) {
        return By.cssSelector("div.active-choice:has([name='name'][value='" + paramName + "']) > select");
    }

    protected WebElement findSelect(String paramName) {
        return driver.findElement(selects(paramName));
    }

    protected WebElement findParamDiv(String paramName) {
        final WebElement paramValueInput = driver.findElement(By.cssSelector("input[name='parameter.name'][value='" + paramName + "']"));
        // Up to how many parent levels to we want to search for the help button?
        // At the moment it's 3 levels up, so let's give it some room, use 7.
        final int parentsLimit = 7;
        WebElement parentElement = paramValueInput.findElement(By.xpath("./.."));
        for (int i = 0; i < parentsLimit; i++) {
            if (parentElement.getAttribute("name") != null && parentElement.getAttribute("name").equals("parameterDefinitions")) {
                return parentElement;
            }
            parentElement = parentElement.findElement(By.xpath("./.."));
        }
        throw new ElementNotFoundException("div", "parameterDefinitions", "");
    }

    protected void checkOptions(Supplier<WebElement> param1Input, String... options) {
        wait.withMessage(() -> {
                    List<WebElement> optionElements = param1Input.get().findElements(By.cssSelector("option"));
                    List<String> optionValues = optionElements.stream().map(WebElement::getText).collect(Collectors.toList());
                    return MessageFormat.format("{0} should have had {1}. Had {2}", param1Input, Arrays.asList(options), optionValues);
                })
                .until(d -> {
                    try {
                        List<WebElement> optionElements = param1Input.get().findElements(By.cssSelector("option"));
                        List<String> optionValues = optionElements.stream().map(WebElement::getText).collect(Collectors.toList());
                        return optionValues.equals(Arrays.asList(options));
                    } catch (StaleElementReferenceException e) {
                        return false;
                    }
                });
    }

    /**
     * This function receives a {@code By} selector to avoid stale elements - it will repeatedly
     * query the driver for a new element.
     *
     * @param selector selector
     * @param options expected options
     */
    protected void checkRadios(By selector, String... options) {
        wait.withMessage(() -> {
                    final List<WebElement> radios = driver.findElements(selector);
                    List<String> optionValues = radios.stream().map(it -> it.getAttribute("value")).collect(Collectors.toList());
                    return MessageFormat.format("{0} should have had {1}. Had {2}", radios, Arrays.asList(options), optionValues);
                })
                .until(d -> {
                    try {
                        final List<WebElement> radios = driver.findElements(selector);
                        List<String> optionValues = radios.stream().map(it -> it.getAttribute("value")).collect(Collectors.toList());
                        return optionValues.equals(Arrays.asList(options));
                    } catch (StaleElementReferenceException e) {
                        return false;
                    }
                });
    }

    protected void checkRadios(Supplier<List<WebElement>> radios, String... options) {
        wait.withMessage(() -> {
                    List<String> optionValues = radios.get().stream().map(it -> it.getAttribute("value")).collect(Collectors.toList());
                    return MessageFormat.format("{0} should have had {1}. Had {2}", radios, Arrays.asList(options), optionValues);
                })
                .until(d -> {
                    try {
                        List<String> optionValues = radios.get().stream().map(it -> it.getAttribute("value")).collect(Collectors.toList());
                        return optionValues.equals(Arrays.asList(options));
                    } catch (StaleElementReferenceException e) {
                        return false;
                    }
                });
    }
}
