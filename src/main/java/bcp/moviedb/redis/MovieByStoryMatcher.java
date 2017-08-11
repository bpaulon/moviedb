package bcp.moviedb.redis;

import static bcp.moviedb.redis.dbconfig.RedisMovieInfoFeeder.MOVIES_KEY;
import static bcp.moviedb.redis.dbconfig.RedisMovieInfoFeeder.WORD_KEY_PREFIX;
import static java.util.stream.Collectors.toList;

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

import bcp.moviedb.redis.dbconfig.MovieExtendedInfo;
import lombok.extern.slf4j.Slf4j;

@Component
@Scope(value = "prototype")
@Slf4j
public class MovieByStoryMatcher {

	@Autowired
	@Qualifier("RedisTemplate")
	private RedisTemplate<String, Object> template;

	/** The search words	 */
	private List<String> words;

	/** Use double metaphone to encode the search words */
	private DoubleMetaphone doubleMetaphone;

	public MovieByStoryMatcher(List<String> words) {
		this.words = words;
		doubleMetaphone = new DoubleMetaphone();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<MovieExtendedInfo> match() {
		
		List<String> searchKeys = words.stream()
				.filter(s -> s != null && !s.isEmpty())
				.map(this::mapWordToSearchKey)
				.collect(toList());
		
		log.debug("Match movie by encoded keys: {}", searchKeys);

		// get all the movie ids by intersecting the sets
		String resultsSetKey = "search:" + searchKeys.toString().replace(WORD_KEY_PREFIX,"");
		template.opsForZSet()
				.intersectAndStore(searchKeys.get(0), searchKeys, resultsSetKey);
		// expire the set to avoid cluttering
		template.expire(resultsSetKey, 60, TimeUnit.SECONDS);

		
		// Each movie id has an associated score which is the frequency of the searched words
		// Sort the ids in the reverse order so the first movie id will be the one with 
		// the highest frequency of the searched words
		Set<TypedTuple<String>> ids = (Set) template.opsForZSet()
				.reverseRangeWithScores(resultsSetKey, 0, -1);

		// get all the movies info for the found ids
		List<MovieExtendedInfo> movies = ids.stream()
				.map(this::getMovieById)
				.collect(toList());

		return movies;
	}

	private String mapWordToSearchKey(String word) {
		String mp = doubleMetaphone.doubleMetaphone(word, false);
		String key = WORD_KEY_PREFIX + mp;

		log.info("mapped word {} to search key {}", word, mp);
		return key;
	}

	private MovieExtendedInfo getMovieById(TypedTuple<String> idWithScore) {
		Long movieId = Long.valueOf(idWithScore.getValue());
		return (MovieExtendedInfo) template.opsForHash()
				.get(MOVIES_KEY, movieId);
	}

}
