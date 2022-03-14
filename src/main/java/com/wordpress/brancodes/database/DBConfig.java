package com.wordpress.brancodes.database;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
@Configuration
// @PropertySource({ "cl"})
@EnableJpaRepositories("com.wordpress.brancodes.database.repositories")
public class DBConfig {

}
