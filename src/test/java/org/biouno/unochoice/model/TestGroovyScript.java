/*
 * The MIT License (MIT)
 * 
 * Copyright (c) <2014> <Ioannis Moutsatsos, Bruno P. Kinoshita>
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jenkinsci.plugins.scriptler.util.ScriptHelper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ScriptHelper.class})
public class TestGroovyScript {
	
	@Test
	public void testConstructors() {
		GroovyScript script = new GroovyScript("return ['a', 'b']", "return ['EMPTY!']");
		
		assertEquals("return ['a', 'b']", script.getScript());
		assertEquals("return ['EMPTY!']", script.getFallbackScript());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testEvals() {
		GroovyScript script = new GroovyScript("return [flag, 'b']", "return ['EMPTY!']");
		
		List<String> returnValue = (List<String>) script.eval(null);
		assertEquals(Arrays.asList(new String[]{"EMPTY!"}), returnValue);
		
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("flag", "true");
		
		returnValue = (List<String>) script.eval(null,parameters);
		assertEquals(Arrays.asList(new String[]{"true", "b"}), returnValue);
	}
	
	@Test
	public void testEquals() {
		GroovyScript script1 = new GroovyScript("return [flag, 'b']", "return ['EMPTY!']");
		GroovyScript script2 = new GroovyScript("return [flag, 'b']", "return ['EMPTY!']");
		assertEquals(script1, script2);
		
		GroovyScript script3 = new GroovyScript("return [flag, 'c', 'd']", "return ['EMPTY!']");
		assertNotEquals(script1, script3);
	}
	
}
