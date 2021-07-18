package com.wordpress.brancodes.messaging.reactions.commands;

import com.wordpress.brancodes.bot.LiquidRichardBot;
import com.wordpress.brancodes.main.Main;
import com.wordpress.brancodes.messaging.PreparedMessages;
import com.wordpress.brancodes.database.DataBase;
import com.wordpress.brancodes.messaging.reactions.UserCategory;
import com.wordpress.brancodes.util.Config;
import com.wordpress.brancodes.util.Queues;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.internal.utils.PermissionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.wordpress.brancodes.messaging.reactions.commands.Command.getCommandRegex;
import static com.wordpress.brancodes.messaging.reactions.ReactionChannelType.*;
import static com.wordpress.brancodes.messaging.reactions.UserCategory.*;
import static java.util.stream.Collectors.*;

public class Commands {

	private static final Logger LOGGER = LoggerFactory.getLogger(Commands.class);
	// private static final @RegEx String fillerChars = "[.,\\s;:]*";

	public static int qCount = 0;
	final static boolean reactionQuestions = false;

	private static final String[] YAWN_EMOJIS = new String[] { "yawn~1:864990663438106624", "yawn~2:864990672209444864", "yawn~3:864990679645814825", "yawn~4:864990712919883804", "yawn~5:864990744786763797", "yawn~6:864990758711721995", "yawn~7:864990776757190656", "yawn~8:864990782707204108", "yawn~9:864990787028647966", "yawn~10:864990791700578345", "yawn~11:864990796088737792", "yawn~12:864990799086747668", "yawn~13:864990801867964468", "yawn~14:864990805293793310", "yawn~15:864990808766414899" };

