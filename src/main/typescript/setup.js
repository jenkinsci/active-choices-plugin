// tslint:disable-next-line:no-var-requires
const jsdom = require('jsdom');

const dom = new jsdom.JSDOM('<!doctype html><html lang="en"><body></body></html>')

global.window = dom.window
global.document = global.window.document
