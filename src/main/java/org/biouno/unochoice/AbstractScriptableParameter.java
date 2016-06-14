/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2015 Ioannis Moutsatsos, Bruno P. Kinoshita
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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.biouno.unochoice.model.GroovyScript;
import org.biouno.unochoice.model.Script;
import org.biouno.unochoice.util.ScriptCallback;
import org.biouno.unochoice.util.Utils;
import org.kohsuke.stapler.Ancestor;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;

import hudson.model.AbstractItem;
import hudson.model.ParameterValue;
import hudson.model.Project;
import hudson.model.StringParameterValue;

/**
 * Base class for parameters with scripts.
 *
 * @author Bruno P. Kinoshita
 * @since 0.20
 */
public abstract class AbstractScriptableParameter extends AbstractUnoChoiceParameter
    implements ScriptableParameter<Map<Object, Object>> {

    /*
     * Serial UID.
     */
    private static final long serialVersionUID = -6533352776594510145L;
    /**
     * Used to split values that come from the UI via Ajax POST's
     */
    protected static final String SEPARATOR = "__LESEP__";
    /**
     * Constant used to add the project in the environment variables map.
     */
    protected static final String JENKINS_PROJECT_VARIABLE_NAME = "jenkinsProject";
    /**
     * Number of visible items on the screen.
     */
    private volatile int visibleItemCount = 1;
    /**
     * Script used to render the parameter.
     */
    protected final Script script;

    /**
     * Script used to render the visibility.
     */
    protected final Script visibilityScript;

    /**
     * The project name.
     */
    private final String projectName;

    /**
     * Inherited constructor.
     *
     * {@inheritDoc}
     *
     * @param name name
     * @param description description
     * @param script script used to generate the list of parameter values
     * @param visibilityScript script used to determine the visibility of this parameter
     * @deprecated see JENKINS-32149
     */
    protected AbstractScriptableParameter(String name, String description, Script script, Script visibilityScript) {
        super(name, description);
        this.script = script;
        this.visibilityScript = visibilityScript;
        this.projectName = null;
    }

    /**
     * Inherited constructor.
     *
     * {@inheritDoc}
     *
     * @param name name
     * @param description description
     * @param randomName parameter random generated name (uuid)
     * @param script script used to generate the list of parameter values
     * @param visibilityScript script used to determine the visibility of this parameter
     */
    protected AbstractScriptableParameter(String name, String description, String randomName, Script script, Script visibilityScript) {
        super(name, description, randomName);
        this.script = script;
        this.visibilityScript = visibilityScript;
        // Try to get the project name from the current request. In case of being called in some other non-web way,
        // the name will be fetched later via Jenkins.getInstance() and iterating through all items. This is for a
        // performance wise approach first.
        final StaplerRequest currentRequest = Stapler.getCurrentRequest();
        String projectName = null;
        if (currentRequest != null) {
            final Ancestor ancestor = currentRequest.findAncestor(AbstractItem.class);
            if (ancestor != null) {
                final Object o = ancestor.getObject();
                if (o instanceof AbstractItem) {
                    final AbstractItem parentItem = (AbstractItem) o;
                    projectName = parentItem.getName();
                }
            }
        }
        this.projectName = projectName;
    }

    /**
     * Gets the script.
     *
     * @return the script
     */
    public Script getScript() {
        return script;
    }

    /**
     * Gets the visibility script.
     *
     * @return the visibility script
     */
    public Script getVisibilityScript() {
        return visibilityScript;
    }

    public boolean isVisible() {
        return evalVisibility(getParameters());
    }

    @Override
    public boolean isVisible(Map<Object, Object> parameters) {
        return evalVisibility(parameters);
    }

    /**
     * Gets the current parameters, be it before or after other referenced parameters triggered an update. Populates
     * parameters common to all evaluations, such as jenkinsProject, which is the current Jenkins project.
     *
     * @return the current parameters with pre-populated defaults
     */
    public Map<Object, Object> getParameters() {
        return Collections.emptyMap();
    }

    /**
     * Helper parameters used to render the parameter definition.
     * @return Map with helper parameters
     */
    private Map<Object, Object> getHelperParameters() {
        final Map<Object, Object> helperParameters = new LinkedHashMap<Object, Object>();
        Project<?, ?> project = null;
        if (StringUtils.isNotBlank(this.projectName)) {
            // first we try to get the item given its name, which is more efficient
            project = Utils.getProjectByName(this.projectName);
        } else {
            // otherwise, in case we don't have the item name, we iterate looking for a job that uses this UUID
            project = Utils.findProjectByParameterUUID(this.getRandomName());
        }
        if (project != null)
            helperParameters.put(JENKINS_PROJECT_VARIABLE_NAME, project);
        return helperParameters;
    }

    public Map<Object, Object> getChoices() {
        Map<Object, Object> choices = this.getChoices(getParameters());
        visibleItemCount = choices.size();
        return choices;
    }

    /*
     * (non-Javadoc)
     * @see org.biouno.unochoice.ScriptableParameter#getChoices(java.util.Map)
     */
    @Override
    @SuppressWarnings("unchecked") // due to Web + Java and scripts integration
    public Map<Object, Object> getChoices(Map<Object, Object> parameters) {
        final Object value = eval(parameters);
        if (value instanceof Map) {
            Map<Object, Object> map = (Map<Object, Object>) value;
            visibleItemCount = map.size();
            return map;
        }
        if (value instanceof List) {
            // here we take a list and return it as a map
            final Map<Object, Object> map = new LinkedHashMap<Object, Object>();
            for (Object o : (List<Object>) value) {
                map.put(o, o);
            }
            visibleItemCount = map.size();
            return map;
        }
        LOGGER.warning(String.format("Script parameter with name '%s' is not an instance of java.util.Map. The "
                + "parameter value is %s", getName(), value));
        return Collections.emptyMap();
    }

    public String getChoicesAsString() {
        return getChoicesAsString(getParameters());
    }

    public String getChoicesAsString(Map<Object, Object> parameters) {
        final Object value = eval(parameters);
        if (value != null)
            return value.toString();
        return "";
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Object eval(Map<Object, Object> parameters) {
        if (script == null) {
            return true;
        }
        try {
            Map<Object, Object> scriptParameters = getHelperParameters();
            scriptParameters.putAll(parameters);
            final ScriptCallback<Exception> callback = new ScriptCallback(getName(), script, scriptParameters);
            return callback.call();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error executing script for dynamic parameter", e);
            return Collections.emptyMap();
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected boolean evalVisibility(Map<Object, Object> parameters) {
        if (visibilityScript == null) {
            return true;
        }
        try {
            Map<Object, Object> scriptParameters = getHelperParameters();
            scriptParameters.putAll(parameters);
            final ScriptCallback<Exception> callback = new ScriptCallback(getName(), visibilityScript, scriptParameters);
            return (Boolean) callback.call();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error executing visibility script for dynamic parameter", e);
            return true;
        }
    }

    /*
     * (non-Javadoc)
     * @see hudson.model.ParameterDefinition#getDefaultParameterValue()
     */
    @Override
    public ParameterValue getDefaultParameterValue() {
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.entering(AbstractUnoChoiceParameter.class.getName(), "getDefaultParameterValue");
        }
        Object firstElement = "";
        final Map<Object, Object> choices = getChoices(Collections.<Object, Object> emptyMap());
        if (choices != null && !choices.isEmpty()) {
            firstElement = choices.entrySet().iterator().next().getValue();
        }
        final String name = getName();
        final String value = ObjectUtils.toString(firstElement, ""); // Jenkins doesn't like null parameter values
        final StringParameterValue stringParameterValue = new StringParameterValue(name, value);
        return stringParameterValue;
    }

    // --- type types

    /**
     * Get the number of visible items in the select.
     *
     * @return the number of choices or, if it is higher than the default, then it returns the default maximum value
     */
    public int getVisibleItemCount() {
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.entering(AbstractUnoChoiceParameter.class.getName(), "getVisibleItemCount");
        }
        if (visibleItemCount <= 0)
            visibleItemCount = 1;
        final int choicesSize = visibleItemCount;
        if (choicesSize < DEFAULT_MAX_VISIBLE_ITEM_COUNT)
            return choicesSize;
        return DEFAULT_MAX_VISIBLE_ITEM_COUNT;
    }

}
