'use strict';

import React from 'react';
import MovieInfoContainer from './MovieInfoContainer.jsx'

/**
 * List of Movies as DIV table
 */
export default class MovieList extends React.Component{
	
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
		
		this.setState( {
			pageIndex: pi
		});
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
		this.onNavigate(Math.ceil(this.props.count / this.props.pageSize) - 1); 
	}
	
	render() {
		var navLinks, pages, pageIndex, totalPages;
		
		var movies = this.props.movies.map((m, index) =>
			<MovieInfoContainer key = {index} info = {m}/>)
		
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
		
		if(totalPages >= 0) {
			pages = <span>Page {pageIndex + 1} / {totalPages + 1}  ({this.props.count} records found)</span>
		}
		
		return (
			<div>
				<div className='table'>
					{movies}
				</div>
				<div>
					{navLinks}&nbsp;{pages}
				</div>
			</div>
		)
	}
}