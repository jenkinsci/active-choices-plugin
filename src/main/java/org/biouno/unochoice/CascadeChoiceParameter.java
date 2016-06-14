/*
 * The MIT License (MIT)
 *
 * Copyright (c) <2014-2015> Ioannis K. Moutsatsos
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.biouno.unochoice;

import hudson.Extension;

import org.apache.commons.lang.StringUtils;
import org.biouno.unochoice.model.GroovyScript;
import org.biouno.unochoice.model.Script;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * <p>A choice parameter, that gets updated when another parameter changes. The simplest 
 * use case for this, would be having a list of states, and when the user selected a
 * state it would trigger an update of the city fields.</p>
 *
 * <p>The state parameter would be a choice parameter, and the city parameter would be a
 * cascade choice parameter, that referenced the former.</p>
 *
 * <p>Its options are retrieved from the evaluation of a Groovy script.</p>
 *
 * @author Bruno P. Kinoshita
 * @since 0.1
 */
public class CascadeChoiceParameter extends AbstractCascadableParameter {

    /*
     * Serial UID.
     */
    private static final long serialVersionUID = 4524790278642708107L;

    /**
     * Choice type.
     */
    private final String choiceType;

    /**
     * Filter flag.
     */
    private final Boolean filterable;

    /**
     * Constructor called from Jelly with parameters.
     *
     * @param name name
     * @param description description
     * @param script script
     * @param visibilityScript visibility script
     * @param choiceType choice type
     * @param referencedParameters referenced parameters
     * @param filterable filter flag
     * @deprecated see JENKINS-32149
     */
    public CascadeChoiceParameter(String name, String description, Script script, Script visibilityScript,
            String choiceType, String referencedParameters, Boolean filterable) {
        super(name, description, script, visibilityScript, referencedParameters);
        this.choiceType = StringUtils.defaultIfBlank(choiceType, PARAMETER_TYPE_SINGLE_SELECT);
        this.filterable = filterable;
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
     * @param filterable filter flag
     */
    @DataBoundConstructor
    public CascadeChoiceParameter(String name, String description, String randomName, Script script, Script visibilityScript,
            String choiceType, String referencedParameters, Boolean filterable) {
        super(name, description, randomName, script, visibilityScript, referencedParameters);
        this.choiceType = StringUtils.defaultIfBlank(choiceType, PARAMETER_TYPE_SINGLE_SELECT);
        this.filterable = filterable;
    }

    /*
     * (non-Javadoc)
     * @see org.biouno.unochoice.AbstractUnoChoiceParameter#getChoiceType()
     */
    @Override
    public String getChoiceType() {
        return choiceType;
    }

    /**
     * Gets the filter flag.
     *
     * @return filter flag
     */
    public Boolean getFilterable() {
        return filterable;
    }

    // --- descriptor

    @Extension
    public static final class DescriptImpl extends UnoChoiceParameterDescriptor {

        @Override
        public String getDisplayName() {
            return "Active Choices Reactive Parameter";
        }

    }

}
