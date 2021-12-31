// @ts-nocheck
/* tslint:disable */
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

export type ProxyAjaxCallback = (t: XMLHttpRequest) => void

export type JenkinsProxy = {
  doUpdate: (t: string) => void
  getChoicesForUI: (t: ProxyAjaxCallback) => void
  getChoicesAsStringForUI: (t: ProxyAjaxCallback) => void
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
export const makeStaplerProxy2 = (url, crumb, methods) => {
  if (url.substring(url.length - 1) !== '/') url+='/';
  const proxy = {};

  let stringify;
  if (Object.toJSON) // needs to use Prototype.js if it's present. See commit comment for discussion
    stringify = Object.toJSON;  // from prototype
  else if (typeof(JSON)=="object" && JSON.stringify)
    stringify = JSON.stringify; // standard

  const genMethod = function(methodName) {
    proxy[methodName] = function() {
      const args = arguments;

      // the final argument can be a callback that receives the return value
      const callback = (function(){
        if (args.length === 0) return null;
        const tail = args[args.length-1];
        return (typeof(tail) === 'function') ? tail : null;
      })();

      // 'arguments' is not an array so we convert it into an array
      const a = [];
      for (let i=0; i<args.length-(callback!=null?1:0); i++)
        a.push(args[i]);

      if(window.jQuery === window.$) { // Is jQuery the active framework?
        $.ajax({
          type: "POST",
          url: url+methodName,
          data: stringify(a),
          contentType: 'application/x-stapler-method-invocation;charset=UTF-8',
          headers: {'Crumb':crumb},
          dataType: "json",
          async: "false", // HACK: patched here
          success: function(data, textStatus, jqXHR) {
            if (callback != null) {
              const t = {};
              t.responseObject = function() {
                return data;
              };
              callback(t);
            }
          }
        });
      } else { // Assume prototype should work
        new Ajax.Request(url+methodName, {
          method: 'post',
          requestHeaders: {'Content-type':'application/x-stapler-method-invocation;charset=UTF-8','Crumb':crumb},
          postBody: stringify(a),
          onSuccess: function(t) {
            if (callback!=null) {
              t.responseObject = function() {
                return eval('('+this.responseText+')');
              };
              callback(t);
            }
          }
        });
      }
    }
  };

  for(let mi = 0; mi < methods.length; mi++) {
    genMethod(methods[mi]);
  }

  return proxy;
}
