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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;

import org.apache.commons.lang3.StringUtils;
import org.biouno.unochoice.model.Script;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest2;
import org.kohsuke.stapler.bind.JavaScriptMethod;
import org.kohsuke.stapler.json.JsonHttpResponse;

import hudson.Util;
import hudson.model.BooleanParameterValue;
import hudson.model.Item;
import hudson.model.Job;
import hudson.model.ParameterDefinition;
import hudson.model.ParameterValue;
import hudson.model.ParametersDefinitionProperty;
import hudson.model.SimpleParameterDefinition;
import hudson.model.StringParameterValue;

/**
 * Base class for cascadable parameters, providing basic and utility methods.
 *
 * @author Bruno P. Kinoshita
 * @since 0.20
 */
public abstract class AbstractCascadableParameter extends AbstractScriptableParameter implements CascadableParameter<Map<Object, Object>> {

    /*
     * Serial UID. 
     */
    private static final long serialVersionUID = 3795727126307053346L;
    /**
     * Map with parameters in the UI.
     */
    // Map is not serializable, but LinkedHashMap is. Ignore static analysis errors
    protected final Map<Object, Object> parameters = new LinkedHashMap<>();

    /**
     * Referenced parameters.
     */
    private final String referencedParameters;

    /**
     * Whether the default value is computed from the values of the referenced parameters. Off by default, in which
     * case the script gets no referenced values when Jenkins asks for the default value.
     */
    private Boolean computeDefaultFromReferences;

    /**
     * Create a new abstract cascadable parameter.
     * @param name name
     * @param description description
     * @param script script used to generate the list of parameter values
     * @param referencedParameters comma separated list of referenced parameters
     * @deprecated see JENKINS-32149
     */
    @Deprecated
    protected AbstractCascadableParameter(String name, String description, Script script, String referencedParameters) {
        super(name, description, script);
        this.referencedParameters = referencedParameters;
    }

    /**
     * Create a new abstract cascadable parameter.
     * @param name name
     * @param description description
     * @param randomName parameter random generated name (uuid)
     * @param script script used to generate the list of parameter values
     * @param referencedParameters comma separated list of referenced parameters
     */
    protected AbstractCascadableParameter(String name, String description, String randomName,
            Script script, String referencedParameters) {
        super(name, description, randomName, script);
        this.referencedParameters = referencedParameters;
    }

    /*
     * (non-Javadoc)
     * @see org.biouno.unochoice.CascadableParameter#getReferencedParameters()
     */
    @Override
    public String getReferencedParameters() {
        return referencedParameters;
    }

    /**
     * Gets the flag that controls whether the default value is computed from the referenced parameters.
     *
     * @return {@code true} if the default value is computed from the referenced parameters, {@code false} (default)
     * otherwise
     * @since 2.8.12
     */
    public boolean getComputeDefaultFromReferences() {
        return computeDefaultFromReferences != null && computeDefaultFromReferences;
    }

    /**
     * Sets the flag that controls whether the default value is computed from the referenced parameters.
     *
     * @param computeDefaultFromReferences whether the default value is computed from the referenced parameters
     * @since 2.8.12
     */
    @DataBoundSetter
    public void setComputeDefaultFromReferences(Boolean computeDefaultFromReferences) {
        this.computeDefaultFromReferences = computeDefaultFromReferences;
    }

    /*
     * (non-Javadoc)
     * @see org.biouno.unochoice.AbstractScriptableParameter#getParameters()
     */
    @Override
    public Map<Object, Object> getParameters() {
        return parameters;
    }

    /**
     * Passes the default values of the referenced parameters to the script, when
     * {@link #getComputeDefaultFromReferences()} is on.
     *
     * @return the referenced parameters and their default values
     * @since 2.8.12
     */
    @Override
    protected Map<Object, Object> getDefaultValueParameters() {
        if (!getComputeDefaultFromReferences()) {
            return super.getDefaultValueParameters();
        }
        final Job<?, ?> project = findProject();
        final ParametersDefinitionProperty property = project == null ? null
                : project.getProperty(ParametersDefinitionProperty.class);
        if (property == null) {
            return super.getDefaultValueParameters();
        }
        final Resolution resolution = new Resolution(property, Collections.emptyMap(),
                getRequestMemo(Stapler.getCurrentRequest2(), project));
        return resolveReferencedParameters(resolution);
    }

