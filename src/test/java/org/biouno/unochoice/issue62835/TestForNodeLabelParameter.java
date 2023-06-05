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

package org.biouno.unochoice.issue62835;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.biouno.unochoice.CascadeChoiceParameter;
import org.biouno.unochoice.model.GroovyScript;
import org.jenkinsci.plugins.scriptsecurity.sandbox.groovy.SecureGroovyScript;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.Issue;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.JenkinsRule.WebClient;
import org.jvnet.jenkins.plugins.nodelabelparameter.Constants;
import org.jvnet.jenkins.plugins.nodelabelparameter.NodeParameterDefinition;
import org.jvnet.jenkins.plugins.nodelabelparameter.node.AllNodeEligibility;
import org.xml.sax.SAXException;

import org.htmlunit.html.DomElement;
import org.htmlunit.html.DomNode;
import org.htmlunit.html.HtmlOption;
import org.htmlunit.html.HtmlPage;
import org.htmlunit.html.HtmlSelect;
import com.google.common.collect.Lists;

import hudson.model.FreeStyleProject;
import hudson.model.ParameterDefinition;
import hudson.model.ParametersDefinitionProperty;
import hudson.model.labels.LabelAtom;
import hudson.slaves.DumbSlave;

/**
 * Test for parameters created by the NodeLabel Plug-in.
 *
 * @see <a href="https://github.com/jenkinsci/nodelabelparameter-plugin/blob/ea0a822f3a09423eb32eee9d5496ce7a14b4a931/src/test/java/org/jvnet/jenkins/plugins/nodelabelparameter/TriggerJobsTest.java">https://github.com/jenkinsci/nodelabelparameter-plugin/blob/ea0a822f3a09423eb32eee9d5496ce7a14b4a931/src/test/java/org/jvnet/jenkins/plugins/nodelabelparameter/TriggerJobsTest.java</a>
 */
@Issue("62835")
public class TestForNodeLabelParameter {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    private DumbSlave onlineNode;

    @Before
    public void setUp() throws Exception {
        onlineNode = j.createOnlineSlave(new LabelAtom("mylabel1"));
    }

    @After
    public void tearDown() throws Exception {
        j.jenkins.removeNode(onlineNode);
    }

    @Test
    public void testNodeLabelParameterValueFound() throws IOException, SAXException {
        FreeStyleProject project = j.createFreeStyleProject();

        final String nodeName = onlineNode.getNodeName();
        /*
         * Create two parameters. One using the nodelabelparameter-plugin, and the other a
         * parameter from this plug-in. 
         */
        NodeParameterDefinition nodeLabelParameter = new NodeParameterDefinition(
                "NODE_LABEL_PARAM_A", // name
                "NODE-LABEL", // description
                Collections.singletonList(nodeName), // defaultSlaves
                Lists.newArrayList(Constants.ALL_NODES), // allowedSlaves
                Constants.ALL_CASES, // triggerIfResult
                new AllNodeEligibility() // nodeEligibility
                );

        String scriptText = "return [NODE_LABEL_PARAM_A]";
        SecureGroovyScript secureScript = new SecureGroovyScript(scriptText, false, null);

        GroovyScript script = new GroovyScript(secureScript, secureScript);
        CascadeChoiceParameter reactsToNodeLabelParameter = new CascadeChoiceParameter(
                "PARAM_B",
                "Reacts to a NodeLabel Plug-in parameter",
                "random-name",
                script,
                CascadeChoiceParameter.PARAMETER_TYPE_SINGLE_SELECT,
                "NODE_LABEL_PARAM_A",
                true,
                1);

        project.addProperty(new ParametersDefinitionProperty(Arrays.<ParameterDefinition>asList(nodeLabelParameter, reactsToNodeLabelParameter)));
        project.save();

        WebClient wc = j.createWebClient();
        wc.setThrowExceptionOnFailingStatusCode(false);
        HtmlPage configPage = wc.goTo("job/" + project.getName() + "/build?delay=0sec");
        DomElement renderedParameterElement = configPage.getElementById("random-name");
        HtmlSelect select = null;
        for (DomNode node: renderedParameterElement.getChildren()) {
            if (node instanceof HtmlSelect) {
                select = (HtmlSelect) node;
                break;
            }
        }
        if (select == null) {
            fail("Missing cascade parameter select HTML node element!");
        }
        List<HtmlOption> htmlOptions = select.getOptions();
        final List<String> options = htmlOptions
                .stream()
                .map(HtmlOption::getText)
                .collect(Collectors.toList());
        final List<String> expected = new LinkedList<>(Collections.singletonList(nodeName));
        assertEquals("Wrong number of HTML options rendered", expected.size(), options.size());
        assertEquals("Wrong HTML options rendered (or out of order)", expected, options);
    }
}
