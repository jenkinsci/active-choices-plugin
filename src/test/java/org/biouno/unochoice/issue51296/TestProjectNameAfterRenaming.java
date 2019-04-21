/**
 * Tests for projectName being correct after renaming project. See JENKINS-51296.
 *
 * @since 2.2
 */
package org.biouno.unochoice.issue51296;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Map;

import org.biouno.unochoice.CascadeChoiceParameter;
import org.biouno.unochoice.ChoiceParameter;
import org.biouno.unochoice.model.GroovyScript;
import org.jenkinsci.plugins.scriptsecurity.sandbox.groovy.SecureGroovyScript;
import org.jenkinsci.plugins.scriptsecurity.scripts.ScriptApproval;
import org.jenkinsci.plugins.scriptsecurity.scripts.languages.GroovyLanguage;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.Issue;
import org.jvnet.hudson.test.JenkinsRule;

import org.powermock.api.mockito.PowerMockito;
import hudson.model.FreeStyleProject;
import hudson.model.ParametersDefinitionProperty;

import org.kohsuke.stapler.Ancestor;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;
import org.junit.runner.RunWith;
import hudson.model.AbstractItem;

@Issue("JENKINS-51296")
@RunWith(PowerMockRunner.class)
@PrepareForTest({StaplerRequest.class, Stapler.class})
@PowerMockIgnore({"javax.crypto.*" })
public class TestProjectNameAfterRenaming {
    @Rule
    public JenkinsRule j = new JenkinsRule();

    // LIST script
    private final String SCRIPT_LIST = "return ['Test', jenkinsProject.getName()]";
    private final String FALLBACK_SCRIPT_LIST = "return ['EMPTY!']";

    private final String PARAMETER_NAME = "my-parameter-name";

    private final String PROJECT_NAME_BEFORE = "MyOldJenkinsJob";
    private final String PROJECT_NAME_AFTER = "MyRealJenkinsJob";

    @Before
    public void setUp() throws Exception {
        ScriptApproval.get()
                .preapprove(SCRIPT_LIST, GroovyLanguage.get());
        ScriptApproval.get()
                .preapprove(FALLBACK_SCRIPT_LIST, GroovyLanguage.get());
    }

    @Test
    public void testProjectAreDifferent() throws IOException {

        FreeStyleProject project = j.createProject(FreeStyleProject.class, PROJECT_NAME_BEFORE);

        GroovyScript listScript = new GroovyScript(new SecureGroovyScript(SCRIPT_LIST, Boolean.FALSE, null),
                                                    new SecureGroovyScript(FALLBACK_SCRIPT_LIST, Boolean.FALSE, null));

        PowerMockito.mockStatic(Stapler.class);

        StaplerRequest request = PowerMockito.mock(StaplerRequest.class);
        Ancestor ancestor = PowerMockito.mock(Ancestor.class);
        PowerMockito.when(Stapler.getCurrentRequest()).thenReturn(request);
        PowerMockito.when(request.findAncestor(AbstractItem.class)).thenReturn(ancestor);
        PowerMockito.when(ancestor.getObject()).thenReturn(project);
        ChoiceParameter listParam = new ChoiceParameter(PARAMETER_NAME, "description...", "random-name", listScript, null,
                CascadeChoiceParameter.PARAMETER_TYPE_SINGLE_SELECT, false, 1);

        ParametersDefinitionProperty paramsDef = new ParametersDefinitionProperty(listParam);

        project.addProperty(paramsDef);
        
        Map<Object, Object> listSelectionValue = listParam.getChoices();

        String choicesStatus = listParam.getChoicesAsString() ;
        
        // keys and values have the same content when the parameter returns an array...
        assertTrue("Wrong project name from the begging ["+choicesStatus+"] test broken?!", listSelectionValue.containsKey(PROJECT_NAME_BEFORE));
        
        // --- After renaming, the real test ---
        project.renameTo(PROJECT_NAME_AFTER);
        Map<Object, Object> listSelectionValueAfter = listParam.getChoices();

        choicesStatus = listParam.getChoicesAsString() ;

        // Now, check full name!
        assertTrue("Wrong project name after renaming: "+choicesStatus, listSelectionValueAfter.containsKey(PROJECT_NAME_AFTER));
    }
}