    /**
     * When {@link #getComputeDefaultFromReferences()} is on, the default value is computed from the referenced
     * parameters. Jenkins also asks for the default when a {@code buildWithParameters} request lacks this
     * parameter; then the request values of the parameters this one depends on, directly or through other cascade
     * parameters, are used, without the default value cache. Only a POST to this job's {@code buildWithParameters}
     * by a user who may build the job can pass them. For any other {@code buildWithParameters} request with such
     * values (a GET, one allowed by the job's trigger token alone, one for another job or of another plugin) the
     * script gets no referenced values, as when the flag is off. Within one HTTP request each value is computed once.
     */
    @Override
    public ParameterValue getDefaultParameterValue() {
        if (!getComputeDefaultFromReferences()) {
            return super.getDefaultParameterValue();
        }
        final Job<?, ?> project = findProject();
        final StaplerRequest2 req = Stapler.getCurrentRequest2();
        final Map<String, String> requestMemo = getRequestMemo(req, project);
        if (requestMemo != null && requestMemo.containsKey(getName())) {
            return new StringParameterValue(getName(), requestMemo.get(getName()));
        }
        final ParameterValue value = computeDefaultParameterValue(project, req, requestMemo);
        if (requestMemo != null) {
            requestMemo.put(getName(), Objects.toString(value.getValue(), ""));
        }
        return value;
    }

