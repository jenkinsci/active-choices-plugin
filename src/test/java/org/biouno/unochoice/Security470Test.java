package org.biouno.unochoice;

import hudson.model.Job;
import jenkins.model.Jenkins;
import org.biouno.unochoice.model.GroovyScript;
import org.jenkinsci.plugins.scriptsecurity.sandbox.groovy.SecureGroovyScript;
import org.jenkinsci.plugins.scriptsecurity.scripts.ScriptApproval;
import org.jenkinsci.plugins.scriptsecurity.scripts.languages.GroovyLanguage;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.MockAuthorizationStrategy;

public class Security470Test {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Test
    public void testSanitization() throws Exception {
        j.jenkins.setSecurityRealm(j.createDummySecurityRealm());
        MockAuthorizationStrategy strategy = new MockAuthorizationStrategy();
        strategy.grant(Jenkins.READ).onRoot().toEveryone();
        strategy.grant(Job.CREATE, Job.READ, Job.CONFIGURE).everywhere().toAuthenticated();
        j.jenkins.setAuthorizationStrategy(strategy);

        { // test HTML gets sanitized when run in sandbox
            GroovyScript script = new GroovyScript(new SecureGroovyScript("return '<img src=\\'fail\\' onerror=\\'alert(\"foo\");\\' /><b>text</b>'", true, null),
                    null);
            DynamicReferenceParameter param = new DynamicReferenceParameter("whatever", "description", "some-random-name",
                    script, null, CascadeChoiceParameter.ELEMENT_TYPE_FORMATTED_HTML, "", true);
            Assert.assertEquals("<img src=\"fail\"><b>text</b>", param.getChoicesAsStringForUI());
        }

        { // test HTML does not get sanitized when run outside the sandbox
            String htmlScript = "return '<img src=\\'fail\\' onerror=\\'alert(\"foo\");\\' /><b>text</b>'";
            GroovyScript script = new GroovyScript(new SecureGroovyScript(htmlScript, false, null),
                    null);
            DynamicReferenceParameter param = new DynamicReferenceParameter("whatever", "description", "some-random-name",
                    script, null, CascadeChoiceParameter.ELEMENT_TYPE_FORMATTED_HTML, "", true);
            Assert.assertEquals("{}", param.getChoicesAsStringForUI()); // not yet approved

            ScriptApproval.get().preapprove(htmlScript, GroovyLanguage.get());

            Assert.assertEquals("<img src='fail' onerror='alert(\"foo\");' /><b>text</b>", param.getChoicesAsStringForUI()); // approved
        }
    }
}
