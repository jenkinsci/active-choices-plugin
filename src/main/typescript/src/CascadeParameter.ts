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

import {ReferencedParameter} from "./ReferencedParameter"
import {SEPARATOR} from "./constants"
import {log} from "./utils";
import {AbstractParameter} from "./AbstractParameter";
import {JenkinsProxy} from "./Proxy";
import {FilterElement} from "./FilterElement";
import {UnoChoice} from "./UnoChoice";
import JQuery from 'jquery'

/**
 * A parameter that references parameters.
 */
export class CascadeParameter extends AbstractParameter {
  randomName: string
  proxy: JenkinsProxy
  filterElement: FilterElement | null
  referencedParameters: ReferencedParameter[] = []

  /**
   * @param paramName parameter name
   * @param $element parameter HTML element
   * @param randomName randomName given to the parameter
   * @param proxy Stapler proxy object that references the CascadeChoiceParameter
   */
  constructor(paramName: string, $element: JQuery<HTMLElement>, randomName: string, proxy: JenkinsProxy) {
    super(paramName, $element)
    this.randomName = randomName
    this.proxy = proxy
    this.filterElement = null
  }

  /**
   * Used to create the request string that will update the cascade parameter values.
   * Returns a String, with name=value for each referenced parameter.
   *
   * @return string with name=value for each referenced parameter
   */
  getReferencedParametersAsText(): string {
    return this.referencedParameters
      .map(referencedParameter => {
        const name = referencedParameter.paramName
        const value = this.getParameterValue(referencedParameter.$element)
        return `${name}=${value}`
      })
      .join(SEPARATOR)
  }

  reference(referenced: ReferencedParameter): void {
    this.referencedParameters.push(referenced)
  }

  /**
   * Returns true iff the given parameter is not null, and one of its
   * reference parameters is the same parameter as this. In other words,
   * it returns whether or not the given parameter references this parameter.
   *
   * @since 0.22
   * @param cascadeParameter a given parameter
   * @return boolean true iff the given parameter references this parameter
   */
  referencesMe(cascadeParameter: CascadeParameter): boolean {
    if (cascadeParameter == null || cascadeParameter.referencedParameters.length === 0) {
      return false
    }
    return cascadeParameter
      .referencedParameters
      .find(referencedParameter => referencedParameter.paramName === this.paramName) !== undefined
  }

