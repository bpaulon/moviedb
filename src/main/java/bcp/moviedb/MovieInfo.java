package bcp.moviedb;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieInfo {

	private String name;
	private String plot;
	private String director;

	private LocalDate releaseDate;

}
