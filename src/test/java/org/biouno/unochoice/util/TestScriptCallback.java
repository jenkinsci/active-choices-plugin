/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2016 Ioannis Moutsatsos, Bruno P. Kinoshita
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

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.biouno.unochoice.model.GroovyScript;
import org.biouno.unochoice.model.ScriptlerScriptParameter;
import org.jenkinsci.plugins.scriptsecurity.sandbox.groovy.SecureGroovyScript;
import org.jenkinsci.plugins.scriptsecurity.scripts.ScriptApproval;
import org.jenkinsci.plugins.scriptsecurity.scripts.languages.GroovyLanguage;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

/**
 * Test the {@link ScriptCallback} bean.
 */
public class TestScriptCallback {

    private final static String SCRIPT = "return ['a', 'b']";
    private final static String FALLBACK_SCRIPT = "return ['EMPTY!']";

    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Before
    public void setUp() {
        ScriptApproval.get().preapprove(SCRIPT, GroovyLanguage.get());
        ScriptApproval.get().preapprove(FALLBACK_SCRIPT, GroovyLanguage.get());
    }

    @Test
    public void testScriptCallback() {
        List<ScriptlerScriptParameter> params = new ArrayList<ScriptlerScriptParameter>();
        params.add(new ScriptlerScriptParameter("name1", "value1"));
        params.add(new ScriptlerScriptParameter("name2", "value2"));
        GroovyScript script = new GroovyScript(new SecureGroovyScript(SCRIPT, Boolean.FALSE, null),
                new SecureGroovyScript(FALLBACK_SCRIPT, Boolean.FALSE, null));
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("flag", "true");
        ScriptCallback<Exception> sc = new ScriptCallback<Exception>("callback1", script, parameters);

        assertEquals("callback1", sc.getName());
        assertEquals(sc.getScript(), script);
    }

}