	public static final List<Command> commands = List.of(
		new Command(getCommandRegex("(((Turn)?\\s*Off)|(Shut\\s*(Down|Off|Up)))"), "Shut Down", "Shut Me Off",
					OWNER, GUILD_AND_PRIVATE, message -> {
			PreparedMessages.reply(message, message.getGuild().getIdLong(), "positive");
			message.getJDA().shutdown();
			Main.getBot().shutdownChatSchedulers();
			System.exit(0); }),
		// new Command(getCommandRegex("Help(\\s+(Me|Him|Her|Them|It|Every(\\s+One)?)(\\s+Out)?)?(\\s+Here)?(\\s+Right\\s+Now)?"), "Help", "Help On Commands (This Panel)", true,
		// 			DEFAULT, GUILD_AND_PRIVATE, message -> PreparedMessages.replyEmbedMessage(message, "help")),
		new Command("\\s*![Ss]\\s*\\d{1,20}\\D[\\S\\s]+", "Say", OWNER, PRIVATE, message -> {
			String response = sendMessageCommand(message.getJDA(), message.getContentRaw());
			reply(message, response); }),
		new Command("\\s*![Dd]\\s*\\d{1,20}\\D[\\S\\s]+", "DM", OWNER, PRIVATE, message -> {
			String response = sendDirectMessageCommand(message.getJDA(), message.getContentRaw());
			reply(message, response); }),
		new Command("\\s*![Jj]\\s*\\d{1,20}[\\S\\s]*", "Join", OWNER, PRIVATE, message -> {
			String response = joinVoiceChannel(message.getJDA(), message.getContentRaw());
			reply(message, response); }),
		new Command("\\s*![Ss][Ss]\\s*", "Servers", OWNER, PRIVATE, message ->
					reply(message, "`" + message.getJDA().getGuilds().stream().map(Guild::getName).collect(joining("\n")) + "`")),
		new Command("\\s*![Cc]\\s*", "Get Channels", OWNER, GUILD, message -> {
			System.out.printf("Channels in %s: %s\n", message.getGuild(),
					message.getGuild().getTextChannels().stream().map(c -> c.getName() + ":" + hasPermission(c, Permission.VIEW_CHANNEL)).collect(Collectors.joining(", ")));
			// message.getGuild().getTextChannels().stream().filter(c -> c.getName().equals("general")).findFirst().get()
			// 			   .getHistory().retrievePast(15).queue(messageHistory -> {
			// 	messageHistory.stream().map(otherMessage ->  otherMessage.getAuthor().getName()
			// 						+ " " + timeStampOf(otherMessage.getTimeCreated()) + "\n" + otherMessage.getContentDisplay()
			// 						+ otherMessage.getAttachments().stream().map(Message.Attachment::getUrl).collect(joining("\n,"))).forEach(System.out::println);
			// 			   });
		}),
		new Command("\\s*![Hh]\\s+\\d{1,20}\\s+\\d+\\s*", "DM History", OWNER, PRIVATE, message -> {
			String[] messageParts = message.getContentRaw().split("\\s+");
			message.getJDA().retrieveUserById(messageParts[1]).queue(user -> {
				user.openPrivateChannel().queue(privateChannel -> {
					int amount = Integer.parseInt(messageParts[2]);
					privateChannel.getHistory().retrievePast(amount).queue(messageHistory -> {
						EmbedBuilder embedBuilder = new EmbedBuilder().setDescription("Message History With " + LiquidRichardBot.getUserName(user))
																	  .setThumbnail(user.getAvatarUrl())
																	  .setColor((Color) Config.get("embedColor"));
						messageHistory.forEach(otherMessage -> embedBuilder.addField(
								otherMessage.getAuthor().getName() + " " + timeStampOf(otherMessage.getTimeCreated()),
								otherMessage.getContentDisplay() + "\n" +
								otherMessage.getAttachments().stream().map(Message.Attachment::getUrl).collect(joining(", ")),
								false));
						reply(message, embedBuilder.build());
					});
				});
			});
	// AtomicReference<User> other = getUser(message.getJDA(), messageParts[1]); // The order of queueing matters
	// 				AtomicReference<String> history = getPrivateMessageHistory(other.get(), Integer.parseInt(messageParts[2]));
	// 				reply(message.getChannel(), new EmbedBuilder().setDescription("Message History With \"" + LiquidRichardBot.getUserName(other.get()) + "\"")
	// 															  .addField("Moderators", history.get(), true)
	// 															  .setThumbnail(message.getGuild().getIconUrl())
	// 															  .setColor((Color) Config.get("embedColor"))
	// 															  .build());
		}),
		// new Command("000", "Birthday", "Celebrate The Princess's Birthday", true, MOD, GUILD, message -> {
		// 	AudioManager audioManager = message.getGuild().getAudioManager();
		// 	if (audioManager.isConnected())
		// 		;
		// 	GuildVoiceState guildVoiceState = message.getMember().getVoiceState();
		// 	VoiceChannel voiceChannel = guildVoiceState.getChannel();
		// 	Member selfMember = message.getGuild().getSelfMember();
		// 	if (selfMember.hasPermission(voiceChannel, Permission.VOICE_CONNECT))
		// 		;
		// 	audioManager.openAudioConnection(voiceChannel);
		// 	PlayerManager.loadAndPlay(message.getGuild(), "https://www.youtube.com/watch?v=paSQ9mcOUME");
		// 	// audioManager.closeAudioConnection();
		// }),
		// new Command(getCommandRegex("(The|A|An)\\s+(Mod|Moderator)s?\\s*(\\s((In\\s+(This|The)\\s+(Server|Guild|Place))|Here))?[?.]*\\s*", "((What|Who)\\s+(Are|Is)|(Whose|Who's|Whos|Who're))\\s+"),
		// 			"Get Mods", "Who's A Mod In The Server",
		// 			MOD, GUILD, message -> reply(message,
		// 					 new EmbedBuilder().setDescription("Moderators In \"" + message.getGuild().getName() + "\"")
		// 									   .addField("Moderators", String.join("\n", DataBase.getMods(message.getGuild().getIdLong()).get()), true)
		// 									   .setThumbnail(message.getGuild().getIconUrl())
		// 									   .setColor((Color) Config.get("embedColor"))
		// 									   .build())),
		// new Command(getCommandRegex("((((Make|Set|Give)\\s*)@.{1,32}\\s*(A\\s+)?(Mod|Moderator))|(((Make|Set|Give)\\s*)?(Mod|Moderator)\\s+@.{1,32}))"),
		// 			"Give Mod", "Give Moderator", true,
		// 			OWNER, GUILD, message -> reply(message,
		// 					DataBase.addMod(message.getGuild().getIdLong(),
		// 									message.getMentionedMembers().stream()
		// 										   .map(Member::getIdLong) // TODO or filter by if it isn't a bot
		// 										   .filter(memberID -> !memberID.equals(message.getJDA().getSelfUser().getIdLong()))
		// 										   .findFirst()
		// 										   .orElse(null),
		// 									LiquidRichardBot.getUserName(message.getAuthor()))
		// 							.getFeedback())),
		// new Command(getCommandRegex("(((Remove|Re Move|Take)\\s*@.{1,32}\\s*('?s\\s+)?(Mod|Moderator))|(((Remove|Re Move|Take)\\s*)?(Mod|Moderator)\\s*@.{1,32}))"),
		// 			"Remove Mod", "Remove Moderator",
		// 			OWNER, GUILD, message -> reply(message,
		// 					DataBase.removeMod(
		// 							message.getGuild().getIdLong(),
		// 							message.getMentionedMembers().stream()
		// 								   .map(Member::getIdLong) // TODO or filter by if it isn't a bot
		// 								   .filter(memberID -> !memberID.equals(message.getJDA().getSelfUser().getIdLong()))
		// 								   .findFirst()
		// 								   .orElse(null))
		// 							.getFeedback())),
		// new Command(getCommandRegex("((((Make|Set)\\s*)#.{1,32}\\s*((The|A)\\s+)?(Main\\s+Channel))|((Make|Set)\\s+((The|A)\\s+)?(Main\\s+Channel)\\s*#.{1,32}))"),
		// 			"Main Channel", "Set Main Channel", true, MOD, GUILD, message -> {
		// 	Optional<TextChannel> channel = message.getMentionedChannels().stream().findFirst();
		// 	if (channel.isPresent()) {
		// 		DataBase.setMainChannel(message.getGuild().getIdLong(), channel.get().getIdLong());
		// 		Main.getBot().setGuildMainChannel(message.getGuild().getIdLong(), channel.get());
		// 	}
		// }),
		new Command(".*" + censorChainRegex("america, amerimut, kek, based, healthcare, capitali, :gooner:", "[\\s.,]*") + ".*", "Censor", "Automatic Delete/Censorship", //".*([Bb][.,\\s;:]*[Rr][.,\\s;:]*[Aa][.,\\s;:]*[Nn][.,\\s;:]*[Dd][.,\\s;:]*[Oo0]).*"
					CENSORED, GUILD, message -> {
			// if (message.getGuild().getIdLong() == 793333500303769600L)
						System.out.println("delete");
				// message.delete().queue();
		}),
		new Command("me and who", "Me&Whom", DEFAULT, GUILD_AND_PRIVATE, message -> {
			if (message.getAuthor().getIdLong() == 533704679495041034L)
				reply(message, "Me And Whom.*");
		}),
		new Command(".*", "Yawn", YAWN, GUILD_AND_PRIVATE, message -> {
			// if (message.getAuthor().getIdLong() == 749625271937663027L)
			if (!message.isFromGuild() || hasPermission(message.getTextChannel(), Permission.MESSAGE_EXT_EMOJI))
				for (final String yawnEmoji : YAWN_EMOJIS) {
					message.addReaction(yawnEmoji).queue();
				}
			else {
				if (hasPermission(message.getTextChannel(), Permission.MESSAGE_ADD_REACTION))
					message.addReaction("U+1F971").queue();
				logMissingChannelPermissions(message.getTextChannel());
			}
		})
		// new Command("((.*(\\?|:(grey_)?question:|\u2753|\u2754))"
		// 			+ "|([,;:<>&^%$#@!{}\\[\\]=/\\-.*+()_\\s]*(\\?|:(grey_)?question:|\u2753|\u2754)))[,;:<>&^%$#@!{}\\[\\]=/\\-.*+()_\\s]*"
		// 			+ "|([,;:<>&^%$#@!{}\\[\\]=/\\-.*+()_\\s]*(([Oo]+[Mm]+[Gg]+|[Ww]+[Aa]+[Ii]+[Tt]+|[Oo]+[Hh]*)\\s+)?" //([Cc][\s]*[Aa][\s]*[Nn])
		// 			+ "((([Ww][\\s]*[Hh][\\s]*([Ii][\\s]*[Cc][\\s]*[Hh]|[Oo]+|[Aa]+[\\s]*[Tt]|[Ee][\\s]*[Rr][\\s]*[Ee]|[Ee][\\s]*[Nn]+|[Yy]+))|[Hh][\\s]*[Oo]+[\\s]*[Ww])"
		// 			+ "('?[Ss]|[Ii][Ss]|[Aa][Rr][Ee])?([\\s+].*|[,;:<>&^%$#@!{}\\[\\]=/\\-.*+()_\\s])?)"
		// 			+ "|([Rr]+[Ee]+[Aa]+[Ll]+[Yy]+[.?;:\\s]*))", "Questions", //Hey Pimp, Can You Help Me
		// 			DEFAULT, GUILD_AND_PRIVATE, message -> {
		// 	if (!message.getContentRaw().matches("[,;:<>&^%$#@!{}\\[\\]=/\\-.*+()_\\s]*[Ww][Hh][Aa][Tt][.?]*\\s+([Aa][Nn]?[^?]*|[Tt][Hh][Ee]\\s+([Ff][Uu]?[Cc]?[Kk]?|[Hh][Ee]+[Ll]+))[,;:<>&^%$#@!{}\\[\\]=/\\-.*+()_\\s]*")) {
		// 		// qCount++;
		// 		if (reactionQuestions) {
		// 			message.addReaction("U+1F1F3").queue();
		// 			message.addReaction("U+1F1ED").queue();
		// 			message.addReaction("U+1F1F9").queue();
		// 			message.addReaction("U+1F1E6").queue();
		// 			message.addReaction("U+1F1F6").queue();
		// 			// message.addReaction("NHTAQ:864184033046298656").queue();
		// 		}
		// 		else
		// 			reply(message, "Not Here To Answer Questions.");
		// 	}
		// })
	);

