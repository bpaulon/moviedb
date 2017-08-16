package bcp.moviedb;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString(exclude="image")
public class MovieExtendedInfo {

	private byte[] image;

	private MovieInfo movie;

}
