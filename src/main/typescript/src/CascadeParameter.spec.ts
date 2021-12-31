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
import type {JenkinsProxy, ProxyAjaxCallback} from "./Proxy";
import {UnoChoice} from "./UnoChoice";

describe('CascadeParameter', () => {
  let cascadeParameter: CascadeParameter
  let element: JQuery<HTMLElement>
  let requestedValue = ''
  let proxy: JenkinsProxy
  beforeEach(() => {
    initializeTestDom()
    element = $(`
      <div id='parameter1'>
        <div>
          <option name="value" selected="selected" value="xyz" />
        </div>
      </div>
      `)
    proxy = {
      doUpdate: (t: string): void => {
        requestedValue = t
      },
      getChoicesForUI: (t: ProxyAjaxCallback): void => {
        const left = requestedValue.split('=')[0]
        const right = requestedValue.split('=')[1]
        const updatedChoices = [
          [right.toUpperCase()],
          [left]
        ]
        t({
          responseText: JSON.stringify(updatedChoices)
        } as XMLHttpRequest)
      },
      getChoicesAsStringForUI: (t: ProxyAjaxCallback): void => {
        // pass
      }
    } as JenkinsProxy
    cascadeParameter = new CascadeParameter(
      'parameter1',
      element,
      'randomName1',
      proxy
    )
  })
  afterEach(() => {
    UnoChoice.cascadeParameters.length = 0
  })
  it('creates a new parameter', () => {
    expect(cascadeParameter.paramName).to.equal('parameter1')
    expect(cascadeParameter.$element).to.equal(element)
  })
  it('creates the referenced parameters as text (option)', () => {
    const referencedParameterElement = $("<option>", {
      "name": "value",
      "selected": "selected",
      "value": "123"
    })
    // tslint:disable-next-line:no-unused-expression
    new ReferencedParameter('ref1', referencedParameterElement, cascadeParameter)
    const text = cascadeParameter.getReferencedParametersAsText()
    expect(text).to.equal("ref1=123")
  })
  it('creates the referenced parameters as text (div + checkbox)', () => {
    const referencedParameterElement = $(`
    <div>
      <input type="checkbox" name="value" value="123" checked="checked" />
    </div>
    `)
    // tslint:disable-next-line:no-unused-expression
    new ReferencedParameter('ref1', referencedParameterElement, cascadeParameter)
    const text = cascadeParameter.getReferencedParametersAsText()
    expect(text).to.equal("ref1=123")
  })
  it('creates the referenced parameters as text (div + checkbox but not checked)', () => {
    const referencedParameterElement = $(`
    <div>
      <input type="checkbox" name="value" value="123" />
    </div>
    `)
    // tslint:disable-next-line:no-unused-expression
    new ReferencedParameter('ref1', referencedParameterElement, cascadeParameter)
    const text = cascadeParameter.getReferencedParametersAsText()
    expect(text).to.equal("ref1=")
  })
  it('creates the referenced parameters as text (div + missing value)', () => {
    const referencedParameterElement = $(`
    <div>
      <input type="checkbox" name="not-value" value="123" checked="checked" />
    </div>
    `)
    // tslint:disable-next-line:no-unused-expression
    new ReferencedParameter('ref1', referencedParameterElement, cascadeParameter)
    const text = cascadeParameter.getReferencedParametersAsText()
    expect(text).to.equal("ref1=")
  })
  it('creates the referenced parameters as text (unknown tag)', () => {
    const referencedParameterElement = $(`
    <ul>
      <li />
    </ul>
    `)
    // tslint:disable-next-line:no-unused-expression
    new ReferencedParameter('ref1', referencedParameterElement, cascadeParameter)
    const text = cascadeParameter.getReferencedParametersAsText()
    expect(text).to.equal("ref1=")
  })
  it('creates the referenced parameters as text (input)', () => {
    const referencedParameterElement = $(`
    <input name="test" value="123">
    `)
    // tslint:disable-next-line:no-unused-expression
    new ReferencedParameter('ref1', referencedParameterElement, cascadeParameter)
    const text = cascadeParameter.getReferencedParametersAsText()
    expect(text).to.equal("ref1=123")
  })
  it('creates the referenced parameters as text (input but name)', () => {
    const referencedParameterElement = $(`
    <input name="name" value="123">
    `)
    // tslint:disable-next-line:no-unused-expression
    new ReferencedParameter('ref1', referencedParameterElement, cascadeParameter)
    const text = cascadeParameter.getReferencedParametersAsText()
    expect(text).to.equal("ref1=")
  })
  it('creates the referenced parameters as text (select but nothing selected)', () => {
    const referencedParameterElement = $(`
    <div>
      <select name="value">
        <option value=""></option>
        <option value="1">1</option>
        <option value="2">2</option>
      </select>
    </div>
    `)
    // tslint:disable-next-line:no-unused-expression
    new ReferencedParameter('ref1', referencedParameterElement, cascadeParameter)
    const text = cascadeParameter.getReferencedParametersAsText()
    expect(text).to.equal("ref1=")
  })
  it('creates the referenced parameters as text (select one selected)', () => {
    const referencedParameterElement = $(`
    <div>
      <select name="value">
        <option value=""></option>
        <option value="1" selected="selected">1</option>
        <option value="2">2</option>
      </select>
    </div>
    `)
    // tslint:disable-next-line:no-unused-expression
    new ReferencedParameter('ref1', referencedParameterElement, cascadeParameter)
    const text = cascadeParameter.getReferencedParametersAsText()
    expect(text).to.equal("ref1=1")
  })
  it('creates the referenced parameters as text (select two selected)', () => {
    const referencedParameterElement = $(`
    <div>
      <select name="value" multiple>
        <option value=""></option>
        <option value="1" selected="selected">1</option>
        <option value="2" selected="selected">2</option>
      </select>
    </div>
    `)
    // tslint:disable-next-line:no-unused-expression
    new ReferencedParameter('ref1', referencedParameterElement, cascadeParameter)
    const text = cascadeParameter.getReferencedParametersAsText()
    expect(text).to.equal("ref1=1,2")
  })
  it('throws an error when getting parameter value for an unknown element', () => {
    expect(() => cascadeParameter.getParameterValue($('.a123456'))).to.throw(Error)
  })
  it('updates', () => {
    // We will use a parameter that returns some letters in lower case
    const referencedParameterElement = $("<option>", {
      "name": "value",
      "selected": "selected",
      "value": "abc"
    })
    const anotherCascadeParameter = new CascadeParameter(
      'ref1',
      referencedParameterElement,
      'randomName1',
      proxy
    )
    // tslint:disable-next-line:no-unused-expression
    new ReferencedParameter(anotherCascadeParameter.paramName, anotherCascadeParameter.$element, cascadeParameter)
    cascadeParameter.update()
    const elem = cascadeParameter.$element.get(0)
    // Now abc became ABC
    expect(elem?.innerHTML).to.include("ABC")
  })
  it('updates (circular dependency)', () => {
    // We will use a parameter that returns some letters in lower case
    const referencedParameterElement = $(`
      <div id='ref-parameter1'>
        <div>
          <option name="value" selected="selected" value="abc" />
        </div>
      </div>
      `)
    const anotherCascadeParameter = new CascadeParameter(
      'ref1',
      referencedParameterElement,
      'randomName1',
      proxy
    )
    // tslint:disable-next-line:no-unused-expression
    new ReferencedParameter(anotherCascadeParameter.paramName, anotherCascadeParameter.$element, cascadeParameter)
    // tslint:disable-next-line:no-unused-expression
    new ReferencedParameter(cascadeParameter.paramName, cascadeParameter.$element, anotherCascadeParameter)
    UnoChoice.cascadeParameters.push(cascadeParameter)
    UnoChoice.cascadeParameters.push(anotherCascadeParameter)
    cascadeParameter.update(false)
    // Now abc became ABC
    expect(element.get(0)?.innerHTML).to.include("ABC")
    expect(referencedParameterElement.get(0)?.innerHTML).to.include("ref1")
  })
  it('updates (unknown element)', () => {
    const anotherCascadeParameter = new CascadeParameter(
      'ref1',
      $('.a12345'),
      'randomName1',
      proxy
    )
    expect(() => anotherCascadeParameter.update()).to.throw(Error)
  })
  it('referencesMe true', () => {
    const referencedParameterElement = $(`
      <div id='ref-parameter1'>
        <div>
          <option name="value" selected="selected" value="abc" />
        </div>
      </div>
      `)
    const anotherCascadeParameter = new CascadeParameter(
      'ref1',
      referencedParameterElement,
      'randomName1',
      proxy
    )
    // tslint:disable-next-line:no-unused-expression
    new ReferencedParameter(anotherCascadeParameter.paramName, anotherCascadeParameter.$element, cascadeParameter)
    // tslint:disable-next-line:no-unused-expression
    new ReferencedParameter(cascadeParameter.paramName, cascadeParameter.$element, anotherCascadeParameter)
    UnoChoice.cascadeParameters.push(cascadeParameter)
    UnoChoice.cascadeParameters.push(anotherCascadeParameter)
    expect(cascadeParameter.referencesMe(anotherCascadeParameter)).to.equal(true)
  })
  it('referencesMe false', () => {
    const referencedParameterElement = $(`
      <div id='ref-parameter1'>
        <div>
          <option name="value" selected="selected" value="abc" />
        </div>
      </div>
      `)
    const anotherCascadeParameter = new CascadeParameter(
      'ref1',
      referencedParameterElement,
      'randomName1',
      proxy
    )
    UnoChoice.cascadeParameters.push(cascadeParameter)
    UnoChoice.cascadeParameters.push(anotherCascadeParameter)
    expect(cascadeParameter.referencesMe(anotherCascadeParameter)).to.equal(false)
  })
  it('referencesMe false (unknown element)', () => {
    expect(cascadeParameter.referencesMe((null as unknown as CascadeParameter))).to.equal(false)
  })
})
