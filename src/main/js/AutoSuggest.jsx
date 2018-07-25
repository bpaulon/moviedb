import Autosuggest from 'react-autosuggest';

import React from 'react';
import ReactDOM from 'react-dom';
import client from './client';



function getSuggestionValue(suggestion) {
  return suggestion;
}

function renderSuggestion(suggestion) {
  return (
    <span>{suggestion}</span>
  );
}

class App extends React.Component {
  constructor() {
    super();

    this.state = {
      value: '',
      suggestions: [],
      isLoading: false
    };
    
    this.lastRequestId = null;
    
    this.onChange= this.onChange.bind(this);
    this.onSuggestionsFetchRequested = this.onSuggestionsFetchRequested.bind(this);
    this.onSuggestionsClearRequested = this.onSuggestionsClearRequested.bind(this);
  }
  
  loadSuggestions(value) {
    // Cancel the previous request
    if (this.lastRequestId !== null) {
      clearTimeout(this.lastRequestId);
    }
    
    this.setState({
      isLoading: true
    });
    
    // Fake request
    this.lastRequestId = setTimeout(() => {
      client({
			method: 'GET', 
			path: '/searchDirector?name=pa'
		}).done(response => {
			var resp = JSON.parse(response.entity);
			this.setState({ 
			    isLoading: false,
				suggestions: resp 
			});
			
			console.log("suggestions: " + this.state.suggestions);
		});
    }, 500);
  }

  onChange (event, { newValue }){
    this.setState({
      value: newValue
    });
  };
    
  onSuggestionsFetchRequested ({ value }){
    this.loadSuggestions(value);
  };

  onSuggestionsClearRequested () {
    this.setState({
      suggestions: []
    });
  };

  render() {
    const { value, suggestions, isLoading } = this.state;
    const inputProps = {
      placeholder: "Enter Director's name",
      value,
      onChange: this.onChange
    };
    const status = (isLoading ? 'Loading...' : 'Type to load suggestions');
    
    return (
      <div>
        <div className="status">
          <strong>Status:</strong> {status}
        </div>
        <Autosuggest 
          suggestions={suggestions}
          onSuggestionsFetchRequested={this.onSuggestionsFetchRequested}
          onSuggestionsClearRequested={this.onSuggestionsClearRequested}
          getSuggestionValue={getSuggestionValue}
          renderSuggestion={renderSuggestion}
          inputProps={inputProps} />
      </div>
    );
  }
}

ReactDOM.render(<App />, 
document.getElementById('app'));
