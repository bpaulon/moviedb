package bcp.moviedb.redis;

import static java.util.stream.Collectors.toList;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.codec.language.DoubleMetaphone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Component;

import bcp.moviedb.redis.dbconfig.Movie;
import bcp.moviedb.redis.dbconfig.MovieConfig;
import lombok.extern.slf4j.Slf4j;

@Component
@Scope(value = "prototype")
@Slf4j
public class MovieByStoryMatcher {

	@Autowired
	@Qualifier("RedisTemplate")
	private RedisTemplate<String, Object> template;

	private List<String> words;

	private DoubleMetaphone doubleMetaphone;

	public MovieByStoryMatcher(List<String> words) {
		this.words = words;
		doubleMetaphone = new DoubleMetaphone();
	}

	public List<Movie> match() {
		List<String> searchKeys = words.stream()
				.filter(s -> s != null && !s.isEmpty())
				.map(this::createKeyForWord)
				.collect(toList());
		log.debug("Search keys: {}", searchKeys);

		// get all the movie ids by intersecting the sets
		// FIXME - the key of the ZSET in output should be unique
		template.opsForZSet()
				.intersectAndStore(searchKeys.get(0), searchKeys, "out");

		// expire the set to avoid cluttering
		template.expire("out", 10, TimeUnit.SECONDS);

		@SuppressWarnings({ "unchecked", "rawtypes" })
		Set<TypedTuple<String>> ids = (Set) template.opsForZSet()
				.rangeWithScores("out", 0, -1);

		List<Movie> movies = ids.stream()
				// Each movie id has an associated score which is the frequency of the searched words
				// Sort the ids in the reverse order so the first movie id will be the one with 
				// the highest frequency of the searched words
				.sorted(Collections.reverseOrder(Comparator.comparing(TypedTuple::getScore)))
				// map id to movie
				.peek(i -> log.debug("mapping id {} >>", i.getValue()))
				.map(this::getMovieById)
				.peek(m -> log.debug("movie: {}", m))
				.collect(toList());

		return movies;
	}

	private String createKeyForWord(String word) {
		return MovieConfig.WORD_KEY_PREFIX + doubleMetaphone.doubleMetaphone(word, false);
	}

	private Movie getMovieById(TypedTuple<String> idWithScore) {
		long movieId = Long.parseLong(idWithScore.getValue());
		return (Movie) template.opsForHash()
				.get(MovieConfig.MOVIES_KEY, movieId);
	}

}
