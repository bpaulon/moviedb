package bcp.moviedb;

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
	public List<MovieExtendedInfo> search(@RequestParam(value = "word", required = true) List<String> words) {
		log.info("search movies by words: {}",words);
		
		MovieByStoryMatcher movieMatcher = beanFactory.getBean(MovieByStoryMatcher.class, words);
		return movieMatcher.match();
	}

}