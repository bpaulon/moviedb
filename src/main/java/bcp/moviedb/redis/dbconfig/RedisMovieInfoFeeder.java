package bcp.moviedb.redis.dbconfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class RedisMovieInfoFeeder implements CommandLineRunner {

	public final static String MOVIES_KEY = "movies";
	public final static String MOVIE_SEQUENCE_KEY = "movie:id";
	public final static String WORD_KEY_PREFIX = "word:";

	@Autowired
	@Qualifier("yaml")
	ObjectMapper yamlMapper;

	@Value("classpath:/config/movies.yaml")
	Resource movieResource;

	@Value("classpath:/config/images/")
	Resource imagesResourcePath;
	
	@Autowired
	private RedisTemplate<String, Object> template;

	@Override
	public void run(String... args) throws Exception {
		initMovies();
	}

	private void initMovies() throws Exception {
		List<MovieExtendedInfo> movies = readMovies();
		template.opsForValue()
				.set(MOVIE_SEQUENCE_KEY, 0L);
		movies.forEach(movie -> {
			// id must be incremented outside the transaction - otherwise the
			// INCR operation only gets QUEUED and NO value is returned
			long movieId = template.opsForValue()
					.increment(MOVIE_SEQUENCE_KEY, 1L);
			storeMovie(movie, movieId);
		});
	}

	private List<MovieExtendedInfo> readMovies() throws JsonParseException, JsonMappingException, IOException {
		List<MovieLocalConfig> movieConfig = yamlMapper.readValue(movieResource.getFile(),
				new TypeReference<List<MovieLocalConfig>>() {});

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
	private MovieExtendedInfo buildInfo(MovieLocalConfig m) {
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
			public List<Object> execute(RedisOperations operations) throws DataAccessException {
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

		log.debug("found in input: {}", words);
		return words;
	}
	
	private byte[] loadImageFromFile(String filename) {
		byte[] fileContent = new byte[] {};

		try {
			Path imagePath = imagesResourcePath.getFile()
					.toPath()
					.resolve(filename);
			fileContent = Files.readAllBytes(imagePath);
			log.info("image loaded from {}", imagePath.toString());
		} catch (IOException e) {
			log.error("could not load image from {}", filename, e);
		}

		return fileContent;
	}

}
