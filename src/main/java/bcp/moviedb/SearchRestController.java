package bcp.moviedb;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class SearchRestController {

	@Autowired
	private BeanFactory beanFactory;

	@GetMapping("search")
	public MovieSearchResult search(@RequestParam(value = "word", required = true) List<String> words,
			@RequestParam(value = "start", required = true) Integer startIndex,
			@RequestParam(value = "end", required = true) Integer endIndex) {

		log.info("search movies by words: {} in range {}-{}", words, startIndex, endIndex);

		MovieByStoryMatcher movieMatcher = beanFactory.getBean(MovieByStoryMatcher.class, words);
		movieMatcher.match(startIndex, endIndex);

		List<MovieExtendedInfo> movies = movieMatcher.matching();
		Long totalCount = movieMatcher.resultsSize();
		MovieSearchResult msi = MovieSearchResult.builder()
				.movies(movies)
				.count(totalCount)
				.build();

		log.debug("result: {}", msi);
		return msi;
	}
	
	@GetMapping("searchDirector")
  public List<String> searchDirector(@RequestParam(value = "name", required = true) String name) {

    log.info("search by name {}", name);

    return Arrays.asList("name1 <additional_info>", "name2 <additional_info>");
    
  }
}