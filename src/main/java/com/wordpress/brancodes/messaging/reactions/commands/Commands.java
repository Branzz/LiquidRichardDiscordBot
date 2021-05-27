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
import net.dv8tion.jda.api.entities.*;

import java.awt.*;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.wordpress.brancodes.messaging.reactions.commands.Command.getCommandRegex;
import static com.wordpress.brancodes.messaging.reactions.ReactionChannelType.*;
import static com.wordpress.brancodes.messaging.reactions.UserCategory.*;
import static java.util.stream.Collectors.*;

public class Commands {

	public static final List<Command> commands = List.of(
		new Command(getCommandRegex("(((Turn)?\\s*Off)|(Shut\\s*(Down|Off|Up)))"), "Shut Down", "Shut Me Off",
					OWNER, GUILD_AND_PRIVATE, message -> {
					PreparedMessages.reply(message.getChannel(), message.getGuild().getIdLong(), "positive");
					message.getJDA().shutdown(); }),
		new Command(getCommandRegex("Help(\\s+(Me|Him|Her|Them|It|Every(\\s+One)?)(\\s+Out)?)?(\\s+Here)?(\\s+Right\\s+Now)?"), "Help", "Help On Commands (This Panel)", true,
					DEFAULT, GUILD_AND_PRIVATE, message ->
					PreparedMessages.replyEmbedMessage(message.getChannel(), "help")),
		new Command("\\s*![Ss]\\s*\\d{1,20}\\D[\\S\\s]+", "Say", OWNER, PRIVATE, message -> {
					String response = sendMessageCommand(message.getJDA(), message.getContentRaw());
					reply(message.getChannel(), response); }),
		new Command("\\s*![Dd]\\s*\\d{1,20}\\D[\\S\\s]+", "DM", OWNER, PRIVATE, message -> {
					String response = sendDirectMessageCommand(message.getJDA(), message.getContentRaw());
					reply(message.getChannel(), response); }),
		new Command("\\s*![Ss][Ss]\\s*", "Servers", OWNER, PRIVATE, message ->
					reply(message.getChannel(), "`" + message.getJDA().getGuilds().stream().map(Guild::getName).collect(joining("\n")) + "`")),
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
										otherMessage.getContentDisplay(),
										false));
								reply(message.getChannel(), embedBuilder.build());
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
		new Command(getCommandRegex("(The|A|An)\\s+(Mod|Moderator)s?\\s*(\\s((In\\s+(This|The)\\s+(Server|Guild|Place))|Here))?[?.]*\\s*", "((What|Who)\\s+(Are|Is)|(Whose|Who's|Whos|Who're))\\s+"),
					"Get Mods", "Who's A Mod In The Server",
					MOD, GUILD, message -> reply(message.getChannel(),
							 new EmbedBuilder().setDescription("Moderators In \"" + message.getGuild().getName() + "\"")
											   .addField("Moderators", String.join("\n", DataBase.getMods(message.getGuild().getIdLong()).get()), true)
											   .setThumbnail(message.getGuild().getIconUrl())
											   .setColor((Color) Config.get("embedColor"))
											   .build())),
		new Command(getCommandRegex("((((Make|Set|Give)\\s*)@.{1,32}\\s*(A\\s+)?(Mod|Moderator))|(((Make|Set|Give)\\s*)?(Mod|Moderator)\\s+@.{1,32}))"),
					"Give Mod", "Give Moderator", true,
					OWNER, GUILD, message -> reply(message.getChannel(),
							DataBase.addMod(message.getGuild().getIdLong(),
											message.getMentionedMembers().stream()
												   .map(Member::getIdLong) // TODO or filter by if it isn't a bot
												   .filter(memberID -> !memberID.equals(message.getJDA().getSelfUser().getIdLong()))
												   .findFirst()
												   .orElse(null),
											LiquidRichardBot.getUserName(message.getAuthor()))
									.getFeedback())),
		new Command(getCommandRegex("(((Remove|Re Move|Take)\\s*@.{1,32}\\s*('?s\\s+)?(Mod|Moderator))|(((Remove|Re Move|Take)\\s*)?(Mod|Moderator)\\s*@.{1,32}))"),
					"Remove Mod", "Remove Moderator",
					OWNER, GUILD, message -> reply(message.getChannel(),
							DataBase.removeMod(
									message.getGuild().getIdLong(),
									message.getMentionedMembers().stream()
										   .map(Member::getIdLong) // TODO or filter by if it isn't a bot
										   .filter(memberID -> !memberID.equals(message.getJDA().getSelfUser().getIdLong()))
										   .findFirst()
										   .orElse(null))
									.getFeedback())),
		new Command(getCommandRegex("((((Make|Set)\\s*)#.{1,32}\\s*((The|A)\\s+)?(Main\\s+Channel))|((Make|Set)\\s+((The|A)\\s+)?(Main\\s+Channel)\\s*#.{1,32}))"),
					"Main Channel", "Set Main Channel", true, MOD, GUILD, message -> {
			Optional<TextChannel> channel = message.getMentionedChannels().stream().findFirst();
			if (channel.isPresent()) {
				DataBase.setMainChannel(message.getGuild().getIdLong(), channel.get().getIdLong());
				Main.getBot().setGuildMainChannel(message.getGuild().getIdLong(), channel.get());
			}
		})

	);

	// private static String messageToString(Message message) {
	// 	return message.getAuthor().getName() + " on " + getDate(message.getTimeCreated()) + "\n" + message.getContentDisplay();
	// }

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

	public static void reply(final MessageChannel channel, final String reply) {
		channel.sendMessage(reply).queue();
	}

	public static void reply(final MessageChannel channel, final MessageEmbed reply) {
		channel.sendMessage(reply).queue();
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

}
