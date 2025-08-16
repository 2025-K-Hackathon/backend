package com.dajeong.dajeong;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Objects;

@SpringBootApplication
public class DajeongApplication {

	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
		
		// DB 설정
		System.setProperty("DB_URL", Objects.requireNonNull(dotenv.get("DB_URL")));
		System.setProperty("DB_USERNAME", Objects.requireNonNull(dotenv.get("DB_USERNAME")));
		System.setProperty("DB_PASSWORD", Objects.requireNonNull(dotenv.get("DB_PASSWORD")));

		// OpenAI 설정
		System.setProperty("openai.api.key", Objects.requireNonNull(dotenv.get("API_KEY")));
		System.setProperty("openai.api.base", Objects.requireNonNull(dotenv.get("API_BASE")));
		System.setProperty("openai.model.id", Objects.requireNonNull(dotenv.get("MODEL_ID")));

		// DeepL 설정
		System.setProperty("deepl.api.key", Objects.requireNonNull(dotenv.get("DEEPL_API_KEY")));
		SpringApplication.run(DajeongApplication.class, args);

	}
}
