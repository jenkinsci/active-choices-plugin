package org.biouno.unochoice;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.recipes.LocalData;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.Select;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class UiAcceptanceTest {
    @Rule
    public JenkinsRule j = new JenkinsRule();

    private WebDriver driver;

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

    private static boolean isCi() {
        return StringUtils.isNotBlank(System.getenv("CI"));
    }

    @Before
    public void setUp() throws Exception {
        if (isCi()) {
            driver = new ChromeDriver(new ChromeOptions().addArguments("--headless", "--disable-dev-shm-usage", "--no-sandbox"));
        } else {
            driver = new ChromeDriver(new ChromeOptions());
        }
    }

    @After
    public void tearDown() {
         driver.quit();
    }

    @LocalData
    @Test
    public void test() throws Exception {
        // Load the page
        driver.get(j.getURL().toString() + "job/test/build");

        /*
         PARAM1 and PARAM1A are SINGLE_SELECT based
         */
        WebElement param1 = driver.findElement(By.cssSelector("div.active-choice:has([name='name'][value='PARAM1']) > select"));
        WebElement param1A = driver.findElement(By.cssSelector("div.active-choice:has([name='name'][value='PARAM1A']) > select"));

        assertTrue(param1.isDisplayed());
        assertTrue(param1.isEnabled());
        assertTrue(param1A.isDisplayed());
        assertTrue(param1A.isEnabled());

        checkOptions(param1, "A", "B", "C");

        assertEquals("A", new Select(param1).getFirstSelectedOption().getText());
        param1.sendKeys("A");

        checkOptions(param1A, "A1", "A2", "A3");

        new Select(param1).selectByValue("B");
        checkOptions(param1A, "B1", "B2", "B3");

        new Select(param1).selectByValue("C");
        checkOptions(param1A, "C1", "C2", "C3");

        new Select(param1).selectByValue("A");
        checkOptions(param1A, "A1", "A2", "A3");

        /*
         PARAM2 and PARAM2A are RADIO based
         */
        List<WebElement> param2Choices = driver.findElements(By.cssSelector("div.active-choice:has([name='name'][value='PARAM2']) input[type='radio']"));
        List<WebElement> param2AChoices = driver.findElements(By.cssSelector("div.active-choice:has([name='name'][value='PARAM2A']) input[type='radio']"));

        checkRadios(param2Choices, "A", "B", "C");
        // this is until something is selected
        checkRadios(param2AChoices, "1", "2", "3");

        param2Choices.get(0).click();
        param2AChoices = driver.findElements(By.cssSelector("div.active-choice:has([name='name'][value='PARAM2A']) input[type='radio']"));
        checkRadios(param2AChoices, "A1", "A2", "A3");

        param2Choices.get(1).click();
        param2AChoices = driver.findElements(By.cssSelector("div.active-choice:has([name='name'][value='PARAM2A']) input[type='radio']"));
        checkRadios(param2AChoices, "B1", "B2", "B3");

        param2Choices.get(2).click();
        param2AChoices = driver.findElements(By.cssSelector("div.active-choice:has([name='name'][value='PARAM2A']) input[type='radio']"));
        checkRadios(param2AChoices, "C1", "C2", "C3");

        param2Choices.get(0).click();
        param2AChoices = driver.findElements(By.cssSelector("div.active-choice:has([name='name'][value='PARAM2A']) input[type='radio']"));
        checkRadios(param2AChoices, "A1", "A2", "A3");

        /*
         PARAM3 and PARAM3A are CHECKBOX based
         */
        List<WebElement> param3Choices = driver.findElements(By.cssSelector("div.active-choice:has([name='name'][value='PARAM3']) input[type='checkbox']"));
        List<WebElement> param3AChoices = driver.findElements(By.cssSelector("div.active-choice:has([name='name'][value='PARAM3A']) input[type='checkbox']"));

        checkRadios(param3Choices, "A", "B", "C");
        // this is until something is selected
        checkRadios(param3AChoices);

        param3Choices.get(0).click();
        param3AChoices = driver.findElements(By.cssSelector("div.active-choice:has([name='name'][value='PARAM3A']) input[type='checkbox']"));
        checkRadios(param3AChoices, "A1", "A2", "A3");

        param3Choices.get(1).click();
        param3AChoices = driver.findElements(By.cssSelector("div.active-choice:has([name='name'][value='PARAM3A']) input[type='checkbox']"));
        checkRadios(param3AChoices, "A1", "B1", "A2", "B2", "A3", "B3");

        param3Choices.get(2).click();
        param3AChoices = driver.findElements(By.cssSelector("div.active-choice:has([name='name'][value='PARAM3A']) input[type='checkbox']"));
        checkRadios(param3AChoices, "A1", "B1", "C1", "A2", "B2", "C2", "A3", "B3", "C3");

        param3Choices.get(0).click();
        param3AChoices = driver.findElements(By.cssSelector("div.active-choice:has([name='name'][value='PARAM3A']) input[type='checkbox']"));
        checkRadios(param3AChoices, "B1", "C1", "B2", "C2", "B3", "C3");

        param3Choices.get(1).click();
        param3AChoices = driver.findElements(By.cssSelector("div.active-choice:has([name='name'][value='PARAM3A']) input[type='checkbox']"));
        checkRadios(param3AChoices, "C1", "C2", "C3");

        param3Choices.get(0).click();
        param3AChoices = driver.findElements(By.cssSelector("div.active-choice:has([name='name'][value='PARAM3A']) input[type='checkbox']"));
        checkRadios(param3AChoices, "A1", "C1", "A2", "C2", "A3", "C3");

        param3Choices.get(2).click();
        param3AChoices = driver.findElements(By.cssSelector("div.active-choice:has([name='name'][value='PARAM3A']) input[type='checkbox']"));
        checkRadios(param3AChoices, "A1", "A2", "A3");

        param3Choices.get(0).click();
        param3AChoices = driver.findElements(By.cssSelector("div.active-choice:has([name='name'][value='PARAM3A']) input[type='checkbox']"));
        checkRadios(param3AChoices);

        /*
         PARAM4 and PARAM4A are MULTI_SELECT based
         */
        WebElement param4Input = driver.findElement(By.cssSelector("div.active-choice:has([name='name'][value='PARAM4']) select"));
        WebElement param4AInput = driver.findElement(By.cssSelector("div.active-choice:has([name='name'][value='PARAM4A']) select"));

        checkOptions(param4Input, "A", "B", "C");
        // this is until something is selected
        checkOptions(param4AInput);

        new Select(param4Input).selectByVisibleText("A");
        checkOptions(param4AInput, "A1", "A2", "A3");

        new Select(param4Input).selectByVisibleText("B");
        checkOptions(param4AInput, "A1", "B1", "A2", "B2", "A3", "B3");

        new Select(param4Input).selectByVisibleText("C");
        checkOptions(param4AInput, "A1", "B1", "C1", "A2", "B2", "C2", "A3", "B3", "C3");

        new Select(param4Input).deselectByVisibleText("A");
        checkOptions(param4AInput, "B1", "C1", "B2", "C2", "B3", "C3");

        new Select(param4Input).deselectByVisibleText("B");
        checkOptions(param4AInput, "C1", "C2", "C3");

        new Select(param4Input).selectByValue("A");
        checkOptions(param4AInput, "A1", "C1", "A2", "C2", "A3", "C3");

        new Select(param4Input).deselectByValue("C");
        checkOptions(param4AInput, "A1", "A2", "A3");

        new Select(param4Input).deselectByValue("A");
        checkOptions(param4AInput);

    }

    private static void checkOptions(WebElement param1Input, String... options) {
        assertEquals(options.length, param1Input.findElements(By.cssSelector("option")).size());
        for (int i = 0; i < options.length; i++) {
            assertEquals(options[i], param1Input.findElements(By.cssSelector("option")).get(i).getAttribute("value"));
            assertEquals(options[i], param1Input.findElements(By.cssSelector("option")).get(i).getText());
        }
    }

    private static void checkRadios(List<WebElement> radios, String... options) {
        assertEquals(options.length, radios.size());
        for (int i = 0; i < options.length; i++) {
            assertTrue(radios.get(i).isDisplayed());
            assertTrue(radios.get(i).isEnabled());
            assertEquals(options[i], radios.get(i).getAttribute("value"));
        }
    }
}
