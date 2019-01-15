package com.abhidev.agri;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.cassandra.CassandraAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import com.abhidev.agri.repository.CartRepository;

@SpringBootApplication
@EnableMongoRepositories(basePackageClasses = {CartRepository.class})
public class AgriApplication {

	public static void main(String[] args) {
		SpringApplication.run(AgriApplication.class, args);
	}

}

