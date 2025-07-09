/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2025 Ioannis Moutsatsos, Bruno P. Kinoshita
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
package org.biouno.unochoice.issue73239;

import org.biouno.unochoice.BaseUiTest;
import org.biouno.unochoice.util.PrintAllWebConsolerLogger;
import org.htmlunit.HttpMethod;
import org.htmlunit.WebClient;
import org.htmlunit.WebConsole;
import org.htmlunit.WebRequest;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.Issue;
import org.jvnet.hudson.test.recipes.LocalData;

import java.net.URL;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * In JENKINS-73239, the issue itself was discarded (loading JS files is blocked by Jenkins
 * security), but the example in the issue included a cascade parameter that was not referencing
 * anything.
 *
 * <p>Even though this looks harmless and is supposed to work, due to changes to fix other
 * bugs that case was broken but was not caught in the existing tests.</p>\
 *
 * <p>This is a test to prevent that from happening again.</p>
 */
@Issue("JENKINS-73239")
public class TestCascadeParameterIsEmpty extends BaseUiTest {

    /**
     * Test that a cascade parameter without a referenced parameter does not result in
     * JS console errors. These errors, normally, also prevent the plug-in from working
     * correctly, causing parameters to fail to be updated.
     *
     * <p>The console messages were captured using this example answer:
     * <a href="https://github.com/tntim96/htmlunit/blob/b0152aaa2f54344f6b4cfcb41bf22381c1bdc522/src/test/java/com/gargoylesoftware/htmlunit/javascript/host/ConsoleTest.java">https://github.com/tntim96/htmlunit/blob/master/src/test/java/com/gargoylesoftware/htmlunit/javascript/host/ConsoleTest.java</a>.</p>
     */
    @Test
    @LocalData("test")
    public void test() throws Exception {
        try (WebClient client = j.createWebClient().withThrowExceptionOnFailingStatusCode(false)) {
            final WebConsole console = client.getWebConsole();
            final PrintAllWebConsolerLogger logger = new PrintAllWebConsolerLogger();
            console.setLogger(logger);

            final WebRequest request = new WebRequest(new URL(j.getURL() + "job/test/build"), HttpMethod.GET);
            client.getPage(request);

            waitLoadingMessage();

            final List<String> messages = logger.getMessages();
            assertFalse(messages.isEmpty());

            final String needle = "referencedParameters is undefined";
            assertFalse(messages.stream().anyMatch(message -> message.contains(needle)));
        }
    }

}
