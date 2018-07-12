var path = require('path');

module.exports = {
    entry: {
    	app: './src/main/js/app.js',
    	autosuggest: './src/main/js/AutoSuggest.jsx'
    },
    devtool: 'sourcemaps',
    cache: true,
    debug: true,
    output: {
        path: __dirname,
        //filename: './src/main/resources/static/built/[name].js'
        // In Eclipse remove main/js from sources so the app does not get redeployed and change filename to
        filename: './bin/static/built/[name].js'
    },
    module: {
        loaders: [
            {
                test: path.join(__dirname, '.'),
                exclude: /(node_modules)/,
                loader: 'babel-loader',
                query: {
                    cacheDirectory: true,
                    presets: ['es2015', 'react']
                }
            }
        ]
    }
};