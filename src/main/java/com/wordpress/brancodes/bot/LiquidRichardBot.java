package com.wordpress.brancodes.bot;

import com.wordpress.brancodes.database.DataBase;
import com.wordpress.brancodes.messaging.PreparedMessages;
import com.wordpress.brancodes.messaging.chat.ChatScheduler;
import com.wordpress.brancodes.messaging.chat.Chats;
import com.wordpress.brancodes.util.Config;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

/**
 * JDA wrapper
 */
public class LiquidRichardBot {

	private static final Logger LOGGER = LoggerFactory.getLogger(LiquidRichardBot.class);
	private static final Random random = new Random();

	private final JDA jda;
	private Map<Long, ChatScheduler> guildChatSchedulers;

	public LiquidRichardBot() throws LoginException, InterruptedException {
		jda =
		JDABuilder.createDefault(
				(String) Config.get("token"),
				GatewayIntent.DIRECT_MESSAGES,
				GatewayIntent.DIRECT_MESSAGE_TYPING,
				GatewayIntent.GUILD_MESSAGES,
				GatewayIntent.GUILD_MESSAGE_REACTIONS,
				GatewayIntent.GUILD_EMOJIS,
				GatewayIntent.GUILD_VOICE_STATES
		)
				  .disableCache(EnumSet.of(
				  		CacheFlag.ROLE_TAGS
				  ))
				  .enableCache(EnumSet.of(
				  		CacheFlag.VOICE_STATE,
						CacheFlag.EMOTE
				  ))
				  .setStatus(OnlineStatus.DO_NOT_DISTURB)
				  .addEventListeners(
				  		// new CommandClientBuilder()
										// 	 .setOwnerId(String.valueOf(Config.get("ownerID")))
										// 	 .setPrefix((String) Config.get("prefix"))
										// 	 .addCommands(new AboutCommand(Color.BLUE,
										// 								   "an example bot",
										// 								   new String[] { "Cool commands", "Nice examples", "Lots of fun!" }),
										// 				  new PingCommand(),
										// 				  new ShutdownCommand())
										// 	 .build(),
									 new Listener()
				  )
				  .setActivity(Activity.playing("Not Here To Play Games."))
				  .build();
		// try {
		// 	jda.awaitReady();
		// } catch (InterruptedException e) {
		// 	LOGGER.warn("Interrupted exception while waiting for creating jda");
		// 	e.printStackTrace();
		// }
		denyCommands = true;
		denyChance = .2;
		jda.getGuilds();
	}

	// public void addChats(final long guildID) {
	// 	guildChatSchedulers.put(guildID, new ChatScheduler(new Chats(guildID)));

	// }
	public void cacheDependantInit() {
		guildChatSchedulers = jda.getGuildCache()
								 .applyStream(guilds -> guilds.collect(
										 toMap(Guild::getIdLong,
											   guild -> new ChatScheduler(new Chats(guild.getDefaultChannel())))));
	}

	public void setGuildMainChannel(final long guildID, final TextChannel channel) {
		guildChatSchedulers.get(guildID).setMainChannel(channel);
	}

	public void addChats(final long guildID, final TextChannel channel) {
		guildChatSchedulers.put(guildID, new ChatScheduler(new Chats(channel)));
	}

	public void removeChats(final long guildID) {
		guildChatSchedulers.remove(guildID);
	}

	@Deprecated
	public String getUserName(final long id) {
		return getUserName(getUser(id));
	}

	public static String getUserName(User user) {
		return user.getName() + "#" + user.getDiscriminator();
	}

	/**
	 * This is completely wrong:
	 *
	 * This query will not finish before you use the immediate result,
	 * or process it beforehand (in {@link Config})
	 * or call after onReady
	 */
	@Deprecated
	public User getUser(final long id) {
		final AtomicReference<User> user = new AtomicReference<>();
		jda.retrieveUserById(id).queue(user::set);
		// try {
		// 	jda.awaitReady();
		// } catch (InterruptedException e) {
		// 	LOGGER.warn("Interrupted exception while waiting for retrieving a User");
		// 	e.printStackTrace();
		// }
		System.out.println(user.get());
		return user.get();
	}
	public JDA getJDA() {
		return jda;
	}
	private static boolean denyCommands;

	private static double denyChance;

	// private static double denyOwner;

	public static boolean deny(final MessageChannel channel, final Long guildID) {
		boolean deny = !denyCommands || random.nextDouble() < denyChance;
		if (deny)
			PreparedMessages.reply(channel, guildID, "talk back");
		return deny;
	}

}
