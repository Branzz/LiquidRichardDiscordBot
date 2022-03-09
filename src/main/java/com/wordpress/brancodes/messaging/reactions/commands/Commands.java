package com.wordpress.brancodes.messaging.reactions.commands;

import com.wordpress.brancodes.bot.LiquidRichardBot;
import com.wordpress.brancodes.database.DataBase;
import com.wordpress.brancodes.database.voice.PlayerManager;
import com.wordpress.brancodes.main.Main;
import com.wordpress.brancodes.messaging.PreparedMessages;
import com.wordpress.brancodes.messaging.reactions.Reaction;
import com.wordpress.brancodes.messaging.reactions.ReactionChannelType;
import com.wordpress.brancodes.messaging.reactions.users.UserCategory;
import com.wordpress.brancodes.util.Config;
import com.wordpress.brancodes.util.JSONReader;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.internal.utils.PermissionUtil;
import org.apache.commons.collections4.Get;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.RegEx;
import java.awt.*;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.wordpress.brancodes.messaging.reactions.Reaction.getMatcher;
import static com.wordpress.brancodes.messaging.reactions.ReactionChannelType.*;
import static com.wordpress.brancodes.messaging.reactions.users.UserCategory.*;
import static com.wordpress.brancodes.messaging.reactions.commands.Command.getCommandRegex;
import static java.util.stream.Collectors.*;

public class Commands {

	private static final Logger LOGGER = LoggerFactory.getLogger(Commands.class);

	public static int qCount = 0;
	final static boolean reactionQuestions = false;

	private static final String[] YAWN_EMOJIS = new String[] { "yawn~1:864990663438106624", "yawn~2:864990672209444864",
			"yawn~3:864990679645814825", "yawn~4:864990712919883804", "yawn~5:864990744786763797"/*, "yawn~6:864990758711721995",
			"yawn~7:864990776757190656", "yawn~8:864990782707204108", "yawn~9:864990787028647966", "yawn~10:864990791700578345",
			"yawn~11:864990796088737792", "yawn~12:864990799086747668", "yawn~13:864990801867964468", "yawn~14:864990805293793310", "yawn~15:864990808766414899"*/ };

	private static final Map<Character, String> homoglyphs = new HashMap<>();

	static {
		Map<String, String> joHoms = ((Map<String, String>) JSONReader.getData().get("homoglyphs"));
		if (joHoms == null) {
			for (char c = 'a'; c <= 'z'; c++)
				homoglyphs.put(c, (String.valueOf(c) + Character.toUpperCase(c)));
		} else {
			joHoms.forEach((k, v) -> {
				homoglyphs.put(k.charAt(0), v);
			});

		}
	}

	private static final List<String> censoredWords = (List<String>) JSONReader.getData().get("censored_words");

	private static final @RegEx String fullCensorBuffer    = "[!@#$%^&*()\\[\\]/=\\-\\\\;',.{}?+|S_:\"\\s]*"; // >= 5
	private static final @RegEx String noSpaceCensorBuffer = "[!@#$%^&*()\\[\\]/=\\-\\\\;',.{}?+|S_:\"]*"; // 3-4
	private static final @RegEx String acronymCensorBuffer = "\\.*"; // <= 2

	private static String getCensor(int length) {
		return length <= 2 ? acronymCensorBuffer : (length <= 4 ? noSpaceCensorBuffer : fullCensorBuffer);
	}

	private static final @RegEx String censoredWordsRegex = orChainRegex(censoredWords.stream()
																					  .map(Commands::censorRegex)
																					  .toArray(String[]::new));

	private static final Matcher censoredWordsMatcher = getMatcher(anyEndsRegex(censoredWordsRegex));

	// private static final @RegEx String censoredUsersMatcher = orChainRegex(Stream.of("a", "x", "e")
	// 																			 .map(Commands::censorRegex)
	// 																			 .toArray(String[]::new));

	private static final Map<String, Matcher> censoredWordsMatchers =
			censoredWords.stream()
						 .collect(Collectors.toMap(Function.identity(),
												   word -> getMatcher(censorRegex(word))));

