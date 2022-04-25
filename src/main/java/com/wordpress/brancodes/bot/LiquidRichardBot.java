package com.wordpress.brancodes.bot;

import com.wordpress.brancodes.main.ChannelOutputStream;
import com.wordpress.brancodes.main.Main;
import com.wordpress.brancodes.messaging.PreparedMessages;
import com.wordpress.brancodes.messaging.chats.ChatScheduler;
import com.wordpress.brancodes.messaging.chats.Chats;
import com.wordpress.brancodes.util.Config;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.PrintStream;
import java.net.URL;
import java.util.EnumSet;
import java.util.Map;
import java.util.Random;

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
				GatewayIntent.GUILD_VOICE_STATES,
				GatewayIntent.GUILD_MEMBERS
				// GatewayIntent.GUILD_PRESENCES
		)
				  .disableCache(EnumSet.of(
				  		CacheFlag.ROLE_TAGS
				  ))
				  .enableCache(EnumSet.of(
				  		CacheFlag.VOICE_STATE,
						CacheFlag.EMOTE
				  ))
				  .setChunkingFilter(ChunkingFilter.include(907042440924528662L, 910004207120183326L))
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

	private static final Map<Long, Long> guildMainChannels = Map.of(910004207120183326L, 966044757744824360L); // <Guild, TextChannel> ?

	public static TextChannel autodeleteLog;

	public Channel getMainChannel(final long guildID) {
		return jda.getTextChannelById(guildMainChannels.get(guildID));
	}

	public void cacheDependantInit() {
		// jda.getGuilds().forEach(guild -> guild.pruneMemberCache());
		guildChatSchedulers =
				jda.getGuildCache()
				   .applyStream(guilds -> guilds.filter(guild -> guildMainChannels.containsKey(guild.getIdLong()))
				   							    .collect(toMap(Guild::getIdLong, guild -> new ChatScheduler(
				   							    		new Chats(jda.getTextChannelById(guildMainChannels.get(guild.getIdLong()))), guild.getIdLong()))));

		LOGGER.info("In Servers: {}", jda.getGuilds().stream().map(Guild::getName).collect(joining(", ")));
		autodeleteLog = (TextChannel) Main.getBot().getJDA().getGuildChannelById(920653763130310706L);
		// jda.getGuildById(910004207120183326L).getTextChannelById(938608631086190642L).getIterableHistory().stream()
		//    .filter(m -> !(m instanceof SystemMessage) && m.getAuthor().getIdLong() == 849711011456221285L)
		//    // .peek(m -> System.out.println("checking " + m.getContentRaw()))
		//    .map(Message::getContentRaw)
		//    .filter(t -> Arrays.stream(t.split("\\s+")).anyMatch(word -> word.length() >= 2 && Character.isUpperCase(word.charAt(0)) && Character.isUpperCase(word.charAt(1))))
		//    .forEachOrdered(System.out::println);
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
		guildChatSchedulers.put(guildID, new ChatScheduler(new Chats(channel), guildID));
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

	// private static double denyOwner; TODO

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
