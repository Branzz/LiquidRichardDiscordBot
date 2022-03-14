package com.wordpress.brancodes.database.model;

import com.wordpress.brancodes.database.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BotService {

	private final UserRepository userRepository;

	@Autowired
	public BotService(final UserRepository botRepository) {
		this.userRepository = botRepository;
	}

	public List<User> getAllUsers() {
		return userRepository.findAll();
	}

}
