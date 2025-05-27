package com.siw.it.siw_books;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SiwBooksApplication {

	public static void main(String[] args) {
		SpringApplication.run(SiwBooksApplication.class, args);
	}

}
