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

import hudson.Extension;
import hudson.Util;
import hudson.model.AbstractProject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.biouno.unochoice.util.Utils;
import org.jenkinsci.plugins.scriptler.config.Script;
import org.jenkinsci.plugins.scriptler.util.ScriptHelper;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * A scriptler script.
 *
 * @author Bruno P. Kinoshita
 * @since 0.1
 */
public class ScriptlerScript extends AbstractScript {

    /*
     * Serial UID.
     */
    private static final long serialVersionUID = -4833367326600112145L;

    private final String scriptlerScriptId;

    private final Map<String, String> parameters;

    @DataBoundConstructor
    public ScriptlerScript(String scriptlerScriptId, List<ScriptlerScriptParameter> parameters) {
        super();
        this.scriptlerScriptId = scriptlerScriptId;
        this.parameters = new HashMap<String, String>();
        if (parameters != null) {
            for (ScriptlerScriptParameter parameter : parameters) {
                this.parameters.put(parameter.getName(), parameter.getValue());
            }
        }
    }

    /**
     * @return the scriptlerScriptId
     */
    public String getScriptlerScriptId() {
        return scriptlerScriptId;
    }

    /**
     * @return the parameters
     */
    public Map<String, String> getParameters() {
        return parameters;
    }

    public Object eval(AbstractProject project) {
        return eval(project, null);
    }

    /*
     * (non-Javadoc)
     * @see org.biouno.unochoice.model.Script#eval(java.util.Map)
     */
    public Object eval(AbstractProject project, Map<String, String> parameters) {
        final Map<String, String> envVars = Utils.getSystemEnv();
        Map<String, String> evaledParameters = new HashMap<String, String>(envVars);
        // if we have any parameter that came from UI, let's eval and use them
        if (parameters != null && parameters.size() > 0) {
            // fill our map with the given parameters
            evaledParameters.putAll(parameters);
            // and now try to expand env vars
            for (String key : this.getParameters().keySet()) {
                String value = this.getParameters().get(key);
                value = Util.replaceMacro((String) value, parameters);
                evaledParameters.put(key, value);
            }
        } else {
            evaledParameters.putAll(this.getParameters());
        }
        return this.toGroovyScript().eval(project,evaledParameters);
    }

    // --- utility methods for conversion

    /**
     * Converts this scriptler script to a GroovyScript.
     *
     * @return a GroovyScript
     */
    public GroovyScript toGroovyScript() {
        final Script scriptler   = ScriptHelper.getScript(getScriptlerScriptId(), true);
        return new GroovyScript(scriptler.script, null);
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
            return "Scriptler Script"; 
        }

    }

}