	private static String anyEndsRegex(final String regex) {
		return "[\\w\\W]*" + regex + "[\\w\\W]*";
	}

	private static String noLetterEndsRegex(final String regex) {
		return "(?<![\\w]+)" + regex + "(?![\\w]+)";
	}

	public static List<Command> commands;

	public static Map<String, Command> commandsByName;

	static {
		commands = List.of(
		// new Command.Builder("", "Create Command", OWNER, GUILD_AND_PRIVATE, (message, matcher) -> {
		// 	// addCommand()
		// }).build(),
		new Command.Builder(getCommandRegex("(((Turn)?\\s*Off)|(Shut\\s*(Down|Off|Up)))"), "Shut Down", OWNER, GUILD_AND_PRIVATE, message -> {
			message.getJDA().cancelRequests();
			message.getChannel().sendMessage(PreparedMessages.preparedMessages().get("positive")
															 .get(message.getGuild().getIdLong())).queue(s -> {
				message.getJDA().shutdown();
				Main.getBot().shutdownChatSchedulers();
				System.exit(0);
			}, s -> {
				message.getJDA().shutdown();
				Main.getBot().shutdownChatSchedulers();
				System.exit(0); });
		}).helpPanel("Shut Me Off").build(),
		new Command.Builder(getCommandRegex("Restart"), "Restart", OWNER, GUILD_AND_PRIVATE, message -> {
			message.getJDA().cancelRequests();
			message.getChannel()
				   .sendMessage(PreparedMessages.preparedMessages().get("positive").get(message.getGuild().getIdLong()))
				   .complete();
			Main.restart();
		}).helpPanel("Restart Me").build(),
		new Command.Builder(getCommandRegex("Help(\\s+(Me|Him|Her|Them|It|Every(\\s+One)?)(\\s+Out)?)?(\\s+Here)?(\\s+Right\\s+Now)?"), "Help",
					DEFAULT, GUILD_AND_PRIVATE, message -> PreparedMessages.replyEmbedMessage(message, "help"))
				.helpPanel("Help On Commands (This Panel)").deniable().build(),
		new Command.Builder("![Ss]\\s*(\\d{18,20})(\\D[\\S\\s]+)", "Say In", MOD, PRIVATE, (message, matcher) -> {
			final TextChannel textChannelById = message.getJDA().getTextChannelById(matcher.group(1));
			if (textChannelById == null)
				reply(message, "I Was Not Able To Find That Channel \"" + matcher.group(1) + "\".");
			else {
				textChannelById.sendMessage(matcher.group(2)).queue();
				reply(message, PreparedMessages.getMessage("positive") + " Sent To " + textChannelById.getAsMention()
							   + " In " + textChannelById.getGuild().getName());
			}
			}).build(),
		new Command("![Ss][\\S\\s]+", "Say Here", MOD, GUILD, message -> {
			String response = message.getContentRaw().substring(2);
			message.delete().queue();
			reply(message, response);
		}),
		new Command("![Dd]\\s*(\\d{18,20})(\\D[\\S\\s]+)", "DM", OWNER, GUILD_AND_PRIVATE, (message, matcher) ->  // (@?.{2,32})#(\d{4})
			message.getJDA().retrieveUserById(matcher.group(1)).queue(
			/*success*/	user -> user.openPrivateChannel().queue(privateChannel ->
										privateChannel.sendMessage(matcher.group(2)).queue(s ->
												reply(message, PreparedMessages.getMessage("positive") + " Sent Message."))),
			/*failure*/	user -> reply(message, PreparedMessages.getMessage("negative") + " Failed To Find User."))
		),
		new Command("![Jj]\\s*(\\d{18,20})[\\S\\s]*", "Join", OWNER, GUILD_AND_PRIVATE, (message, matcher) -> {
			final VoiceChannel voiceChannel = message.getJDA().getVoiceChannelById(matcher.group(1));
			if (voiceChannel == null)
				reply(message, "No Voice Channel ID Reference.");
			voiceChannel.getGuild().getAudioManager().openAudioConnection(voiceChannel);
			reply(message, PreparedMessages.getMessage("positive") + " Will Attempt To Join.");
		}),
		new Command("![Ss][Ss]\\s*", "Servers", OWNER, PRIVATE, message ->
			reply(message, "`" + message.getJDA().getGuilds().stream().map(Guild::getName).collect(joining("\n")) + "`")),
		new Command("![Cc]\\s*", "Get Channels", OWNER, GUILD, message -> {
			System.out.printf("Channels in %s: %s\n", message.getGuild(),
				message.getGuild()
					   .getTextChannels()
					   .stream()
					   .map(c -> c.getName() + ":" + hasPermission(c, Permission.VIEW_CHANNEL))
					   .collect(Collectors.joining(", ")));
		}),
		new Command("(![Mm])|(" + getCommandRegex("(Get|Tell|Show|Give)\\s+(Me\\s+)?((Every|All)\\s+)?(The\\s+)?Commands") + ")",
					"Get Commands", OWNER, GUILD_AND_PRIVATE, message ->
			reply(message, commands.stream().map(Command::getName).collect(joining(", ", "All Commands: ", ".")))),
		new Command("(![Rr])|(" + getCommandRegex("((Get|Tell|Show|Give)\\s+(Me\\s+)?|What(\\s+I|')s)\\s+(My\\s+)?(Role|Position)") + ")",
					"Get Role", DEFAULT, GUILD, message ->
			reply(message, "You Are " + UserCategory.getUserCategory(message.getJDA(), message.getAuthor()).getDisplayName() + ".")),
		new Command("![Hh]\\s+\\d{1,20}\\s+\\d+\\s*", "DM History", OWNER, PRIVATE, message -> {
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
		}),
		new Command.Builder(getCommandRegex("(Tell\\s+Me(\\s+What|\\s+About)?|Define|What\\s+Is)\\s+(The\\s+)?(Command\\s+(.{1,16})|(.{1,16})\\s+Command)(\\s+Is)?"),
					"Info", OWNER, GUILD_AND_PRIVATE, (message, matcher) -> {
			String commandName = matcher.group(19);
			if (commandName == null)
				commandName = matcher.group(20);
			if (commandName == null) {
				LOGGER.info("Command Info regex group number misplacement encountered");
				return;
			}
			final Command command = commandsByName.get(commandName);
			if (command == null)
				reply(message, "That Command Doesn't Exist."); // TODO Generegexify
			else
				reply(message, command.toFullString()); // TODO "btw, you don't have permission to use it / VIEW it
		}).helpPanel("Guide On Activating A Command").build(),
		new Command(getCommandRegex("\\s*(" + (Config.get("aliasesRegex") + "\\s*(\\?+|\\.+|,|!+)?\\s+" + "(Greetings|Sup|Hi|Hey|Hello|Yo)"
										   + ")|(" + "(Greetings|Sup|Hi|Hey|Hello|Yo)" + "\\s*[,.]?\\s+" + (Config.get("aliasesRegex"))) + ")\\s*[.!\\s]*"),
					"Greeting", DEFAULT, GUILD_AND_PRIVATE, message ->
			reply(message.getTextChannel(), Math.random() < .5 ? "Not Here To Greet." : "Yo.")
		),
		new Command(anyEndsRegex(censoredWordsRegex), "Auto Delete", SELF, GUILD, (message, matcher) -> {
			if (!message.isPinned() && message.getGuild().getIdLong() == 907042440924528662L) { // TODO guild subscribed to this command
				logWordCensor(message, matcher);
				// LOGGER.info("Censoring: " + message.getContentDisplay());
			}
		}),
		new Command.Builder(getCommandRegex("Purge(\\s+(Every)\\s+(Chat|Channel))?"), "Purge All", SELF, GUILD, message -> {
			reply(message, PreparedMessages.getMessage("positive"));
			final AtomicLong count = new AtomicLong(0L);
			message.getGuild().getChannels().forEach(channel -> {
				if (channel.getType().isMessage()) {
					((MessageChannel) channel).getIterableHistory().takeAsync(300).thenAccept(list -> list.forEach(m -> {
						if (m.getContentRaw().length() > 0 && censoredWordsMatcher.reset(m.getContentRaw()).matches() && !m.isPinned()) {
							m.delete().queue();
							count.getAndIncrement();
							// logWordCensor(message, matcher);
						}
					}));
				}
			});
			reply(message, PreparedMessages.getMessage("positive") + " Deleted " + count + " Messages.");
		}).helpPanel("Purge Censor All Channels").build(),
		new Command.Builder(getCommandRegex("Purge\\s+(The|This)?\\s+(Chat|Channel|(Right )?Here)"), "Purge Current", MOD, GUILD, message -> {
			reply(message, PreparedMessages.getMessage("positive"));
			final AtomicLong count = new AtomicLong(0L);
			message.getChannel().getIterableHistory().forEach(m -> {
				// LOGGER.info("Checking message to purge: " + m.getContentRaw() + " - " + censoredWordsMatcher.reset(m.getContentRaw()).matches());
				if (m.getContentRaw().length() > 0 && censoredWordsMatcher.reset(m.getContentRaw()).matches() && !m.isPinned()) {
					m.delete().queue();
					count.getAndIncrement();
					// logWordCensor(message, matcher);
				}
			});
			reply(message, PreparedMessages.getMessage("positive") + " Deleted " + count + " Messages.");
		}).helpPanel("Purge Censor Current Channel").build(),
		new Command(("(-*)([\\d]+(\\.[\\d]*)?)\\s*([Kk][Gg][Ss]?([\\s]+|$)|[Kk][Ii][Ll][Oo][SsGg]\\w*|[Ll][Bb][Ss]?([\\s]+|$)"
								 + "|[Pp][Oo][Uu][Nn][Dd]\\w*|[Mm]([\\s]+|$)[Mm][Ee][Tt][Ee][Rr][Ss]?([\\s]+|$)|[Cc][Mm][Ss]?([\\s]+|$))"),
					//|[\d]+\\s*'\\s*([\d]+(\.[\d]*)?)("|'')
					"Convert Units", DEFAULT, GUILD_AND_PRIVATE, (message, matcher) ->
			reply(message, matcher.results().map(match -> (match.group(1).length() % 2 == 1 ? '-' : "")
												+ Unit.of(match.group(4)).convert(match.group(2)))
								  .collect(Collectors.joining(", ")))
		),
		new Command.Builder(".*", "Delete Ping", PING_CENSORED, GUILD_AND_PRIVATE, message -> {
			if (message.getMentions(Message.MentionType.USER)
					   .stream()
					   .anyMatch(i -> i.getIdLong() == 849711011456221285L)) {
				message.delete().queue();
			} else {
				final Message referenceMessage = message.getReferencedMessage();
				if (referenceMessage != null &&
					referenceMessage.getAuthor().getIdLong() == 849711011456221285L)
				message.delete().queue();
			}
		}).deactivate().build(),
		new Command.Builder("It's Time To Celebrate.", "Birthday", MOD, GUILD, message -> {
			AudioManager audioManager = message.getGuild().getAudioManager();
			if (!audioManager.isConnected())
				return;
			GuildVoiceState guildVoiceState = message.getMember().getVoiceState();
			AudioChannel voiceChannel = guildVoiceState.getChannel();
			Member selfMember = message.getGuild().getSelfMember();
			if (selfMember.hasPermission(voiceChannel, Permission.VOICE_CONNECT))
				audioManager.openAudioConnection(voiceChannel);
			PlayerManager.loadAndPlay(message.getGuild(), "https://www.youtube.com/watch?v=paSQ9mcOUME");
			// audioManager.closeAudioConnection();
		}).helpPanel("Celebrate The Princess's Birthday").deniable().deactivate().build(),
		new Command.Builder(getCommandRegex("(The|A|An)\\s+(Mod|Moderator)s?\\s*(\\s((In\\s+(This|The)\\s+(Server|Guild|Place))|Here))?[?.]*\\s*",
											"((What|Who)\\s+(Are|Is)|(Whose|Who's|Whos|Who're))\\s+"), "Get Mods", MOD, GUILD, message ->
			reply(message, new EmbedBuilder().setDescription("Moderators In \"" + message.getGuild().getName() + "\"")
											 .addField("Moderators", String.join("\n", DataBase.getMods(message.getGuild().getIdLong()).get()), true)
											 .setThumbnail(message.getGuild().getIconUrl())
											 .setColor((Color) Config.get("embedColor"))
											 .build())
		).helpPanel("Get Mods In Server").deactivate().build(),
		new Command.Builder(getCommandRegex("((((Make|Set|Give)\\s*)@.{1,32}\\s*(A\\s+)?(Mod|Moderator))|(((Make|Set|Give)\\s*)?(Mod|Moderator)\\s+@.{1,32}))"),
					"Give Mod", OWNER, GUILD, message ->
			reply(message, DataBase.addMod(message.getGuild().getIdLong(),
											message.getMentionedMembers().stream()
												   .map(Member::getIdLong) // TODO or filter by if it isn't a bot
												   .filter(memberID -> !memberID.equals(message.getJDA().getSelfUser().getIdLong()))
												   .findFirst()
												   .orElse(null),
											LiquidRichardBot.getUserName(message.getAuthor()))
									.getFeedback())
		).helpPanel("Give Moderator").deniable().deactivate().build(),
		new Command.Builder(getCommandRegex("(((Remove|Re Move|Take)\\s*@.{1,32}\\s*('?s\\s+)?(Mod|Moderator))|(((Remove|Re Move|Take)\\s*)?(Mod|Moderator)\\s*@.{1,32}))"),
					"Remove Mod", OWNER, GUILD, message ->
			reply(message, DataBase.removeMod(
									message.getGuild().getIdLong(),
									message.getMentionedMembers().stream()
										   .map(Member::getIdLong) // TODO or filter by if it isn't a bot
										   .filter(memberID -> !memberID.equals(message.getJDA().getSelfUser().getIdLong()))
										   .findFirst()
										   .orElse(null))
									.getFeedback())
		).helpPanel("Remove A Moderator").deactivate().build(),
		new Command.Builder(getCommandRegex("((((Make|Set)\\s*)#.{1,32}\\s*((The|A)\\s+)?(Main\\s+Channel))|((Make|Set)\\s+((The|A)\\s+)?(Main\\s+Channel)\\s*#.{1,32}))"),
					"Main Channel", MOD, GUILD, message -> {
			Optional<TextChannel> channel = message.getMentionedChannels().stream().findFirst();
			if (channel.isPresent()) {
				DataBase.setMainChannel(message.getGuild().getIdLong(), channel.get().getIdLong());
				Main.getBot().setGuildMainChannel(message.getGuild().getIdLong(), channel.get());
			}
		}).helpPanel("Set Main Channel").deniable().deactivate().build(),
		new Command.Builder(".*" + censorChainRegex("america, amerimut, kek, based, healthcare, capital", "[\\s.,]*") + ".*", "Censor Target", //".*([Bb][.,\\s;:]*[Rr][.,\\s;:]*[Aa][.,\\s;:]*[Nn][.,\\s;:]*[Dd][.,\\s;:]*[Oo0]).*"
					CENSORED, GUILD, message -> { // TODO censor optimizer
			if (message.getGuild().getIdLong() == 793333500303769600L)
						// System.out.println("delete");
				message.delete().queue();
		}).helpPanel("Censor Targeted Users").deactivate().build(),
		new Command.Builder("([Mm][Ee]\\s*[Aa][Nn][Dd]\\s*[Ww][Hh][Oo])[^Mm][.,;:!?\\s]*", "Me&Whom", DEFAULT, GUILD, message -> {
			if (message.getGuild().getIdLong() == 864896744491974676L)
				reply(message, "Me And Whom.*");
		}).deactivate().build(),
		new Command.Builder(".*", "Yawn", YAWN, GUILD_AND_PRIVATE, message -> {
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
		}).deactivate().build(),
		new Command.Builder(".*", "Gay", DEFAULT, GUILD_AND_PRIVATE, message -> {
			if (message.getAuthor().getIdLong() == 748583074073280532L) { // TheRealBrady
					message.addReaction("U+1F3F3U+FE0FU+200DU+1F308").queue();
			}
		}).deactivate().build(),
		new Command.Builder("((.*(\\?|:(grey_)?question:|\u2753|\u2754))"
					+ "|([,;:<>&^%$#@!{}\\[\\]=/\\-.*+()_\\s]*(\\?|:(grey_)?question:|\u2753|\u2754)))[,;:<>&^%$#@!{}\\[\\]=/\\-.*+()_\\s]*"
					+ "|([,;:<>&^%$#@!{}\\[\\]=/\\-.*+()_\\s]*(([Oo]+[Mm]+[Gg]+|[Ww]+[Aa]+[Ii]+[Tt]+|[Oo]+[Hh]*)\\s+)?" //([Cc][\s]*[Aa][\s]*[Nn])
					+ "((([Ww][\\s]*[Hh][\\s]*([Ii][\\s]*[Cc][\\s]*[Hh]|[Oo]+|[Aa]+[\\s]*[Tt]|[Ee][\\s]*[Rr][\\s]*[Ee]|[Ee][\\s]*[Nn]+|[Yy]+))|[Hh][\\s]*[Oo]+[\\s]*[Ww])"
					+ "('?[Ss]|[Ii][Ss]|[Aa][Rr][Ee])?([\\s+].*|[,;:<>&^%$#@!{}\\[\\]=/\\-.*+()_\\s])?)"
					+ "|([Rr]+[Ee]+[Aa]+[Ll]+[Yy]+[.?;:\\s]*))", "Questions", //Hey Pimp, Can You Help Me
					DEFAULT, GUILD_AND_PRIVATE, message -> {
			if (!message.getContentRaw().matches("[,;:<>&^%$#@!{}\\[\\]=/\\-.*+()_\\s]*[Ww][Hh][Aa][Tt][.?]*\\s+([Aa][Nn]?[^?]*|[Tt][Hh][Ee]\\s+([Ff][Uu]?[Cc]?[Kk]?|[Hh][Ee]+[Ll]+))[,;:<>&^%$#@!{}\\[\\]=/\\-.*+()_\\s]*")) {
				// qCount++;
				if (reactionQuestions) {
					message.addReaction("U+1F1F3").queue();
					message.addReaction("U+1F1ED").queue();
					message.addReaction("U+1F1F9").queue();
					message.addReaction("U+1F1E6").queue();
					message.addReaction("U+1F1F6").queue();
					// message.addReaction("NHTAQ:864184033046298656").queue();
				}
				else
					reply(message, "Not Here To Answer Questions.");
			}
		}).deactivate().build()

	  	).stream().filter(c -> !c.isDeactivated()).collect(toList());
	}

