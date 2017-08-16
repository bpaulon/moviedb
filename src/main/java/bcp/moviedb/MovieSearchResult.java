package bcp.moviedb;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MovieSearchResult {

	Long count;
	List<MovieExtendedInfo> movies;
}
