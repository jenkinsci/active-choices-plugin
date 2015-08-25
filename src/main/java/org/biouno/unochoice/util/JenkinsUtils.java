/*
 * Copyright 2012 Seitenbau
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.biouno.unochoice.util;

import groovy.lang.GroovyShell;
import hudson.FilePath;
import hudson.Plugin;
import hudson.model.AbstractProject;
import hudson.model.Hudson;
import hudson.model.Label;
import hudson.model.Node;
import hudson.model.ParameterDefinition;
import hudson.model.ParametersDefinitionProperty;
import hudson.remoting.VirtualChannel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.ObjectUtils;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.jenkinsci.plugins.scriptler.config.Script;
import org.jenkinsci.plugins.scriptler.config.ScriptlerConfiguration;

//import com.seitenbau.jenkins.plugins.dynamicparameter.BaseParameterDefinition;
import jenkins.model.Jenkins;
import org.biouno.unochoice.AbstractUnoChoiceParameter;

/**
 * Jenkins utility methods.
 */
public final class JenkinsUtils
{
  /** Logger. */
  private static final Logger logger = Logger.getLogger(JenkinsUtils.class.getName());
  

  /** Private constructor. */
  private JenkinsUtils()
  {
  }

  /**
   * Execute the script locally.
   * @param script script to execute
   * @return result from the script
   */
  public static Object execute(String script)
  {
    Object evaluate = execute(script, Collections.<String, String> emptyMap());
    return evaluate;
  }

  /**
   * Execute the script locally using the given parameters.
   * @param script script to execute
   * @param parameters parameters
   * @return result from the script
   */
  public static Object execute(String script, Map<String, String> parameters)
  {
	  FilePath[] emptyClassPaths = new FilePath[]{};
	  return execute(script, parameters, emptyClassPaths);
  }

  /**
   * Execute the script locally with the given parameters and using the given class path.
   * @param script script to execute
   * @param parameters parameters
   * @param classPaths class paths
   * @return result from the script
   */
  public static Object execute(String script, Map<String, String> parameters, FilePath[] classPaths)
  {
    try
    {
      CompilerConfiguration config = new CompilerConfiguration();
      
      // set class path
      ArrayList<String> classPathList = new ArrayList<String>(classPaths.length);
      try 
      {
          for (FilePath path : classPaths)
          {
            String classPathString = path.absolutize().toURI().toURL().getPath();
            classPathList.add(classPathString);
            FilePath[] jars = path.list("*.jar");
            for (FilePath jar : jars) {
              String jarClassPathString = jar.absolutize().toURI().toURL().getPath();
              classPathList.add(jarClassPathString);
            }
          }
      } 
      catch(Exception exp) 
      {
          logger.log(Level.INFO, "Cannot access path", exp);
      }
      config.setClasspathList(classPathList);
      GroovyShell groovyShell = new GroovyShell(config);
      
      for (Entry<String, String> parameter : parameters.entrySet())
      {
        groovyShell.setVariable(parameter.getKey(), parameter.getValue());
      }

      // execute script
      Object evaluate = groovyShell.evaluate(script);

      return evaluate;
    }
    catch (Exception e)
    {
      logger.log(Level.SEVERE, "Cannot access class path", e);
      return null;
    }
  }

  /**
   * Check if a plugin is available.
   * @param shortName plugin short name
   * @return {@code true} if plugin is installed
   */
  public static boolean isPluginAvailable(String shortName)
  {
    Plugin plugin = Hudson.getInstance().getPlugin(shortName);
    if(plugin == null)
    {
      return false;
    }
    return true;
  }

  /**
   * Check if the Scriptler plugin is available.
   * @return {@code true} if Scriptler is installed
   */
  public static boolean isScriptlerAvailable()
  {
    return JenkinsUtils.isPluginAvailable("scriptler");
  }

  /**
   * Get all Scriptler scripts.
   * @return a set of Scriptler scripts
   */
  public static Set<Script> getAllScriptlerScripts()
  {
    Set<Script> scripts = ScriptlerConfiguration.getConfiguration().getScripts();
    return scripts;
  }

  /**
   * Find an active node channel for the label of the current project.
   * @param label label whose nodes to check
   * @return active node channel or {@code null} if none found
   */
  public static VirtualChannel findActiveChannel(Label label)
  {
    Iterator<Node> iterator = label.getNodes().iterator();
    while (iterator.hasNext())
    {
      final VirtualChannel channel = iterator.next().getChannel();
      if (channel != null)
      {
        return channel;
      }
    }
    return null;
  }

  /**
   * Find the label assigned to the current project.
   * @return {@code null} if the label of the current project cannot be found
   */
  @SuppressWarnings("rawtypes")
  public static Label findProjectLabel(UUID parameterUUID)
  {
	AbstractProject project = findCurrentProject(parameterUUID);
    if (project != null)
    {
       return project.getAssignedLabel();
    }
    return null;
  }

  /**
   * Find the current project.
   * @return {@code null} if the current project cannot be found
   */
  @SuppressWarnings("rawtypes")
  public static AbstractProject findCurrentProject(UUID parameterUUID)
  {
    Jenkins instance = Jenkins.getInstance();
    if (instance != null)
    {
      List<AbstractProject> projects = instance.getAllItems(AbstractProject.class);
      for (AbstractProject project : projects)
      {
        if (isParameterDefintionOf(parameterUUID, project))
        {
          return project;
        }
      }
    }
    return null;
  }
  

  /**
   * Returns true if this parameter definition is a definition of the given project.
   * @param parameterUUID UUID of the project parameter
   * @param project the project to search for this parameter definition.
   * @return {@code true} if the project contains this parameter definition.
   */
  @SuppressWarnings("rawtypes")
  private static boolean isParameterDefintionOf(UUID parameterUUID, AbstractProject project)
  {
    List<ParameterDefinition> parameterDefinitions = getProjectParameterDefinitions(project);
    for (ParameterDefinition pd : parameterDefinitions)
    {
      if (pd instanceof AbstractUnoChoiceParameter)
      {
        AbstractUnoChoiceParameter parameterDefinition = (AbstractUnoChoiceParameter) pd;
        UUID uuid = parameterDefinition.getUUID();
        if (ObjectUtils.equals(parameterUUID, uuid))
        {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Get the parameter definitions for the given project.
   * @param project the project for which the parameter definitions should be found
   * @return parameter definitions or an empty list
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public static List<ParameterDefinition> getProjectParameterDefinitions(AbstractProject project)
  {
    ParametersDefinitionProperty parametersDefinitionProperty =
        (ParametersDefinitionProperty) project.getProperty(ParametersDefinitionProperty.class);
    if (parametersDefinitionProperty != null)
    {
      List<ParameterDefinition> parameterDefinitions = parametersDefinitionProperty
          .getParameterDefinitions();
      if (parameterDefinitions != null)
      {
        return parameterDefinitions;
      }
    }
    return Collections.EMPTY_LIST;
  }

  public static AbstractProject findProjectFromName(String projectDisplayName)
  {
    Jenkins instance = Jenkins.getInstance();
    if (instance != null)
    {
      List<AbstractProject> projects = instance.getAllItems(AbstractProject.class);
      for (AbstractProject project : projects)
      {
        if (project.getDisplayName().equals(projectDisplayName))
        {
          return project;
        }
      }
    }
    return null;
  }
  
}