	/**
	 * @return if the message should have been censored
	 */
	private static boolean logWordCensor(final Message message, final Matcher matcher) {
		final String messageContent = message.getContentRaw();
		final String[] censoredWords = getMatcher(censoredWordsRegex).reset(messageContent).results().map(MatchResult::group).toArray(String[]::new);
		if (censoredWords.length == 0) {
			return false;
		}
		message.delete().queueAfter(1, TimeUnit.HOURS); // check if pinned right before delete
		LiquidRichardBot.autodeleteLog.sendMessageEmbeds(
				new EmbedBuilder()
						.setAuthor(LiquidRichardBot.getUserName(message.getAuthor()), message.getJumpUrl(), message.getAuthor().getAvatarUrl())
						.addField("Message", messageContent.substring(0, Math.min(messageContent.length(), 1024)), false)
						.addField("ID", message.getId(), false)
						.addField("Censored Word" + (censoredWords.length > 1 ? "s" : ""), String.join(", ", censoredWords), false)
						.setColor(Color.RED)
						.build()).queue();
		return true;
	}

	private static String substring(String string, int fromEndIndex) {
		return string.substring(0, string.length() - fromEndIndex);
	}

	private static String censorChainRegex(String words, String joiner) {
		return censorChainRegex(words.split("\\s*,\\s*"), joiner);
	}

