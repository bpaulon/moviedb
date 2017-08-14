package bcp.moviedb.redis.dbconfig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class RedisConfiguration {

	@Value("${redis.server.host:localhost}")
	String redisServerHost;

	@Value("${redis.server.port:6379 }")
	int redisServerPort;

	@Autowired
	ObjectMapper objectMapper;
	
	@Bean
	JedisConnectionFactory jedisConnectionFactory() {
		JedisConnectionFactory factory = new JedisConnectionFactory();
		factory.setHostName(redisServerHost);
		factory.setPort(redisServerPort);
		factory.setUsePool(true);

		log.info("Connecting to REDIS server {}:{}", redisServerHost, redisServerPort);
		return factory;
	}


	@Bean
	@Qualifier("RedisTemplate")
	RedisTemplate<String, Object> redisTemplate() {

		final RedisTemplate<String, Object> template = new RedisTemplate<String, Object>();
		template.setConnectionFactory(jedisConnectionFactory());

		// these are required to ensure keys and values are correctly serialized
		template.setKeySerializer(new StringRedisSerializer());
		
		// hashes are serialized as JSON containing the class name
		// we copy the default serialization features (related to LocalDate) from the primary mapper
		ObjectMapper ob = objectMapper.copy();
		// need to store the type of the serialized object in the JSON string. Jackson needs to know the type 
		// in order to deserialize the stored object
		ob.enableDefaultTyping(DefaultTyping.NON_FINAL, As.PROPERTY);
		template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer(ob));
		
		// values are serialized as strings
		template.setValueSerializer(new GenericToStringSerializer<Object>(Object.class));

		return template;
	}

}