	// private static String messageToString(Message message) {
	// 	return message.getAuthor().getName() + " on " + getDate(message.getTimeCreated()) + "\n" + message.getContentDisplay();
	// }

	private static String censorChainRegex(String words, String joiner) {
		return censorChainRegex(words.split("\\s*,\\s*"), joiner);
	}

	private static String censorChainRegex(String[] words, String joiner) {
		return orChainRegex(Arrays.stream(words).map(w -> censorRegex(w,joiner)).toArray(String[]::new));
	}

	private static String orChainRegex(String[] chain) {
		StringBuilder sb = new StringBuilder();
		sb.append('(');
		for (String part: chain)
			sb.append(part)
			  .append('|');
		sb.delete(sb.length() - 1, sb.length());
		sb.append(')');
		return sb.toString();
	}

	private static String censorRegex(String word, String joiner) {
		return word.chars().mapToObj(c -> "[" + Character.toUpperCase((char) c) + Character.toLowerCase((char) c) + "]").collect(joining(joiner));
	}

	private static String timeStampOf(final OffsetDateTime date) {
		return DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss").format(date);
	}

	public static final Map<ChannelType, Map<UserCategory, Set<Command>>> commandsByCategoryChannel =
			Stream.of(ChannelType.values()).collect(toMap(channelType -> channelType, channelType ->
			Stream.of(UserCategory.values()).collect(toMap(userCategory -> userCategory, userCategory ->
					commands.stream()
							.filter(cT -> cT.getChannelType().inRange(channelType))
							.filter(uC -> uC.getUserCategory().inRange(userCategory))
							.collect(toSet())))));

