package com.wordpress.brancodes.bot;

import com.wordpress.brancodes.database.DataBase;
import com.wordpress.brancodes.main.ChannelOutputStream;
import com.wordpress.brancodes.main.Main;
import com.wordpress.brancodes.messaging.PreparedMessages;
import com.wordpress.brancodes.messaging.chats.ChatScheduler;
import com.wordpress.brancodes.messaging.chats.Chats;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
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
	private final PrintStream defaultOut;
	private Map<Long, ChatScheduler> guildChatSchedulers;
	private final Listener listener;

	public LiquidRichardBot() throws LoginException {
		listener = new Listener();
		jda =
		JDABuilder.createDefault(
				(String) Config.get("token"),
				GatewayIntent.DIRECT_MESSAGES,
				GatewayIntent.DIRECT_MESSAGE_TYPING,
				GatewayIntent.GUILD_MESSAGES,
				GatewayIntent.GUILD_MESSAGE_REACTIONS,
				GatewayIntent.DIRECT_MESSAGE_REACTIONS,
				GatewayIntent.GUILD_EMOJIS_AND_STICKERS,
				GatewayIntent.GUILD_VOICE_STATES,
				GatewayIntent.MESSAGE_CONTENT,
						// GatewayIntent.GUILD_PRESENCES
						GatewayIntent.GUILD_MEMBERS
		)
				  .disableCache(EnumSet.of(
				  		CacheFlag.ROLE_TAGS
				  ))
				  .enableCache(EnumSet.of(
				  		CacheFlag.VOICE_STATE,
						CacheFlag.EMOJI
				  ))
				  .setMemberCachePolicy(MemberCachePolicy.ALL)
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
						listener
				  )
				  .setActivity(Activity.streaming("My Shitty Gameplay", "https://www.youtube.com/watch?v=rrS5HDSBm1Y"))
				  .build();
		// final String name = "Liquid Richard";
		// if (!bot.getJDA().getSelfUser().getName().equals(name))
		// 	bot.setName(name);
		denyCommands = true;
		denyChance = .2;
		defaultOut = System.out;
	}


	// public void addChats(final long guildID) {
	// 	guildChatSchedulers.put(guildID, new ChatScheduler(new Chats(guildID)));
	// }

	private static final Map<Long, Long> guildMainChannels = Map.of(910004207120183326L, 910004207438954567L); // <Guild, TextChannel> ?

	public static Map<Long, TextChannel> autodeleteLogMap; // <guildId, auto-delete channel>

	public static TextChannel editChannel;

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
		autodeleteLogMap = DataBase.guildAutoDeleteChannelMap.entrySet()
															 .stream()
															 .collect(toMap(Map.Entry::getKey, e -> (TextChannel) Main.getBot()
																													  .getJDA()
																													  .getGuildChannelById(e.getValue())));
		// LOGGER.info("Commands: {}", jda.retrieveCommands().complete().stream().map(c -> c.getName() + " " + c.getId()).collect(joining(", ")));
		editChannel = Main.getBot().getJDA().getTextChannelById(1034944543205883995L);
		// jda.getPrivateChannels().forEach(System.out::println);

//		TextChannel channel = jda.getTextChannelById(907045483476836412L);
//		if (channel != null)
//			channel.getHistoryAfter(990030713199919154L, 100).queue(s ->
//						channel.getHistoryAfter(990096355173203989L, 100).queue(r -> {
//							List<Message> history = new ArrayList<>(s.getRetrievedHistory());
////							history.removeAll(r.getRetrievedHistory());
//							System.out.println(history.get(1).getContentRaw());
//							channel.purgeMessages(history);
//						})
//			);

		// new ColorChangingRole(981357094164889600L, 750L, Arrays.stream(new int[] { 0xF32D2D, 0xF55916, 0xFFD61F, 0x1CBD0D, 0x3D51FF, 0xB53DFF })
		// 														.mapToObj(Color::new)
		// 														.toArray(Color[]::new))
		// 		.start();

		// jda.getGuildById(910004207120183326L).getTextChannelById(938608631086190642L).getIterableHistory().stream()
		//    .filter(m -> !(m instanceof SystemMessage) && m.getAuthor().getIdLong() == 849711011456221285L)
		//    // .peek(m -> System.out.println("checking " + m.getContentRaw()))
		//    .map(Message::getContentRaw)
		//    .filter(t -> Arrays.stream(t.split("\\s+")).anyMatch(word -> word.length() >= 2 && Character.isUpperCase(word.charAt(0)) && Character.isUpperCase(word.charAt(1))))
		//    .forEachOrdered(System.out::println);
	}

	public void setConsole(TextChannel textChannel) {
		LOGGER.info("Setting sysout to log channel...");
		System.setOut(new PrintStream(new ChannelOutputStream(textChannel)));
		LOGGER.info("Set sysout to log channel");
	}

	public void setConsoleIDE() {
		LOGGER.info("Setting sysout to console...");
		System.setOut(defaultOut);
		LOGGER.info("Set sysout to console");
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
		return user == null ? "null " : user.getName() + "#" + user.getDiscriminator();
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

	public void pause() {
		listener.pause();
	}

}
