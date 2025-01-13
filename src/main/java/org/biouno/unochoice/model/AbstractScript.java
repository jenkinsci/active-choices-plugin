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

import hudson.model.Descriptor;
import jenkins.model.Jenkins;

/**
 * Abstract script.
 *
 * @author Bruno P. Kinoshita
 * @since 0.23
 */
public abstract class AbstractScript implements Script {

    /*
     * Serial UID.
     */
    private static final long serialVersionUID = 4027103576278802323L;

    // TODO could be pulled up into Script (default method);
    // in fact this intermediate type could probably be deleted
    // (assuming there is no Java serialization outside of Remoting,
    // since such a change would break the stream class description;
    // XStream should not care about that)
    @Override
    @SuppressWarnings("unchecked")
    public Descriptor<Script> getDescriptor() {
        final Jenkins instance = Jenkins.getInstanceOrNull();
        Descriptor<Script> descriptor = null;
        if (instance != null) {
            descriptor = instance.getDescriptor(getClass());
        }
        return descriptor;
    }

}
