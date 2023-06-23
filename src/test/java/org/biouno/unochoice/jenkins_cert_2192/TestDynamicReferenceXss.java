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
package org.biouno.unochoice.jenkins_cert_2192;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;

import org.biouno.unochoice.AbstractCascadableParameter;
import org.biouno.unochoice.ChoiceParameter;
import org.biouno.unochoice.DynamicReferenceParameter;
import org.biouno.unochoice.model.GroovyScript;
import org.jenkinsci.plugins.scriptsecurity.sandbox.groovy.SecureGroovyScript;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.Issue;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.JenkinsRule.WebClient;
import org.xml.sax.SAXException;

import org.htmlunit.CollectingAlertHandler;

import hudson.model.FreeStyleProject;
import hudson.model.ParametersDefinitionProperty;

/**
 * Prevent a case where a DynamicReference parameter with a referenced
 * parameter name equal to {@code "+alert(123)+"} results in a possible
 * XSS case.
 *
 * @since 2.5.2
 * @see hudson.Util#escape(String)
 */
@Issue("2192")
public class TestDynamicReferenceXss {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    /**
     * Tests that a {@code DynamicReference} reference parameter name is sanitized against XSS.
     * @throws IOException if it fails to load the script
     * @throws SAXException if the XML is malformed
     */
    @Test
    public void testChoicesParameterXss() throws IOException, SAXException {
        FreeStyleProject project = j.createFreeStyleProject();
        String scriptText = "return ['OK']";
        SecureGroovyScript secureScript = new SecureGroovyScript(scriptText, true, null);
        GroovyScript script = new GroovyScript(secureScript, secureScript);

        final String xssString = "\"+alert(123)+\"";

        DynamicReferenceParameter parameter = new DynamicReferenceParameter(
                "PARAMETER_NAME",
                "Description",
                "random-name",
                script,
                ChoiceParameter.PARAMETER_TYPE_RADIO,
                xssString,
                false);
        project.addProperty(new ParametersDefinitionProperty(parameter));
        project.save();

        final CollectingAlertHandler alertHandler = new CollectingAlertHandler();
        final WebClient wc = j.createWebClient();
        wc.setThrowExceptionOnFailingStatusCode(false);
        wc.setAlertHandler(alertHandler);

        wc.goTo("job/" + project.getName() + "/build?delay=0sec");
        final List<String> alerts = alertHandler.getCollectedAlerts();

        assertEquals("You got a JS alert, look out for XSS!", 0, alerts.size());
    }

    /**
     * Test that the reference parameter value is escaped.
     */
    @Test
    public void testGetReferencedParametersAsArray() {
        String scriptText = "return ['OK']";
        SecureGroovyScript secureScript = new SecureGroovyScript(scriptText, true, null);
        GroovyScript script = new GroovyScript(secureScript, secureScript);
        final String xssString = "\"+alert(123)+\"";
        AbstractCascadableParameter parameter = new DynamicReferenceParameter(
                "PARAMETER_NAME",
                "Description",
                "random-name",
                script,
                ChoiceParameter.PARAMETER_TYPE_RADIO,
                xssString,
                false);

        String[] parameters = parameter.getReferencedParametersAsArray();
        String[] expected = new String[] {"&quot;+alert(123)+&quot;"};
        assertArrayEquals("Your reference parameter name was not escaped, look out for XSS!", expected, parameters);
    }
}
