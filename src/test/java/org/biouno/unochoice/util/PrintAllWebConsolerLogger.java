/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2025 Ioannis Moutsatsos, Bruno P. Kinoshita
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

import org.htmlunit.WebConsole;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A {@link WebConsole} that saves everything logged to the JS console.
 *
 * @since 2.8.7
 */
public class PrintAllWebConsolerLogger implements WebConsole.Logger {

    private final List<String> messages;

    public PrintAllWebConsolerLogger() {
        messages = new ArrayList<String>();
    }

    public List<String> getMessages() {
        return Collections.unmodifiableList(messages);
    }

    @Override
    public boolean isErrorEnabled() {
        return true;
    }

    @Override
    public boolean isTraceEnabled() {
        return true;
    }

    @Override
    public boolean isInfoEnabled() {
        return true;
    }

    @Override
    public boolean isDebugEnabled() {
        return true;
    }

    @Override
    public boolean isWarnEnabled() {
        return true;
    }

    @Override
    public void info(final Object message) {
        messages.add("info: " + message);
    }

    @Override
    public void error(final Object message) {
        messages.add("error: " + message);
    }

    @Override
    public void debug(final Object message) {
        messages.add("debug: " + message);
    }

    @Override
    public void warn(final Object message) {
        messages.add("warn: " + message);
    }

    @Override
    public void trace(final Object message) {
        messages.add("trace: " + message);
    }
}