	private static String censorChainRegex(String[] words, String joiner) {
		return orChainRegex(Arrays.stream(words).map(w -> censorRegex(w, joiner)).toArray(String[]::new));
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
		final String regex = word.chars()
								 .mapToObj(c -> "[" + homoglyphs.getOrDefault((char) c, c == ' ' ? "\\s" : String.valueOf((char) c)) + "]+")
								 .map(s -> word.length() <= 2 ? noLetterEndsRegex(s) : s)
								 .collect(joining(joiner));
		return "((?!\\d{" + word.length() + "})" + regex + ")";
	}

	private static String censorRegex(String word) {
		return censorRegex(word, getCensor(word.length()));
	}

	final static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss");

	private static String timeStampOf(final OffsetDateTime date) {
		return dateTimeFormatter.format(date);
	}

	public static final Map<ReactionChannelType, Map<UserCategory, Set<Command>>> commandsByCategoryChannel =
			Stream.of(ReactionChannelType.values()).collect(toMap(channelType -> channelType, channelType ->
			Stream.of(UserCategory.values()).collect(toMap(userCategory -> userCategory, userCategory ->
					commands.stream()
							.filter(cT -> cT.getChannelType().inRange(channelType))
							.filter(uC -> uC.getUserCategory().inRange(userCategory))
							.collect(toSet())))));

