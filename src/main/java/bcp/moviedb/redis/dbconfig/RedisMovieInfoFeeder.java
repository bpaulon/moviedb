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
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class RedisMovieInfoFeeder implements CommandLineRunner {

	public final static String MOVIES_KEY = "movies";
	public final static String MOVIE_SEQUENCE_KEY = "movie:id";
	public final static String WORD_KEY_PREFIX = "word:";

	ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

	@Value("classpath:/config/movies.yaml")
	Resource movieResource;

	@Value("classpath:/config/images/")
	Resource image;
	
	@Autowired
	@Qualifier("RedisTemplate")
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
			// INCR operation
			// only gets QUEUED and NO value is returned
			long movieId = template.opsForValue()
					.increment(MOVIE_SEQUENCE_KEY, 1L);
			storeMovie(movie, movieId);
		});
	}

	private List<MovieExtendedInfo> readMovies() throws JsonParseException, JsonMappingException, IOException {
		List<MovieLocalConfig> movies = mapper.readValue(movieResource.getFile(),
				new TypeReference<List<MovieLocalConfig>>() {
				});

		
		List<MovieExtendedInfo> extMovies = movies.stream()
				.map(m -> {
					MovieExtendedInfo mei = MovieExtendedInfo.builder()
							.movie(m.getMovie())
							.image(loadImageFromFile(m.getImageFileName()))
							.build();
					return mei;
				})
				.collect(Collectors.toList());
		
		return extMovies;
	}

	private void storeMovie(MovieExtendedInfo info, long movieId) {
		template.execute(new SessionCallback<List<Object>>() {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			public List<Object> execute(RedisOperations operations) throws DataAccessException {
				operations.multi();

				operations.opsForHash()
						.put(MOVIES_KEY, movieId, info);

				Map<String, Integer> metaphones = extractWords(info.getMovie().getPlot());
				metaphones.putAll(extractWords(info.getMovie().getName()));
				
				metaphones.keySet()
						.forEach(mp -> {
							operations.opsForZSet()
									.add(WORD_KEY_PREFIX + mp, movieId, metaphones.get(mp));
						});

				return operations.exec();
			}

		});
	}

	private Map<String, Integer> extractWords(String input) {
		Map<String, Integer> words = new HashMap<>();
		Pattern p = Pattern.compile("[\\w']+");
		Matcher m = p.matcher(input);

		DoubleMetaphone doubleMetaphone = new DoubleMetaphone();

		while (m.find()) {
			String word = input.substring(m.start(), m.end());
			String encoding = doubleMetaphone.doubleMetaphone(word, false);
			Integer frequency = words.getOrDefault(encoding, 0);
			words.put(encoding, ++frequency);

			// optional - do the same for the alternate metaphone encoding
		}

		log.debug("found in input: {}", words);
		return words;
	}
	
	private byte[] loadImageFromFile(String filename) {
		byte[] fileContent = new byte[] {};

		try {
			Path p = image.getFile()
					.toPath()
					.resolve(filename);
			fileContent = Files.readAllBytes(p);
			log.info("image loaded from {}", p.toString());
		} catch (IOException e) {
			log.error("could not load image from {}", filename, e);;
		}
		
		return fileContent;
	}

}
