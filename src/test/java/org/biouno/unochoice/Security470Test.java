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

package org.biouno.unochoice;

import static org.junit.jupiter.api.Assertions.assertEquals;

import hudson.model.Job;
import jenkins.model.Jenkins;
import org.biouno.unochoice.model.GroovyScript;
import org.jenkinsci.plugins.scriptsecurity.sandbox.groovy.SecureGroovyScript;
import org.jenkinsci.plugins.scriptsecurity.scripts.ScriptApproval;
import org.jenkinsci.plugins.scriptsecurity.scripts.languages.GroovyLanguage;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.MockAuthorizationStrategy;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class Security470Test {

    @Test
    void testSanitization(JenkinsRule j) throws Exception {
        j.jenkins.setSecurityRealm(j.createDummySecurityRealm());
        MockAuthorizationStrategy strategy = new MockAuthorizationStrategy();
        strategy.grant(Jenkins.READ).onRoot().toEveryone();
        strategy.grant(Job.CREATE, Job.READ, Job.CONFIGURE).everywhere().toAuthenticated();
        j.jenkins.setAuthorizationStrategy(strategy);

        { // test HTML gets sanitized when run in sandbox
            GroovyScript script = new GroovyScript(new SecureGroovyScript("return '<img src=\\'fail\\' onerror=\\'alert(\"foo\");\\' /><b>text</b>'", true, null),
                    null);
            DynamicReferenceParameter param = new DynamicReferenceParameter("whatever", "description", "some-random-name",
                    script, CascadeChoiceParameter.ELEMENT_TYPE_FORMATTED_HTML, "", true);
            assertEquals("<img src=\"fail\" /><b>text</b>", param.getChoicesAsStringForUI());
        }

        { // test HTML does not get sanitized when run outside the sandbox
            String htmlScript = "return '<img src=\\'fail\\' onerror=\\'alert(\"foo\");\\' /><b>text</b>'";
            GroovyScript script = new GroovyScript(new SecureGroovyScript(htmlScript, false, null),
                    null);
            DynamicReferenceParameter param = new DynamicReferenceParameter("whatever", "description", "some-random-name",
                    script, CascadeChoiceParameter.ELEMENT_TYPE_FORMATTED_HTML, "", true);
            assertEquals("{}", param.getChoicesAsStringForUI()); // not yet approved

            ScriptApproval.get().preapprove(htmlScript, GroovyLanguage.get());

            assertEquals("<img src='fail' onerror='alert(\"foo\");' /><b>text</b>", param.getChoicesAsStringForUI()); // approved
        }
    }
}