	static {
		commandsByName = commands.stream().collect(toMap(Reaction::getName, Function.identity()));
		System.out.println(censorRegex(" hate"));
	}

	private static Set<Command> getCommandSet(final Command command) {
		return commandsByCategoryChannel.get(command.getChannelType())
										.get(command.getUserCategory());
	}

	static boolean addCommand(Command command) {
		final Set<Command> commandSet = getCommandSet(command);
		if (commandSet.stream().anyMatch(c -> c.equals(command) || c.getName().equals(command.getName())))
			return false;
		return commandSet.add(command);
	}

	static void removeCommand(Command command) {
		getCommandSet(command).remove(command);
	}

	public static void reply(final Message message, final String reply) {
		if (message.getChannelType() == ChannelType.TEXT)
			reply(message.getTextChannel(), reply);
		else
			message.getChannel().sendMessage(truncate(reply)).queue();
	}

	public static void reply(final Message message, final MessageEmbed reply) {
		if (message.getChannelType() == ChannelType.TEXT)
			reply(message.getTextChannel(), reply);
		else
			message.getChannel().sendMessageEmbeds(reply).queue();
	}

	public static void reply(final TextChannel channel, final String reply) {
		if (canUseChannel(channel))
			channel.sendMessage(truncate(reply)).queue();
		else
			logMissingChannelPermissions(channel);
	}

	private static CharSequence truncate(final String text) {
		return text.length() > 2000 ? text.substring(0, 998) + "..." + text.substring(text.length() - 998) : text;
	}

	public static void reply(final TextChannel channel, final MessageEmbed reply) {
		if (canUseChannel(channel))
			channel.sendMessageEmbeds(reply).queue();
		else
			logMissingChannelPermissions(channel);
	}

	private static boolean canUseChannel(final TextChannel channel) {
		return hasPermission(channel, Permission.MESSAGE_SEND);
	}

	private static boolean hasPermission(final TextChannel channel, Permission permission) {
		return PermissionUtil.checkPermission(channel, Objects.requireNonNull(channel.getGuild().getMember(channel.getJDA().getSelfUser())),
											  permission);
	}

	private static void logMissingChannelPermissions(final TextChannel channel) {
		LOGGER.info("Tried to do command in {} in {}, but is missing permissions", channel, channel.getGuild());

	}

}
