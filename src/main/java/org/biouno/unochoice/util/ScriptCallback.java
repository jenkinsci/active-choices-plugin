/*
 * The MIT License (MIT)
 *
 * Copyright (c) <2014-2015> Ioannis K. Moutsatsos
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.biouno.unochoice.util;

import hudson.remoting.Callable;
import jenkins.security.Roles;

import java.util.Map;

import org.biouno.unochoice.model.Script;
import org.jenkinsci.remoting.Role;
import org.jenkinsci.remoting.RoleChecker;
import org.jenkinsci.remoting.RoleSensitive;

/**
 * A callable (Jenkins remoting API) object that executes the script locally (when executed in the master)
 * or remotely. 
 *
 * @author dynamic-parameter-plugin
 * @author Bruno P. Kinoshita
 * @since 0.1
 */
public class ScriptCallback<T extends Throwable> implements Callable<Object, T> {

    private static final long serialVersionUID = 4524316203276099968L;

    private final String name;
    private final Script script;
    // Map is not serializable, but LinkedHashMap is. Ignore static analysis errors
    private Map<String, String> parameters;

    /**
     * Create a new ScriptCallback. This can be used to execute code either local or
     * remotely.
     * @param name callable name
     * @param script script
     * @param parameters Map of parameters
     */
    public ScriptCallback(String name, Script script, Map<String, String> parameters) {
        this.name = name;
        this.script = script;
        this.parameters = parameters;
    }

    /**
     * Get script callback name.
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Get script parameters. Used to populate bound variables.
     * @return Map with parameters
     */
    public Map<String, String> getParameters() {
        return parameters;
    }

    /**
     * Get the script.
     * @return Script script
     */
    public Script getScript() {
        return script;
    }

    /*
     * (non-Javadoc)
     * @see hudson.remoting.Callable#call()
     */
    @Override
    public Object call() throws T {
        final Object eval = script.eval(getParameters());
        return eval;
    }

    /*
     * (non-Javadoc)
     * @see org.jenkinsci.remoting.RoleSensitive#checkRoles(org.jenkinsci.remoting.RoleChecker)
     */
    @Override
    public void checkRoles(RoleChecker roleChecker) throws SecurityException {
        // normally parameters will be executed on the master, but in the
        // future we may start evaluating on slaves as well, with a custom
        // classpath as in dynamic-parameter-plugin
        roleChecker.check((RoleSensitive) this, Role.UNKNOWN);
    }

}
