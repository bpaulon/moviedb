'use strict';

const React = require('react');
const ReactDOM = require('react-dom')
const client = require('./client');

/**
 * App Component
 */
class App extends React.Component {
	constructor(props) {
		super(props);
		this.state = {
			movies: [],
			searchWord: ''
		};
		// This binding is necessary to make `this` work in the callback. Create
		// a new function bound to this
		this.updateResults = this.updateResults.bind(this);
		this.updateInputValue = this.updateInputValue.bind(this);
	}
	
	updateResults() {
		client({method: 'GET', path: '/search?word=' + this.state.searchWord}).done(response => {
			this.setState({movies: []});
			this.setState({ movies: this.state.movies.concat(JSON.parse(response.entity)) });
		});
	}
	
	render() {
		return (
			<div>
				<input value={this.state.searchWord} onChange={this.updateInputValue}/>
				<button onClick={this.updateResults}>Search</button>
				<MovieList movies={this.state.movies}/>
			</div>
		)
	}
	
	updateInputValue(evt) {
	    this.setState({
	    	searchWord: evt.target.value
	    });
	  }
}

/**
 * List of Movies as DIV table
 */
class MovieList extends React.Component{
	render() {
		var movies = this.props.movies.map((m, index) =>
			<Movie key={index} info={m}/>)
		return (
			<div className='table'>
				{movies}
			</div>
		)
	}
}

/**
 * Movie component
 */
class Movie extends React.Component{
	render() {
		return (
			<div className="divider"> {/* add some space after each movie*/}
				<div className="row">
					<div className="cell">
						<div className="row">
							<div className="cell">Name</div>
							<div className="cell">{this.props.info.movie.name}</div>
						</div>
						<div className="row">
							<div className="cell">Director</div>
							<div className="cell">{this.props.info.movie.director}</div>
						</div>
						<div className="row">
							<div className="cell">Story</div>
							<div className="cell">{this.props.info.movie.plot}</div>
						</div>
					</div>
					<div>
						<img src={"data:image/jpg;base64," + this.props.info.image}></img>
					</div>
				</div>
			</div>
		)
	}
}

ReactDOM.render(
	<App />,
	document.getElementById('react')
)