    private ParameterValue computeDefaultParameterValue(Job<?, ?> project, StaplerRequest2 req,
            Map<String, String> requestMemo) {
        final ParametersDefinitionProperty property = project == null ? null
                : project.getProperty(ParametersDefinitionProperty.class);
        if (property == null || !isBuildWithParametersRequest(req)) {
            return super.getDefaultParameterValue();
        }
        final Map<String, String> requestValues = new HashMap<>();
        for (String name : property.getParameterDefinitionNames()) {
            final String value = req.getParameter(name);
            if (value != null) {
                requestValues.put(name, value);
            }
        }
        final Set<String> dependencies = new HashSet<>();
        collectDependencies(property, dependencies);
        if (Collections.disjoint(dependencies, requestValues.keySet())) {
            return super.getDefaultParameterValue();
        }
        if (req.findAncestorObject(Job.class) != project || !"POST".equals(req.getMethod())
                || !project.hasPermission(Item.BUILD)) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine(String.format("Parameter '%s' of '%s' ignores the request values of the parameters it "
                        + "references, as only a POST to the job's buildWithParameters by a user with Build "
                        + "permission may pass them", getName(), project.getFullName()));
            }
            return new StringParameterValue(getName(), computeDefaultValue(Collections.emptyMap()));
        }
        final Resolution resolution = new Resolution(property, requestValues, requestMemo);
        return new StringParameterValue(getName(), computeDefaultValue(resolveReferencedParameters(resolution)));
    }

    /**
     * Returns true for any {@code buildWithParameters} request, including those for other jobs and those of plugins
     * such as Build Authorization Token Root, so their values for the referenced parameters are never mixed with
     * referenced defaults.
     */
    private static boolean isBuildWithParametersRequest(StaplerRequest2 req) {
        if (req == null) {
            return false;
        }
        final String uri = req.getRequestURI();
        return uri.endsWith("/buildWithParameters") || uri.endsWith("/buildWithParameters/");
    }

    /**
     * Returns the values of the job's parameters found so far in the current HTTP request, or {@code null} outside a
     * request.
     */
    @SuppressWarnings("unchecked")
    private static Map<String, String> getRequestMemo(StaplerRequest2 req, Job<?, ?> project) {
        if (req == null || project == null) {
            return null;
        }
        final String attribute = AbstractCascadableParameter.class.getName() + ".values." + project.getFullName();
        Map<String, String> memo = (Map<String, String>) req.getAttribute(attribute);
        if (memo == null) {
            memo = new HashMap<>();
            req.setAttribute(attribute, memo);
        }
        return memo;
    }

    /**
     * Adds the names of the parameters this one references, directly or through other cascade parameters.
     */
    private void collectDependencies(ParametersDefinitionProperty property, Set<String> names) {
        for (String name : getReferencedParametersAsArray()) {
            if (names.add(name)) {
                final ParameterDefinition definition = property.getParameterDefinition(name);
                if (definition instanceof AbstractCascadableParameter) {
                    ((AbstractCascadableParameter) definition).collectDependencies(property, names);
                }
            }
        }
    }

    /**
     * What one computation of referenced values works with.
     */
    private static final class Resolution {
        /** The parameters of the job. */
        final ParametersDefinitionProperty property;
        /** Values of the job's parameters in an allowed {@code buildWithParameters} request, else empty. */
        final Map<String, String> requestValues;
        /** Values found in this computation. */
        final Map<String, String> computed = new HashMap<>();
        /** Parameters being computed; a reference back to one of them gets an empty value, so cycles end. */
        final Set<String> inProgress = new HashSet<>();
        /** Values found earlier in the same HTTP request, or {@code null} outside a request. */
        final Map<String, String> requestMemo;

        Resolution(ParametersDefinitionProperty property, Map<String, String> requestValues,
                Map<String, String> requestMemo) {
            this.property = property;
            this.requestValues = requestValues;
            this.requestMemo = requestMemo;
        }
    }

    /**
     * Gives each referenced parameter the value {@code valueOf} returns for it.
     */
    private Map<Object, Object> resolveReferencedParameters(Resolution resolution) {
        resolution.inProgress.add(getName());
        final Map<Object, Object> resolved = new LinkedHashMap<>();
        for (String name : getReferencedParametersAsArray()) {
            resolved.put(name, valueOf(name, resolution));
        }
        resolution.inProgress.remove(getName());
        return resolved;
    }

    /**
     * Returns the value of a referenced parameter as the script sees it: the request value if there is one, else
     * its default. A cascade parameter with {@link #getComputeDefaultFromReferences()} on gets the value it gets as
     * its own default, see {@code referencedValue}. Only string and boolean values that are not
     * sensitive are passed on; any other kind (password, file, run) is empty.
     */
    private static String valueOf(String name, Resolution resolution) {
        if (resolution.computed.containsKey(name)) {
            return resolution.computed.get(name);
        }
        if (resolution.inProgress.contains(name)) {
            return "";
        }
        final ParameterDefinition definition = resolution.property.getParameterDefinition(name);
        final String requestValue = resolution.requestValues.get(name);
        final boolean computedCascade = definition instanceof AbstractCascadableParameter
                && ((AbstractCascadableParameter) definition).getComputeDefaultFromReferences();
        final String value;
        if (definition == null) {
            value = "";
        } else if (requestValue != null && definition instanceof SimpleParameterDefinition) {
            value = plainValue(((SimpleParameterDefinition) definition).createValue(requestValue));
        } else if (resolution.requestMemo != null && resolution.requestMemo.containsKey(name)) {
            value = resolution.requestMemo.get(name);
        } else if (computedCascade) {
            value = ((AbstractCascadableParameter) definition).referencedValue(resolution);
        } else {
            value = plainValue(definition.getDefaultParameterValue());
            if (resolution.requestMemo != null) {
                resolution.requestMemo.put(name, value);
            }
        }
        resolution.computed.put(name, value);
        return value;
    }

    /**
     * Returns the value this parameter gets as its own default in the same request: its cached value unless a
     * parameter it depends on has a request value, else computed from the referenced parameters. The value goes to
     * the request memo unless it depends on a parameter still being computed (a cycle).
     */
    private String referencedValue(Resolution resolution) {
        final Set<String> dependencies = new HashSet<>();
        collectDependencies(resolution.property, dependencies);
        final String cached = getCachedDefaultValue();
        final String value;
        if (cached != null && Collections.disjoint(dependencies, resolution.requestValues.keySet())) {
            value = cached;
        } else {
            value = computeDefaultValue(resolveReferencedParameters(resolution));
        }
        if (resolution.requestMemo != null && Collections.disjoint(dependencies, resolution.inProgress)) {
            resolution.requestMemo.put(getName(), value);
        }
        return value;
    }

    private static String plainValue(ParameterValue value) {
        if ((value instanceof StringParameterValue || value instanceof BooleanParameterValue) && !value.isSensitive()) {
            return Objects.toString(value.getValue(), "");
        }
        return "";
    }

    // --- methods called from the UI

    /*
     * (non-Javadoc)
     * @see org.biouno.unochoice.CascadableParameter#doUpdate(java.lang.String)
     */
    @Override
    @JavaScriptMethod
    public void doUpdate(String parameters) {
        getParameters().clear();
        final String[] params = parameters.split(SEPARATOR);
        for (String param : params) {
            final String[] nameValue = param.split(EQUALS);
            if (nameValue.length == 1) {
                final String name = nameValue[0].trim();
                if (!name.isEmpty())
                    getParameters().put(name, "");
            } else if (nameValue.length == 2) {
                final String name = nameValue[0];
                final String value = nameValue[1];
                getParameters().put(name, value);
            } else if (nameValue.length > 2) {
                // TBD: we can eliminate this branch by splitting only on the first EQUALS
                final String name = nameValue[0];
                final StringBuilder sb = new StringBuilder();
                // rebuild the rest of the string, skipping the first value
                for (int i = 1; i < nameValue.length; ++i) {
                    sb.append(nameValue[i]);
                    if (i+1 < nameValue.length) {
                        sb.append(EQUALS);
                    }
                }
                final String value = sb.toString();
                getParameters().put(name, value);
            }
        }
        throw new JsonHttpResponse(null);
    }

    /*
     * (non-Javadoc)
     * @see org.biouno.unochoice.CascadableParameter#getChoicesForUI()
     */
    @Override
    @JavaScriptMethod
    public List<Object> getChoicesForUI() {
        Map<Object, Object> mapResult = getChoices(getParameters());
        return Arrays.asList(mapResult.values(), mapResult.keySet());
    }

    public String[] getReferencedParametersAsArray() {
        String referencedParameters = this.getReferencedParameters();
        if (StringUtils.isNotBlank(referencedParameters)) {
            String[] array = referencedParameters.split(",");
            List<String> list = new ArrayList<>();
            for (String value : array) {
                value = value.trim();
                if (StringUtils.isNotBlank(value)) {
                    list.add(Util.escape(value));
                }
            }
            return list.toArray(new String[0]);
        }
        return new String[]{};
    }

}
