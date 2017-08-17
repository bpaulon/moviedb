'use strict';

import React from 'react';
import dateFormat from 'dateformat';

export default class MovieInfoContainer extends React.Component {
    
    render() {
        return <MovieInfo {...this._extract(this.props.info)} />
    }
    
    _extract(info) {
        
        return {
            "title": info.movie.name + " (Released: " + dateFormat(Date.parse(info.movie.releaseDate), "mmmm yyyy") + ")",
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

    var [title, director, story, image] = [
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
