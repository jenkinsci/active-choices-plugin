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

package org.biouno.unochoice.model;

import antlr.Version;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import hudson.Extension;
import hudson.Util;
import hudson.model.AbstractProject;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import jenkins.model.Jenkins;
import org.biouno.unochoice.util.JenkinsUtils;

import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;

/**
 * A Groovy script.
 *
 * @author Bruno P. Kinoshita
 * @since 0.23
 */
public class GroovyScript extends AbstractScript {

    /*
     * Serial UID.
     */
    private static final long serialVersionUID = -4886250205110550815L;

    private static final Logger LOGGER = Logger.getLogger(GroovyScript.class.getName());
    
    private static final String JENKINS_PROJECT_VARIABLE_NAME = "jenkinsProject";

    /**
     * Script content.
     */
    private final String script;

    @Nullable
    private final String fallbackScript;

    @DataBoundConstructor
    public GroovyScript(String script, String fallbackScript) {
        this.script = script;
        this.fallbackScript = fallbackScript;
    }

    /**
     * @return the script
     */
    public String getScript() {
        return script;
    }

    /**
     * @return the fallbackScript
     */
    public String getFallbackScript() {
        return fallbackScript;
    }

    /*
     * (non-Javadoc)
     * @see org.biouno.unochoice.model.Script#eval()
     */
    public Object eval(AbstractProject project) {
        return eval(project, Collections.<String, String>emptyMap());
    }

    /*
     * (non-Javadoc)
     * @see org.biouno.unochoice.model.Script#eval(java.util.Map)
     */
     public Object eval(AbstractProject project, Map<String, String> parameters) throws RuntimeException {
        ClassLoader cl = null;
        try {
            cl = Jenkins.getInstance().getPluginManager().uberClassLoader;
        } catch (Exception e) {
            LOGGER.finest(e.getMessage());
        }
        if (cl == null) {
            cl = Thread.currentThread().getContextClassLoader();
        }
   
        final Binding context = new Binding();
       
        // @SuppressWarnings("unchecked")
        final Map<String, String> envVars = System.getenv();
        for (Entry<String, String> parameter : parameters.entrySet()) {
//            
//            if(parameter.getKey().equals(JENKINS_PROJECT_VARIABLE_NAME)){
//                String projectName = parameter.getValue();
//                project=JenkinsUtils.findProjectFromName(projectName);
//            }
//            
            Object value = parameter.getValue();
            if (value != null) {
                if (value instanceof String) {
                    value = Util.replaceMacro((String) value, envVars);
                }
                context.setVariable(parameter.getKey().toString(), value);
            }
        }
        if(project==null){
            StaplerRequest req = Stapler.getCurrentRequest();
            if (req!=null) {
                project = req.findAncestorObject(AbstractProject.class);
            }
        }
        context.setVariable(JENKINS_PROJECT_VARIABLE_NAME, project);

        final GroovyShell shell = new GroovyShell(cl, context, CompilerConfiguration.DEFAULT);
        try {
            return shell.evaluate(script);
        } catch (RuntimeException re) {
            if (this.fallbackScript != null) {
                try {
                    LOGGER.log(Level.FINEST, "Fallback to default script...", re);
                    return shell.evaluate(fallbackScript);
                } catch (CompilationFailedException cfe2) {
                    LOGGER.log(Level.WARNING, "Error executing fallback script", cfe2);
                    throw cfe2;
                }
            } else {
                LOGGER.log(Level.WARNING, "No fallback script configured for '%s'");
                throw re;
            }
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "GroovyScript [script=" + script + ", fallbackScript="
                + fallbackScript + "]";
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((fallbackScript == null) ? 0 : fallbackScript.hashCode());
        result = prime * result + ((script == null) ? 0 : script.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        GroovyScript other = (GroovyScript) obj;
        if (fallbackScript == null) {
            if (other.fallbackScript != null)
                return false;
        } else if (!fallbackScript.equals(other.fallbackScript))
            return false;
        if (script == null) {
            if (other.script != null)
                return false;
        } else if (!script.equals(other.script))
            return false;
        return true;
    }

    // --- descriptor

    @Extension
    public static class DescriptorImpl extends ScriptDescriptor {
        /*
         * (non-Javadoc)
         *
         * @see hudson.model.Descriptor#getDisplayName()
         */
        @Override
        public String getDisplayName() {
            return "Groovy Script"; 
        }
    }

}
