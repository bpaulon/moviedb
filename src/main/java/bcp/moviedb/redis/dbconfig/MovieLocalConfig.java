package bcp.moviedb.redis.dbconfig;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class MovieLocalConfig {

	@JsonProperty
	private String imageFileName;

	@JsonIgnore
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private MovieInfo movie;

	@JsonUnwrapped
	public MovieInfo getMovie() {
		return movie;
	}

	@JsonCreator
	public MovieLocalConfig(@JsonProperty("name") String name, @JsonProperty("plot") String plot,
			@JsonProperty("director") String director,
			@JsonProperty("releaseDate") @JsonDeserialize(using = LocalDateDeserializer.class) @JsonSerialize(using = LocalDateSerializer.class) LocalDate releaseDate) {
		this.movie = new MovieInfo(name, plot, director, releaseDate);

	}
}
