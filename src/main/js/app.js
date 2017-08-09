'use strict';

// tag::vars[]
const React = require('react');
const ReactDOM = require('react-dom')
const client = require('./client');
// end::vars[]

// tag::app[]
class App extends React.Component {

	constructor(props) {
		super(props);
		this.state = {movies: []};
	}

	componentDidMount() {
		client({method: 'GET', path: '/search?word=and'}).done(response => {
			console.log(response.entity);
			this.setState({ movies: this.state.movies.concat(JSON.parse(response.entity)) });
		});
	}

	render() {
		return (
			<MovieList movies={this.state.movies}/>
		)
	}
}
// end::app[]

// tag::movie-list[]
class MovieList extends React.Component{
	render() {
		var movies = this.props.movies.map((movie, index) =>
			<Movie key={index} movie={movie}/>
		);
		return (
			<table>
				<tbody>
					<tr>
						<th>Name</th>
						<th>Director</th>
						<th>Story</th>
					</tr>
					{movies}
				</tbody>
			</table>
		)
	}
}
// end::movie-list[]

// tag::movie[]
class Movie extends React.Component{
	render() {
		return (
			<tr>
				<td>{this.props.movie.name}</td>
				<td>{this.props.movie.director}</td>
				<td>{this.props.movie.plot}</td>
			</tr>
		)
	}
}
// end::movie[]

// tag::render[]
ReactDOM.render(
	<App />,
	document.getElementById('react')
)
// end::render[]

