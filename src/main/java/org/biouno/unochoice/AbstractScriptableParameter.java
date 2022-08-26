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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.biouno.unochoice.model.Script;
import org.biouno.unochoice.util.ScriptCallback;
import org.biouno.unochoice.util.Utils;
import org.kohsuke.stapler.Ancestor;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;

import hudson.model.AbstractBuild;
import hudson.model.AbstractItem;
import hudson.model.ParameterValue;
import hudson.model.Project;
import hudson.model.StringParameterValue;
import jenkins.model.Jenkins;

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
    private static final long serialVersionUID = 1322134413144485771L;
    /**
     * Used to split values that come from the UI via Ajax POST's
     */
    protected static final String SEPARATOR = "__LESEP__";
    /**
     * Used to split values when scripts return values like A=2, B=3.
     */
    protected static final String EQUALS = "=";
    /**
     * Constant used to add the project in the environment variables map.
     */
    protected static final String JENKINS_PROJECT_VARIABLE_NAME = "jenkinsProject";
    /**
     * Constant used to add the build in the environment variables map.
     */
    protected static final String JENKINS_BUILD_VARIABLE_NAME = "jenkinsBuild";
    /**
     * Constant used to add the parameter name in the environment variables map.
     */
    protected static final String JENKINS_PARAMETER_VARIABLE_NAME = "jenkinsParameter";
    /**
     * Number of visible items on the screen.
     */
    private volatile int visibleItemCount = 1;
    /**
     * Script used to render the parameter.
     */
    protected final Script script;
    /**
     * The project name.
     */
    private final String projectName;
    /**
     * The project Full Name (including folder).
     */
    private final String projectFullName;

    /**
     * Inherited constructor.
     *
     * {@inheritDoc}
     *
     * @param name name
     * @param description description
     * @param script script used to generate the list of parameter values
     * @deprecated see JENKINS-32149
     */
    protected AbstractScriptableParameter(String name, String description, Script script) {
        super(name, description);
        this.script = script;
        this.projectName = null;
        this.projectFullName = null;
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
     */
    protected AbstractScriptableParameter(String name, String description, String randomName, Script script) {
        super(name, description, randomName);
        this.script = script;
        // Try to get the project name from the current request. In case of being called in some other non-web way,
        // the name will be fetched later via Jenkins.getInstance() and iterating through all items. This is for a
        // performance wise approach first.
        final StaplerRequest currentRequest = Stapler.getCurrentRequest();
        String projectName = null;
        String projectFullName = null;
        if (currentRequest != null) {
            final Ancestor ancestor = currentRequest.findAncestor(AbstractItem.class);
            if (ancestor != null) {
                final Object o = ancestor.getObject();
                if (o instanceof AbstractItem) {
                    final AbstractItem parentItem = (AbstractItem) o;
                    projectName = parentItem.getName();
                    projectFullName = parentItem.getFullName();
                }
            }
        }
        this.projectName = projectName;
        this.projectFullName = projectFullName;
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
        // map with parameters
        final Map<Object, Object> helperParameters = new LinkedHashMap<>();

        // First, if the project name is set, we then find the project by its name, and inject into the map
        Project<?, ?> project = null;
        if (StringUtils.isNotBlank(this.projectFullName)) {
            // First try full name if exists
            project = Jenkins.get().getItemByFullName(this.projectFullName, Project.class);
        } else if (StringUtils.isNotBlank(this.projectName)) {
            // next we try to get the item given its name, which is more efficient
            project = Utils.getProjectByName(this.projectName);
        }
        // Last chance, if we were unable to get project from name and full name, try uuid
        if (project == null) {
            // otherwise, in case we don't have the item name, we iterate looking for a job that uses this UUID
            project = Utils.findProjectByParameterUUID(this.getRandomName());
        }
        if (project != null) {
            helperParameters.put(JENKINS_PROJECT_VARIABLE_NAME, project);
            AbstractBuild<?, ?> build = project.getLastBuild();
            if (build != null && build.getHasArtifacts()) {
                helperParameters.put(JENKINS_BUILD_VARIABLE_NAME, build);
            }
        }

        // Here we set the parameter name
        helperParameters.put(JENKINS_PARAMETER_VARIABLE_NAME, this);

        // Here we inject the global node properties
        final Map<String, Object> globalNodeProperties = Utils.getGlobalNodeProperties();
        helperParameters.putAll(globalNodeProperties);
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
            final Map<Object, Object> map = new LinkedHashMap<>();
            for (Object o : (List<Object>) value) {
                map.put(o, o);
            }
            visibleItemCount = map.size();
            return map;
        }
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine(String.format("Script parameter with name '%s' is not an instance of java.util.Map. The "
                    + "parameter value is %s", getName(), value));
        }
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

    /*
     * (non-Javadoc)
     * @see hudson.model.ParameterDefinition#getDefaultParameterValue()
     */
    @Override
    public ParameterValue getDefaultParameterValue() {
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.entering(AbstractUnoChoiceParameter.class.getName(), "getDefaultParameterValue");
        }
        final String name = getName();
        String defaultValue = findDefaultValue(getChoices(Collections.<Object, Object> emptyMap()));
        final String value = ObjectUtils.toString(defaultValue, ""); // Jenkins doesn't like null parameter values
        final StringParameterValue stringParameterValue = new StringParameterValue(name, value);
        return stringParameterValue;
    }

    private static String findDefaultValue(Map<Object, Object> choices) {
        if (choices == null || choices.isEmpty()) {
            return null;
        }

        List<String> defaultValues = new ArrayList<>();
        List<Entry<?, ?>> entries = new ArrayList<>(choices.entrySet());
        for (Entry<?, ?> entry : entries) {
            String valueText = ObjectUtils.toString(entry.getValue(), "");
            if (Utils.isSelected(valueText)) {
                String keyText = ObjectUtils.toString(entry.getKey(), "");
                defaultValues.add(Utils.escapeSelectedAndDisabled(keyText));
            }
        }
        if (defaultValues.isEmpty()) {
            String keyText = ObjectUtils.toString(entries.get(0).getKey(), "");
            return ObjectUtils.toString(keyText, "");
        }

        StringBuilder defaultValuesText = new StringBuilder();
        for (String value : defaultValues) {
            defaultValuesText.append(',');
            defaultValuesText.append(value);
        }
        return defaultValuesText.substring(1);
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
        return Math.min(visibleItemCount, DEFAULT_MAX_VISIBLE_ITEM_COUNT);
    }

}
