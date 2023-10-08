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

import hudson.Extension;

import org.apache.commons.lang.StringUtils;
import org.biouno.unochoice.model.Script;
import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;

import java.util.Objects;

/**
 * A parameter that renders its options as a choice (select) HTML component.
 *
 * @author Bruno P. Kinoshita
 * @since 0.1
 */
public class ChoiceParameter extends AbstractScriptableParameter {

    /*
     * Serial UID.
     */
    private static final long serialVersionUID = -4449319038169585222L;

    /**
     * Choice type.
     */
    private final String choiceType;

    /**
     * Filter flag.
     */
    private final Boolean filterable;

    /**
     * Filter length. Defines a minimum number of characters that must be entered before the filter
     * is activated.
     */
    private final Integer filterLength;

    /**
     * Constructor called from Jelly with parameters.
     *
     * @param name name
     * @param description description
     * @param script script
     * @param choiceType choice type
     * @param filterable filter flag
     * @deprecated see JENKINS-32149
     */
    public ChoiceParameter(String name, String description, Script script, String choiceType, Boolean filterable) {
        super(name, description, script);
        this.choiceType = StringUtils.defaultIfBlank(choiceType, PARAMETER_TYPE_SINGLE_SELECT);
        this.filterable = filterable;
        this.filterLength = null;
    }

    /**
     * Constructor called from Jelly with parameters.
     * @param name name
     * @param description description
     * @param randomName parameter random generated name
     * @param script script
     * @param choiceType choice type
     * @param filterable filter flag
     * @deprecated see JENKINS-31625
     */
    public ChoiceParameter(String name, String description, String randomName, Script script, String choiceType,
                           Boolean filterable) {
        super(name, description, randomName, script);
        this.choiceType = StringUtils.defaultIfBlank(choiceType, PARAMETER_TYPE_SINGLE_SELECT);
        this.filterable = filterable;
        this.filterLength = null;
    }

    /**
     * Constructor called from Jelly with parameters.
     * @param name name
     * @param description description
     * @param randomName parameter random generated name
     * @param script script
     * @param choiceType choice type
     * @param filterable filter flag
     * @param filterLength length when filter start filtering
     */
    @DataBoundConstructor
    public ChoiceParameter(String name, String description, String randomName, Script script, String choiceType,
                           Boolean filterable, Integer filterLength) {
        super(name, description, randomName, script);
        this.choiceType = StringUtils.defaultIfBlank(choiceType, PARAMETER_TYPE_SINGLE_SELECT);
        this.filterable = filterable;
        this.filterLength = filterLength;
    }

    /*
     * (non-Javadoc)
     * @see org.biouno.unochoice.AbstractUnoChoiceParameter#getChoiceType()
     */
    @Override
    public String getChoiceType() {
        return this.choiceType;
    }

    /**
     * Get the filter flag.
     * @return filter flag
     */
    public boolean getFilterable() {
        return Objects.equals(filterable, Boolean.TRUE);
    }

    /**
     * Get the filter length.
     * @return filter length
     */
    public Integer getFilterLength() {
        return filterLength == null ? (Integer) 1 : filterLength;
    }

    // --- descriptor

    @Extension
    @Symbol({"activeChoice"})
    public static final class DescriptImpl extends UnoChoiceParameterDescriptor {

        @Override
        public String getDisplayName() {
            return "Active Choices Parameter";
        }

        @Override
        public String getHelpFile() {
            return "/plugin/uno-choice/help/parameter/active-choices-choice.html";
        }
    }

}
