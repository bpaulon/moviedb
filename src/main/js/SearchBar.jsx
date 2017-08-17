'use strict';

import React from 'react';

export default class SearchBar extends React.Component {
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
                <input value = {this.state.searchWord } onChange = {this.updateInputValue}/>
                <button disabled = {!this.state.searchWords} onClick = {this.updateResults}>Search</button>
            </div>
        )
    }
}