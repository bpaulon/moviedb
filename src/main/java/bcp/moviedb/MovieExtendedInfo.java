package bcp.moviedb;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MovieExtendedInfo {

	private byte[] image;

	private MovieInfo movie;

}
