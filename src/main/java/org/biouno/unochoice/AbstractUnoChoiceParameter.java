/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2025 Ioannis Moutsatsos, Bruno P. Kinoshita
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

import hudson.model.FileParameterValue;
import hudson.model.ParameterValue;
import hudson.model.SimpleParameterDefinition;
import hudson.model.StringParameterValue;
import jakarta.servlet.ServletException;
import jenkins.model.Jenkins;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.util.JSONUtils;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.lang.StringUtils;
import org.biouno.unochoice.util.Utils;
import org.kohsuke.stapler.StaplerRequest2;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Abstract Uno Choice parameter. Provides basic methods common to all Uno Choice parameters.
 *
 * @author Bruno P. Kinoshita
 * @since 0.20
 */
public abstract class AbstractUnoChoiceParameter extends SimpleParameterDefinition implements UnoChoiceParameter {

    /*
     * Le logger.
     */
    protected static final Logger LOGGER = Logger.getLogger(AbstractUnoChoiceParameter.class.getName());

    /*
     * Serial UID.
     */
    private static final long serialVersionUID = -6027543114170652870L;

    /*
     * Constants.
     */
    public static final String PARAMETER_TYPE_SINGLE_SELECT = "PT_SINGLE_SELECT"; // default choice type
    public static final String PARAMETER_TYPE_MULTI_SELECT = "PT_MULTI_SELECT";
    public static final String PARAMETER_TYPE_CHECK_BOX = "PT_CHECKBOX";
    public static final String PARAMETER_TYPE_RADIO = "PT_RADIO";

    public static final String ELEMENT_TYPE_TEXT_BOX = "ET_TEXT_BOX"; // default choice type
    public static final String ELEMENT_TYPE_ORDERED_LIST = "ET_ORDERED_LIST";
    public static final String ELEMENT_TYPE_UNORDERED_LIST = "ET_UNORDERED_LIST";
    public static final String ELEMENT_TYPE_FORMATTED_HTML = "ET_FORMATTED_HTML";
    public static final String ELEMENT_TYPE_FORMATTED_HIDDEN_HTML = "ET_FORMATTED_HIDDEN_HTML";

    public static final int DEFAULT_MAX_VISIBLE_ITEM_COUNT = 10;

    private final String randomName;

    /**
     * Inherited constructor.
     *
     * {@inheritDoc}.
     *
     * @param name name
     * @param description description
     * FIXME: remove in new major release
     * @deprecated to fix JENKINS-32149 (create random name only once - this is the parameter ID)
     */
    @Deprecated
    protected AbstractUnoChoiceParameter(String name, String description) {
        super(name, description);
        randomName = Utils.createRandomParameterName("choice-parameter", "");
    }

    /**
     * Inherited constructor.
     *
     * {@inheritDoc}.
     *
     * @param name name
     * @param description description
     * @param randomName the parameter unique ID (a random uuid) created once per parameter. If blank or {@code null}
     * it will be created a new random parameter name that starts with choice-parameter
     */
    protected AbstractUnoChoiceParameter(String name, String description, String randomName) {
        super(name, description);
        if (StringUtils.isBlank(randomName))
            this.randomName = Utils.createRandomParameterName("choice-parameter", "");
        else
            this.randomName = randomName;
    }

    /**
     * Gets the randomly generated parameter name. Used in the UI for objecting binding.
     *
     * @return a random string, created during object instantiation.
     */
    public String getRandomName() {
        return randomName;
    }

    /*
     * (non-Javadoc)
     * @see hudson.model.SimpleParameterDefinition#createValue(java.lang.String)
     */
    @Override
    public ParameterValue createValue(String value) {
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.entering(AbstractUnoChoiceParameter.class.getName(), "createValue", value);
        }
        final String description = getDescription();
        final String name = getName();
        final StringParameterValue parameterValue = new StringParameterValue(name, value, description);
        return parameterValue;
    }

    /*
     * (non-Javadoc)
     * @see hudson.model.ParameterDefinition#createValue(org.kohsuke.stapler.StaplerRequest2, net.sf.json.JSONObject)
     */
    @Override
    public ParameterValue createValue(StaplerRequest2 request, JSONObject json) {
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.entering(AbstractUnoChoiceParameter.class.getName(), "createValue", new Object[] {request, json});
        }
        if (json.containsKey("file")) {
            // copied from FileParameterDefinition
            FileItem src;
            try {
                src = request.getFileItem( json.getString("file") );
            } catch (ServletException e) {
                LOGGER.log(Level.SEVERE, "Fatal error while reading uploaded file: " + e.getMessage(), e);
                return null;
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "IO error while reading uploaded file: " + e.getMessage(), e);
                return null;
            }
            if ( src == null ) {
                // the requested file parameter wasn't uploaded
                return null;
            }
            FileParameterValue p = new FileParameterValue(getName(), src);
            p.setDescription(getDescription());
            return p;
        } else {
            final JSONObject parameterJsonModel = new JSONObject(false);
            final Object value = json.get("value");
            final Object name = json.get("name");
            final String valueAsText;

            if (JSONUtils.isArray(value)) {
                valueAsText = ((JSONArray) value).join(",", true);
            } else {
                valueAsText = (value == null) ? "" : String.valueOf(value);
            }

            parameterJsonModel.put("name",  name);
            parameterJsonModel.put("value", valueAsText);

            StringParameterValue parameterValue = request.bindJSON(StringParameterValue.class, parameterJsonModel);
            parameterValue.setDescription(getDescription());
            return parameterValue;
        }
    }

    /**
     * <p>Gets the choice type.</p>
     *
     * <p>This method can be called from Javascript</p>
     *
     * @return choice type
     */
    public abstract String getChoiceType();

    @Override
    public ParameterDescriptor getDescriptor() {
        final ParameterDescriptor desc = (ParameterDescriptor) Jenkins
                .get()
                .getDescriptor(getClass());
        if (desc != null) {
            return desc;
        }
        throw new RuntimeException("Missing parameter description!");
    }

}
