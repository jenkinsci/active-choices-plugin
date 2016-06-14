/*
 * The MIT License (MIT)
 *
 * Copyright (c) <2014-2015> <Ioannis Moutsatsos, Bruno P. Kinoshita>
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

import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.ParameterDefinition;
import hudson.util.FormValidation;

import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.biouno.unochoice.model.GroovyScript;
import org.biouno.unochoice.model.Script;
import org.kohsuke.stapler.Ancestor;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.bind.JavaScriptMethod;

/**
 * <p>Provides a <b>dynamic reference parameter</b> for users. This is a not so elegant
 * solution, since we are using a ParameterDefinition extension point, but it 
 * actually <b>doesn't provide any parameter value</b>.</p>
 *
 * <p>This kind of parameter is only for reference. An use case is when you have several
 * job parameters, but your input values may vary depending on previous executions. You 
 * can get the previous executions by accessing from your Groovy code the jenkinsProject
 * variable.</p>
 *
 * <p>Its options are retrieved from the evaluation of a Groovy script.</p>
 *
 * @author Bruno P. Kinoshita
 * @since 0.1
 */
public class DynamicReferenceParameter extends AbstractCascadableParameter {

    /*
     * Serial UID.
     */
    private static final long serialVersionUID = 3583160434198488019L;

    private static final String JENKINS_PROJECT_VARIABLE_NAME = "jenkinsProject";
    private static final String JENKINS_BUILD_VARIABLE_NAME = "jenkinsBuild";

    /**
     * Choice type.
     */
    private final String choiceType;

    private final Boolean omitValueField;

    /**
     * Constructor called from Jelly with parameters.
     *
     * @param name name
     * @param description description
     * @param script script
     * @param visibilityScript visibility script
     * @param choiceType choice type
     * @param referencedParameters referenced parameters
     * @param omitValueField used in the UI to decide whether to include a hidden empty &lt;input name=value&gt;.
     * <code>false</code> by default.
     * @deprecated see JENKINS-32149
     */
    public DynamicReferenceParameter(String name, String description, Script script, Script visibilityScript,
            String choiceType, String referencedParameters, Boolean omitValueField) {
        super(name, description, script, visibilityScript, referencedParameters);
        this.choiceType = StringUtils.defaultIfBlank(choiceType, PARAMETER_TYPE_SINGLE_SELECT);
        this.omitValueField = BooleanUtils.toBooleanDefaultIfNull(omitValueField, Boolean.FALSE);
    }

    /**
     * Constructor called from Jelly with parameters.
     *
     * @param name name
     * @param description description
     * @param randomName parameter random generated name (uuid)
     * @param script script
     * @param visibilityScript visibility script
     * @param choiceType choice type
     * @param referencedParameters referenced parameters
     * @param omitValueField used in the UI to decide whether to include a hidden empty &lt;input name=value&gt;.
     * <code>false</code> by default.
     */
    @DataBoundConstructor
    public DynamicReferenceParameter(String name, String description, String randomName, Script script, Script visibilityScript,
            String choiceType, String referencedParameters, Boolean omitValueField) {
        super(name, description, randomName, script, visibilityScript, referencedParameters);
        this.choiceType = StringUtils.defaultIfBlank(choiceType, PARAMETER_TYPE_SINGLE_SELECT);
        this.omitValueField = BooleanUtils.toBooleanDefaultIfNull(omitValueField, Boolean.FALSE);
    }

    /*
     * (non-Javadoc)
     * @see org.biouno.unochoice.AbstractUnoChoiceParameter#getChoiceType()
     */
    @Override
    public String getChoiceType() {
        return this.choiceType;
    }

    public Boolean getOmitValueField() {
        return omitValueField;
    }

    /**
     * {@inheritDoc}
     *
     * This parameter also includes the Jenkins project and build objects in the Groovy variables map. It
     * means that you can use these two in your code for rendering the parameter.
     */
    @Override
    public Map<Object, Object> getParameters() {
        Map<Object, Object> parameters = super.getParameters();
        final AbstractProject<?, ?> project = ((DescriptorImpl) getDescriptor()).getProject();
        if (project != null) {
            parameters.put(JENKINS_PROJECT_VARIABLE_NAME, project);
            AbstractBuild<?, ?> build = project.getLastBuild();
            if (build != null && build.getHasArtifacts()) {
                parameters.put(JENKINS_BUILD_VARIABLE_NAME, build);
            }
        }
        return parameters;
    }

    @JavaScriptMethod
    public String getChoicesAsStringForUI() {
        String result = getChoicesAsString(getParameters());
        return result;
    }

    // --- descriptor

    @Extension
    public static final class DescriptorImpl extends UnoChoiceParameterDescriptor {

        private AbstractProject<?, ?> project;

        /*
         * Used to store a reference to the Jenkins project related to this parameter.
         * A bit hacky, probably using another extension point would be a good idea.
         */
        @Override
        public ParameterDefinition newInstance(StaplerRequest req, JSONObject formData) throws hudson.model.Descriptor.FormException {
            List<Ancestor> ancestors = req.getAncestors();
            AbstractProject<?, ?> project = null;
            for (Ancestor ancestor : ancestors) {
                Object object = ancestor.getObject();
                if (object instanceof AbstractProject<?, ?>) {
                    project = (AbstractProject<?, ?>) object;
                    break;
                }
            }
            this.project = project;
            return super.newInstance(req, formData);
        }

        public AbstractProject<?, ?> getProject() {
            return project;
        }

        @Override
        public String getDisplayName() {
            return "Active Choices Reactive Reference Parameter";
        }

        public FormValidation doCheckRequired(@QueryParameter String value) {
            String strippedValue = StringUtils.strip(value);
            if ("".equals(strippedValue)) {
                return FormValidation.error("This field is required.");
            }
            return FormValidation.ok();
        }

    }

}
