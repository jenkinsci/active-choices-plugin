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
import {log} from "./utils";

/**
 * An element that acts as filter for other elements.
 */
export class FilterElement {
  paramElement: HTMLElement
  filterElement: HTMLInputElement
  filterLength: number
  originalArray: any[]

  /**
   * @param paramElement parameter HTML element being filtered
   * @param filterElement HTML element where the user enter the filter
   * @param filterLength filter length
   */
  constructor(paramElement: HTMLElement, filterElement: HTMLInputElement, filterLength: number) {
    this.paramElement = paramElement
    this.filterElement = filterElement
    this.filterLength = filterLength
    this.originalArray = []
    this.initFilter(paramElement)
    this.initEventHandler()
  }

  private initFilter(paramElement: HTMLElement): void {
    // push existing values into originalArray array
    if (paramElement.tagName === 'SELECT') { // handle SELECTS
      this.originalArray.concat(...paramElement.children)
    } else if (paramElement.tagName === 'DIV') { // handle CHECKBOXES
      const $element = $(paramElement)
      if ($element.children().length > 0 && paramElement.children[0].tagName === 'TABLE') {
        const table = paramElement.children[0]
        const tbody = table.children[0]
        if (paramElement.className === 'dynamic_checkbox') {
          const $trs = $(tbody).find('tr')
          for (const tr of $trs) {
            const $tds = $(tr).find('td')
            const $inputs = $($tds[0]).find('input')
            const input = $inputs[0]
            this.originalArray.push(input)
          }
        } else {
          const $trs = $(tbody).find('tr')
          for (const tr of $trs) {
            const $tds = $(tr).find('td')
            const $inputs = $($tds[0]).find('input')
            const input = $inputs[0]
            this.originalArray.push(input)
          }
        }
      } // if (jqueryElement.children().length > 0 && paramElement.children[0].tagName === 'TABLE') {
      if ($element.children().length > 0 && paramElement.children[0].tagName === 'DIV') {
        const tbody = paramElement.children[0]
        if (paramElement.className === 'dynamic_checkbox') {
          const $trs = $(tbody).find('div')
          for (const tr of $trs) {
            const $tds = $(tr).find('div')
            const $inputs = $($tds[0]).find('input')
            const input = $inputs[0]
            this.originalArray.push(input)
          }
        } else {
          const $trs = $(tbody).find('div')
          for (const tr of $trs) {
            const $tds = $(tr).find('div')
            const $inputs = $($tds[0]).find('input')
            const input = $inputs[0]
            this.originalArray.push(input)
          }
        }
      } // if (jqueryElement.children().length > 0 && paramElement.children[0].tagName === 'DIV') {
    }
  }

