package com.wordpress.brancodes.main;

import com.wordpress.brancodes.database.model.Guild;
import com.wordpress.brancodes.database.model.User;
import com.wordpress.brancodes.database.repositories.GuildRepository;
import com.wordpress.brancodes.database.repositories.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.wordpress.brancodes.database")
@ComponentScan("com.wordpress.brancodes.database.model")
@ComponentScan("com.wordpress.brancodes.database.repositories")
@EntityScan("com.wordpress.brancodes.database.model")
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Bean
	public CommandLineRunner run(UserRepository userRepository, GuildRepository guildRepository) {
		return args -> {
			final User user1 = new User(5L);
			userRepository.save(user1);
			userRepository.save(new User(6L));

			final Guild guild1 = new Guild(4L);

			guildRepository.save(guild1);
			System.out.println(userRepository.getById(user1.getID()));
			user1.addGuild(guild1);
			user1.getModdedGuilds().forEach(System.out::println);
		};
	}

}

/*
 */