	public static void reply(final Message message, final String reply) {
		if (message.getChannelType() == ChannelType.TEXT)
			reply(message.getTextChannel(), reply);
		else
			message.getChannel().sendMessage(reply).queue();
	}

	public static void reply(final Message message, final MessageEmbed reply) {
		if (message.getChannelType() == ChannelType.TEXT)
			reply(message.getTextChannel(), reply);
		else
			message.getChannel().sendMessage(reply).queue();
	}

	public static void reply(final TextChannel channel, final String reply) {
		if (canUseChannel(channel))
			channel.sendMessage(reply).queue();
		else
			logMissingChannelPermissions(channel);
	}

	public static void reply(final TextChannel channel, final MessageEmbed reply) {
		if (canUseChannel(channel))
			channel.sendMessage(reply).queue();
		else
			logMissingChannelPermissions(channel);
	}

	private static boolean canUseChannel(final TextChannel channel) {
		return hasPermission(channel, Permission.MESSAGE_WRITE);
	}

	private static boolean hasPermission(final TextChannel channel, Permission permission) {
		return PermissionUtil.checkPermission(channel, Objects.requireNonNull(channel.getGuild().getMember(channel.getJDA().getSelfUser())),
											  permission);
	}

	private static void logMissingChannelPermissions(final TextChannel channel) {
		LOGGER.info("Tried to do command in {} in {}, but is missing permissions", channel, channel.getGuild());

	}

