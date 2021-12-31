/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2021 Ioannis Moutsatsos, Bruno P. Kinoshita
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

import {beforeEach, describe, it} from 'mocha';
import {expect} from 'chai';
import {initializeTestDom} from "./test-utils";
import $ from "jquery";
import {CascadeParameter} from "./CascadeParameter";
import {ReferencedParameter} from "./ReferencedParameter";
import sinon from 'sinon';

describe('ReferencedParameter', () => {
  let ref: ReferencedParameter
  let cascade: CascadeParameter
  beforeEach(() => {
    initializeTestDom()
    cascade = sinon.createStubInstance(CascadeParameter) as unknown as CascadeParameter
    ref = new ReferencedParameter('some-name', $('<div></div>'), cascade)
  })
  it('creates a new ReferencedParameter', () => {
    expect(ref.paramName).to.equal('some-name')
    expect(ref.$element.get(0)?.tagName).to.equal('DIV')
  })
  it('does not implement other methods', () => {
    expect(() => ref.getParameterValue(null as unknown as JQuery)).to.throw(Error)
    expect(() => ref.getElementValue(null as unknown as JQuery)).to.throw(Error)
    expect(() => ref.getSelectValues(null as unknown as JQuery<HTMLSelectElement>)).to.throw(Error)
  })
  it('listens for changes but ignores when self', () => {
    ref.$element.trigger('change', { parameterName: ref.paramName })
    // @ts-ignore
    expect(cascade.update.calledOnce).to.equal(false)
  })
  it('listens for changes', async () => {
    const clock = sinon.useFakeTimers()
    ref.$element.trigger('change', { parameterName: 'anything' })
    clock.tick(2000)
    await Promise.resolve()
    // @ts-ignore
    expect(cascade.update.calledOnce).to.equal(true)
    clock.restore()
  })
})
