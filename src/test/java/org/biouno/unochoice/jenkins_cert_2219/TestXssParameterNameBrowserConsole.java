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
package org.biouno.unochoice.jenkins_cert_2219;

import org.htmlunit.CollectingAlertHandler;
import hudson.model.FreeStyleProject;
import hudson.model.ParametersDefinitionProperty;
import org.biouno.unochoice.CascadeChoiceParameter;
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

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Prevent a case where a parameter name is logged to the browser console,
 * causing an XSS security issue.
 *
 * <p>Tests the three parameters, {@code ChoiceParameter}, {@code CascadeChoiceParameter},
 * and {@code DynamicReferenceParameter}.</p>
 *
 * @since 2.6.0
 * @see hudson.Util#escape(String)
 */
@Issue("2219")
public class TestXssParameterNameBrowserConsole {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    /**
     * Tests that a {@code ChoiceParameter} reference parameter name is sanitized against XSS.
     * @throws IOException if it fails to load the script
     * @throws SAXException if the XML is malformed
     */
    @Test
    public void testChoiceParameterXss() throws IOException, SAXException {
        final FreeStyleProject project = j.createFreeStyleProject();
        final String scriptText = "return ['1']";
        final SecureGroovyScript secureScript = new SecureGroovyScript(scriptText, true, null);
        final GroovyScript script = new GroovyScript(secureScript, secureScript);

        ChoiceParameter parameter = new ChoiceParameter(
                "]');alert(\"XSS\");console.log('[",
                "]');alert(\"XSS\");console.log('[",
                "random-name", // using ]');alert("XSS");console.log('[ as random-name breaks the JS code, with no XSS
                script,
                ChoiceParameter.PARAMETER_TYPE_RADIO,
                false,
                1);
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
     * Tests that a {@code CascadeChoiceParameter} reference parameter name is sanitized against XSS.
     * @throws IOException if it fails to load the script
     * @throws SAXException if the XML is malformed
     */
    @Test
    public void testCascadeChoiceParameterXss() throws IOException, SAXException {
        final FreeStyleProject project = j.createFreeStyleProject();
        final String scriptText = "return ['1']";
        final SecureGroovyScript secureScript = new SecureGroovyScript(scriptText, true, null);
        final GroovyScript script = new GroovyScript(secureScript, secureScript);

        CascadeChoiceParameter parameter = new CascadeChoiceParameter(
                "]');alert(\"XSS\");console.log('[",
                "]');alert(\"XSS\");console.log('[",
                "random-name", // using ]');alert("XSS");console.log('[ as random-name breaks the JS code, with no XSS
                script,
                ChoiceParameter.PARAMETER_TYPE_RADIO,
                "]');alert(\"XSS\");console.log('[",
                false,
                1);
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
     * Tests that a {@code DynamicReferenceParameter} reference parameter name is sanitized against XSS.
     * @throws IOException if it fails to load the script
     * @throws SAXException if the XML is malformed
     */
    @Test
    public void testDynamicReferenceParameterXss() throws IOException, SAXException {
        final FreeStyleProject project = j.createFreeStyleProject();
        final String scriptText = "return ['1']";
        final SecureGroovyScript secureScript = new SecureGroovyScript(scriptText, true, null);
        final GroovyScript script = new GroovyScript(secureScript, secureScript);

        DynamicReferenceParameter parameter = new DynamicReferenceParameter(
                "]');alert(\"XSS\");console.log('[",
                "]');alert(\"XSS\");console.log('[",
                "random-name", // using ]');alert("XSS");console.log('[ as random-name breaks the JS code, with no XSS
                script,
                ChoiceParameter.ELEMENT_TYPE_ORDERED_LIST,
                "]');alert(\"XSS\");console.log('[",
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
}
