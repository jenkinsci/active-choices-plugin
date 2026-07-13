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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

@WithJenkins
public abstract class BaseUiTest {

    protected JenkinsRule j;

    protected WebDriver driver;
    protected WebDriverWait wait;
    private static String ciBrowserBinary;
    private static final int PLAYWRIGHT_BROWSER_SCAN_DEPTH = 8;

    protected static boolean isCi() {
        return StringUtils.isNotBlank(System.getenv("CI"));
    }

    protected static final Duration MAX_WAIT = Duration.parse(System.getProperty("ui.loading.timeout", "PT300S"));

    @BeforeAll
    public static void setUpClass() {
        if (isCi()) {
            ciBrowserBinary = findCiBrowserBinary();
            if (StringUtils.isNotBlank(ciBrowserBinary)) {
                WebDriverManager.chromedriver().browserBinary(ciBrowserBinary).setup();
            } else {
                WebDriverManager.chromedriver().setup();
            }
        } else {
            WebDriverManager.chromedriver().setup();
        }
    }

    @BeforeEach
    public void setUp(JenkinsRule j) {
        this.j = j;
        final ChromeOptions options = new ChromeOptions();
        if (isCi()) {
            options.addArguments("--headless", "--disable-dev-shm-usage", "--no-sandbox");
            if (StringUtils.isNotBlank(ciBrowserBinary)) {
                options.setBinary(ciBrowserBinary);
            }
        }
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, MAX_WAIT);
        driver.manage().window().setSize(new Dimension(2560, 1440));
    }

    private static String findCiBrowserBinary() {
        String configuredBinary = System.getProperty("ui.chrome.binary");
        if (StringUtils.isBlank(configuredBinary)) {
            configuredBinary = System.getenv("CHROME_BIN");
        }
        if (StringUtils.isNotBlank(configuredBinary) && Files.exists(Path.of(configuredBinary))) {
            return configuredBinary;
        }

        final List<Path> searchRoots = browserSearchRoots();
        for (Path searchRoot : searchRoots) {
            if (Files.isDirectory(searchRoot)) {
                final String detectedBinary = findChromeExecutable(searchRoot);
                if (StringUtils.isNotBlank(detectedBinary)) {
                    return detectedBinary;
                }
            }
        }

        System.out.println("No CI Chrome binary found. Checked " + searchRoots
                + ", user.home=" + System.getProperty("user.home")
                + ", HOME=" + System.getenv("HOME")
                + ", CHROME_BIN=" + System.getenv("CHROME_BIN"));
        return null;
    }

    private static List<Path> browserSearchRoots() {
        final Set<Path> searchRoots = new LinkedHashSet<>();
        addPlaywrightCache(searchRoots, System.getProperty("user.home"));
        addPlaywrightCache(searchRoots, System.getenv("HOME"));
        searchRoots.add(Path.of("/home/jenkins/.cache/ms-playwright"));
        return new ArrayList<>(searchRoots);
    }

    private static void addPlaywrightCache(Set<Path> searchRoots, String homeDirectory) {
        if (StringUtils.isNotBlank(homeDirectory)) {
            searchRoots.add(Path.of(homeDirectory, ".cache", "ms-playwright"));
        }
    }

    private static String findChromeExecutable(Path searchRoot) {
        try (Stream<Path> paths = Files.walk(searchRoot, PLAYWRIGHT_BROWSER_SCAN_DEPTH)) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(BaseUiTest::isChromeExecutable)
                    .sorted(Comparator.comparing(Path::toString).reversed())
                    .map(Path::toString)
                    .findFirst()
                    .orElse(null);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to scan browser cache " + searchRoot, e);
        }
    }

    private static boolean isChromeExecutable(Path path) {
        final String fileName = path.getFileName().toString();
        return "headless_shell".equals(fileName)
                || "chrome".equals(fileName)
                || "chromium".equals(fileName)
                || "chromium-browser".equals(fileName);
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    protected static By radios(String paramName) {
        return By.cssSelector("div.active-choice:has([name='name'][value='" + paramName + "']) input[type='radio']");
    }

    protected List<WebElement> findRadios(String paramName) {
        return driver.findElements(radios(paramName));
    }

    protected WebElement getLabel(WebElement element) {
        return element.findElement(By.xpath("following-sibling::*"));
    }

    protected void clickRadio(WebElement element) {
        getLabel(element).click();
    }

    protected static By checkboxes(String paramName) {
        return By.cssSelector("div.active-choice:has([name='name'][value='" + paramName + "']) input[type='checkbox']");
    }

    protected List<WebElement> findCheckboxes(String paramName) {
        return driver.findElements(checkboxes(paramName));
    }

    protected static By selects(String paramName) {
        return By.cssSelector("div.active-choice:has([name='name'][value='" + paramName + "']) > div > select");
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
            if (parentElement.getDomAttribute("name") != null && parentElement.getDomAttribute("name").equals("parameterDefinitions")) {
                return parentElement;
            }
            parentElement = parentElement.findElement(By.xpath("./.."));
        }
        throw new ElementNotFoundException("div", "parameterDefinitions", "");
    }

    protected void checkOptions(Supplier<WebElement> param1Input, String... options) {
        wait.withMessage(() -> {
                    List<WebElement> optionElements = param1Input.get().findElements(By.cssSelector("option"));
                    List<String> optionValues = optionElements.stream().map(WebElement::getText).toList();
                    return MessageFormat.format("{0} should have had {1}. Had {2}", param1Input, Arrays.asList(options), optionValues);
                })
                .until(d -> {
                    try {
                        List<WebElement> optionElements = param1Input.get().findElements(By.cssSelector("option"));
                        List<String> optionValues = optionElements.stream().map(WebElement::getText).toList();
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
                    List<String> optionValues = radios.stream().map(it -> it.getDomAttribute("value")).toList();
                    return MessageFormat.format("{0} should have had {1}. Had {2}", radios, Arrays.asList(options), optionValues);
                })
                .until(d -> {
                    try {
                        final List<WebElement> radios = driver.findElements(selector);
                        List<String> optionValues = radios.stream().map(it -> it.getDomAttribute("value")).toList();
                        return optionValues.equals(Arrays.asList(options));
                    } catch (StaleElementReferenceException e) {
                        return false;
                    }
                });
    }

    protected void checkRadios(Supplier<List<WebElement>> radios, String... options) {
        wait.withMessage(() -> {
                    List<String> optionValues = radios.get().stream().map(it -> it.getDomAttribute("value")).toList();
                    return MessageFormat.format("{0} should have had {1}. Had {2}", radios, Arrays.asList(options), optionValues);
                })
                .until(d -> {
                    try {
                        List<String> optionValues = radios.get().stream().map(it -> it.getDomAttribute("value")).toList();
                        return optionValues.equals(Arrays.asList(options));
                    } catch (StaleElementReferenceException e) {
                        return false;
                    }
                });
    }

    protected void waitLoadingMessage() {
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".jenkins-spinner")));
    }
}
