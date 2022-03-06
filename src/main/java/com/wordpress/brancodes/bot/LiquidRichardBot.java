package com.wordpress.brancodes.bot;

import com.wordpress.brancodes.main.Main;
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
import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

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
		// try {
		// 	jda.awaitReady();
		// } catch (InterruptedException e) {
		// 	LOGGER.warn("Interrupted exception while waiting for creating jda");
		// 	e.printStackTrace();
		// }
		denyCommands = true;
		denyChance = .2;
	}

	// public void addChats(final long guildID) {
	// 	guildChatSchedulers.put(guildID, new ChatScheduler(new Chats(guildID)));

	// }

	private static final Set<Long> enabledGuilds = Set.of(873658002710888448L);
	public static TextChannel autodeleteLog;

	public void cacheDependantInit() {
		guildChatSchedulers =
				jda.getGuildCache()
				   .applyStream(guilds -> guilds.filter(guild -> enabledGuilds.contains(guild.getIdLong()))
				   							    .collect(toMap(Guild::getIdLong, guild -> new ChatScheduler(new Chats(guild.getDefaultChannel())))));

		LOGGER.info("In Servers: {}", jda.getGuilds().stream().map(Guild::getName).collect(joining(", ")));
		autodeleteLog = (TextChannel) Main.getBot().getJDA().getGuildChannelById(920653763130310706L);

		// verifiedRole = jda.getGuildById(929974932417437726L).getRoles().stream().filter(n -> n.getName().equals("Verified")).findFirst().get();

		// setGuildMainChannel(722001554374131713L, jda.getTextChannelById(823025247883755531L));
		//setGuildMainChannel(873658002710888448L, jda.getTextChannelById(873658293493592074L));
		startUp();
	}

	private static final ClassLoader loader = Thread.currentThread().getContextClassLoader();

	// private Role verifiedRole;

	// public Role getVerifiedRole() {
	// 	return verifiedRole;
	// }

	private void startUp() {

		// Message m = event.retrieveMessage().complete();
		// final List<User> users = m.getReactions()
		// 						  .stream()
		// 						  .findFirst()
		// 						  .get()
		// 						  .retrieveUsers()
		// 						  .complete();
		// users.forEach(u -> System.out.println(
		// 		u + ":" + event.getGuild()
		// 					   .getMember(u)
		// 					   .getRoles()
		// 					   .stream()
		// 					   .map(Role::getName)
		// 					   .collect(Collectors.joining())));

		// jda.getTextChannelById(930031689953079336L).sendMessageEmbeds(
		// 		new EmbedBuilder().setTitle("Please read the rules below and click on :white_check_mark: to show you agree")
		// .addField(new MessageEmbed.Field ("Rules", "1. No spamming in server.\n\n" +
		// 		  "2. No disrespecting other members or any staff members.\n\n" +
		// 		  "3. No sexually explicit content, racism, harassing or NSFW content is allowed anywhere throughout the server.\n\n" +
		// 		  "4. Maintain professionalism.\n\n" +
		// 		  "5. No advertising, promoting, posting social media or referral links anywhere on the server. SPECIALLY OTHER DISCORDS!\n\n" +
		// 		  "6. Do your own research and due diligence. You are responsible for your trades so do not blindly follow others.\n\n" +
		// 		  "7. Do not post false information. This will result in a permanent ban.\n\n" +
		// 		  "8. Nothing posted is intended to be interpreted as trading advice.\n\n" +
		// 		  "9. No creation of other servers!! WILL BE ENFORCED!\n\n" +
		// 		  "10. Enjoy yourself. This community was built for you to be surrounded by a group of like-minded individuals with similar goals.\n\n" +
		// 		  "11. **Nothing here is investment advice.**", true)).build()).queue(
		// 		  		s -> s.addReaction("\u2705").queue());

		// try {
		// 	System.out.println(((TextChannel) jda.getGuildChannelById(ChannelType.TEXT, 907111693446950912L))
		// 			.getIterableHistory()
		// 			.takeAsync(2000)
		// 			.get()
		// 			.stream()
		// 			.filter(m -> m.getAuthor().getIdLong() == 704971546946699357L)
		// 			.mapToInt(m -> m.getContentDisplay().split("\\s").length)
		// 			.average()
		// 			.orElse(-1));
		// } catch (InterruptedException | ExecutionException e) {
		// 	e.printStackTrace();
		// }

		// final AtomicInteger num = new AtomicInteger(1);
		// jda.getSelfUser().getManager().setName("Crypto Uni Bot").queue();

		// jda.getGuildById(859544014034698290L).getTextChannelById(874954384377786369L)
		//    .sendMessage(new EmbedBuilder().setColor(Color.red).setDescription("\u2705 ***Sup.Zero was trolled***\n").build()).queue();

		// final PaginationAction.PaginationIterator<Message> messageHistoryIterator
		// if (messageHistoryIterator.hasNext()) {
		// 	Message mostRecentMessage = messageHistoryIterator.next();
		// 	mostRecentMessage.addReaction("U+1F449U+1F3FF").queue();
		// 	mostRecentMessage.addReaction("U+1F44CU+1F3FF").queue();
		// }

		// final Guild guild = jda.getGuildById(867541393966366720L);
		// jda.retrieveUserById(839640096030195712L).queue(user -> {
		// 	final TextChannel textChannelById = jda.getTextChannelById(867845016093458443L);
			// System.out.println(guild);
			// guild.retrieveMemberById(839640096030195712L).queue(m -> {
				// m.kick("Being A Fucking Swine.").queue(v -> {
				// 	textChannelById.sendMessage(new EmbedBuilder().setDescription("User Has Been Kicked").setColor(Color.RED).build()).queue();
				// });
			// });
		// });
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

	public void shutdownChatSchedulers() {
		guildChatSchedulers.values().forEach(ChatScheduler::shutdown);
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

}
