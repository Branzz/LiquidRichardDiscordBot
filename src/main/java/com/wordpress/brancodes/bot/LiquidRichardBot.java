package com.wordpress.brancodes.bot;

import com.wordpress.brancodes.main.ChannelOutputStream;
import com.wordpress.brancodes.main.Main;
import com.wordpress.brancodes.messaging.PreparedMessages;
import com.wordpress.brancodes.messaging.chats.ChatScheduler;
import com.wordpress.brancodes.messaging.chats.Chats;
import com.wordpress.brancodes.messaging.reactions.Reactions;
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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.net.URL;
import java.util.*;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;

/**
 * JDA wrapper
 */
public class LiquidRichardBot {

	private static final Logger LOGGER = LoggerFactory.getLogger(LiquidRichardBot.class);
	private static final Random random = new Random();

	private final JDA jda;
	private Map<Long, ChatScheduler> guildChatSchedulers;

	public LiquidRichardBot() throws LoginException {
		jda =
		JDABuilder.createDefault(
				(String) Config.get("token"),
				GatewayIntent.DIRECT_MESSAGES,
				GatewayIntent.DIRECT_MESSAGE_TYPING,
				GatewayIntent.GUILD_MESSAGES,
				GatewayIntent.GUILD_MESSAGE_REACTIONS,
				GatewayIntent.DIRECT_MESSAGE_REACTIONS,
				GatewayIntent.GUILD_EMOJIS,
				GatewayIntent.GUILD_VOICE_STATES
//				GatewayIntent.GUILD_MEMBERS,
						// GatewayIntent.GUILD_PRESENCES
		)
				  .disableCache(EnumSet.of(
				  		CacheFlag.ROLE_TAGS
				  ))
				  .enableCache(EnumSet.of(
				  		CacheFlag.VOICE_STATE,
						CacheFlag.EMOTE
				  ))
				  .setStatus(OnlineStatus.ONLINE)
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
				  .setActivity(Activity.streaming("My Shitty Gameplay", "https://www.youtube.com/watch?v=rrS5HDSBm1Y"))
				  .build();

		// final String name = "Liquid Richard";
		// if (!bot.getJDA().getSelfUser().getName().equals(name))
		// 	bot.setName(name);

		denyCommands = true;
		denyChance = .2;
	}

	// public void addChats(final long guildID) {
	// 	guildChatSchedulers.put(guildID, new ChatScheduler(new Chats(guildID)));
	// }

	private static final Set<Long> enabledGuilds = Set.of(910004207120183326L);
	public static TextChannel autodeleteLog;

	public void cacheDependantInit() {
		guildChatSchedulers =
				jda.getGuildCache()
				   .applyStream(guilds -> guilds.filter(guild -> enabledGuilds.contains(guild.getIdLong()))
				   							    .collect(toMap(Guild::getIdLong, guild -> new ChatScheduler(new Chats(guild.getSystemChannel())))));

		LOGGER.info("In Servers: {}", jda.getGuilds().stream().map(Guild::getName).collect(joining(", ")));
		autodeleteLog = (TextChannel) Main.getBot().getJDA().getGuildChannelById(920653763130310706L);

		/* send starboard embed messages to test channel */

		// final TextChannel channel = jda.getGuildById(722001554374131713L)
		// 							.getTextChannels()
		// 							.stream()
		// 							.filter(c -> c.getName()
		// 										  .equals("test"))
		// 							.findFirst()
		// 							.get();
		// jda.getGuildById(910004207120183326L)
		//    .getTextChannels()
		//    .stream()
		//    .filter(c -> c.getName().equals("starboard"))
		//    .findFirst()
		//    .ifPresent(c -> c.getIterableHistory().forEachAsync(m -> {
		// 	   channel.sendMessage(m).queue();
		// 	   m.getAttachments().forEach(a -> a.);
		// 		// System.out.println(
		// 		// 		m.getAuthor().getName() + " " + Reactions.timeStampOf(m.getTimeCreated()) + "\n" +
		// 		// 		m.getContentDisplay() + "\n" +
		// 		// 		m.getAttachments().stream().map(m -> {
		// 		// 			if (m.isImage() || m.isVideo()) {
		// 		// 				m.getUrl()
		// 		// 			} else {
		// 		// 				m.
		// 		// 			}
		// 		// 		}).collect(joining(", "))
		// 		// );
		// 		return true;
		//    }));


		// setConsole();

		// LOGGER.info("a\nbb\ncccccc\nbth");
		// jda.retrieveUserById(955111291272450048L).queue();
//		System.out.println(Reactions.censoredWordsRegex);
//		System.out.println(UserCategory.getUserCategory(jda, jda.getSelfUser()));

		// verifiedRole = jda.getGuildById(929974932417437726L).getRoles().stream().filter(n -> n.getName().equals("Verified")).findFirst().get();

		// setGuildMainChannel(722001554374131713L, jda.getTextChannelById(823025247883755531L));
		//setGuildMainChannel(873658002710888448L, jda.getTextChannelById(873658293493592074L));
	}

	private void setConsole() {
		LOGGER.info("Setting sysout to log channel...");
		System.setOut(new PrintStream(new ChannelOutputStream(jda.getTextChannelById(955111291272450048L))));
	}

	private static final ClassLoader loader = Thread.currentThread().getContextClassLoader();

	public void setGuildMainChannel(final long guildID, final TextChannel channel) {
		guildChatSchedulers.get(guildID).setMainChannel(channel);
	}

	public void addChats(final long guildID, final TextChannel channel) {
		guildChatSchedulers.put(guildID, new ChatScheduler(new Chats(channel)));
	}

	public void removeChats(final long guildID) {
		guildChatSchedulers.remove(guildID);
	}

	public void shutdownChatSchedulers() {
		guildChatSchedulers.values().forEach(ChatScheduler::shutdown);
	}

	public static String getUserName(User user) {
		return user.getName() + "#" + user.getDiscriminator();
	}

	public JDA getJDA() {
		return jda;
	}

	private static boolean denyCommands;

	private static double denyChance;

	// private static double denyOwner;

	public static boolean deny(final Message message) {
		return deny(message, message.isFromGuild() ? message.getGuild().getIdLong() : null);
	}

	public static boolean deny(final Message message, final Long guildID) {
		boolean deny = !denyCommands || random.nextDouble() < denyChance;
		if (deny)
			PreparedMessages.reply(message, guildID, "talk back");
		return deny;
	}

	public void setName(final String name) {
		jda.getSelfUser().getManager().setName(name).queue();
	}

	public void setProfilePicture(final String fileName) {
		try {
			URL path = loader.getResource(fileName);
			File pfp = new File(path.getFile());
			jda.getSelfUser().getManager().setAvatar(Icon.from(pfp)).queue();
		} catch (Exception e) {
			LOGGER.info("Failed to get pfp");
		}
	}

	void findUser(String tag) {
		for (int i = 1; i <= 9999; i++) {
			String s = String.valueOf(i);
			final User userByTag = jda.getUserByTag(tag, "0".repeat(4 - s.length()) + s);
			System.out.println(tag + ": " + (userByTag != null ? userByTag.getId() : "Not found"));
		}
	}

}