  /**
   * Initiates an event listener for Key Up events. Depending on the element type it will interpret the filter, and
   * the filtered element, to update its values.
   */
  private initEventHandler() {
    this.filterElement.onkeyup = (e: KeyboardEvent) => {
      // const filterElement = e.target
      const filterElement = this.filterElement
      const filteredElement = this.paramElement
      const text = filterElement.value.toLowerCase()
      if (text.length !== 0 && text.length < this.filterLength) {
        return
      }
      const newOptions = Array()
      for (const option of this.originalArray) {
        if (option.tagName === 'INPUT') {
          if (option.getAttribute('alt') && option.getAttribute('alt') !== option.value) {
            if (option.getAttribute('alt').toLowerCase().match(text)) {
              newOptions.push(option)
            }
          } else if (option.value.toLowerCase().match(text)) {
            newOptions.push(option)
          }
        } else {
          if (option.innerHTML.toLowerCase().match(text)) {
            newOptions.push(option)
          }
        }
      }
      const tagName = filteredElement.tagName
      const $filteredElement = $(filteredElement)
      if (tagName === 'SELECT') {
        $filteredElement.children().remove()
        for (const newOption of newOptions) {
          const opt = document.createElement('option')
          opt.value = newOption.value
          opt.innerHTML = newOption.innerHTML
          $filteredElement.append(opt)
        }
      } else if (tagName === 'DIV') {
        // handle check boxes, radio boxes and other elements (Jenkins renders them as tables)
        if ($filteredElement.children().length > 0 && $filteredElement.children()[0].tagName === 'TABLE') {
          const table = filteredElement.children[0]
          const tbody = table.children[0]
          $(tbody).empty()
          if (filteredElement.className === 'dynamic_checkbox') {
            for (let i = 0; i < newOptions.length; i++) {
              const entry = newOptions[i]
              // TR
              const tr = document.createElement('tr')
              const idValue = `ecp_${(e.target as any).randomName}_${i}`.replace(' ', '_')
              tr.setAttribute('id', idValue)
              tr.setAttribute('style', 'white-space:nowrap')
              // TD
              const td = document.createElement('td')
              // INPUT
              let input = null
              // LABEL
              const label: HTMLLabelElement = document.createElement('label')
              if (entry instanceof HTMLElement) {
                label.className = "attach-previous"
                if (entry.tagName === 'INPUT') {
                  input = entry
                  label.innerHTML = input.getAttribute('title') || ''
                  label.title = input.getAttribute('title') || ''
                } else {
                  input = document.createElement('input')
                  // FIXME: see these two as any below
                  input.setAttribute('json', JSON.stringify((entry as any).value))
                  input.setAttribute('name', 'value')
                  input.setAttribute("value", JSON.stringify((entry as any).value))
                  input.setAttribute("type", "radio")
                  // TODO: it was doing `label.innerHTML = input`?
                  label.appendChild(input)
                }
                // Put everything together
                td.appendChild(input)
                td.appendChild(label)
                tr.appendChild(td)
                tbody.appendChild(tr)
              }
              // } else {
              //   input.setAttribute('json', entry)
              //   input.setAttribute('name', 'value')
              //   input.setAttribute("value", entry)
              //   input.setAttribute("type", "checkbox")
              //   label.className = "attach-previous"
              //   label.title = entry.getAttribute('title')
              //   label.innerHTML = entry.getAttribute('title')
              // }
            }
          } else {
            for (let i = 0; i < newOptions.length; i++) {
              const entry = newOptions[i]
              // TR
              const tr = document.createElement('tr')
              let idValue = ''
              if (typeof (entry) !== 'string') {
                if (entry.tagName === 'INPUT') {
                  idValue = 'ecp_' + entry.getAttribute('name') + '_' + i
                }
              } else {
                idValue = 'ecp_' + entry + '_' + i
              }
              idValue = idValue.replace(' ', '_')
              tr.setAttribute('id', idValue)
              tr.setAttribute('style', 'white-space:nowrap')
              // TD
              const td = document.createElement('td')
              const jsonInput = document.createElement('input'); // used to help in the selection
              let input = document.createElement('input')
              const label = document.createElement('label')
              label.className = "attach-previous"
              input = entry
              input.checked = false
              jsonInput.setAttribute('id', FilterElement.getAttribute(input, 'otherid'))
              jsonInput.setAttribute('json', FilterElement.getAttribute(input, 'json'))
              jsonInput.setAttribute('name', '')
              jsonInput.setAttribute("value", FilterElement.getAttribute(input, 'value'))
              jsonInput.setAttribute("class", FilterElement.getAttribute(input, 'name'))
              jsonInput.setAttribute("type", "hidden")
              jsonInput.setAttribute('title', FilterElement.getAttribute(input, 'alt'))
              label.innerHTML = FilterElement.getAttribute(input, 'alt')
              // Put everything together
              td.appendChild(input)
              td.appendChild(label)
              td.appendChild(jsonInput)
              tr.appendChild(td)
              tbody.appendChild(tr)
            }
          }
        }
        if ($filteredElement.children().length > 0 && $filteredElement.children()[0].tagName === 'DIV') {
          const tbody = filteredElement.children[0]
          $(tbody).empty()
          if (filteredElement.className === 'dynamic_checkbox') {
            for (let i = 0; i < newOptions.length; i++) {
              const entry = newOptions[i]
              // TR
              const tr = document.createElement('div')
              // TODO: see as any below
              const idValue = `ecp_${(e.target as any).randomName}_${i}`.replace(' ', '_')
              tr.setAttribute('id', idValue)
              tr.setAttribute('style', 'white-space:nowrap')
              tr.setAttribute('class', 'tr')
              // TD
              const td = document.createElement('div')
              // INPUT
              let input : HTMLElement | HTMLInputElement | null = null
              // LABEL
              const label = document.createElement('label')
              if (entry instanceof HTMLElement) {
                label.className = "attach-previous"
                if (entry.tagName === 'INPUT') {
                  input = entry
                  label.innerHTML = FilterElement.getAttribute(input, 'title')
                  label.title = FilterElement.getAttribute(input, 'title')
                } else {
                  input = document.createElement('input')
                  // TODO: see the two as any below
                  input.setAttribute('json', JSON.stringify((entry as any).value))
                  input.setAttribute('name', 'value')
                  input.setAttribute("value", JSON.stringify((entry as any).value))
                  input.setAttribute("type", "radio")
                  // TODO: it was doing innerHTML before
                  label.appendChild(input)
                }
                // Put everything together
                td.appendChild(input)
                td.appendChild(label)
                tr.appendChild(td)
                tbody.appendChild(tr)
              }
              // } else {
              //   input.setAttribute('json', entry)
              //   input.setAttribute('name', 'value')
              //   input.setAttribute("value", entry)
              //   input.setAttribute("type", "checkbox")
              //   label.className = "attach-previous"
              //   label.title = entry.getAttribute('title')
              //   label.innerHTML = entry.getAttribute('title')
              // }
            }
          } else {
            for (let i = 0; i < newOptions.length; i++) {
              const entry = newOptions[i]
              // TR
              const tr = document.createElement('div')
              let idValue = ''
              if (entry instanceof HTMLElement) {
                if (entry.tagName === 'INPUT') {
                  idValue = `ecp_${entry.getAttribute('name')}_${i}`
                }
              } else if (entry instanceof String) {
                idValue = `ecp_${entry}_${i}`
              }
              idValue = idValue.replace(' ', '_')
              tr.setAttribute('id', idValue)
              tr.setAttribute('style', 'white-space:nowrap')
              tr.setAttribute('class', 'tr')
              // TD
              const td = document.createElement('div')
              // INPUT
              const jsonInput = document.createElement('input'); // used to help in the selection
              let input = document.createElement('input')
              // LABEL
              const label = document.createElement('label')
              label.className = "attach-previous"
              input = entry
              input.checked = false
              jsonInput.setAttribute('id', FilterElement.getAttribute(input, 'otherid'))
              jsonInput.setAttribute('json', FilterElement.getAttribute(input, 'json'))
              jsonInput.setAttribute('name', '')
              jsonInput.setAttribute("value", FilterElement.getAttribute(input, 'value'))
              jsonInput.setAttribute("class", FilterElement.getAttribute(input, 'name'))
              jsonInput.setAttribute("type", "hidden")
              jsonInput.setAttribute('title', FilterElement.getAttribute(input, 'alt'))
              label.innerHTML = FilterElement.getAttribute(input, 'alt')
              // Put everything together
              td.appendChild(input)
              td.appendChild(label)
              td.appendChild(jsonInput)
              tr.appendChild(td)
              tbody.appendChild(tr)
            }
          }
        }
      }
      // Propagate the changes made by the filter
      log('Propagating change event after filtering')
      const newEvent = jQuery.Event('change', {parameterName: 'Filter Element Event'})
      $filteredElement.trigger(newEvent)
    }
  }

  private static getAttribute(element: HTMLElement, attribute: string, defaultValue: string = '') {
    return element.getAttribute(attribute) || defaultValue
  }

  /**
   * Sets the array with the original options of the filtered element. Once the array has been
   * set, it empties the value of the filter input box, thus allowing the user to type in again.
   *
   * @param originalArray
   */
  setOriginalArray(originalArray: any[]): void {
    this.originalArray = originalArray
    this.clearFilterElement()
  }

  /**
   * Clears the filter input box.
   *
   * @since 0.23
   */
  clearFilterElement() {
    this.filterElement.value = ''
  }

}
