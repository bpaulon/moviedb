var path = require('path');

if (process.env.ECLIPSE_WORKSPACE) {
	console.log("Building in Eclipse.");
	console.log("Current working dir:" + __dirname);

	var filePath = './bin/static/built/';
} else {
	filePath = './src/main/resources/static/built/';
}

module.exports = {
	entry : {
		app : './src/main/js/app.js',
		autosuggest : './src/main/js/AutoSuggest.jsx'
	},
	devtool : 'sourcemaps',
	cache : true,
	debug : true,
	output : {
		path : __dirname,
		filename : filePath + '[name].js'
	},
	module : {
		loaders : [ {
			test : path.join(__dirname, '.'),
			exclude : /(node_modules)/,
			loader : 'babel-loader',
			query : {
				cacheDirectory : true,
				presets : [ 'es2015', 'react' ]
			}
		} ]
	}
};