package bcp.moviedb.config;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

import bcp.moviedb.MovieInfo;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

@Data
public class MovieLocalInfo {

	@JsonProperty
	private String imageFileName;

	@JsonIgnore
	@Setter(AccessLevel.NONE)
	private MovieInfo movie;

	@JsonUnwrapped
	public MovieInfo getMovie() {
		return movie;
	}

	@JsonCreator
	public MovieLocalInfo(@JsonProperty("name") String name, 
			@JsonProperty("plot") String plot,
			@JsonProperty("director") String director,
			@JsonProperty("releaseDate") LocalDate releaseDate) {
		this.movie = new MovieInfo(name, plot, director, releaseDate);
	}
}
