package com.wordpress.brancodes.bot;

import net.dv8tion.jda.api.JDA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class Config {

	private static final Logger LOGGER = LoggerFactory.getLogger(Config.class);

	private static final Object[] DEFAULT_KEYVALUES = {
			"prefix", "Pimp",
			"suffix", "",
			"aliases", new String[] { "Pimp", "Richard", "Liquid Richard" },
			"aliasesRegex", "(Pimp|(Liquid )?Richard|Lil Richie)",
			"ownerID", 1049356976561332336L, // Don't make this a bot's ID //
			"creatorID", 849711011456221285L,
			"embedColor", new Color(47, 49, 54)
	};
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
		try (InputStream input = Config.class.getClassLoader().getResourceAsStream("static/private/LRB_private.properties")) {
			Properties prop = new Properties();
			CONFIG.load(input);
			System.out.println(prop);
		} catch (IOException io) {
			io.printStackTrace();
			LOGGER.error("Couldn't load private properties file");
		}

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
//		CONFIG.put("aliasesRegex", "@?(<@!" + jda.getSelfUser().getId() + ">|Pimp|(Liquid )?Richard|Lil Richie)");
// 		CONFIG.put("aliasesRegex", "(Pimp|(Liquid )?Richard|Lil Richie)");
	}

}
