/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2026 Ioannis Moutsatsos, Bruno P. Kinoshita
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

import static org.junit.jupiter.api.Assertions.*;

import hudson.model.Descriptor;
import hudson.model.Failure;
import hudson.model.FreeStyleProject;
import hudson.model.JobProperty;
import hudson.model.JobPropertyDescriptor;
import hudson.model.ParameterDefinition;
import hudson.model.ParametersDefinitionProperty;
import hudson.model.StringParameterValue;
import hudson.XmlFile;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.biouno.unochoice.model.GroovyScript;
import org.jenkinsci.plugins.scriptsecurity.sandbox.groovy.SecureGroovyScript;
import org.jenkinsci.plugins.scriptsecurity.scripts.ScriptApproval;
import org.jenkinsci.plugins.scriptsecurity.scripts.languages.GroovyLanguage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;
import org.kohsuke.stapler.StaplerRequest2;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map.Entry;

/**
 * Tests for the {@code required} option on Active Choices parameters.
 *
 * <p>Covers server-side enforcement in both {@code createValue(String)} and
 * {@code createValue(StaplerRequest2, JSONObject)}, and verifies that the flag
 * survives a save/reload round-trip.</p>
 */
@WithJenkins
class TestRequiredParameter {

    private static final String SCRIPT = "return ['a', 'b', 'c']";
    private static final String FALLBACK_SCRIPT = "return ['EMPTY!']";

    private JenkinsRule j;

    @BeforeEach
    void setUp(JenkinsRule j) {
        this.j = j;
        ScriptApproval.get().preapprove(SCRIPT, GroovyLanguage.get());
        ScriptApproval.get().preapprove(FALLBACK_SCRIPT, GroovyLanguage.get());
    }

    // --- createValue(String) tests

    @Test
    void testCreateValueStringThrowsFailureWhenRequiredAndBlank() throws Descriptor.FormException {
        ChoiceParameter param = buildRequiredCheckboxParam("required-blank-string");
        assertThrows(Failure.class, () -> param.createValue(""));
    }

    @Test
    void testCreateValueStringThrowsFailureWhenRequiredAndWhitespace() throws Descriptor.FormException {
        ChoiceParameter param = buildRequiredCheckboxParam("required-ws-string");
        assertThrows(Failure.class, () -> param.createValue("   "));
    }

    @Test
    void testCreateValueStringPassesWhenRequiredAndNonBlank() throws Descriptor.FormException {
        ChoiceParameter param = buildRequiredCheckboxParam("required-nonblank-string");
        var value = param.createValue("a");
        assertEquals("a", value.getValue().toString());
    }

    @Test
    void testCreateValueStringPassesWhenNotRequiredAndBlank() throws Descriptor.FormException {
        ChoiceParameter param = buildOptionalCheckboxParam("optional-blank-string");
        var value = param.createValue("");
        assertEquals("", value.getValue().toString());
    }

    // --- createValue(StaplerRequest2, JSONObject) tests — null / missing value

    @Test
    void testCreateValueJsonThrowsFailureWhenRequiredAndMissingValue() throws Descriptor.FormException {
        ChoiceParameter param = buildRequiredCheckboxParam("required-missing-json");
        JSONObject json = new JSONObject();
        json.put("name", "db");
        // "value" key absent → treated as null → empty string
        StaplerRequest2 request = Mockito.mock(StaplerRequest2.class);
        assertThrows(Failure.class, () -> param.createValue(request, json));
    }

    @Test
    void testCreateValueJsonThrowsFailureWhenRequiredAndEmptyArray() throws Descriptor.FormException {
        ChoiceParameter param = buildRequiredCheckboxParam("required-empty-array-json");
        JSONObject json = new JSONObject();
        json.put("name", "db");
        json.put("value", new JSONArray());
        StaplerRequest2 request = Mockito.mock(StaplerRequest2.class);
        assertThrows(Failure.class, () -> param.createValue(request, json));
    }

