'use strict';

const React = require('react');
const ReactDOM = require('react-dom')
const client = require('./client');
const dateFormat = require('dateformat');


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
		console.log("update results");
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

class SearchBar extends React.Component {
	constructor(props) {
		super(props);
		this.state = {
			searchWords: []
		};
		
		this.updateInputValue = this.updateInputValue.bind(this);
		this.updateResults = this.updateResults.bind(this);
	}
	
	updateInputValue(evt) {
		evt.preventDefault();
		var str = evt.target.value;
	    this.setState({
	    	searchWords: str.match(/\S+/g)
	    });
	  }
	
	updateResults(evt) {
		this.props.update(this.state.searchWords, 0, 1);
	}
	
	render() {
		return (
			<div className="divider">
				<input value = { this.state.searchWord } onChange = { this.updateInputValue }/>
				<button disabled = { !this.state.searchWords } onClick = { this.updateResults }>Search</button>
			</div>
		)
	}
}
/**
 * List of Movies as DIV table
 */
class MovieList extends React.Component{
	
	constructor(props) {
		super(props);
		this.state = {
			pageIndex: 0
		}
		this.onNavigate = this.onNavigate.bind(this)
		this.handleNavFirst = this.handleNavFirst.bind(this);
		this.handleNavPrev = this.handleNavPrev.bind(this);
		this.handleNavNext = this.handleNavNext.bind(this);
		this.handleNavLast = this.handleNavLast.bind(this);
	}
	
	onNavigate(pi) {
		var fromIndex;
		
		this.setState( {pageIndex: pi});
		
		fromIndex = pi * this.props.pageSize;
		this.props.update( fromIndex, fromIndex + this.props.pageSize -1 );
	}
	
	handleNavFirst(e) {
		this.onNavigate(0);
	}
	
	handleNavPrev(e) {
		this.onNavigate(this.state.pageIndex - 1);
	}
	
	handleNavNext(e) {
		this.onNavigate(this.state.pageIndex + 1);
	}
	
	handleNavLast(e) {
		this.onNavigate(Math.ceil(this.props.count / this.props.pageSize) -1); 
	}
	
	render() {
		var navLinks, pages, pageIndex, totalPages;
		
		var movies = this.props.movies.map((m, index) =>
			<MovieInfoContainer key = { index } info = { m }/>)
		
		this.state.pageIndex = this.props.start / this.props.pageSize;
		
		pageIndex = this.state.pageIndex;
		totalPages = Math.ceil(this.props.count/ this.props.pageSize) - 1;
		
		navLinks = [];
		if (pageIndex >= 1) {
			navLinks.push(<button key="first" onClick={this.handleNavFirst}>&lt;&lt;</button>);
		}
		if (pageIndex >= 1) {
			navLinks.push(<button key="prev" onClick={this.handleNavPrev}>&lt;</button>);
		}
		if (pageIndex < totalPages && pageIndex >= 0) {
			navLinks.push(<button key="next" onClick={this.handleNavNext}>&gt;</button>);
		}
		if (pageIndex < totalPages) {
			navLinks.push(<button key="last" onClick={this.handleNavLast}>&gt;&gt;</button>);
		}
		
		var pages;
		if(totalPages >= 0) {
			pages = <span>Page {pageIndex + 1} / {totalPages + 1}  ({this.props.count} records found) </span>
		}
		
		return (
			<div>
				<div className='table'>
					{ movies }
				</div>
				<div>
					{navLinks} {pages}
				</div>
			</div>
		)
	}
}

class MovieInfoContainer extends React.Component {
	
	render() {
		return <MovieInfo { ...this._extract(this.props.info) } />
	}
	
	_extract(info) {
		return {
			"title": info.movie.name + " (Released: " + dateFormat(Date.parse(info.movie.releaseDate), "mmmm - yyyy") + ")",
			"director": info.movie.director,
			"story": info.movie.plot,
			"image": "data:image/jpg;base64," + info.image
		};
	}
}

/**
 * Movie component
 */
function MovieInfo(props) {

	var [ title, director, story, image ] = [
		props.title, 
		props.director, 
		props.story,
		props.image
	];
	
	return (
		<div className="divider"> {/* add some space after each movie*/}
			<div className="row">
				<div className="cell">
					<div className="row">
						<div className="cell">Title</div>
						<div className="cell">{title}</div>
					</div>
					<div className="row">
						<div className="cell">Director</div>
						<div className="cell">{director}</div>
					</div>
					<div className="row">
						<div className="cell">Story</div>
						<div className="cell">{story}</div>
					</div>
				</div>
				<div>
					<img src={image}></img>
				</div>
			</div>
		</div>
	);
}

ReactDOM.render(
	<App />,
	document.getElementById('react')
)

