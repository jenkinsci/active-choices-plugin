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

package org.biouno.unochoice;

import java.util.List;

/**
 * A parameter that monitors other referenced parameters. When any of these parameters change, 
 * this parameter gets updated. It has a map with the current parameters that can be retrieved
 * by the UI to render the most updated view, based on the state of the referenced parameters.
 *
 * @author Bruno P. Kinoshita
 * @since 0.20
 */
public interface CascadableParameter<T> extends ScriptableParameter<T> {

    /**
     * Gets the list of referenced parameters. If any of these parameters change in the
     * UI we will update our current parameters.
     *
     * @return the referencedParameters
     */
    public String getReferencedParameters();

    /**
     * Evaluates a script and returns its result as a Map. List values are automatically handled and converted to
     * Maps too.
     *
     * @return script result as Map
     */
    public List<Object> getChoicesForUI();

    /**
     * Evaluates a script and returns the visibility of this parameter.
     *
     * @return script result as boolean
     */
    public boolean isVisible();

    /**
     * Exposed to the UI. Is triggered everytime any of the referenced parameters gets updated.
     * @param parameters Comma separated list of parameters
     */
    public void doUpdate(String parameters);

}
