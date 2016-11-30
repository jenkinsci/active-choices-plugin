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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.biouno.unochoice.util.Utils;
import org.jenkinsci.plugins.scriptler.config.Script;
import org.jenkinsci.plugins.scriptler.util.ScriptHelper;
import org.jenkinsci.plugins.scriptsecurity.sandbox.groovy.SecureGroovyScript;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import hudson.Extension;
import hudson.Util;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

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
    private static final long serialVersionUID = -6600327523009436354L;

    private final String scriptlerScriptId;

    // Map is not serializable, but LinkedHashMap is. Ignore static analysis errors
    private final Map<String, String> parameters;

    @DataBoundConstructor
    public ScriptlerScript(String scriptlerScriptId, List<ScriptlerScriptParameter> parameters) {
        super();
        this.scriptlerScriptId = scriptlerScriptId;
        this.parameters = new LinkedHashMap<String, String>();
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

    @Override
    public Object eval() {
        return eval(null);
    }

    /*
     * (non-Javadoc)
     * @see org.biouno.unochoice.model.Script#eval(java.util.Map)
     */
    @Override
    public Object eval(Map<String, String> parameters) {
        final Map<String, String> envVars = Utils.getSystemEnv();
        Map<String, String> evaledParameters = new LinkedHashMap<String, String>(envVars);
        // if we have any parameter that came from UI, let's eval and use them
        if (parameters != null && !parameters.isEmpty()) {
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
        return this.toGroovyScript().eval(evaledParameters);
    }

    // --- utility methods for conversion

    /**
     * Converts this scriptler script to a GroovyScript.
     *
     * @return a GroovyScript
     */
    public GroovyScript toGroovyScript() {
        final Script scriptler = ScriptHelper.getScript(getScriptlerScriptId(), true);
        return new GroovyScript(new SecureGroovyScript(scriptler.script, false, null), null);
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

        @Override
        public AbstractScript newInstance(StaplerRequest req, JSONObject jsonObject) throws FormException {
            ScriptlerScript script = null;
            String scriptScriptId = jsonObject.getString("scriptlerScriptId");
            if (scriptScriptId != null && !scriptScriptId.trim().equals("")) {
                List<ScriptlerScriptParameter> parameters = new ArrayList<ScriptlerScriptParameter>();

                final JSONObject defineParams = jsonObject.getJSONObject("defineParams");
                if (defineParams != null && !defineParams.isNullObject()) {
                    JSONObject argsObj = defineParams.optJSONObject("parameters");
                    if (argsObj == null) {
                        JSONArray argsArrayObj = defineParams.optJSONArray("parameters");
                        if (argsArrayObj != null) {
                            for (int i = 0; i < argsArrayObj.size(); i++) {
                                JSONObject obj = argsArrayObj.getJSONObject(i);
                                String name = obj.getString("name");
                                String value = obj.getString("value");
                                if (name != null && !name.trim().equals("") && value != null) {
                                    ScriptlerScriptParameter param = new ScriptlerScriptParameter(name, value);
                                    parameters.add(param);
                                }
                            }
                        }
                    } else {
                        String name = argsObj.getString("name");
                        String value = argsObj.getString("value");
                        if (name != null && !name.trim().equals("") && value != null) {
                            ScriptlerScriptParameter param = new ScriptlerScriptParameter(name, value);
                            parameters.add(param);
                        }
                    }
                }
                script = new ScriptlerScript(scriptScriptId, parameters);
            }
            return script;
        }
    }

}
