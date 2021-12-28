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

import $ from "jquery"

type ProxyAjaxCallback = (t: XMLHttpRequest) => void

export type JenkinsProxy = {
  doUpdate: (t: string) => void
  getChoicesForUI: (t: ProxyAjaxCallback) => void
  getChoicesAsStringForUI: (t: ProxyAjaxCallback) => void
}

const genMethod = (proxy: any, methodName: string, url: string, crumb: string): void => {
  proxy[methodName] = (...args: any[]) => {
    // the final argument can be a callback that receives the return value
    if (args.length === 0) {
      throw new Error(`The proxy callback must have at least one argument! None given.`)
    }
    const callback = args.slice(-1) as unknown as (t: any) => {}
    $.ajax(`${url}${methodName}`, {
      type: "POST" as "POST",
      data: JSON.stringify(args),
      contentType: 'application/x-stapler-method-invocationcharset=UTF-8',
      headers: {'Crumb': crumb},
      dataType: "json" as "json",
      async: false as false, // Here's the juice
      success: (data: any) => {
        const t = {
          responseObject: () => {
            return data
          }
        }
        if (callback) {
          callback(t)
        }
      }
    })
  }
}

/**
 * This function is the same as makeStaplerProxy available in Jenkins core, but executes
 * calls synchronously. Since many parameters must be filled only after other parameters
 * have been updated, calling Jenkins methods asynchronously causes several unpredictable
 * errors.
 *
 * @param url Jenkins URL
 * @param crumb
 * @param methods
 */
export const makeStaplerProxy2 = (url: string, crumb: string, methods: any) => {
  const url2 = url.slice(-1) === '/' ? url : `${url}/`
  const proxy = {}
  for (const method of methods) {
    genMethod(proxy, method, url2, crumb)
  }
  return proxy
}
