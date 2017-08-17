'use strict';

import React from 'react';
import ReactDOM from 'react-dom';
import client from './client';
import MovieList from './MovieList.jsx';
import SearchBar from './SearchBar.jsx';


/**
 * App Component
 */
class App extends React.Component {
	
	constructor(props) {
		super(props);
		this.state = {
			movies: [],
			count: 0,
			pageSize: 2,
			start: 0,
			searchWords:[]
		};
		
		// This binding is necessary to make 'this' work in the callback. Create
		// a new function bound to this
		this.updateResults = this.updateResults.bind(this);
		this.onNavigate = this.onNavigate.bind(this);
	}
	
	updateResults(searchWords, start, end) {
		this.state.searchWords = searchWords;
		this.state.start = start;
		
		client({
			method: 'GET', 
			path: '/search?word=' + searchWords + '&start=' + start +'&end=' + end
		}).done(response => {
			var resp = JSON.parse(response.entity);
			this.setState({ 
				movies: resp.movies, 
				count: resp.count 
			});
		});
	}
	
	render() {
		return (
			<div>
				<SearchBar update={this.updateResults}/>
				
				<MovieList movies = {this.state.movies} 
						   update = {this.onNavigate}
						   start = {this.state.start}
						   count = {this.state.count}	
						   pageSize = {this.state.pageSize}/>
			</div>
		)
	}
	
	onNavigate(start, end) {
		console.log("onNavigate " + start + "-" + end);
		this.updateResults(this.state.searchWords, start, end);
		
	}
}

ReactDOM.render(
	<App />,
	document.getElementById('react')
)

