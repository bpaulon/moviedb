package bcp.moviedb.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
public class WebApplicationConfig extends WebMvcConfigurerAdapter {

	@Bean
	// primary JSON object mapper used by REST Controllers
	@SuppressWarnings({"squid:S1488"})
	public ObjectMapper objectMapper() {
		ObjectMapper mapper = Jackson2ObjectMapperBuilder.json().indentOutput(true)
				.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
				.build();
		
		// optionally register the JSR-301 module for deserialization of java.time.* types
		// @see ObjectMapper.registerModule(new JavaTimeModule())
		return mapper;
	}

	@Bean
	@Qualifier("yaml")
	// object mapper for YAML application resources (locally stored data)
	public ObjectMapper yamlMapper() {
		ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
		mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		
		// register the JSR-310 module for the deserialization of java.time.* types
		mapper.registerModule(new JavaTimeModule());
		return mapper;
	}
}