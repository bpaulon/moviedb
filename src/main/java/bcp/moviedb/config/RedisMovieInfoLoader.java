package bcp.moviedb.config;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.codec.language.DoubleMetaphone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.StreamUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import bcp.moviedb.MovieExtendedInfo;
import bcp.moviedb.MovieInfo;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class RedisMovieInfoLoader implements CommandLineRunner {

	public static final String MOVIES_KEY = "movies";
	public static final String MOVIE_SEQUENCE_KEY = "movie:id";
	public static final String WORD_KEY_PREFIX = "word:";

	@Autowired
	@Qualifier("yaml")
	ObjectMapper yamlMapper;

	@Value("classpath:/config/movies.yaml")
	Resource movieResource;

	@Value("classpath:/config/images/")
	Resource imagesResourcePath;
	
	@Autowired
	private RedisTemplate<String, Object> template;
	
	@javax.annotation.Resource(name="redisTemplate")
	private ValueOperations<String, Object> valueOps;

	@Override
	public void run(String... args) throws Exception {
		initMovies();
	}

	private void initMovies() throws IOException {
		List<MovieExtendedInfo> movies = readMovies();
		valueOps.set(MOVIE_SEQUENCE_KEY, 0L);
		
		movies.forEach(movie -> {
			// id must be incremented outside the transaction - otherwise the
			// INCR operation only gets QUEUED and NO value is returned
			long movieId = valueOps.increment(MOVIE_SEQUENCE_KEY, 1L);
			storeMovie(movie, movieId);
		});
	}

	@SuppressWarnings({"squid:S1488"})
	private List<MovieExtendedInfo> readMovies() throws IOException {
		List<MovieLocalInfo> movieConfig = yamlMapper.readValue( movieResource.getInputStream(),
				new TypeReference<List<MovieLocalInfo>>() {});

		List<MovieExtendedInfo> extMovies = movieConfig.stream()
				.map(this::buildInfo)
				.collect(Collectors.toList());
		
		return extMovies;
	}

	/**
	 * Build the info from local configuration
	 * 
	 * @param m - local info 
	 * @return - external info
	 */
	@SuppressWarnings({"squid:S1488"})
	private MovieExtendedInfo buildInfo(MovieLocalInfo m) {
		byte[] image = loadImageFromFile(m.getImageFileName());
		
		MovieExtendedInfo mei = MovieExtendedInfo.builder()
				.movie(m.getMovie())
				.image(image)
				.build();
		return mei;
	}
	
	private void storeMovie(MovieExtendedInfo info, long movieId) { 
	  
	  template.execute(new SessionCallback<List<Object>>() {
			
			@SuppressWarnings({ "unchecked", "rawtypes" })
			public List<Object> execute(RedisOperations operations) {
				// begin transaction. 
				operations.multi();

				// store the movie info
				operations.opsForHash().put(MOVIES_KEY, movieId, info);
				
				// for each search key add to the associated sorted set the movie id. Each movie id has a 
				// score representing the frequency of the word in story and name
				Map<String, Integer> searchKeys = buildSearchKeysWithFrequency(info);
				searchKeys.forEach((key, freq) -> operations.opsForZSet()
						.add(WORD_KEY_PREFIX + key, movieId, freq));
				
				//end transaction
				return operations.exec();
			}

		});
		
	}

	@SuppressWarnings({"squid:S1488"})
	private Map<String, Integer> buildSearchKeysWithFrequency(MovieExtendedInfo info) {
		MovieInfo mi = info.getMovie();
		String text = mi.getPlot() + mi.getName();
		
		Map<String, Integer> searchKeys = extractWords(text);
		return searchKeys;
	}
	
	/**
	 * Extracts words from the input text. For each word in the text it creates the associated metaphone.
	 * All the metaphones are mapped to the frequency of the word in the input text 
	 * 
	 * @param input
	 * @return
	 */
	private Map<String, Integer> extractWords(String input) {
		DoubleMetaphone doubleMetaphone = new DoubleMetaphone();
		Map<String, Integer> words = new HashMap<>();
		
		Pattern p = Pattern.compile("[\\w']+");
		Matcher m = p.matcher(input);

		while (m.find()) {
			String word = input.substring(m.start(), m.end());
			String metaphone = doubleMetaphone.doubleMetaphone(word, false);
			// store or increment existing frequency
			words.merge(metaphone, 1, (freq, val) -> freq + val);

			// Optionally store the alternate metaphone encoding
		}

		log.debug("Found in input: {}", words);
		return words;
	}
	
	private byte[] loadImageFromFile(String filename) {
		byte[] fileContent = new byte[] {};
		
		try {
		  URI imagePath = imagesResourcePath.getURI();
      // When this application is executed with java -jar the URI is jar: URI ("jar:file:/"..".) 
		  // URI.resolve and URI.relativize don't work on jar: URIs which are opaque and not hierarchical. 
		  // imagePath.resolve(filename) won't work so I construct the URI by hand 
		  imagePath = new URI(imagePath + filename);
		  
		  log.debug("Loading image from imagePath {}", imagePath);
		  URL imageURL = imagePath.toURL();
		  fileContent= StreamUtils.copyToByteArray(imageURL.openStream());
		} catch (IOException | URISyntaxException e) {
			log.error("Could not load image from {}", filename, e);
		}

		return fileContent;
	}

}
