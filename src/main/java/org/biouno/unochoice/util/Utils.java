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

package org.biouno.unochoice.util;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.biouno.unochoice.AbstractUnoChoiceParameter;

import hudson.model.Item;
import hudson.model.Items;
import hudson.model.ParameterDefinition;
import hudson.model.ParametersDefinitionProperty;
import hudson.model.Project;
import hudson.security.ACL;
import hudson.slaves.EnvironmentVariablesNodeProperty;
import hudson.slaves.NodeProperty;
import hudson.slaves.NodePropertyDescriptor;
import hudson.tasks.BuildWrapper;
import hudson.util.DescribableList;
import hudson.util.ReflectionUtils;
import jenkins.model.Jenkins;
import org.acegisecurity.Authentication;

/**
 * Utility methods.
 *
 * @author Bruno P. Kinoshita
 * @since 0.23
 */
public class Utils {

    protected static final Logger LOGGER = Logger.getLogger(Utils.class.getName());

    private Utils() {}

    // --- methods called from Jelly pages

    /**
     * Checks whether a parameter value contains the :selected suffix, returning
     * {@code true} if it does, {@code false} otherwise.
     *
     * @param obj parameter value
     * @return {@code true} if the parameter name contains the :selected suffix {@code false} otherwise.
     */
    public static boolean isSelected(@Nullable Object obj) {
        if (obj == null)
            return false;
        final String text = obj.toString();
        return StringUtils.isNotBlank(text) && (text.endsWith(":selected") || text.endsWith(":selected:disabled"));
    }

    /**
     * Escapes the parameter value, removing the :selected suffix.
     *
     * @param obj parameter value
     * @return escaped parameter value
     */
    public static @NonNull String escapeSelected(@Nullable Object obj) {
        if (obj == null)
            return "";
        final String text = obj.toString();
        if (StringUtils.isBlank(text))
            return "";
        if (isSelected(text))
            return text.replaceAll(":selected$", "").replaceAll(":selected:disabled$", ":disabled");
        return text;
    }

    /**
     * Checks whether a parameter value contains the :disabled suffix, returning
     * {@code true} if it does, {@code false} otherwise.
     *
     * @param obj parameter value
     * @return {@code true} if the parameter name contains the :disabled suffix {@code false} otherwise.
     */
    public static boolean isDisabled(@Nullable Object obj) {
        if (obj == null)
            return false;
        final String text = obj.toString();
        return StringUtils.isNotBlank(text) && (text.endsWith(":disabled") || text.endsWith(":disabled:selected"));
    }

    /**
     * Escapes the parameter value, removing the :disabled suffix.
     *
     * @param obj parameter value
     * @return escaped parameter value
     */
    public static @NonNull String escapeDisabled(@Nullable Object obj) {
        if (obj == null)
            return "";
        final String text = obj.toString();
        if (StringUtils.isBlank(text))
            return "";
        if (isDisabled(text))
            return text.replaceAll(":disabled$", "").replaceAll(":disabled:selected$", ":selected");
        return text;
    }

    /**
     * Escapes the parameter value, removing the :selected and :disabled suffixes.
     *
     * @param obj parameter value
     * @return escaped parameter value
     */
    public static @NonNull String escapeSelectedAndDisabled(@Nullable Object obj) {
        if (obj == null)
            return "";
        final String text = obj.toString();
        if (StringUtils.isBlank(text))
            return "";
        if (isSelected(text) || isDisabled(text))
            return escapeSelected(escapeDisabled(text));
        return text;
    }

    /**
     * Creates a random parameter name.
     *
     * @param prefix parameter prefix
     * @param suffix parameter suffix
     * @return random parameter name
     */
    public static @NonNull String createRandomParameterName(@Nullable String prefix, @Nullable String suffix) {
        String paramName = "";
        if (StringUtils.isNotBlank(prefix))
            paramName = prefix + "-";
        paramName += System.nanoTime();
        if (StringUtils.isNotBlank(suffix))
            paramName = paramName + "-" + suffix;
        return paramName;
    }

