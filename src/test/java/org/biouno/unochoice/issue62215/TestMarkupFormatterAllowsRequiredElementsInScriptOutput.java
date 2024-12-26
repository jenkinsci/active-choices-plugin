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
package org.biouno.unochoice.issue62215;

import static org.junit.Assert.assertEquals;

import hudson.model.Descriptor;
import org.biouno.unochoice.model.GroovyScript;
import org.jenkinsci.plugins.scriptsecurity.sandbox.groovy.SecureGroovyScript;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.Issue;
import org.jvnet.hudson.test.JenkinsRule;

@Issue("62215")
public class TestMarkupFormatterAllowsRequiredElementsInScriptOutput {

    @Rule
    public JenkinsRule j = new JenkinsRule();


    @Test
    public void testInputIsNotRemovedFromGroovySandboxOutput() throws Descriptor.FormException {
        String markup = "<input type=\"text\" name=\"value\" value=\"value\" />";
        GroovyScript script = new GroovyScript(new SecureGroovyScript("return '" + markup + "'", true, null), null);
        String result = (String) script.eval();

        assertEquals(markup, result);
    }

    @Test
    public void testTextareaIsNotRemovedFromGroovySandboxOutput() throws Descriptor.FormException {
        String markup = "<textarea name=\"value\" placeholder=\"test\"></textarea>";
        GroovyScript script = new GroovyScript(new SecureGroovyScript("return '" + markup + "'", true, null), null);
        String result = (String) script.eval();

        assertEquals(markup, result);
    }

    @Test
    public void testSelectIsNotRemovedFromGroovySandboxOutput() throws Descriptor.FormException {
        String markup = "<select id=\"cars\"><option value=\"volvo\">Volvo</option><option value=\"saab\">Saab</option><option value=\"opel\">Opel</option><option value=\"audi\">Audi</option></select>";
        GroovyScript script = new GroovyScript(new SecureGroovyScript("return '" + markup + "'", true, null), null);
        String result = (String) script.eval();

        assertEquals(markup, result);
    }
}
