package com.ncba;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
public class CountryInfoServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CountryInfoServiceApplication.class, args);
	}

}