	/**
	 * @return private message back to indicate status
	 */
	@Queues
	private static String sendMessageCommand(final JDA jda, final String message) {
		String[] messageParts = message.split("\\s+");
		int messageStart = 1;
		long channelID = 0L;
		if (Character.isDigit(message.charAt(2))) {
			channelID = Long.parseLong(messageParts[0].substring(2));
		} else {
			for (; messageStart < messageParts.length; messageStart++)
				if (messageParts[messageStart].length() > 0 && Character.isDigit(messageParts[messageStart].charAt(0))) {
					try {
						channelID = Long.parseLong(messageParts[messageStart++]);
					} catch (NumberFormatException e) {
						return "Typo In Channel ID.";
					}
					break;
				}
		}
		if (channelID == 0)
			return "No Channel ID Reference.";
		final TextChannel textChannelById = jda.getTextChannelById(channelID);
		if (textChannelById == null)
			return "I Was Not Able To Find That Channel \"" + channelID + "\".";
		else {
			textChannelById.sendMessage(IntStream.range(messageStart, messageParts.length)
												 .collect(StringBuilder::new, (sb, index) -> sb.append(messageParts[index]).append(" "), StringBuilder::append)
												 .toString()
												 .trim()).queue();
		}
		return PreparedMessages.getMessage("positive") + " Sent To: \"#" + textChannelById.getName()
			   + "\" Channel In \"" + textChannelById.getGuild().getName() + "\" Server.";
	}

	@Queues
	private static String sendDirectMessageCommand(final JDA jda, final String message) {
		String[] messageParts = message.split("\\s+");
		int messageStart = 1;
		long userID = 0L;
		if (Character.isDigit(message.charAt(2))) {
			userID = Long.parseLong(messageParts[0].substring(2));
		} else {
			for (; messageStart < messageParts.length; messageStart++)
				if (messageParts[messageStart].length() > 0 && Character.isDigit(messageParts[messageStart].charAt(0))) {
					try {
						userID = Long.parseLong(messageParts[messageStart++]);
					} catch (NumberFormatException e) {
						return "Typo In User ID.";
					}
					break;
				}
		}
		if (userID == 0)
			return "No User ID Reference.";

		final int finalMessageStart = messageStart;
		jda.retrieveUserById(userID).queue(user -> {
			if (user != null) {
				user.openPrivateChannel().queue(privateChannel ->
						privateChannel.sendMessage(IntStream.range(finalMessageStart, messageParts.length)
									  .collect(StringBuilder::new, (sb, index) -> sb.append(messageParts[index]).append(" "), StringBuilder::append)
									  .toString()
									  .trim()).queue());
			}
		}, user -> {

		});
		/*		final AtomicReference<User> user = new AtomicReference<>();
		final int finalMessageStart = messageStart;
		jda.retrieveUserById(userID).queue(user::set);
			if (user.get() != null) {
				user.get().openPrivateChannel().queue(privateChannel ->
						privateChannel.sendMessage(IntStream.range(finalMessageStart, messageParts.length)
									  .collect(StringBuilder::new, (sb, index) -> sb.append(messageParts[index]).append(" "), StringBuilder::append)
									  .toString()
									  .trim()).queue());
			} else {
				return "I Was Not Able To Find That User \"" + userID + "\".";
			}
			*/
		return PreparedMessages.getMessage("positive") + " Will Send Unless User Was Not Found.";
	}

	@Queues
	private static String joinVoiceChannel(final JDA jda, final String message) {
		String[] messageParts = message.split("\\s+");
		int messageStart = 1;
		long voiceChannelID = 0L;
		if (Character.isDigit(message.charAt(2))) {
			voiceChannelID = Long.parseLong(messageParts[0].substring(2));
		} else {
			for (; messageStart < messageParts.length; messageStart++)
				if (messageParts[messageStart].length() > 0 && Character.isDigit(messageParts[messageStart].charAt(0))) {
					try {
						voiceChannelID = Long.parseLong(messageParts[messageStart++]);
					} catch (NumberFormatException e) {
						return "Typo In Voice Channel ID.";
					}
					break;
				}
		}
		if (voiceChannelID == 0L)
			return "No Voice Channel ID Reference.";
		final VoiceChannel voiceChannel = jda.getVoiceChannelById(voiceChannelID);
		voiceChannel.getGuild().getAudioManager().openAudioConnection(voiceChannel);
		return PreparedMessages.getMessage("positive") + " Will Send Unless User Was Not Found.";
	}

}
