import {log} from "./utils";
import {afterEach, beforeEach, describe, it} from 'mocha';
import {expect} from 'chai';
import * as sinon from 'sinon';

describe('utils', () => {
  let spy: any
  beforeEach(() => {
    spy = sinon.spy(console, 'log')
  })
  afterEach(() => {
    spy.restore()
  })
  it('log', () => {
    log('test')
    expect(spy.calledOnce).to.equal(true)
  })
})
