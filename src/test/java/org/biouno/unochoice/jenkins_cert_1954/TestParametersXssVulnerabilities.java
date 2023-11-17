/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2021 Ioannis Moutsatsos, Bruno P. Kinoshita
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
package org.biouno.unochoice.jenkins_cert_1954;

import org.apache.commons.io.IOUtils;
import org.htmlunit.html.*;
import hudson.model.FreeStyleProject;
import hudson.model.ParametersDefinitionProperty;
import hudson.util.VersionNumber;
import jenkins.model.Jenkins;
import org.biouno.unochoice.ChoiceParameter;
import org.biouno.unochoice.model.GroovyScript;
import org.jenkinsci.plugins.scriptsecurity.sandbox.groovy.SecureGroovyScript;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.JenkinsRule.WebClient;
import org.xml.sax.SAXException;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Tests against XSS. See SECURITY-1954, and SECURITY-2008.
 * @since 2.5
 */
public class TestParametersXssVulnerabilities {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    /**
     * Tests that a {@code ChoiceParameter} using a Groovy script has its output value sanitized against XSS when
     * returning a List.
     * @throws IOException if it fails to load the script
     * @throws SAXException if the XML is malformed
     */
    @Test
    public void testChoicesParameterXss() throws IOException, SAXException {
        String xssString = "<img src=x onerror=alert(123)>";
        FreeStyleProject project = j.createFreeStyleProject();
        String scriptText = String.format("return '%s'", xssString);
        SecureGroovyScript secureScript = new SecureGroovyScript(scriptText, true, null);
        GroovyScript script = new GroovyScript(secureScript, secureScript);
        ChoiceParameter parameter = new ChoiceParameter(
                xssString,
                xssString,
                "random-name",
                script,
                ChoiceParameter.PARAMETER_TYPE_CHECK_BOX,
                false,
                0);
        project.addProperty(new ParametersDefinitionProperty(parameter));
        project.save();


        try (WebClient wc = j.createWebClient()) {
            wc.setThrowExceptionOnFailingStatusCode(false);
            HtmlPage configPage = wc.goTo("job/" + project.getName() + "/build?delay=0sec");
            DomNodeList<DomElement> nodes = configPage.getElementsByTagName("div");
            DomElement renderedParameterElement = null;
            for (DomElement elem : nodes) {
                if (elem.getAttribute("class").contains("setting-main")) {
                    renderedParameterElement = elem;
                    break;
                }
            }
            assertNotNull("Could not locate rendered parameter element", renderedParameterElement);

            DomNode firstChild = null;
            for (DomNode child : renderedParameterElement.getChildren()) {
                // Spinner element must be ignored
                firstChild = child;
                if (child instanceof HtmlDivision && ((HtmlDivision) child).getAttribute("id").endsWith("-spinner")) {
                    continue;
                }
                break;
            }
            assertNotNull("Could not locate first child element", firstChild);
            String renderedText = firstChild.asXml();
            assertNotEquals("XSS string was not escaped!", xssString, renderedText);
            assertTrue("XSS string was not escaped!", renderedText.trim().contains("&amp;lt;img src=x onerror=alert(123)&amp;gt;"));
        }
    }
}
