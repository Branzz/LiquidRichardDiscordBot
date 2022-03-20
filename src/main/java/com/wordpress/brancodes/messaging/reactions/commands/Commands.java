package com.wordpress.brancodes.messaging.reactions.commands;

import com.wordpress.brancodes.database.DataBase;
import com.wordpress.brancodes.util.CaseUtil;
import com.wordpress.brancodes.voice.PlayerManager;
import com.wordpress.brancodes.main.Main;
import com.wordpress.brancodes.messaging.PreparedMessages;
import com.wordpress.brancodes.messaging.reactions.Reaction;
import com.wordpress.brancodes.messaging.reactions.ReactionChannelType;
import com.wordpress.brancodes.messaging.reactions.users.UserCategory;
import com.wordpress.brancodes.util.Config;
import com.wordpress.brancodes.util.JSONReader;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;
import net.dv8tion.jda.internal.utils.PermissionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.RegEx;
import java.awt.*;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.wordpress.brancodes.bot.LiquidRichardBot.*;
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

	public static final @RegEx String censoredWordsRegex = censoredWords.stream()
																		.map(Commands::censorRegex)
																		.collect(Commands.orChainRegex());

	private static final Matcher breakCharMatcher = getMatcher("\\\\u([\\da-fA-F]{4})");

	// public static final @RegEx String censoredWordsGeneRegex =
	// 		censoredWords.stream()
	// 					 .map(word -> breakCharMatcher.reset(censorGeneRegex(word))
	// 												  .replaceAll(String.valueOf((char) Integer.parseInt(breakCharMatcher.group(1), 16))))
	// 					 .collect(Commands.orChainRegex());

	private static final Matcher censoredWordsMatcher = getMatcher(censoredWordsRegex);

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

	static final String aaveRegex = "(^|\\s)" + ((List<String>) JSONReader.getData().get("aave_terms"))
														.stream()
														.map(Commands::censorBasicRegex)
														.collect(Commands.orChainRegex()) + "($|\\s)";

	static {
	  commands = List.of(
		// new Command.Builder("", "Create Command", OWNER, GUILD_AND_PRIVATE, (message, matcher) -> {
		// 	// addCommand()
		// }).build(),
		new Command.Builder("Shut Down", getCommandRegex("(((Turn)?\\s*Off)|(Shut\\s*(Down|Off|Up)))"), OWNER, GUILD_AND_PRIVATE).helpPanel("Shut Me Off").execute(message -> {
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
		}).build(),
		new Command.Builder("Restart", getCommandRegex("Restart"), OWNER, GUILD_AND_PRIVATE).helpPanel("Restart Me").execute(message -> {
			message.getJDA().cancelRequests();
			message.getChannel()
				   .sendMessage(PreparedMessages.preparedMessages().get("positive").get(message.getGuild().getIdLong()))
				   .complete();
			Main.restart();
		}).build(),
		new Command.Builder("Help", getCommandRegex("Help(\\s+(Me|Him|Her|Them|It|Every(\\s+One)?)(\\s+Out)?)?(\\s+Here)?(\\s+Right\\s+Now)?"),
							DEFAULT, GUILD_AND_PRIVATE).helpPanel("Help On Commands (This Panel)").deniable().execute(
			message -> PreparedMessages.replyEmbedMessage(message, "help"))
		.build(),
		new Command.Builder("Say In", "![Ss]\\s*(\\d{18,20})(\\D[\\S\\s]+)", MOD, GUILD_AND_PRIVATE).executeStatus((message, matcher) -> {
			final TextChannel textChannelById = message.getJDA().getTextChannelById(matcher.group(1));
			if (textChannelById == null) {
				reply(message, "I Was Not Able To Find That Channel \"" + matcher.group(1) + "\".");
				return false;
			} else {
				textChannelById.sendMessage(matcher.group(2)).queue();
				reply(message, PreparedMessages.getMessage("positive") + " Sent To " + textChannelById.getAsMention()
							   + " In " + textChannelById.getGuild().getName());
				return true;
			}
		}).build(),
		new Command.Builder("Say Here", "![Ss][\\S\\s]+", MOD, GUILD).execute(message -> {
			String response = message.getContentRaw().substring(2);
			message.delete().queue();
			reply(message, response);
		}).build(),
		new Command.Builder("Say Proper", "![Pp][\\S\\s]+", MOD, GUILD).execute(message -> {
			String response = message.getContentRaw().substring(2);
			message.delete().queue();
			reply(message, CaseUtil.properCase(response));
		}).build(),
		new Command.Builder("DM", "![Dd]\\s*(\\d{17,20})(\\D[\\S\\s]+)", MOD, GUILD_AND_PRIVATE).execute((message, matcher) ->  // (@?.{2,32})#(\d{4})
			message.getJDA().retrieveUserById(matcher.group(1)).queue(
			/*success*/	user -> user.openPrivateChannel().queue(privateChannel ->
										privateChannel.sendMessage(matcher.group(2)).queue(s -> {
												reply(message, PreparedMessages.getMessage("positive") + " Sent Message To " + getUserName(user));
												LOGGER.info("Sent " + matcher.group(2) + " To " + getUserName(user) + " By " + getUserName(message.getAuthor()));
										})),
			/*failure*/	user -> reply(message, PreparedMessages.getMessage("negative") + " Failed To Find User."))
		).build(),
		new Command.Builder("Join Voice", "![Jj]\\s*(\\d{18,20})[\\S\\s]*", MOD, GUILD_AND_PRIVATE).executeStatus((message, matcher) -> {
			final VoiceChannel voiceChannel = message.getJDA().getVoiceChannelById(matcher.group(1));
			if (voiceChannel == null) {
				reply(message, "Couldn't Find That Voice Channel ID.");
				return false;
			}
			voiceChannel.getGuild().getAudioManager().openAudioConnection(voiceChannel);
			return true;
			// reply(message, PreparedMessages.getMessage("positive") + " Will Attempt To Join.");
		}).build(),
		new Command.Builder("Disconnect", "![Dd]|Disconnect\\.?", MOD, GUILD).executeStatus((Message message) -> {
			final AudioManager audioManager = message.getGuild().getAudioManager();
			if (audioManager.isConnected()) {
				audioManager.closeAudioConnection();
				return true;
			}
			return false;
		}).build(),
		new Command.Builder("Servers", "![Ss][Ss]\\s*", OWNER, PRIVATE).execute(message ->
			reply(message, "`" + message.getJDA().getGuilds().stream().map(Guild::getName).collect(joining("\n")) + "`")).build(),
		new Command.Builder("Get Channels", "![Cc]\\s*", OWNER, GUILD).execute(message -> {
			System.out.printf("Channels in %s: %s\n", message.getGuild(),
				message.getGuild()
					   .getTextChannels()
					   .stream()
					   .map(c -> c.getName() + ":" + hasPermission(c, Permission.VIEW_CHANNEL))
					   .collect(joining(", ")));
		}).build(),
		new Command.Builder("Get Commands", "(![Mm])|(" + getCommandRegex("(Get|Tell|Show|Give)\\s+(Me\\s+)?((Every|All)\\s+)?(The\\s+)?Commands") + ")", MOD, GUILD_AND_PRIVATE).execute(message ->
			reply(message, commands.stream().map(Command::getName).collect(joining(", ", "All Commands: ", ".")))).build(),
		new Command.Builder("Get Role", "(![Rr])|(" + getCommandRegex("((Get|Tell|Show|Give)\\s+(Me\\s+)?|What(\\s+I|')s)\\s+(My\\s+)?(Role|Position)") + ")", DEFAULT, GUILD).execute(message ->
			reply(message, "You Are " + getUserCategory(message.getJDA(), message.getAuthor()).getDisplayName() + ".")).build(),
		new Command.Builder("DM History", "![Hh]\\s+\\d{1,20}\\s+\\d+\\s*", OWNER, PRIVATE).execute(message -> {
			String[] messageParts = message.getContentRaw().split("\\s+");
			message.getJDA().retrieveUserById(messageParts[1]).queue(user -> {
				user.openPrivateChannel().queue(privateChannel -> {
					int amount = Integer.parseInt(messageParts[2]);
					privateChannel.getHistory().retrievePast(amount).queue(messageHistory -> {
						EmbedBuilder embedBuilder = new EmbedBuilder().setDescription("Message History With " + getUserName(user))
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
		}).build(),

		new Command.Builder("Info", getCommandRegex("(Tell\\s+Me(\\s+What|\\s+About)?|Define|What\\s+Is)\\s+(The\\s+)?(Command\\s+(.{1,16})|(.{1,16})\\s+Command)(\\s+Is)?"),
							MOD, GUILD_AND_PRIVATE).executeStatus((message, matcher) -> {
			String commandName = matcher.group(19);
			if (commandName == null)
				commandName = matcher.group(20);
			if (commandName == null) {
				LOGGER.info("Command Info regex group number misplacement encountered");
				return false;
			}
			final Command command = commandsByName.get(commandName);
			if (command == null) {
				reply(message, "That Command Doesn't Exist."); // TODO Generegexify
				return false;
			} else {
				reply(message, command.toFullString()); // TODO "btw, you don't have permission to use it / VIEW it
				return true;
			}
		}).helpPanel("Guide On Activating A Command").build(),

		// new Command.Builder("Execute Code", "![Ee].+", OWNER, GUILD_AND_PRIVATE).execute(message -> {
		// 	final String content = message.getContentRaw().substring(2);
		// 	// JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		// 	// compiler.run(null, null, null, javaFile.toFile().getAbsolutePath());
		// 	// javaFile.getParent().resolve(className);
		// }).deactivate().build(),

		new Command.Builder("Greeting", getCommandRegex("\\s*(" + (Config.get("aliasesRegex") + "\\s*(\\?+|\\.+|,|!+)?\\s+" + "(Greetings|Sup|Hi|Hey|Hello|Yo)"
																   + ")|(" + "(Greetings|Sup|Hi|Hey|Hello|Yo)" + "\\s*[,.]?\\s+" + (Config.get("aliasesRegex"))) + ")\\s*[.!\\s]*"),
							DEFAULT, GUILD_AND_PRIVATE).execute(message ->
			reply(message.getTextChannel(), Math.random() < .5 ? "Not Here To Greet." : "Yo.")
		).build(),

		new Command.Builder("Auto Delete", censoredWordsRegex, SELF, GUILD).executeStatus((message, matcher) -> {
			if (message.getGuild().getIdLong() == 907042440924528662L && !message.isPinned()) { // TODO guild subscribed to this command
				logWordCensor(message, matcher);
				// LOGGER.info("Censoring: " + message.getContentDisplay());
				return true;
			} else
				return false;
		}).build(),

		new Command.Builder("Censor Japanese", "[\\s\\S]*[\u4E00-\u9FA0\u3041-\u3094\u30A1-\u30F4\u30FC\u3005\u3006\u3024][\\s\\S]*", DEFAULT, GUILD).executeStatus(message -> {
			///[]+/u
			if (message.getGuild().getIdLong() == 907042440924528662L) {
				message.delete().queue();
				return true;
			} else
				return false;

		}).build(),

		new Command.Builder("Purge All", getCommandRegex("Purge(\\s+(Every)\\s+(Chat|Channel))?"), MOD, GUILD).execute(message -> {
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

		new Command.Builder("Purge Current", getCommandRegex("Purge\\s+(The|This)?\\s+(Chat|Channel|(Right )?Here)"), MOD, GUILD).execute(message -> {
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

		new Command.Builder("Convert Units", ("(-*)((((\\d+)['\u2019])((\\d+)(\\.(\\d*))?|\\.(\\d+))?+)([^Ss]|$)" //|(\d+(\.(\d*))?("|''|[ ]?[Ii][Nn]([.CcSs\s]|$)?)?)
											  + "|(\\d+\\.?\\d*|\\.\\d+)\\s*([Kk][Gg][Ss]?([\\s]+|$)|[Kk][Ii][Ll][Oo][SsGg]\\w*|[Ll][Bb][Ss]?([^\\w]|$)"
											  + "|[Pp][Oo][Uu][Nn][Dd]\\w*|[Mm]([^\\w]+|$)|[Mm][Ee][Tt][Ee][Rr][Ss]?([^\\w]+|$)|[Cc][Mm][Ss]?([^\\w]+|$)|[Ii][Nn]\\w*))"),
							//|[\d]+\\s*'\\s*([\d]+(\.[\d]*)?)
							DEFAULT, GUILD_AND_PRIVATE).executeStatus((message, matcher) -> {
			String reply = matcher.reset()
								  .results()
								  .map(Unit::convertUnit)
								  .filter(Objects::nonNull)
								  .collect(joining(", "));
			if (reply.length() == 0)
				return false;
			else {
				reply(message, reply);
				return true;
			}
		}).build(),

		new Command.Builder("Delete Ping", ".*", PING_CENSORED, GUILD_AND_PRIVATE).execute(message -> {
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
		}).deactivated().build(),

		new Command.Builder("Birthday", "It'?s Time To Celebrate\\.?", DEFAULT, GUILD).executeStatus(message -> {
			AudioManager audioManager = message.getGuild().getAudioManager();
			if (!audioManager.isConnected()) {
				GuildVoiceState guildVoiceState = message.getMember().getVoiceState();
				AudioChannel voiceChannel = null;
				if (!guildVoiceState.inAudioChannel()) {
					final List<VoiceChannel> voiceChannels = message.getGuild().getVoiceChannels();
					if (voiceChannels.isEmpty()) {
						final ChannelAction<VoiceChannel> vC = message.getGuild().createVoiceChannel("Celebration Time.");
						message.getGuild().getCategories().stream().findFirst().ifPresent(vC::setParent);
						vC.queue(channel -> {
							if (message.getGuild().getSelfMember().hasPermission(channel, Permission.VOICE_CONNECT))
								audioManager.openAudioConnection(channel);
							// else return false; TODO ???
						});
					}
					else
						voiceChannel = voiceChannels.get(0);
				} else
					voiceChannel = guildVoiceState.getChannel();
				if (message.getGuild().getSelfMember().hasPermission(voiceChannel, Permission.VOICE_CONNECT))
					audioManager.openAudioConnection(voiceChannel);
				else
					return false;
			}
			PlayerManager.loadAndPlay(message.getGuild(), "static/Happy_Birthday_Princess.mp3");
			return true;
			// audioManager.closeAudioConnection();
		}).helpPanel("Celebrate The Princess's Birthday").deniable().build(),

		new Command.Builder("Get Mods", getCommandRegex("(The|A|An)\\s+(Mod|Moderator)s?\\s*(\\s((In\\s+(This|The)\\s+(Server|Guild|Place))|Here))?[?.]*\\s*",
														"((What|Who)\\s+(Are|Is)|(Whose|Who'?s|Who're))\\s+"), MOD, GUILD).execute(message ->
			reply(message, new EmbedBuilder().setDescription("Moderators In \"" + message.getGuild().getName() + "\"")
											 .addField("Moderators", String.join("\n", DataBase.getMods(message.getGuild().getIdLong()).get()), true)
											 .setThumbnail(message.getGuild().getIconUrl())
											 .setColor((Color) Config.get("embedColor"))
											 .build())
		).helpPanel("Get Mods In Server").deactivated().build(),
		new Command.Builder("Give Mod", getCommandRegex("((((Make|Set|Give)\\s*)@.{1,32}\\s*(A\\s+)?(Mod|Moderator))|(((Make|Set|Give)\\s*)?(Mod|Moderator)\\s+@.{1,32}))"), OWNER, GUILD).execute(message ->
			reply(message, DataBase.addMod(message.getGuild().getIdLong(),
											message.getMentionedMembers().stream()
												   .map(Member::getIdLong) // TODO or filter by if it isn't a bot
												   .filter(memberID -> !memberID.equals(message.getJDA().getSelfUser().getIdLong()))
												   .findFirst()
												   .orElse(null),
											getUserName(message.getAuthor()))
									.getFeedback())
		).helpPanel("Give Moderator").deniable().deactivated().build(),
		new Command.Builder("Remove Mod", getCommandRegex("(((Remove|Re Move|Take)\\s*@.{1,32}\\s*('?s\\s+)?(Mod|Moderator))|(((Remove|Re Move|Take)\\s*)?(Mod|Moderator)\\s*@.{1,32}))"), OWNER, GUILD).execute(message ->
			reply(message, DataBase.removeMod(
									message.getGuild().getIdLong(),
									message.getMentionedMembers().stream()
										   .map(Member::getIdLong) // TODO or filter by if it isn't a bot
										   .filter(memberID -> !memberID.equals(message.getJDA().getSelfUser().getIdLong()))
										   .findFirst()
										   .orElse(null))
									.getFeedback())
		).helpPanel("Remove A Moderator").deactivated().build(),
		new Command.Builder("Main Channel", getCommandRegex("((((Make|Set)\\s*)#.{1,32}\\s*((The|A)\\s+)?(Main\\s+Channel))|((Make|Set)\\s+((The|A)\\s+)?(Main\\s+Channel)\\s*#.{1,32}))"), MOD, GUILD).execute(message -> {
			Optional<TextChannel> channel = message.getMentionedChannels().stream().findFirst();
			if (channel.isPresent()) {
				DataBase.setMainChannel(message.getGuild().getIdLong(), channel.get().getIdLong());
				Main.getBot().setGuildMainChannel(message.getGuild().getIdLong(), channel.get());
			}
		}).helpPanel("Set Main Channel").deniable().deactivated().build(),
		new Command.Builder("Censor Target", ".*" + censorChainRegex("america, amerimut, kek, based, healthcare, capital", "[\\s.,]*") + ".*", CENSORED, GUILD)
				.helpPanel("Censor Targeted Users").execute(message -> { // TODO censor optimizer
			if (message.getGuild().getIdLong() == 793333500303769600L)
						// System.out.println("delete");
				message.delete().queue();
		}).deactivated().build(),
		new Command.Builder("Censor AAVE", aaveRegex,  //".*([Bb][.,\\s;:]*[Rr][.,\\s;:]*[Aa][.,\\s;:]*[Nn][.,\\s;:]*[Dd][.,\\s;:]*[Oo0]).*"
							DEFAULT, GUILD).execute((message, matcher) -> { // TODO censor optimizer
			if (message.getGuild().getIdLong() == 910004207120183326L) {
				reply(message, String.format(
						"Please Retract This Message, %s. Are You A Black Person Of Color? "
						+ "No? Then No, You Should **NOT** Be Using The Word \"%s\", "
						+ "As It Is From From The AAVE Dialect And Is **Racist** To "
						+ "Use It. Please Educate Your Self And Avoid Any Future "
						+ "Microaggressions Against Black Individuals At https://aavenb.carrd.co/",
											 CaseUtil.properCase(message.getAuthor().getName()),
											 CaseUtil.properCase(matcher.reset().results().findFirst().get().group())));
			}
		}).build(),
		new Command.Builder("Me&Whom", "([Mm][Ee]\\s*[Aa][Nn][Dd]\\s*[Ww][Hh][Oo])[^Mm][.,;:!?\\s]*", DEFAULT, GUILD).execute(message -> {
			if (message.getGuild().getIdLong() == 864896744491974676L)
				reply(message, "Me And Whom.*");
		}).deactivated().build(),
		new Command.Builder("Yawn", ".*", YAWN, GUILD_AND_PRIVATE).execute(message -> {
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
		}).deactivated().build(),
		new Command.Builder("Gay", ".*", DEFAULT, GUILD_AND_PRIVATE).executeStatus(message -> {
			if (message.getAuthor().getIdLong() == 748583074073280532L) { // TheRealBrady
					message.addReaction("U+1F3F3U+FE0FU+200DU+1F308").queue();
					return true;
			} else
				return false;
		}).deactivated().build(),
		new Command.Builder("Questions", "((.*(\\?|:(grey_)?question:|\u2753|\u2754))"
										 + "|([,;:<>&^%$#@!{}\\[\\]=/\\-.*+()_\\s]*(\\?|:(grey_)?question:|\u2753|\u2754)))[,;:<>&^%$#@!{}\\[\\]=/\\-.*+()_\\s]*"
										 + "|([,;:<>&^%$#@!{}\\[\\]=/\\-.*+()_\\s]*(([Oo]+[Mm]+[Gg]+|[Ww]+[Aa]+[Ii]+[Tt]+|[Oo]+[Hh]*)\\s+)?" //([Cc][\s]*[Aa][\s]*[Nn])
										 + "((([Ww][\\s]*[Hh][\\s]*([Ii][\\s]*[Cc][\\s]*[Hh]|[Oo]+|[Aa]+[\\s]*[Tt]|[Ee][\\s]*[Rr][\\s]*[Ee]|[Ee][\\s]*[Nn]+|[Yy]+))|[Hh][\\s]*[Oo]+[\\s]*[Ww])"
										 + "('?[Ss]|[Ii][Ss]|[Aa][Rr][Ee])?([\\s+].*|[,;:<>&^%$#@!{}\\[\\]=/\\-.*+()_\\s])?)"
										 + "|([Rr]+[Ee]+[Aa]+[Ll]+[Yy]+[.?;:\\s]*))",  // Hey Pimp, Can You Help Me
							DEFAULT, GUILD_AND_PRIVATE).execute(message -> {
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
		}).deactivated().build()
	  ).stream().filter(c -> !c.isDeactivated()).collect(toList());
	}

	/**
	 * @return if the message should have been censored
	 */
	private static boolean logWordCensor(final Message message, final Matcher matcher) {
		final String messageContent = message.getContentRaw();
		final String[] censoredWords = matcher.reset(messageContent).results().map(MatchResult::group).toArray(String[]::new);
		if (censoredWords.length == 0) {
			return false;
		}
		message.delete().queueAfter(1, TimeUnit.HOURS); // check if pinned right before delete
		autodeleteLog.sendMessageEmbeds(
				new EmbedBuilder()
						.setAuthor(getUserName(message.getAuthor()), message.getJumpUrl(), message.getAuthor().getAvatarUrl())
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

	private static Collector<CharSequence, ?, String> orChainRegex() {
		return Collectors.joining("|", "(", ")");
	}

	public static String censorBasicRegex(String word) {
		final String collect = word.chars()
								   .mapToObj(c -> c == '+' || c == '*'
												  ? String.valueOf((char) c)
												  : ("[" + (c == ' ' ? "\\s" : (String.valueOf((char) Character.toUpperCase(c))
																				+ ((char) Character.toLowerCase(c)))) + "]"))
								   .collect(joining(""));
		return collect;
	}

	public static String censorRegex(String word) {
		return censorRegex(word, getCensor(word.length()));
	}

	public static String censorGeneRegex(String word) {
		return censorGeneRegex(word, getCensor(word.length()));
	}

	private static String censorRegex(String word, String joiner) {
		return notNumberRegex(word.chars()
								 .mapToObj(c -> "[" + homoglyphs.getOrDefault((char) c, c == ' ' ? "\\s" : String.valueOf((char) c)) + "]+")
								 .map(s -> word.length() <= 2 ? noLetterEndsRegex(s) : s)
								 .collect(joining(joiner)), word.length());
	}

	private static String notNumberRegex(final String regex, int wordLength) {
		return "((?!\\d{" + wordLength + "})" + regex + ")";
	}

	private static String censorGeneRegex(String word, String joiner) {
		return word.chars()
								 .mapToObj(c -> "[" + homoglyphs.getOrDefault((char) c, c == ' ' ? "\\s" : String.valueOf((char) c)) + "]+")
								 .map(s -> word.length() <= 2 ? noLetterEndsRegex(s) : s)
								 .collect(joining(joiner));
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

	private static String truncate(final String text) {
		return text.length() > 2000 ? text.substring(0, 998) + "..." + text.substring(text.length() - 998) : text;
	}

	public static void reply(final TextChannel channel, final MessageEmbed reply) {
		if (canUseChannel(channel))
			channel.sendMessageEmbeds(reply).queue();
		else
			logMissingChannelPermissions(channel);
	}

	private static Member getMember(Message message) {
		return message.getGuild().getMember(message.getAuthor());
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

	abstract class Test {

		int a = 0;

		Test() {

		}

	}

}
