package bcp.moviedb;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import bcp.moviedb.config.MovieLocalInfo;

@RunWith(SpringRunner.class)
@SpringBootTest
public class YamlConfigurator {

	@Value("classpath:/config/movies.yaml")
	Resource movieResource;

	@Value("classpath:/config/images/")
	Resource image;

	ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

	private byte[] loadImage(String filename) {
		byte[] fileContent = new byte[] {};

		try {
			Path p = image.getFile()
					.toPath()
					.resolve(filename);
			System.out.println(p.toString());
			fileContent = Files.readAllBytes(p);

		} catch (IOException e) {
			e.printStackTrace();
		}
		return fileContent;
	}

	@Test
	public void testDeserialize() throws JsonParseException, JsonMappingException, IOException {
		List<MovieLocalInfo> movies = mapper.readValue(movieResource.getFile(),
				new TypeReference<List<MovieLocalInfo>>() {
				});

		System.out.println(movies);
		List<MovieExtendedInfo> extMovies = movies.stream()
				.map(m -> {
					MovieExtendedInfo mei = MovieExtendedInfo.builder()
							.movie(m.getMovie())
							.image(loadImage(m.getImageFileName()))
							.build();
					return mei;
				})
				.collect(Collectors.toList());

		System.out.println(extMovies);
	}
}
