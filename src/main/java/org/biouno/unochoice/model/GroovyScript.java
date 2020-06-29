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

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nullable;

import hudson.markup.EscapedMarkupFormatter;
import hudson.markup.MarkupFormatter;
import hudson.markup.RawHtmlMarkupFormatter;
import org.jenkinsci.plugins.scriptsecurity.sandbox.groovy.SecureGroovyScript;
import org.jenkinsci.plugins.scriptsecurity.scripts.ApprovalContext;
import org.kohsuke.stapler.DataBoundConstructor;

import groovy.lang.Binding;
import hudson.Extension;
import hudson.PluginManager;
import hudson.Util;
import jenkins.model.Jenkins;

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
    private static final long serialVersionUID = -3741105849416473898L;

    private static final Logger LOGGER = Logger.getLogger(GroovyScript.class.getName());

    /**
     * Script content.
     */
    @Deprecated
    private transient String script;

    /**
     * Secure script content.
     */
    private SecureGroovyScript secureScript;

    @Nullable
    @Deprecated
    private transient String fallbackScript;

    /**
     * Secure fallback script content.
     */
    @Nullable
    private SecureGroovyScript secureFallbackScript;

    @Deprecated
    public GroovyScript(String script, String fallbackScript) {
        this(new SecureGroovyScript(script, false, null), new SecureGroovyScript(fallbackScript, false, null));
    }

    @DataBoundConstructor
    public GroovyScript(SecureGroovyScript script, SecureGroovyScript fallbackScript) {
        if (script != null) {
            this.secureScript = script.configuringWithNonKeyItem();
        }
        if (fallbackScript != null) {
            this.secureFallbackScript = fallbackScript.configuringWithNonKeyItem();
        }
    }

    private Object readResolve() {
        if (script != null) {
            secureScript = new SecureGroovyScript(script, false, null).configuring(ApprovalContext.create());
        }
        if (fallbackScript != null) {
            secureFallbackScript = new SecureGroovyScript(fallbackScript, false, null)
                    .configuring(ApprovalContext.create());
        }
        return this;
    }

    /**
     * @return the script
     */
    public SecureGroovyScript getScript() {
        return secureScript;
    }

    /**
     * @return the fallbackScript
     */
    public SecureGroovyScript getFallbackScript() {
        return secureFallbackScript;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.biouno.unochoice.model.Script#eval()
     */
    @Override
    public Object eval() {
        return eval(Collections.<String, String> emptyMap());
    }

    /*
     * (non-Javadoc)
     *
     * @see org.biouno.unochoice.model.Script#eval(java.util.Map)
     */
    @Override
    public Object eval(Map<String, String> parameters) throws RuntimeException {
        if (secureScript == null) {
            return null;
        }
        final Jenkins instance = Jenkins.getInstance();
        ClassLoader cl = null;
        if (instance != null) {
            try {
                PluginManager pluginManager = instance.getPluginManager();
                cl = pluginManager.uberClassLoader;
            } catch (Exception e) {
                LOGGER.log(Level.FINEST, e.getMessage(), e);
            }
        }
        if (cl == null) {
            cl = Thread.currentThread().getContextClassLoader();
        }

        final Binding context = new Binding();

        // @SuppressWarnings("unchecked")
        final Map<String, String> envVars = System.getenv();
        for (Entry<String, String> parameter : parameters.entrySet()) {
            Object value = parameter.getValue();
            if (value != null) {
                if (value instanceof String) {
                    value = Util.replaceMacro((String) value, envVars);
                }
                context.setVariable(parameter.getKey().toString(), value);
            }
        }

        try {
            Object returnValue = secureScript.evaluate(cl, context);
            if (returnValue instanceof CharSequence) {
                if (secureScript.isSandbox()) {
                    return determineMarkupFormatter().translate(returnValue.toString());
                }
            }
            return returnValue;
        } catch (Exception re) {
            if (this.secureFallbackScript != null) {
                try {
                    LOGGER.log(Level.FINEST, "Fallback to default script...", re);
                    Object returnValue = secureFallbackScript.evaluate(cl, context);
                    if (returnValue instanceof CharSequence) {
                        if (secureFallbackScript.isSandbox()) {
                            return determineMarkupFormatter().translate(returnValue.toString());
                        }
                    }
                    return returnValue;
                } catch (Exception e2) {
                    LOGGER.log(Level.WARNING, "Error executing fallback script", e2);
                    throw new RuntimeException("Failed to evaluate fallback script: " + e2.getMessage(), e2);
                }
            } else {
                LOGGER.log(Level.WARNING, "No fallback script configured for '%s'");
                throw new RuntimeException("Failed to evaluate script: " + re.getMessage(), re);
            }
        }
    }

    private MarkupFormatter determineMarkupFormatter() {
        MarkupFormatter formatter = Jenkins.get().getMarkupFormatter();
        if(formatter instanceof EscapedMarkupFormatter) {
            // EscapedMarkupFormatter renders the markup as text, which we ostensibly never want to happen in this plugin.
            // Returning RawHtmlMarkupFormatter.INSTANCE preserves legacy behavior unless a different markup formatter is selected.
            return RawHtmlMarkupFormatter.INSTANCE;
        } else {
            return formatter;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        final String secureScriptText = (secureScript != null) ? secureScript.getScript() : "";
        final String fallbackScriptText = (secureFallbackScript != null) ? secureFallbackScript.getScript() : "";
        return "GroovyScript [script=" + secureScriptText + ", fallbackScript=" + fallbackScriptText + "]";
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((secureFallbackScript == null) ? 0 : secureFallbackScript.hashCode());
        result = prime * result + ((secureScript == null) ? 0 : secureScript.hashCode());
        return result;
    }

    /*
     * (non-Javadoc)
     *
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
        if (secureFallbackScript == null) {
            if (other.secureFallbackScript != null)
                return false;
        } else if (!secureFallbackScript.equals(other.secureFallbackScript))
            return false;
        if (secureScript == null) {
            return other.secureScript == null;
        }
        return secureScript.equals(other.secureScript);
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
