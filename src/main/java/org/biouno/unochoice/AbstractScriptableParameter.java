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

import hudson.model.ParameterValue;
import hudson.model.StringParameterValue;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.apache.commons.lang.ObjectUtils;
import org.biouno.unochoice.model.Script;
import org.biouno.unochoice.util.ScriptCallback;

/**
 * Base class for parameters with scripts.
 *
 * @author Bruno P. Kinoshita
 * @since 0.20
 */
public abstract class AbstractScriptableParameter extends AbstractUnoChoiceParameter implements ScriptableParameter<Map<Object, Object>> {

    /*
     * Serial UID.
     */
    private static final long serialVersionUID = -1718340722437175976L;

    protected static final String SEPARATOR = "__LESEP__"; // used to split values that come from the UI via Ajax POST's

    protected final Script script;

    private volatile int visibleItemCount = 1;

    /**
     * Inherited constructor.
     *
     * {@inheritDoc}
     *
     * @param name name
     * @param description description
     * @param script script used to generate the list of parameter values
     */
    protected AbstractScriptableParameter(String name, String description, Script script) {
        super(name, description);
        this.script = script;
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
     * Gets the current parameters, be it before or after other referenced parameters 
     * triggered an update.
     *
     * @return the current parameters
     */
    public Map<Object, Object> getParameters() {
        return Collections.emptyMap();
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
        LOGGER.warning(String.format("Script parameter with name '%s' is not an instance of java.util.Map. The parameter value is %s", getName(), value));
        return Collections.EMPTY_MAP;
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
            final ScriptCallback<Exception> callback = new ScriptCallback(getName(), script, parameters);
            return callback.call();
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, "Error executing script for dynamic parameter", e);
            return Collections.EMPTY_MAP;
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
        /* The commented out approach led into huge performance loss in the Jenkins UI,
         * when the default parameter values are calculated multiple times, eg by other plugins
         * (like the inheritance plugin).
         * Anyway, one should reconsider if this approach is right, as the 'default' value
         * has rather more static semantic, while the below approach assumes the first element in the list,
         * which could be rather volatile value, by the nature and dynamic of the script logic behind it.
         * Maybe the 'default' value should be provided as an UI option, used in case when the script 
         * returns 'null'.
         * Until it's redesigned this method will return an empty string.
         *
        */
        Object firstElement = "";
//        final Map<Object, Object> choices = getChoices(Collections.<Object, Object> emptyMap());
//        if (choices != null && choices.size() > 0) {
//            firstElement = choices.entrySet().iterator().next().getValue();
//        }
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