    /**
     * Helped method to return the system environment variables. The main advantage
     * over calling the System.getenv method directly, is that we can mock this call
     * (System is final).
     *
     * @return System environment variables as map
     */
    public static @NonNull Map<String, String> getSystemEnv() {
        return System.getenv();
    }

    /**
     * Get project in Jenkins given its name.
     *
     * @since 1.3
     * @param projectName project name in Jenkins
     * @return Project or {@code null} if none with this name
     * @deprecated The choice is arbitrary if there are multiple matches; use {@link Item#getFullName} and {@link Jenkins#getItemByFullName(String, Class)} instead.
     */
    @Deprecated
    @SuppressWarnings("rawtypes")
    public static @CheckForNull Project<?, ?> getProjectByName(@NonNull String projectName) {
        Authentication auth = Jenkins.getAuthentication();
        for (Project p : Items.allItems(ACL.SYSTEM, Jenkins.getInstance(), Project.class)) {
            if (p.getName().equals(projectName) && p.getACL().hasPermission(auth, Item.READ)) {
                return p;
            }
        }
        return null;
    }

    /**
     * Find the current project give its parameter UUID.
     *
     * @author dynamic-parameter-plugin
     * @since 1.3
     * @param parameterUUID parameter UUID
     * @return {@code null} if the current project cannot be found
     */
    @SuppressWarnings("rawtypes")
    public static @CheckForNull Project findProjectByParameterUUID(@NonNull String parameterUUID) {
        Authentication auth = Jenkins.getAuthentication();
        for (Project p : Items.allItems(ACL.SYSTEM, Jenkins.get(), Project.class)) {
            if (isParameterDefinitionOf(parameterUUID, p) && p.getACL().hasPermission(auth, Item.READ)) {
                return p;
            }
        }
        return null;
    }

