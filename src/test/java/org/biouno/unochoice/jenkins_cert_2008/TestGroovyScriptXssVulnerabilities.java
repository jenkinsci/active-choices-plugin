/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2020 Ioannis Moutsatsos, Bruno P. Kinoshita
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
package org.biouno.unochoice.jenkins_cert_2008;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.io.IOException;
import java.util.List;

import hudson.model.Descriptor;
import org.htmlunit.html.HtmlElement;
import org.biouno.unochoice.ChoiceParameter;
import org.biouno.unochoice.DynamicReferenceParameter;
import org.biouno.unochoice.model.GroovyScript;
import org.jenkinsci.plugins.scriptsecurity.sandbox.groovy.SecureGroovyScript;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.JenkinsRule.WebClient;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;
import org.xml.sax.SAXException;

import org.htmlunit.html.DomElement;
import org.htmlunit.html.HtmlImage;
import org.htmlunit.html.HtmlInput;
import org.htmlunit.html.HtmlLabel;
import org.htmlunit.html.HtmlPage;

import hudson.model.FreeStyleProject;
import hudson.model.ParametersDefinitionProperty;

/**
 * Tests against XSS. See SECURITY-1954, and SECURITY-2008.
 * @since 2.5
 */
@WithJenkins
class TestGroovyScriptXssVulnerabilities {

    /**
     * Tests that a {@code ChoiceParameter} using a Groovy script has its output value sanitized against XSS when
     * returning a List.
     * @throws IOException if it fails to load the script
     * @throws SAXException if the XML is malformed
     */
    @Test
    void testChoicesParameterXss(JenkinsRule j) throws IOException, SAXException, Descriptor.FormException {
        String xssString = "<img src=x onerror=alert(123)>";
        FreeStyleProject project = j.createFreeStyleProject();
        String scriptText = String.format("return ['%s']", xssString);
        SecureGroovyScript secureScript = new SecureGroovyScript(scriptText, true, null);
        GroovyScript script = new GroovyScript(secureScript, secureScript);
        ChoiceParameter parameter = new ChoiceParameter(
                xssString,
                "Description",
                "random-name",
                script,
                ChoiceParameter.PARAMETER_TYPE_CHECK_BOX,
                false,
                0);
        project.addProperty(new ParametersDefinitionProperty(parameter));
        project.save();

        WebClient wc = j.createWebClient();
        wc.setThrowExceptionOnFailingStatusCode(false);
        HtmlPage configPage = wc.goTo("job/" + project.getName() + "/build?delay=0sec");
        DomElement renderedParameterElement = configPage.getElementById("ecp_random-name_0");
        HtmlLabel renderedParameterLabel = (HtmlLabel) renderedParameterElement.getElementsByTagName("label").get(0);
        String renderedText = renderedParameterLabel.getTextContent();
        assertNotEquals(xssString, renderedText, "XSS string was not escaped!");
        assertEquals("<img src=\"x\" />", renderedText, "XSS string was not escaped!");
    }

    /**
     * Tests that a {@code ChoiceParameter} using a Groovy script has its output value sanitized against XSS when
     * returning a Map.
     * @throws IOException if it fails to load the script
     * @throws SAXException if the XML is malformed
     */
    @Test
    void testChoicesParameterXssWithMaps(JenkinsRule j) throws IOException, SAXException, Descriptor.FormException {
        String xssString = "<img src=x onerror=alert(123)>";
        FreeStyleProject project = j.createFreeStyleProject();
        String scriptText = String.format("return ['%s': '%s']", xssString, xssString);
        SecureGroovyScript secureScript = new SecureGroovyScript(scriptText, true, null);
        GroovyScript script = new GroovyScript(secureScript, secureScript);
        ChoiceParameter parameter = new ChoiceParameter(
                xssString,
                "Description",
                "random-name",
                script,
                ChoiceParameter.PARAMETER_TYPE_RADIO,
                false,
                0);
        project.addProperty(new ParametersDefinitionProperty(parameter));
        project.save();

        WebClient wc = j.createWebClient();
        wc.setThrowExceptionOnFailingStatusCode(false);
        HtmlPage configPage = wc.goTo("job/" + project.getName() + "/build?delay=0sec");
        DomElement renderedParameterElement = configPage.getElementById("tbl_tr_ecp_random-name");
        HtmlInput renderedParameterInput = (HtmlInput) renderedParameterElement.getElementsByTagName("input").get(0);
        String renderedText = renderedParameterInput.getAttribute("value");
        assertNotEquals(xssString, renderedText, "XSS string was not escaped in map key!");
        assertEquals("<img src=\"x\" />", renderedText, "XSS string was not escaped in map key!");
        HtmlLabel renderedParameterLabel = (HtmlLabel) renderedParameterElement.getElementsByTagName("label").get(0);
        renderedText = renderedParameterLabel.getTextContent();
        assertNotEquals(xssString, renderedText, "XSS string was not escaped in map key!");
        assertEquals("<img src=\"x\" />", renderedText, "XSS string was not escaped in map key!");
    }

    /**
     * Tests that a {@code ChoiceParameter} using a Groovy script has its output value sanitized against XSS when
     * returning a String (rendered as HTML).
     * @throws IOException if it fails to load the script
     * @throws SAXException if the XML is malformed
     */
    @Test
    void testReferenceParameterXss(JenkinsRule j) throws IOException, SAXException, Descriptor.FormException {
        String xssString = "<img src=x onerror=alert(123)>";
        FreeStyleProject project = j.createFreeStyleProject();
        String scriptText = String.format("return '%s'", xssString);
        SecureGroovyScript secureScript = new SecureGroovyScript(scriptText, true, null);
        GroovyScript script = new GroovyScript(secureScript, secureScript);
        DynamicReferenceParameter parameter = new DynamicReferenceParameter(
                xssString,
                "Description",
                "random-name",
                script,
                DynamicReferenceParameter.ELEMENT_TYPE_FORMATTED_HTML,
                "",
                false);
        project.addProperty(new ParametersDefinitionProperty(parameter));
        project.save();

        WebClient wc = j.createWebClient();
        wc.setThrowExceptionOnFailingStatusCode(false);
        HtmlPage configPage = wc.goTo("job/" + project.getName() + "/build?delay=0sec");
        List<HtmlElement> renderedParameterElement = configPage.getByXPath("//*[@class='setting-main']");
        HtmlImage renderedParameterLabel = (HtmlImage) renderedParameterElement.get(0).getElementsByTagName("img").get(0);
        String renderedText = renderedParameterLabel.asXml();
        assertNotEquals(xssString, renderedText, "XSS string was not escaped!");
        assertEquals("<img src=\"x\"/>", renderedText.trim(), "XSS string was not escaped!");
    }

}
