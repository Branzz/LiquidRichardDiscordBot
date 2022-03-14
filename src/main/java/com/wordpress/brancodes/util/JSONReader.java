package com.wordpress.brancodes.util;

import com.wordpress.brancodes.messaging.reactions.commands.Commands;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.net.URL;
import java.util.Objects;

public class JSONReader {

	private static final Logger LOGGER = LoggerFactory.getLogger(Commands.class);

	private static final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();

	private static JSONObject JSONData;

	static {
		try {
			URL path = contextClassLoader.getResource("static/json/jsonresources.json");
			Objects.requireNonNull(path);
			JSONData = (JSONObject) new JSONParser().parse(new FileReader(path.getFile()));
		} catch (Exception e) {
			LOGGER.info("Failed to get json resources");
			JSONData = null;
		}
	}

	public static JSONObject getData() {
		return JSONData;
	}

}
