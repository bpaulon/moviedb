package bcp.moviedb;

import static bcp.moviedb.config.RedisMovieInfoLoader.MOVIES_KEY;
import static bcp.moviedb.config.RedisMovieInfoLoader.WORD_KEY_PREFIX;
import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.apache.commons.codec.language.DoubleMetaphone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Scope(value = "prototype")
@Slf4j
public class MovieByStoryMatcher {

	@Autowired
	@Qualifier("RedisTemplate")
	private RedisTemplate<String, Object> template;
	
	@Resource(name="redisTemplate")
	private ZSetOperations<String, Object> zsetOps;

	/** The search words	 */
	private List<String> words;

	/** Use double metaphone to encode the search words */
	private DoubleMetaphone doubleMetaphone;

	private List<MovieExtendedInfo> matchingList;
	
	private Long resultSize;
	
	
	public MovieByStoryMatcher(List<String> words) {
		this.words = words;
		doubleMetaphone = new DoubleMetaphone();
	}
	
	
	public void match(Integer startIndex, Integer endIndex) {
		startIndex = startIndex == null ? 0 : startIndex;
		endIndex = endIndex == null ? -1 : endIndex;

		List<String> searchKeys = words.stream()
				.filter(s -> s != null && !s.isEmpty())
				.map(this::mapWordToSearchKey)
				.collect(toList());
		
		log.debug("Match movie by encoded keys: {}", searchKeys);

		// get all the movie ids by intersecting the sets
		final String resultSetKey = buildSearchResultKey();
		
		
		zsetOps.intersectAndStore(searchKeys.get(0), searchKeys, resultSetKey);
		
		resultSize = zsetOps.zCard(resultSetKey);
		// expire the set to avoid cluttering
		template.expire(resultSetKey, 60, TimeUnit.SECONDS);

		
		// Each movie id has an associated score which is the frequency of the searched words
		// Sort the ids in the reverse order so the first movie id will be the one with 
		// the highest frequency of the searched words
		@SuppressWarnings({ "unchecked", "rawtypes" })
		Set<TypedTuple<String>> ids = (Set) zsetOps.reverseRangeWithScores(resultSetKey, startIndex, endIndex);

		// get all the movies info for the found ids
		List<MovieExtendedInfo> movies = ids.stream()
				.map(this::getMovieById)
				.collect(toList());

		this.matchingList = movies;
	}

	public List<MovieExtendedInfo> matching() {
		return matchingList;
	}
	
	public Long resultsSize() {
		return resultSize;
	}
	
	private String buildSearchResultKey() {
		return "search:" + words;
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
