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

package org.biouno.unochoice.util;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.biouno.unochoice.AbstractUnoChoiceParameter;
import org.jenkinsci.plugins.scriptler.config.Script;
import org.jenkinsci.plugins.scriptler.config.ScriptlerConfiguration;

import hudson.model.ParameterDefinition;
import hudson.model.ParametersDefinitionProperty;
import hudson.model.Project;
import jenkins.model.Jenkins;

/**
 * Utility methods.
 *
 * @author Bruno P. Kinoshita
 * @since 0.23
 */
public class Utils {

    private Utils() {}

    /**
     * Returns all scriptler scripts available.
     *
     * @return all scriptler scripts available in Jenkins
     */
    public static Set<Script> getAllScriptlerScripts() {
        final Set<Script> scripts = ScriptlerConfiguration.getConfiguration().getScripts();
        return scripts;
    }

    // --- methods called from Jelly pages

    /**
     * Checks whether a parameter value contains the :selected suffix, returning
     * {@code true} if it does, {@code false} otherwise.
     *
     * @param obj parameter value
     * @return {@code true} if the parameter name contains the :selected suffix {@code false} otherwise.
     */
    public static boolean isSelected(Object obj) {
        if (obj == null)
            return false;
        final String text = obj.toString();
        return StringUtils.isNotBlank(text) && text.endsWith(":selected");
    }

    /**
     * Escapes the parameter value, removing the :selected suffix.
     *
     * @param obj parameter value
     * @return escaped parameter value
     */
    public static String escapeSelected(Object obj) {
        if (obj == null)
            return "";
        final String text = obj.toString();
        if (StringUtils.isBlank(text))
            return "";
        if (isSelected(text))
            return text.substring(0, text.indexOf(":selected"));
        return text;
    }

    /**
     * Creates a random parameter name.
     *
     * @param prefix parameter prefix
     * @param suffix parameter suffix
     * @return random parameter name
     */
    public static String createRandomParameterName(String prefix, String suffix) {
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
    public static Map<String, String> getSystemEnv() {
        return System.getenv();
    }

    /**
     * Get project in Jenkins given its name.
     *
     * @since 1.3
     * @param projectName project name in Jenkins
     * @return Project or {@code null} if none with this name
     */
    public static Project<?, ?> getProjectByName(String projectName) {
        Jenkins instance = Jenkins.getInstance();
        if (instance != null) {
            Object o = instance.getItem(projectName);
            if (o instanceof Project)
                return (Project<?, ?>) o;
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
    public static Project findProjectByParameterUUID(String parameterUUID) {
        Jenkins instance = Jenkins.getInstance();
        if (instance != null) {
            List<Project> projects = instance.getAllItems(Project.class);
            for (Project project : projects) {
                if (isParameterDefintionOf(parameterUUID, project)) {
                    return project;
                }
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
    @SuppressWarnings("rawtypes")
    private static boolean isParameterDefintionOf(String parameterUUID, Project project) {
        List<ParameterDefinition> parameterDefinitions = getProjectParameterDefinitions(project);
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
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static List<ParameterDefinition> getProjectParameterDefinitions(Project project) {
        ParametersDefinitionProperty parametersDefinitionProperty = (ParametersDefinitionProperty) project
                .getProperty(ParametersDefinitionProperty.class);
        if (parametersDefinitionProperty != null) {
            List<ParameterDefinition> parameterDefinitions = parametersDefinitionProperty.getParameterDefinitions();
            if (parameterDefinitions != null) {
                return parameterDefinitions;
            }
        }
        return Collections.EMPTY_LIST;
    }
}