    /**
     * Returns true if this parameter definition is a definition of the given project.
     *
     * @since 1.3
     * @author dynamic-parameter-plugin
     * @param parameterUUID UUID of the project parameter
     * @param project the project to search for this parameter definition.
     * @return {@code true} if the project contains this parameter definition.
     */
    private static boolean isParameterDefinitionOf(@NonNull String parameterUUID, @NonNull Project<?, ?> project) {
        List<ParameterDefinition> parameterDefinitions = new ArrayList<>(getProjectParameterDefinitions(project));
        for (List<ParameterDefinition> params : getBuildWrapperParameterDefinitions(project).values()) {
            parameterDefinitions.addAll(params);
        }
        for (ParameterDefinition pd : parameterDefinitions) {
            if (pd instanceof AbstractUnoChoiceParameter) {
                AbstractUnoChoiceParameter parameterDefinition = (AbstractUnoChoiceParameter) pd;
                String uuid = parameterDefinition.getRandomName();
                if (ObjectUtils.equals(parameterUUID, uuid)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Get the parameter definitions for the given project.
     *
     * @since 1.3
     * @author dynamic-parameter-plugin
     * @param project the project for which the parameter definitions should be found
     * @return parameter definitions or an empty list
     */
    public static @NonNull List<ParameterDefinition> getProjectParameterDefinitions(@NonNull Project<?, ?> project) {
        ParametersDefinitionProperty parametersDefinitionProperty = project.getProperty(ParametersDefinitionProperty.class);
        if (parametersDefinitionProperty != null) {
            List<ParameterDefinition> parameterDefinitions = parametersDefinitionProperty.getParameterDefinitions();
            if (parameterDefinitions != null) {
                return parameterDefinitions;
            }
        }
        return Collections.emptyList();
    }

    /**
     * Get a map with the global node properties.
     *
     * @since 1.6
     * @return map with global node properties
     */
    public static @NonNull Map<String, Object> getGlobalNodeProperties() {
        Map<String, Object> map = new HashMap<>();
        Jenkins instance = Jenkins.get();
        DescribableList<NodeProperty<?>, NodePropertyDescriptor> globalNodeProperties = instance.getGlobalNodeProperties();
        if (globalNodeProperties != null) {
            for (NodeProperty<?> nodeProperty : globalNodeProperties) {
                if (nodeProperty instanceof EnvironmentVariablesNodeProperty) {
                    EnvironmentVariablesNodeProperty envNodeProperty = (EnvironmentVariablesNodeProperty) nodeProperty;
                    map.putAll(envNodeProperty.getEnvVars());
                }
            }
        }
        return map;
    }

    /**
     * Get parameter definitions associated with {@link BuildWrapper}s of the given {@link Project}.
     * @param project the project for which the parameter definitions should be found
     * @return Map
     */
    public static @NonNull Map<BuildWrapper, List<ParameterDefinition>> getBuildWrapperParameterDefinitions(@NonNull Project<?, ?> project) {
        final List<BuildWrapper> buildWrappersList = project.getBuildWrappersList();

        final Map<BuildWrapper, List<ParameterDefinition>> result = new LinkedHashMap<>();

        List<ParameterDefinition> value = new ArrayList<>();

        for (BuildWrapper buildWrapper : buildWrappersList) {
            final PropertyDescriptor[] propertyDescriptors;
            try {
                propertyDescriptors = Introspector.getBeanInfo(buildWrapper.getClass()).getPropertyDescriptors();
            } catch (IntrospectionException e) {
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.log(Level.FINE,
                            String.format("Introspector.getBeanInfo failed for build wrapper class: [%s]",
                                    buildWrapper.getClass()
                                            .getCanonicalName()),
                            e);
                }
                continue;
            }
            for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
                try {
                    addParameterDefinitionsTo(value, buildWrapper, propertyDescriptor);
                } catch (RuntimeException e) {
                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.log(Level.FINE,
                                String.format("Failed to add parameter [%s] to the ParameterDefinition list",
                                        propertyDescriptor.getName()),
                                e);
                    }
                }
            }
            if (!value.isEmpty()) {
                result.put(buildWrapper, value);
                value = new ArrayList<>();
            }
        }
        return result.isEmpty() ? Collections.emptyMap() : result;
    }

    private static void addParameterDefinitionsTo(List<ParameterDefinition> target, Object bean, PropertyDescriptor pd) {
        if (ParameterDefinition.class.isAssignableFrom(pd.getPropertyType())) {
            final ParameterDefinition param = read(bean, pd);
            if (param != null) {
                target.add(param);
            }
            return;
        }
        Iterable<?> iterable = null;

        if (Iterable.class.isAssignableFrom(pd.getPropertyType())) {
            iterable = read(bean, pd);
        } else if (Object[].class.isAssignableFrom(pd.getPropertyType())) {
            final Object[] array = read(bean, pd);
            if (array != null)
                iterable = Arrays.asList(array);
        }
        if (iterable == null)
            return;

        for (Object o : iterable) {
            if (o instanceof ParameterDefinition) {
                target.add((ParameterDefinition) o);
            }
        }
    }

    private static <T> T read(Object bean, PropertyDescriptor pd) {
        final Method accessor = pd.getReadMethod();
        if ((accessor != null) && (accessor.getParameterTypes().length == 0)) {
            @SuppressWarnings("unchecked")
            final T result = (T) ReflectionUtils.invokeMethod(accessor, bean);
            return result;
        }
        final Field field = ReflectionUtils.findField(bean.getClass(), pd.getName());
        if (field != null) {
            final Object value = ReflectionUtils.getField(field, bean);
            if (pd.getPropertyType().isInstance(value)) {
                @SuppressWarnings("unchecked")
                final T result = (T) value;
                return result;
            }
        }
        return null;
    }
}