  /**
   * Updates the CascadeParameter object.
   *
   * Once this method gets called, it will call the Java code (using Stapler proxy),
   * that is responsible for updating the referenced parameter values. The Java method
   * receives the value of other referenced parameters.
   *
   * Then, we call the Java code again, now to decide the next values to be displayed.
   * From here, the flow gets split into several branches, one for each HTML element
   * type supported (SELECT, INPUT, UL, etc). Each HTML element gets rendered accordingly
   * and events are triggered.
   *
   * In the last part of the method, before updating other elements, it checks for
   * recursive calls. If this parameter references itself, we need to avoid updating
   * it forever.
   *
   * @param avoidRecursion boolean flag to decide whether we want to permit self-reference
   * parameters or not (default is true)
   */
  public update(avoidRecursion: boolean = true): void {
    const paramElement = this.$element.get(0)
    if (!paramElement) {
      throw new Error(`The cascade parameter ${this.paramName} could not locate the parameter HTML element in the UI (null/undefined).`)
    }
    const parametersString = this.getReferencedParametersAsText() // gets the array parameters, joined by , (e.g. a,b,c,d)
    log('Values retrieved from Referenced Parameters: ' + parametersString)
    // Update the CascadeChoiceParameter Map of parameters
    this.proxy.doUpdate(parametersString)
    // Now we get the updated choices, after the Groovy script is evaluated
    // using the updated Map of parameters. The inner function is called with
    // the response provided by Stapler. Then we update the HTML elements.
    log('Calling Java server code to update HTML elements...')
    this.proxy.getChoicesForUI((t: XMLHttpRequest) => {
      const choices = t.responseText
      log('Values returned from server: ' + choices)
      const data = JSON.parse(choices)
      const newValues = data[0]
      const newKeys = data[1]
      const selectedElements = []
      const disabledElements = []
      // Filter selected and disabled elements and create a matrix for
      // selection and disabled some elements may have key or values
      // with the suffixes :selected and/or :disabled we want to remove
      // these suffixes.
      for (let i = 0; i < newValues.length; i++) {
        const newValue = String(newValues[i])
        if (newValue && (newValue.endsWith(':selected') || newValue.endsWith(':selected:disabled'))) {
          selectedElements.push(i)
          newValues[i] = newValues[i].replace(/:selected$/, '').replace(/:selected:disabled$/, ':disabled')
        }
        if (newValue && (newValue.endsWith(':disabled') || newValue.endsWith(':disabled:selected'))) {
          disabledElements.push(i)
          newValues[i] = newValues[i].replace(/:disabled$/, '').replace(/:disabled:selected$/, ':selected')
        }
        const newKey = String(newKeys[i])
        if (newKey && (newKey.endsWith(':selected') || newKey.endsWith(':selected:disabled'))) {
          newKeys[i] = newKeys[i].replace(/:selected$/, '').replace(/:selected:disabled$/, ':disabled')
        }
        if (newKey && (newKey.endsWith(':disabled') || newKey.endsWith(':disabled:selected'))) {
          newKeys[i] = newKeys[i].replace(/:disabled$/, '').replace(/:disabled:selected$/, ':selected')
        }
      }
      if (this.filterElement) {
        log('Updating values in filter array')
        // FIXME: hm? Why didn't I continue writing the code here? Previous code was the same way!
        //        there is another call similar to this one, inside the next blocks... maybe this
        //        can be safely removed?
      }
      // FIXME
      // http://stackoverflow.com/questions/6364748/change-the-options-array-of-a-select-list
      if (paramElement.tagName === 'SELECT') { // handle SELECT elements
        const selectElement = paramElement as HTMLSelectElement
        while (selectElement.options.length > 0) {
          selectElement.remove(selectElement.options.length - 1)
        }
        for (let i = 0; i < newValues.length; i++) {
          const opt = document.createElement('option')
          const value = newKeys[i]
          const entry = newValues[i]
          if (typeof(entry) !== 'string') {
            opt.text = JSON.stringify(entry)
            opt.value = JSON.stringify(value) // JSON.stringify(entry)
          } else {
            opt.text = entry
            opt.value = value
          }
          if (selectedElements.indexOf(i) >= 0) {
            opt.setAttribute('selected', 'selected')
          }
          if (disabledElements.indexOf(i) >= 0) {
            opt.setAttribute('disabled', 'disabled')
          }
          selectElement.add(opt, null)
        }
        if (selectElement.getAttribute('multiple') === 'multiple') {
          selectElement.setAttribute('size', (newValues.length > 10 ? 10 : newValues.length) + 'px')
        }
        // Update the values for the filtering
        const originalArray: any[] = []
        for (const optionValue of [].slice.call(selectElement.options)) {
          originalArray.push(optionValue)
        }
        if (this.filterElement) {
          this.filterElement.setOriginalArray(originalArray)
        }
      } else if (paramElement.tagName === 'DIV') {
        if (paramElement.children.length > 0 && paramElement.children[0].tagName === 'TABLE') {
          const table = paramElement.children[0]
          let tbody = table.children[0]
          if (tbody) {
            JQuery(tbody).empty()
          } else {
            tbody = document.createElement('tbody')
            table.appendChild(tbody)
          }
          const originalArray = []
          // Check whether it is a radio or checkbox element
          if (paramElement.className === 'dynamic_checkbox') {
            for (let i = 0; i < newValues.length; i++) {
              const entry = newValues[i]
              const key = newKeys[i]
              // <TR>
              const tr = document.createElement('tr')
              let idValue = 'ecp_' + this.randomName + '_' + i
              idValue = idValue.replace(' ', '_')
              tr.setAttribute('id', idValue)
              tr.setAttribute('style', 'white-space:nowrap')
              // <TD>
              const td = document.createElement('td')
              // <INPUT>
              const input = document.createElement('input')
              // <LABEL>
              const label = document.createElement('label')
              if (selectedElements.indexOf(i) >= 0) {
                input.setAttribute('checked', 'checked')
              }
              if (disabledElements.indexOf(i) >= 0) {
                input.setAttribute('disabled', 'disabled')
              }
              input.setAttribute('json', key)
              input.setAttribute('name', 'value')
              input.setAttribute("value", key)
              input.setAttribute("class", " ")
              input.setAttribute("type", "checkbox")
              label.className = "attach-previous"
              if (typeof(entry) !== 'string') {
                input.setAttribute("title", JSON.stringify(entry))
                input.setAttribute("alt", JSON.stringify(entry))
                label.innerHTML = JSON.stringify(entry)
              } else {
                input.setAttribute("title", entry)
                input.setAttribute("alt", entry)
                label.innerHTML = entry
              }
              originalArray.push(input)
              // Put everything together
              td.appendChild(input)
              td.appendChild(label)
              tr.appendChild(td)
              tbody.appendChild(tr)
            }
            // Update the values for the filtering
            if (this.filterElement) {
              this.filterElement.setOriginalArray(originalArray)
            }
          } else { // radio
            for (let i = 0; i < newValues.length; i++) {
              const entry = newValues[i]
              const key = newKeys[i]
              // <TR>
              const tr = document.createElement('tr')
              let idValue = 'ecp_' + this.randomName + '_' + i
              idValue = idValue.replace(' ', '_')
              // tr.setAttribute('id', idValue) // will use the ID for the hidden value element
              tr.setAttribute('style', 'white-space:nowrap')
              // <TD>
              const td = document.createElement('td')
              // <INPUT>
              const input = document.createElement('input')
              // <LABEL>
              const label = document.createElement('label')
              // <HIDDEN>
              const hiddenValue = document.createElement('input')
              if (selectedElements.indexOf(i) >= 0) {
                input.setAttribute('checked', 'checked')
                hiddenValue.setAttribute('name', 'value')
              } else {
                hiddenValue.setAttribute('name', '')
              }
              if (disabledElements.indexOf(i) >= 0) {
                input.setAttribute('disabled', 'disabled')
              }
              input.setAttribute('json', key)
              input.setAttribute('name', this.paramName)
              input.setAttribute("value", key)
              input.setAttribute("class", " ")
              input.setAttribute("type", "radio")
              input.setAttribute('onchange', 'fakeSelectRadioButton("' + this.paramName + '", "' + idValue + '")')
              input.setAttribute('otherId', idValue)
              label.className = "attach-previous"
              if (typeof(entry) !== 'string') {
                input.setAttribute('alt', JSON.stringify(entry))
                label.innerHTML = JSON.stringify(entry)
              } else {
                input.setAttribute('alt', entry)
                label.innerHTML = entry
              }
              hiddenValue.setAttribute('json', key)
              hiddenValue.setAttribute("value", key)
              hiddenValue.setAttribute("class", this.paramName)
              hiddenValue.setAttribute("type", "hidden")
              if (typeof(entry) !== 'string') {
                hiddenValue.setAttribute('title', JSON.stringify(entry))
              } else {
                hiddenValue.setAttribute('title', entry)
              }
              hiddenValue.setAttribute('id', idValue)
              originalArray.push(input)
              // Put everything together
              td.appendChild(input)
              td.appendChild(label)
              td.appendChild(hiddenValue)
              tr.appendChild(td)
              tbody.appendChild(tr)
              const endTr = document.createElement('tr')
              endTr.setAttribute('style', 'display: none')
              endTr.setAttribute('class', 'radio-block-end')
              tbody.appendChild(endTr)
            }
            // Update the values for the filtering
            if (this.filterElement) {
              this.filterElement.setOriginalArray(originalArray)
            }
          } // if (oldSel.className === 'dynamic_checkbox')
          /*
           * This height is equivalent to setting the number of rows displayed in a select/multiple
           */
          paramElement.style.height = newValues.length > 10 ? '230px' : 'auto'
        } // if (paramElement.children.length > 0 && paramElement.children[0].tagName === 'TABLE') {
        if (paramElement.children.length > 0 && paramElement.children[0].tagName === 'DIV') {
          const tbody = paramElement.children[0]
          JQuery(tbody).empty()
          const originalArray = []
          // Check whether it is a radio or checkbox element
          if (paramElement.className === 'dynamic_checkbox') {
            for (let i = 0; i < newValues.length; i++) {
              const entry = newValues[i]
              const key = newKeys[i]
              // <TR>
              const tr = document.createElement('div')
              let idValue = 'ecp_' + this.randomName + '_' + i
              idValue = idValue.replace(' ', '_')
              tr.setAttribute('id', idValue)
              tr.setAttribute('style', 'white-space:nowrap')
              tr.setAttribute('class', 'tr')
              // <TD>
              const td = document.createElement('div')
              // <INPUT>
              const input = document.createElement('input')
              // <LABEL>
              const label = document.createElement('label')
              if (selectedElements.indexOf(i) >= 0) {
                input.setAttribute('checked', 'checked')
              }
              if (disabledElements.indexOf(i) >= 0) {
                input.setAttribute('disabled', 'disabled')
              }
              input.setAttribute('json', key)
              input.setAttribute('name', 'value')
              input.setAttribute("value", key)
              input.setAttribute("class", " ")
              input.setAttribute("type", "checkbox")
              label.className = "attach-previous"
              if (typeof(entry) !== 'string') {
                input.setAttribute("title", JSON.stringify(entry))
                input.setAttribute("alt", JSON.stringify(entry))
                label.innerHTML = JSON.stringify(entry)
              } else {
                input.setAttribute("title", entry)
                input.setAttribute("alt", entry)
                label.innerHTML = entry
              }
              originalArray.push(input)
              // Put everything together
              td.appendChild(input)
              td.appendChild(label)
              tr.appendChild(td)
              tbody.appendChild(tr)
            }
            // Update the values for the filtering
            if (this.filterElement) {
              this.filterElement.setOriginalArray(originalArray)
            }
          } else { // radio
            for (let i = 0; i < newValues.length; i++) {
              const entry = newValues[i]
              const key = newKeys[i]
              // <TR>
              const tr = document.createElement('div')
              let idValue = 'ecp_' + this.randomName + '_' + i
              idValue = idValue.replace(' ', '_')
              // tr.setAttribute('id', idValue) // will use the ID for the hidden value element
              tr.setAttribute('style', 'white-space:nowrap')
              tr.setAttribute('class', 'tr')
              // <TD>
              const td = document.createElement('div')
              // <INPUT>
              const input = document.createElement('input')
              // <LABEL>
              const label = document.createElement('label')
              // <HIDDEN>
              const hiddenValue = document.createElement('input')
              if (selectedElements.indexOf(i) >= 0) {
                input.setAttribute('checked', 'checked')
                hiddenValue.setAttribute('name', 'value')
              } else {
                hiddenValue.setAttribute('name', '')
              }
              if (disabledElements.indexOf(i) >= 0) {
                input.setAttribute('disabled', 'disabled')
              }
              input.setAttribute('json', key)
              input.setAttribute('name', this.paramName)
              input.setAttribute("value", key)
              input.setAttribute("class", " ")
              input.setAttribute("type", "radio")
              input.setAttribute('onchange', 'fakeSelectRadioButton("' + this.paramName + '", "' + idValue + '")')
              input.setAttribute('otherId', idValue)
              label.className = "attach-previous"
              if (typeof(entry) !== 'string') {
                input.setAttribute('alt', JSON.stringify(entry))
                label.innerHTML = JSON.stringify(entry)
              } else {
                input.setAttribute('alt', entry)
                label.innerHTML = entry
              }
              hiddenValue.setAttribute('json', key)
              hiddenValue.setAttribute("value", key)
              hiddenValue.setAttribute("class", this.paramName)
              hiddenValue.setAttribute("type", "hidden")
              if (typeof(entry) !== 'string') {
                hiddenValue.setAttribute('title', JSON.stringify(entry))
              } else {
                hiddenValue.setAttribute('title', entry)
              }
              hiddenValue.setAttribute('id', idValue)
              originalArray.push(input)
              // Put everything together
              td.appendChild(input)
              td.appendChild(label)
              td.appendChild(hiddenValue)
              tr.appendChild(td)
              tbody.appendChild(tr)
              const endTr = document.createElement('div')
              endTr.setAttribute('style', 'display: none')
              endTr.setAttribute('class', 'radio-block-end')
              tbody.appendChild(endTr)
            }
            // Update the values for the filtering
            if (this.filterElement) {
              this.filterElement.setOriginalArray(originalArray)
            }
          } // if (oldSel.className === 'dynamic_checkbox')
          /*
           * This height is equivalent to setting the number of rows displayed in a select/multiple
           */
          paramElement.style.height = newValues.length > 10 ? '230px' : 'auto'
        } // if (paramElement.children.length > 0 && paramElement.children[0].tagName === 'DIV') {
      }
    })
    // propagate change
    // log('Propagating change event from ' + this.getParameterName())
    // const e = JQuery.Event('change', {parameterName: this.getParameterName()})
    // JQuery(this.getParameterElement()).trigger(e)
    if (!avoidRecursion) {
      const cascadeParameters = UnoChoice.cascadeParameters
      if (cascadeParameters && cascadeParameters.length > 0) {
        for (const other of cascadeParameters) {
          if (this.referencesMe(other)) {
            log(`Updating ${other.paramName} from ${this.paramName}`)
            other.update(true)
          }
        }
      }
    } else {
      log('Avoiding infinite loop due to recursion!')
    }
  }
}
