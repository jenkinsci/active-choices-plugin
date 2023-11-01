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

package org.biouno.unochoice.model;

import java.util.*;
import java.util.stream.Collectors;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.biouno.unochoice.util.Utils;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.scriptler.ScriptlerManagement;
import org.jenkinsci.plugins.scriptler.builder.ScriptlerBuilder;
import org.jenkinsci.plugins.scriptler.config.Parameter;
import org.jenkinsci.plugins.scriptler.config.Script;
import org.jenkinsci.plugins.scriptler.util.ScriptHelper;
import org.jenkinsci.plugins.scriptsecurity.sandbox.groovy.SecureGroovyScript;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.bind.JavaScriptMethod;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Extension;
import hudson.Util;
import hudson.model.ManagementLink;
import jenkins.model.Jenkins;
import net.sf.json.JSONArray;

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
    private static final long serialVersionUID = 6600927513119226354L;

    /**
     * The ID of the Scriptler script.
     * @deprecated Not used now that we use the {@code ScriptlerBuilder}.
     */
    @Deprecated
    private String scriptlerScriptId;
    /**
     * Map is not serializable, but LinkedHashMap is. Ignore static analysis errors
     * @deprecated Not used now that we use the {@code ScriptlerBuilder}.
     */
    @Deprecated
    private Map<String, String> parameters;

    private ScriptlerBuilder scriptlerBuilder;

    /**
     * Whether this scriptler script will run in the Groovy sandbox or not.
     */
    private final Boolean isSandboxed;

    /**
     * @param scriptlerBuilder Scriptler builder
     * @param isSandboxed whether this script must be sandboxed or not
     */
    @DataBoundConstructor
    public ScriptlerScript(
            ScriptlerBuilder scriptlerBuilder,
            Boolean isSandboxed) {
        super();
        this.scriptlerBuilder = scriptlerBuilder;
        this.isSandboxed = isSandboxed != null ? isSandboxed : Boolean.TRUE;
    }

    public Object readResolve() {
        if (scriptlerBuilder == null) {
            scriptlerBuilder = new ScriptlerBuilder(
                    "active-choices",
                    this.scriptlerScriptId,
                    false,
                    Arrays.asList(getParametersFromDeprecatedMap())
                    );
        }
        return this;
    }

    private Parameter[] getParametersFromDeprecatedMap() {
        if (parameters == null) {
            return new Parameter[0];
        }

        return parameters
                .entrySet()
                .stream()
                .map(entry -> new Parameter(entry.getKey(), entry.getValue()))
                .toArray(Parameter[]::new);
    }

    private void initializeFromDeprecatedProperties() {
        if (scriptlerBuilder == null) {
            readResolve();
        } else {
            String scriptId = scriptlerScriptId == null ? scriptlerBuilder.getScriptId() : scriptlerScriptId;
            Parameter[] parameters = (this.parameters == null || this.parameters.isEmpty())
                    ? scriptlerBuilder.getParametersList().toArray(new Parameter[0])
                    : getParametersFromDeprecatedMap();
            scriptlerBuilder = new ScriptlerBuilder(
                    scriptlerBuilder.getBuilderId(),
                    scriptId,
                    scriptlerBuilder.isPropagateParams(),
                    Arrays.asList(parameters)
            );
        }
    }

    /**
     * @return the Scriptler builder
     */
    public ScriptlerBuilder getScriptlerBuilder() {
        return this.scriptlerBuilder;
    }

    /**
     * @return the scriptler script ID
     */
    public String getScriptlerScriptId() {
        return this.scriptlerBuilder.getScriptId();
    }

    @DataBoundSetter
    public void setScriptlerScriptId(String scriptlerScriptId) {
        this.scriptlerScriptId = scriptlerScriptId;
        initializeFromDeprecatedProperties();
    }

    /**
     * @return the parameters
     */
    public Map<String, String> getParameters() {
        return Arrays.stream(this.scriptlerBuilder.getParameters())
                .collect(Collectors.toMap(Parameter::getName, Parameter::getValue));
    }

    @DataBoundSetter
    public void setParameters(List<Map<String, String>> parametersList) {
        if (parametersList == null) {
            parameters = Collections.emptyMap();
        } else {
            parameters = new LinkedHashMap<>(parametersList
                    .stream()
                    .filter(Objects::nonNull)
                    .filter(map -> map.containsKey("name") && map.containsKey("value"))
                    .collect(Collectors.toMap(map -> map.get("name"), map -> map.get("value"))));
        }
        initializeFromDeprecatedProperties();
    }

    /**
     * @return the sandbox flag
     */
    public Boolean getIsSandboxed() {
        return isSandboxed;
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
        Map<String, String> evaledParameters = new LinkedHashMap<>(envVars);
        // if we have any parameter that came from UI, let's eval and use them
        if (parameters != null && !parameters.isEmpty()) {
            // fill our map with the given parameters
            evaledParameters.putAll(parameters);
            // and now try to expand env vars
            for (String key : this.getParameters().keySet()) {
                String value = this.getParameters().get(key);
                value = Util.replaceMacro(value, parameters);
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
     * The script will run in the Groovy Sandbox environment by default, unless approved by a
     * Jenkins administrator. In this case it won't use the Groovy Sandbox. This is useful if
     * the Groovy script needs access to API not available in the Sandbox (e.g. Grapes).
     *
     * @return a GroovyScript
     */
    public GroovyScript toGroovyScript() {
        final Script scriptler = ScriptHelper.getScript(getScriptlerScriptId(), true);
        if (scriptler == null) {
            throw new RuntimeException("Missing required scriptler!");
        }
        return new GroovyScript(new SecureGroovyScript(scriptler.script, this.isSandboxed, null), null);
    }

    // --- descriptor

    @Extension(optional = true)
    @Symbol({"scriptlerScript"})
    public static class DescriptorImpl extends ScriptDescriptor {
        static {
            // make sure this class fails to load during extension discovery if scriptler isn't present
            ScriptlerManagement.getScriptlerHomeDirectory();
        }
        /*
         * (non-Javadoc)
         *
         * @see hudson.model.Descriptor#getDisplayName()
         */
        @Override
        @NonNull
        public String getDisplayName() {
            return "Scriptler Script";
        }

        @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
        private ManagementLink getScriptler() {
            return Jenkins.get().getExtensionList(ScriptlerManagement.class).get(0);
        }

        /**
         * gets the argument description to be displayed on the screen when selecting a config in the dropdown
         *
         * @param scriptlerScriptId
         *            the config id to get the arguments description for
         * @return the description
         */
        @JavaScriptMethod
        public JSONArray getParameters(String scriptlerScriptId) {
            final ManagementLink scriptler = this.getScriptler();
            if (scriptler != null) {
                ScriptlerManagement scriptlerManagement = (ScriptlerManagement) scriptler;
                final Script script = scriptlerManagement.getConfiguration().getScriptById(scriptlerScriptId);
                if (script != null) {
                    return JSONArray.fromObject(script.getParameters());
                }
            }
            return null;
        }
    }

}
