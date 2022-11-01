package com.wordpress.brancodes.util;

import com.wordpress.brancodes.messaging.reactions.ReactionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

public class Resourcer {

	private static final Logger LOGGER = LoggerFactory.getLogger(ReactionManager.class);

	private static final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();

	public static URL getURL(String filePath) {
		return contextClassLoader.getResource(filePath);
	}

}
