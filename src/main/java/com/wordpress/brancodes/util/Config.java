package com.wordpress.brancodes.util;

import net.dv8tion.jda.api.JDA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.Properties;

public final class Config {

	private static final Logger LOGGER = LoggerFactory.getLogger(Config.class);

	private static final Object[] DEFAULT_KEYVALUES = {
			"token", "",
			"prefix", "",
			"suffix", "",
			"aliases", new String[] { "" },
			"aliasesRegex", "",
			"ownerID", 0, // Don't make a bot's ID
			"creatorID", 0,
			"embedColor", new Color(47, 49, 54) };
	private static final Properties CONFIG = new Properties();

	static {
		for (int i = 0; i < DEFAULT_KEYVALUES.length / 2; i++)
			CONFIG.put(DEFAULT_KEYVALUES[i * 2], DEFAULT_KEYVALUES[i * 2 + 1]);
		// try {
		// 	Main.getBot().getJDA().awaitReady();
		// } catch (InterruptedException e) {
		// 	LOGGER.warn("Interrupted exception while initializing");
		// 	e.printStackTrace();
		// }
	}

	public static Object get(Object key) {
		return CONFIG.get(key);
	}

	public static Properties getConfig() {
		return CONFIG;
	}

	public static void createJDADependantProperties(JDA jda) {
		jda.retrieveUserById((Long) Config.get("ownerID")).queue(u -> CONFIG.put("ownerUser", u));
		jda.retrieveUserById((Long) Config.get("creatorID")).queue(u -> CONFIG.put("creatorUser", u));
	}

}
