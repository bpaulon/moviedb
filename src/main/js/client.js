'use strict';

var rest = require('rest');
var defaultRequest = require('rest/interceptor/defaultRequest');
var mime = require('rest/interceptor/mime');
var uriTemplateInterceptor = require('./api/uriTemplateInterceptor');
var errorCode = require('rest/interceptor/errorCode');

module.exports = rest
		.wrap(errorCode)
		.wrap(defaultRequest, { headers: { 'Accept': 'application/json' }});
