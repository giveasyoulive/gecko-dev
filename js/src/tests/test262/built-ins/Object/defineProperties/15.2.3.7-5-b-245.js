// Copyright (c) 2012 Ecma International.  All rights reserved.
// This code is governed by the BSD license found in the LICENSE file.

/*---
es5id: 15.2.3.7-5-b-245
description: >
    Object.defineProperties - 'descObj' is a Date object which
    implements its own [[Get]] method to get 'set' property (8.10.5
    step 8.a)
---*/

var data = "data";
var descObj = new Date(0);
var setFun = function(value) {
  data = value;
};
descObj.prop = {
  set: setFun
};

var obj = {};
Object.defineProperties(obj, descObj);
obj.prop = "dateData";

assert(obj.hasOwnProperty("prop"), 'obj.hasOwnProperty("prop") !== true');
assert.sameValue(data, "dateData", 'data');

reportCompare(0, 0);