    @Test
    void testCreateValueJsonPassesWhenRequiredAndNonBlankValue() throws Descriptor.FormException {
        ChoiceParameter param = buildRequiredCheckboxParam("required-nonblank-json");
        JSONObject json = new JSONObject();
        json.put("name", "db");
        json.put("value", "host1.example.com");

        StringParameterValue spv = new StringParameterValue("db", "host1.example.com");
        StaplerRequest2 request = Mockito.mock(StaplerRequest2.class);
        Mockito.when(request.bindJSON(Mockito.eq(StringParameterValue.class), Mockito.any(JSONObject.class)))
                .thenReturn(spv);

        var value = param.createValue(request, json);
        assertEquals("host1.example.com", value.getValue().toString());
    }

    @Test
    void testCreateValueJsonPassesWhenNotRequiredAndMissingValue() throws Descriptor.FormException {
        ChoiceParameter param = buildOptionalCheckboxParam("optional-missing-json");
        JSONObject json = new JSONObject();
        json.put("name", "db");

        StringParameterValue spv = new StringParameterValue("db", "");
        StaplerRequest2 request = Mockito.mock(StaplerRequest2.class);
        Mockito.when(request.bindJSON(Mockito.eq(StringParameterValue.class), Mockito.any(JSONObject.class)))
                .thenReturn(spv);

        var value = param.createValue(request, json);
        assertEquals("", value.getValue().toString());
    }

    // --- CascadeChoiceParameter (reactive) variant

    @Test
    void testCascadeRequiredThrowsFailureWhenBlank() throws Descriptor.FormException {
        GroovyScript script = groovyScript();
        CascadeChoiceParameter param = new CascadeChoiceParameter(
                "cascade-required", "desc", "random-cascade",
                script, AbstractUnoChoiceParameter.PARAMETER_TYPE_CHECK_BOX,
                "", true, 1);
        param.setRequired(true);
        assertThrows(Failure.class, () -> param.createValue(""));
    }

    // --- Config round-trip

    @Test
    void testRequiredFlagSurvivesConfigRoundTrip() throws Exception {
        FreeStyleProject project = j.createFreeStyleProject();
        ChoiceParameter param = buildRequiredCheckboxParam("round-trip-param");
        project.addProperty(new ParametersDefinitionProperty(List.of(param)));
        project.save();

        XmlFile configXml = project.getConfigFile();
        FreeStyleProject reRead = (FreeStyleProject) configXml.read();

        boolean foundAndVerified = false;
        for (Entry<JobPropertyDescriptor, JobProperty<? super FreeStyleProject>> entry : reRead.getProperties().entrySet()) {
            if (entry.getValue() instanceof ParametersDefinitionProperty paramDef) {
                for (ParameterDefinition pd : paramDef.getParameterDefinitions()) {
                    if (pd instanceof AbstractUnoChoiceParameter up && "round-trip-param".equals(up.getName())) {
                        assertTrue(up.getRequired(), "required flag should survive save/reload");
                        foundAndVerified = true;
                    }
                }
            }
        }
        assertTrue(foundAndVerified, "Required parameter was not found after config round-trip");
    }

    @Test
    void testRequiredDefaultIsFalse() throws Descriptor.FormException {
        ChoiceParameter param = buildOptionalCheckboxParam("default-required");
        assertFalse(param.getRequired(), "required should default to false");
    }

    // --- Helpers

    private GroovyScript groovyScript() throws Descriptor.FormException {
        return new GroovyScript(
                new SecureGroovyScript(SCRIPT, Boolean.FALSE, null),
                new SecureGroovyScript(FALLBACK_SCRIPT, Boolean.FALSE, null));
    }

    private ChoiceParameter buildRequiredCheckboxParam(String name) throws Descriptor.FormException {
        ChoiceParameter param = new ChoiceParameter(name, "description", "random-" + name, groovyScript(),
                AbstractUnoChoiceParameter.PARAMETER_TYPE_CHECK_BOX, false, 1);
        param.setRequired(true);
        return param;
    }

    private ChoiceParameter buildOptionalCheckboxParam(String name) throws Descriptor.FormException {
        return new ChoiceParameter(name, "description", "random-" + name, groovyScript(),
                AbstractUnoChoiceParameter.PARAMETER_TYPE_CHECK_BOX, false, 1);
    }
}
