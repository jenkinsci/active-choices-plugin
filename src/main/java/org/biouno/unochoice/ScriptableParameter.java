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

import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.ParameterDefinition;
import java.util.List;
import java.util.Map;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.Ancestor;
import org.kohsuke.stapler.StaplerRequest;

/**
 * A parameter that is obtained through the execution of a script.
 *
 * @author Bruno P. Kinoshita
 * @since 0.20
 */
public interface ScriptableParameter<T> extends UnoChoiceParameter {

    /**
     * Evaluates a script and returns its result as a Map. List values are automatically handled and converted to
     * Maps too.
     *
     * @param parameters parameters
     * @return script result as Map
     */
    T getChoices(Map<Object, Object> parameters);
    
    
        // --- descriptor

    @Extension
    public static final class DescriptorImpl extends UnoChoiceParameterDescriptor {

        private AbstractProject<?, ?> project;

        /*
         * Used to store a reference to the Jenkins project related to this parameter.
         * A bit hacky, probably using another extension point would be a good idea.
         */
        @Override
        public ParameterDefinition newInstance(StaplerRequest req, JSONObject formData) throws hudson.model.Descriptor.FormException {
            List<Ancestor> ancestors = req.getAncestors();
            AbstractProject<?, ?> project = null;
            for (Ancestor ancestor : ancestors) {
                Object object = ancestor.getObject();
                if (object instanceof AbstractProject<?, ?>) {
                    project = (AbstractProject<?, ?>) object;
                    break;
                }
            }
            this.project = project;
            return super.newInstance(req, formData);
        }

        public AbstractProject<?, ?> getProject() {
            return project;
        }
    }    
}
