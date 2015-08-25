/*
 * The MIT License (MIT)
 * 
 * Copyright (c) 2014-2015 Ioannis Moutsatsos, Bruno P. Kinoshita
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
package org.biouno.unochoice.issue28785;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.biouno.unochoice.model.ScriptlerScript;
import org.biouno.unochoice.model.ScriptlerScriptParameter;
import org.jenkinsci.plugins.scriptler.config.Parameter;
import org.jenkinsci.plugins.scriptler.config.Script;
import org.jenkinsci.plugins.scriptler.util.ScriptHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ ScriptHelper.class })
public class TestIssue28785 {

    private List<ScriptlerScriptParameter> params = null;

    private Script script = null;
    
    private ScriptlerScript scriptlerScript = null;

    @Before
    public void setUp() {
        params = new ArrayList<ScriptlerScriptParameter>();

        script = new Script("id", "name", "comment", true, "originCatalog",
                "originScript", "originDate", true,
                (Parameter[]) params.toArray(new Parameter[0]), false);
        script.setScript("if(binding.variables.get('flag') == null) {return [1, 2, 3]} else {return [1000, 100, 1]}");

        scriptlerScript = new ScriptlerScript("id", params);
        
        // Mock
        PowerMockito.mockStatic(ScriptHelper.class);
        PowerMockito.when(
                ScriptHelper.getScript(Mockito.anyString(),
                        Mockito.anyBoolean())).thenReturn(this.script);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testEnvVarsExpanding() {
        Map<String, String> referencedParameters = new HashMap<String, String>();
        referencedParameters.put("flag", "any value...");
        List<Integer> returnValue = (List<Integer>) scriptlerScript.eval(null,referencedParameters);
        assertEquals(Arrays.asList(new Integer[] { 1000, 100, 1 }), returnValue);
    }

}
