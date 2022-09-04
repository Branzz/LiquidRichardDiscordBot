package com.wordpress.brancodes.messaging.reactions;

import bran.parser.CompositionParser;
import bran.tree.compositions.Composition;
import bran.tree.compositions.expressions.Expression;
import bran.tree.compositions.statements.Statement;
import com.wordpress.brancodes.bot.LiquidRichardBot;
import com.wordpress.brancodes.database.DataBase;
import com.wordpress.brancodes.main.Main;
import com.wordpress.brancodes.messaging.PreparedMessages;
import com.wordpress.brancodes.messaging.chats.Chats;
import com.wordpress.brancodes.messaging.reactions.message.MessageReaction;
import com.wordpress.brancodes.messaging.reactions.message.MessageReaction.MessageReactionBuilder;
import com.wordpress.brancodes.messaging.reactions.message.commands.custom.CustomCommand.CustomCommandBuilder;
import com.wordpress.brancodes.messaging.reactions.message.commands.custom.CustomCommandCompiler;
import com.wordpress.brancodes.messaging.reactions.message.commands.custom.types.obj.ClassType;
import com.wordpress.brancodes.messaging.reactions.unit.BMI;
import com.wordpress.brancodes.messaging.reactions.message.commands.Command.CommandBuilder;
import com.wordpress.brancodes.messaging.reactions.message.commands.SlashCommand;
import com.wordpress.brancodes.messaging.reactions.message.commands.SlashCommand.SlashCommandBuilder;
import com.wordpress.brancodes.messaging.reactions.message.commands.custom.CustomCommand;
import com.wordpress.brancodes.messaging.reactions.message.commands.custom.exception.CustomCommandCompileErrorException;
import com.wordpress.brancodes.messaging.reactions.message.commands.custom.exception.InvalidCustomCommandException;
import com.wordpress.brancodes.messaging.reactions.unit.UnitMatch;
import com.wordpress.brancodes.messaging.reactions.users.UserCategory;
import com.wordpress.brancodes.util.*;
import com.wordpress.brancodes.voice.PlayerManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.Message.MentionType;
import net.dv8tion.jda.api.entities.emoji.CustomEmoji;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;
import net.dv8tion.jda.internal.entities.emoji.CustomEmojiImpl;
import net.dv8tion.jda.internal.requests.RestActionImpl;
import net.dv8tion.jda.internal.utils.PermissionUtil;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.RegEx;
import java.awt.*;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.wordpress.brancodes.bot.LiquidRichardBot.autodeleteLog;
import static com.wordpress.brancodes.bot.LiquidRichardBot.getUserName;
import static com.wordpress.brancodes.messaging.reactions.message.MessageReaction.getMatcher;
import static com.wordpress.brancodes.messaging.reactions.ReactionChannelType.*;
import static com.wordpress.brancodes.messaging.reactions.ReactionResponse.FAILURE;
import static com.wordpress.brancodes.messaging.reactions.ReactionResponse.SUCCESS;
import static com.wordpress.brancodes.messaging.reactions.message.commands.Command.getCommandRegex;
import static com.wordpress.brancodes.messaging.reactions.users.UserCategory.*;
import static com.wordpress.brancodes.util.ImageUtil.*;
import static java.util.stream.Collectors.*;

public class Reactions { // TODO convert into singleton (?) or a Manager

	// TODO awkward encapsulation and property ordering

	private static final Logger LOGGER = LoggerFactory.getLogger(Reactions.class);

	public static int qCount = 0;
	final static boolean reactionQuestions = false;

	private static final CustomEmoji[] YAWN_EMOJIS = Arrays.stream(new String[] { "yawn~1:864990663438106624", "yawn~2:864990672209444864",
			"yawn~3:864990679645814825", "yawn~4:864990712919883804", "yawn~5:864990744786763797"/*, "yawn~6:864990758711721995",
			"yawn~7:864990776757190656", "yawn~8:864990782707204108", "yawn~9:864990787028647966", "yawn~10:864990791700578345",
			"yawn~11:864990796088737792", "yawn~12:864990799086747668", "yawn~13:864990801867964468", "yawn~14:864990805293793310", "yawn~15:864990808766414899"*/ }
	).map(e -> e.split(":")).map(s -> new CustomEmojiImpl(s[0], Long.parseLong(s[1]), false)).toArray(CustomEmoji[]::new);

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

	private static final @RegEx
	String fullCensorBuffer = "[!@#$%^&*()\\[\\]/=\\-\\\\;',.{}?+|S_:\"\\s]*"; // >= 5
	private static final @RegEx
	String noSpaceCensorBuffer = "[!@#$%^&*()\\[\\]/=\\-\\\\;',.{}?+|S_:\"]*"; // 3-4
	private static final @RegEx
	String acronymCensorBuffer = "\\.*"; // <= 2

	private static String getCensor(int length) {
		return length <= 2 ? acronymCensorBuffer : (length <= 4 ? noSpaceCensorBuffer : fullCensorBuffer);
	}

	public static final @RegEx
	String censoredWordsRegex = censoredWords.stream()
			.map(Reactions::censorRegex)
			.collect(Reactions.orChainRegex());

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

	private static ScheduledExecutorService censorScheduler = new ScheduledThreadPoolExecutor(1);

	private static void flushCensors() {
//		censorScheduler.
	}

	public static List<Reaction> reactions;
	public static List<MessageReaction> messageReactions;

	public static Map<String, Reaction> commandsByName;

	static final String aaveRegex = "(^|\\s)" + ((List<String>) JSONReader.getData().get("aave_terms"))
			.stream()
			.map(Reactions::censorBasicRegexCaseInsensitive)
			.collect(Reactions.orChainRegex()) + "($|\\s)";

	public static final long GUILD_GS = 910004207120183326L;
	public static final long GUILD_C = 907042440924528662L;
	public static final long GUILD_DW = 959299477729079328L;

	static {
		reactions = List.of(
				// new Command.Builder("", "Create Command", OWNER, GUILD_AND_PRIVATE, (message, matcher) -> {
				// 	// addCommand()
				// }).build(),
		new CommandBuilder("Shut Down", getCommandRegex("(((Turn)?\\s*Off)|(Shut\\s*(Down|Off|Up)))"), OWNER, GUILD_AND_PRIVATE).execute(message -> {
			message.getJDA().cancelRequests();
			message.getChannel().sendMessage(PreparedMessages.preparedMessages().get("positive")
					.get(message.getGuild().getIdLong())).queue(s -> {
				Main.reset();
				System.exit(0);
			}, s -> {
				Main.reset();
				System.exit(0);
			});
		}).helpPanel("Shut Me Off").build(),
		new CommandBuilder("Restart", getCommandRegex("Restart"), OWNER, GUILD_AND_PRIVATE).execute(message -> {
			message.getJDA().cancelRequests();
			message.getChannel()
					.sendMessage(PreparedMessages.preparedMessages().get("positive").get(message.getGuild().getIdLong()))
					.complete();
			Main.reset();
		}).helpPanel("Restart Me").build(),
		new CommandBuilder("Help", getCommandRegex("Help(\\s+(Me|Him|Her|Them|It|Every(\\s+One)?)(\\s+Out)?)?(\\s+Here)?(\\s+Right\\s+Now)?"),
								   DEFAULT, GUILD_AND_PRIVATE).execute(message ->
				PreparedMessages.replyEmbedMessage(message, "help")
		).helpPanel("Help On Commands (This Panel)").deniable().addChannelCooldown(10_000L).build(),
		new CommandBuilder("Say In", "^!s\\s*(\\d{18,20})(\\D[\\S\\s]+)", MOD, GUILD_AND_PRIVATE).caseInsensitive().executeStatus((message, matcher) -> {
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
		new CommandBuilder("Say Here", "^!s[\\S\\s]+", MOD, GUILD).caseInsensitive().execute(message -> {
			String response = truncate(message.getContentRaw().substring(2));
			message.delete().queue();
			if (message.getMessageReference() != null) {
				message.getReferencedMessage().reply(response).queue();
			} else {
				reply(message, response);
			}
		}).build(),
		new CommandBuilder("Say Proper", "^!p[\\S\\s]+", MOD, GUILD).caseInsensitive().execute(message -> {
			String response = truncate(CaseUtil.properCase(message.getContentRaw().substring(2)));
			message.delete().queue();
			if (message.getMessageReference() != null) {
				message.getReferencedMessage().reply(response).queue();
			} else {
				reply(message, response);
			}
		}).build(),
		new CommandBuilder("DM", "^!d\\s*(\\d{17,20})(\\D[\\S\\s]+)", MOD, GUILD_AND_PRIVATE).caseInsensitive().execute((message, matcher) ->  // (@?.{2,32})#(\d{4})
				message.getJDA().retrieveUserById(matcher.group(1)).queue(
						/*success*/    user -> user.openPrivateChannel().queue(privateChannel ->
								privateChannel.sendMessage(matcher.group(2)).queue(s -> {
									message.reply(truncate(PreparedMessages.getMessage("positive") + " Sent Message To " + getUserName(user))).queue();
									LOGGER.info("Sent " + matcher.group(2) + " To " + getUserName(user) + " By " + getUserName(message.getAuthor()));
								})),
						/*failure*/    user -> message.reply(truncate(PreparedMessages.getMessage("negative") + " Failed To Find User.")).queue())
		).build(),
		new CommandBuilder("Join Voice", "^!j\\s*(\\d{18,20})[\\S\\s]*", MOD, GUILD_AND_PRIVATE).caseInsensitive().executeStatus((message, matcher) -> {
			final VoiceChannel voiceChannel = message.getJDA().getVoiceChannelById(matcher.group(1));
			if (voiceChannel == null) {
				message.reply(truncate("Couldn't Find That Voice Channel ID.")).queue();
				return false;
			}
			voiceChannel.getGuild().getAudioManager().openAudioConnection(voiceChannel);
			return true;
			// reply(message, PreparedMessages.getMessage("positive") + " Will Attempt To Join.");
		}).build(),
		new CommandBuilder("Disconnect", "^!di|Disconnect\\.?", MOD, GUILD).caseInsensitive().executeStatus((Message message) -> {
			final AudioManager audioManager = message.getGuild().getAudioManager();
			if (audioManager.isConnected()) {
				audioManager.closeAudioConnection();
				return true;
			}
			return false;
		}).build(),

		new CommandBuilder("Servers", "^!ss\\s*", OWNER, PRIVATE).caseInsensitive().execute(message ->
				message.reply(truncate("`" + message.getJDA().getGuilds().stream().map(Guild::getName).collect(joining("\n")) + "`")).queue()).build(),
		new CommandBuilder("Get Channels", "^!c\\s*", OWNER, GUILD).caseInsensitive().execute(message -> {
			System.out.printf("Channels in %s: %s\n", message.getGuild(),
					message.getGuild()
							.getTextChannels()
							.stream()
							.map(c -> c.getName() + ":" + hasPermission(c, Permission.VIEW_CHANNEL))
							.collect(joining(", ")));
		}).build(),

		new CommandBuilder("Get Commands", "(^!m)|(" + getCommandRegex("(Get|Tell|Show|Give)\\s+(Me\\s+)?((Every|All)\\s+)?(The\\s+)?Commands") + ")",
						   MOD, GUILD_AND_PRIVATE).caseInsensitive().execute(message -> {
			Map<Boolean, String> deactivatedGroupReactions = reactions.stream()//.filter(r -> r instanceof Command)
					.collect(groupingBy(Reaction::isDeactivated, mapping(Reaction::getName, joining(", ", "", "."))));
			message.reply(truncate("All Commands: " + deactivatedGroupReactions.get(false) + "\nDeactivated:\n" + deactivatedGroupReactions.get(true))).queue();
		}).build(),

		new CommandBuilder("Disable Command", "^!dc\\s+([\\W\\w]+)", MOD, GUILD).caseInsensitive().executeResponse((message, matcher) ->
				parseCommand(message, command -> {
					if (command.getName().equals("Disable Command"))
						return;
					command.deactivate();
					message.reply(truncate(PreparedMessages.getMessage(message.getGuild().getIdLong(), "positive") + " Disabled " + command)).queue();
				}, matcher.group(1))
		).build(),
		new CommandBuilder("Enable Command", "^!ec\\s+([\\W\\w]+)", OWNER, GUILD).caseInsensitive().executeResponse((message, matcher) ->
				parseCommand(message, command -> {
					command.activate();
					message.reply(truncate(PreparedMessages.getMessage(message.getGuild().getIdLong(), "positive") + " Enabled " + command)).queue();
				}, matcher.group(1))
		).build(),

		new CommandBuilder("Get Role", "(^!r)|(" + getCommandRegex("((Get|Tell|Show|Give)\\s+(Me\\s+)?|What(\\s+I|')s)\\s+(My\\s+)?(Role|Position)") + ")",
						   DEFAULT, GUILD).caseInsensitive().execute(message ->
				message.reply(truncate("You Are " + getUserCategory(message.getJDA(), message.getAuthor()).getDisplayName() + ".")).queue()
		).build(),

		new CommandBuilder("DM History", "^!h\\s+\\d{1,20}\\s+\\d+\\s*", OWNER, PRIVATE).caseInsensitive().execute(message -> {
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

		new CommandBuilder("Nick All", "^Nick\\s+\"([\\w]{2,32})\"\\s[\\s\\S]+", MOD, GUILD).executeStatus((message, matcher) -> {
			message.getMentions().getMembers().forEach(member -> { // TODO bad return status... needs to wait for queue?
				try {
					member.modifyNickname(matcher.group(1)).queue();
				} catch (InsufficientPermissionException ignored) {
					// return false;
				}
			});
			return true;
		}).build(),

		new CommandBuilder("Kick All", "^Kick[\\s\\S]+", MOD, GUILD).executeStatus((message, matcher) -> {
			message.getMentions().getMembers().forEach(member -> { // TODO bad return status... needs to wait for queue? (if found nobody?)
				try {
					member.kick().queue();
				} catch (InsufficientPermissionException ignored) {
				}
			});
			return true;
		}).build(),

		new CommandBuilder("Info", getCommandRegex("(Tell\\s+Me(\\s+What|\\s+About)?|Define|What\\s+Is)\\s+(The\\s+)?(Command\\s+(.{1,16})|(.{1,16})\\s+Command)(\\s+Is)?") + "$",
								   MOD, GUILD_AND_PRIVATE).executeResponse((message, matcher) ->
				parseCommand(message, command -> reply(message, command.toFullString()), matcher.group(18), matcher.group(19))
		).andChainable().helpPanel("Guide On Activating A Command").build(),

		new CommandBuilder("Example", getCommandRegex("(Tell|Show|Give)\\s+(Me\\s+)?(An?\\s+)?Example\\s+((For|Of)\\s+)?(The\\s+)?(Command\\s+(.{1,16})|(.{1,16})\\s+Command)") + "$",
								   DEFAULT, GUILD_AND_PRIVATE).executeResponse((message, matcher) ->
				parseCommand(message, command -> message.reply(truncate(command.getGenerex().random())).queue(), matcher.group(22), matcher.group(23))
		).andChainable().helpPanel("Give Example On How To Activate A Command").build(),

		// new Command.Builder("Execute Code", "![Ee].+", OWNER, GUILD_AND_PRIVATE).execute(message -> {
		// 	final String content = message.getContentRaw().substring(2);
		// 	// JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		// 	// compiler.run(null, null, null, javaFile.toFile().getAbsolutePath());
		// 	// javaFile.getParent().resolve(className);
		// }).deactivate().build(),

		new CommandBuilder("Custom Command", "(Pimp\\s+)?(Create|Make|Add)\\s+(The\\s+)?(Custom\\s+)?Command\\s+When\\s+(.+)\\s+(Named|Called)\\s+(.+)\n?+"
											 + "(```)?+(.+)(```)?+", MOD, GUILD_AND_PRIVATE).executeStatus(((message, matcher) -> {
			String event = matcher.group(5);
			String name = matcher.group(7);
			String text = matcher.group(9);
			try {
				new CustomCommandBuilder(name, event, message.getAuthor(), message.getGuild(), text).build().register();
			} catch (CustomCommandCompileErrorException | InvalidCustomCommandException e) {
				return false;
			}
			return true;
		})).deactivated().build(),

		new MessageReactionBuilder("Greeting", "^(" + (Config.get("aliasesRegex") + "\\s*(\\?+|\\.+|,|!+)?\\s+" + "(Greetings|Sup|Hi|Hey|Hello|Yo)"
												+ ")|(" + "(Greetings|Sup|Hi|Hey|Hello|Yo)" + "\\s*[,.]?\\s+" + (Config.get("aliasesRegex"))) + ")\\s*[.!\\s]*$",
							DEFAULT, GUILD_AND_PRIVATE).execute(message ->
				reply(message.getChannel().asTextChannel(), Math.random() < .5 ? "Not Here To Greet." : "Yo.")
		).addChannelCooldown(5_000L).build(),

		new MessageReactionBuilder("Auto Delete", "^?" + censoredWordsRegex + "$?", SELF, GUILD)
				.whitelistGuilds(973797632436760606L, 722001554374131713L, GUILD_C).executeResponse((message, matcher) -> {
			if (!message.isPinned()) { // TODO guild subscribed to this command
				final String censoredWords = logWordCensor(message, matcher);
				if (censoredWords == null)
					return FAILURE;
				// LOGGER.info("Censoring: " + message.getContentDisplay());
				return new ReactionResponse(censoredWords);
			} else
				return FAILURE;
		}).build(),

		new MessageReactionBuilder("Censor Japanese", "[\\s\\S]*[\u4E00-\u9FA0\u3041-\u3094\u30A1-\u30F4\u30FC\u3005\u3006\u3024][\\s\\S]*", DEFAULT, GUILD)
				.whitelistGuilds(GUILD_C).execute(message ->
				message.delete().queue()
		).build(),

		new CommandBuilder("Purge All", getCommandRegex("Purge(\\s+(Every)\\s+(Chat|Channel))?"), MOD, GUILD).execute(message -> {
	//			message.reply(truncate(PreparedMessages.getMessage("positive"))).queue();
			final AtomicLong count = new AtomicLong(0L);
			message.getGuild().getChannels().forEach(channel -> {
				if (channel.getType().isMessage()) {
					((MessageChannel) channel).getIterableHistory().takeAsync(300).thenAccept(list -> list.forEach(m -> {
						if (m.getContentRaw().length() > 0 && censoredWordsMatcher.reset(m.getContentRaw()).matches() && !m.isPinned()) {
							System.out.println(m.getContentRaw());
							m.delete().complete();
							// logWordCensor(message, matcher);
						}
					})).whenComplete((n, t) ->
							count.getAndIncrement()
					);
				}
			});
			message.reply(truncate(PreparedMessages.getMessage("positive") + " Deleted " + count + " Messages.")).queue();
		}).helpPanel("Purge Censor All Channels").build(),

		new CommandBuilder("Purge Current", getCommandRegex("Purge\\s+(The|This)?\\s+(Chat|Channel|(Right )?Here)"), MOD, GUILD).execute(message -> {
			message.reply(truncate(PreparedMessages.getMessage("positive"))).queue();
			final AtomicLong count = new AtomicLong(0L);
			message.getChannel().getIterableHistory().forEach(m -> {
				// LOGGER.info("Checking message to purge: " + m.getContentRaw() + " - " + censoredWordsMatcher.reset(m.getContentRaw()).matches());
				if (m.getContentRaw().length() > 0 && censoredWordsMatcher.reset(m.getContentRaw()).matches() && !m.isPinned()) {
					m.delete().queue();
					count.getAndIncrement();
					// logWordCensor(message, matcher);
				}
			});
			message.reply(truncate(PreparedMessages.getMessage("positive") + " Deleted " + count + " Messages.")).queue();
		}).helpPanel("Purge Censor Current Channel").build(),

		new CommandBuilder("Prune", "^prune", MOD, GUILD).caseInsensitive().executeResponse(commandMessage -> {
			commandMessage.getGuild().loadMembers().onSuccess(guildMembers -> {
				final Map<Long, Role> roles = commandMessage.getGuild().getRoles().stream().collect(toMap(Role::getIdLong, Function.identity()));
				final Set<Role> needsAnyOfRole = Stream.of(907451275807981638L)
													   .map(roles::get).collect(toSet());
				final Set<Role> butNotRole = Stream.of(957805137122971649L, 907468017280090122L, 921657925951447051L, 982590697515413554L, 922778685487087646L)
												   .map(roles::get).collect(toSet());
				final Role exceptAnyAboveRole = roles.get(994224838501748817L);
				commandMessage.getGuild().loadMembers().onSuccess(membersWithRole -> {
					// System.out.println(membersWithRole.stream()
					// 								  .map(Member::getEffectiveName)
					// 								  .collect(joining()));
					Set<Member> membersWithOnlyRole = membersWithRole.stream()
																	  .filter(member -> member.getRoles().stream().anyMatch(needsAnyOfRole::contains))
																	  .filter(member -> member.getRoles().stream().noneMatch(butNotRole::contains))
																	  .filter(member -> member.getRoles().stream().noneMatch(role -> role.getPosition() >= exceptAnyAboveRole.getPosition()))
																	  .collect(toSet());
					Set<Member> recentMembers = commandMessage.getGuild()
												   .getTextChannels()
												   .stream()
												   .flatMap(channel -> channel.getHistory()
																			  .retrievePast(100)
																			  .complete()
																			  .stream())
												   .filter(message -> message.getIdLong() > 1002439766500978688L) // ~2 weeks
												   .map(message -> commandMessage.getGuild().getMember(message.getAuthor()))
												   .collect(toSet());
					Set<Member> nonRecentMembers = new HashSet<>();
					membersWithOnlyRole.stream()
								 .filter(member -> !recentMembers.contains(member))
								 .forEach(nonRecentMembers::add);
					nonRecentMembers.forEach(member -> member.kick("PRUNE (HASN'T TALKED IN 2 WEEKS)").queue());
					System.out.printf("ALL USERS (%d):\n%s\nRECENT USERS(%d):\n%s\nNON RECENT USERS(%d)\n%s\n",
									  guildMembers.size(),
									  guildMembers.stream().map(Member::getUser).map(LiquidRichardBot::getUserName).collect(joining(", ")),
									  recentMembers.size(),
									  recentMembers.stream().filter(Objects::nonNull).map(Member::getUser).map(LiquidRichardBot::getUserName).collect(joining(", ")),
									  nonRecentMembers.size(),
									  nonRecentMembers.stream().map(Member::getUser).map(LiquidRichardBot::getUserName).collect(joining(", ")));
					LOGGER.info("PRUNED " + nonRecentMembers.size() + " MEMBERS.");
				});
			});
			return SUCCESS;
		}).build(),
			  //|(\d+(\.(\d*))?("|''|[ ]?[Ii][Nn]([.CcSs\s]|$)?)?)
		new MessageReactionBuilder("Convert Units", ("(?<!^[?.!]mute\\s{1,5}\\S{1,30}\\s{1,5}\\d{0,10})(?<!https://\\S{0,1990})"
											  + "(-*)(?<!\\$)((((\\d+)['\u2019])((\\d+)(\\.(\\d*))?|\\.(\\d+))?+)([^Ss]|$)|(\\d+\\.?\\d*|\\.\\d+)\\s*"
											  + "(kgs?([\\s]+|$)|kilo([sg\\s]|$)\\w*|lbs?([^a-zA-Z]|$)|pound\\w*|" //m([^\w]+|$)|
											  + "meters?([^\\w]+|$)|cms?([^\\w]+|$)|feet([^\\w]+[\\w\\W]*|$)|in(\\.|ch)\\w*))"),
							//|[\d]+\\s*'\\s*([\d]+(\.[\d]*)?)
					DEFAULT, GUILD_AND_PRIVATE).caseInsensitive().blacklistGuilds(GUILD_DW).executeResponse((message, matcher) -> { // TODO detect number without unit
			List<UnitMatch> matches = matcher.reset().results().map(UnitMatch::new).collect(toList());
			if (matches.size() == 0) {
				LOGGER.error("failed to convert " + message.getContentRaw());
				return FAILURE;
			} else {
				StringJoiner converted = new StringJoiner(", ");
				StringJoiner conversionArrow = new StringJoiner(", ");
				for (UnitMatch match : matches) {
					converted.add(match.convertUnit());
					conversionArrow.add(match.fullMatch() + " -> " + match.convertUnit());
				}
				BMI bmi = BMI.getBMI(matches);
				if (bmi.couldCalculate()) {
					converted.add(bmi.getConvertedString());
					conversionArrow.add(bmi.getLogString());
				}
				message.reply(truncate(converted.toString())).queue();
				return new ReactionResponse(conversionArrow.toString());
			}
		}).addChannelCooldown(4_000L).build(),

		new MessageReactionBuilder("Delete Ping", ".*", PING_CENSORED, GUILD_AND_PRIVATE).execute(message -> {
			if (message.getMentions()
					   .getMentions(MentionType.USER)
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

		new CommandBuilder("Birthday", "It'?s Time To Celebrate\\.?", DEFAULT, GUILD).caseInsensitive().executeStatus(message -> {
			AudioManager audioManager = message.getGuild().getAudioManager();
			if (!audioManager.isConnected()) {
				GuildVoiceState guildVoiceState = message.getMember().getVoiceState();
				if (guildVoiceState == null)
					return false;
				AudioChannel voiceChannel = null;
				if (!guildVoiceState.inAudioChannel()) {
					final List<VoiceChannel> voiceChannels = message.getGuild().getVoiceChannels();
					if (voiceChannels.isEmpty()) {
						final ChannelAction<VoiceChannel> vC = message.getGuild().createVoiceChannel("Celebration Time.");
						message.getGuild().getCategories().stream().findFirst().ifPresent(c -> vC.setParent(c).queue()); // multiple queues??
						vC.queue(channel -> {
							if (message.getGuild().getSelfMember().hasPermission(channel, Permission.VOICE_CONNECT))
								audioManager.openAudioConnection(channel);
							// else return false; voiceChannel will be null TODO ???
						});
					} else
						voiceChannel = voiceChannels.get(0);
				} else
					voiceChannel = guildVoiceState.getChannel();
				if (voiceChannel == null) // if the guild has no channels and we have no perms to make one
					return false;
				if (message.getGuild().getSelfMember().hasPermission(voiceChannel, Permission.VOICE_CONNECT))
					audioManager.openAudioConnection(voiceChannel);
				else return false;
			}
			PlayerManager.loadAndPlay(message.getGuild(), "static/Happy_Birthday_Princess.mp3");
			return true;
			// audioManager.closeAudioConnection();
		}).helpPanel("Celebrate The Princess's Birthday").deniable().build(),

		new CommandBuilder("Speech Bubble", "^(speech\\s*bubble|sb)", DEFAULT, GUILD_AND_PRIVATE).caseInsensitive().executeStatus((Message message) ->
			(message.getGuild().getIdLong() != GUILD_GS || message.getChannel().getIdLong() == 910004207610904673L)
				&& presentOrElseReturnStatus(
					getFirstImage(message) // will users ever be able to send embeds? ImageUtil.getFirstEmbed
					.or(() -> getFirstSticker(message)
					.or(() -> getFirstEmote(message)
					.or(() -> getFirstMessageLink(message)
					.or(() -> Optional.ofNullable(message.getReferencedMessage()).flatMap(ImageUtil::getFirstAttachment)
					.or(() -> getMentionedMemberPFP(message)
					.or(() -> searchFirstAttachment(message))))))),
				image -> sendSpeechBubbleImage(message, image))
			).helpPanel("Add Speech Bubble To Image").build(),

		new MessageReactionBuilder("Harita", "^haritard$", DEFAULT, GUILD).caseInsensitive().execute(message -> {
			message.getChannel().sendMessage("https://media.discordapp.net/attachments/722001554944819202/965120982480224296/Screenshot_20220415-020148_Chrome.png.jpg").queue();
			message.delete().queue();
		}).build(),

		new MessageReactionBuilder("Embed Fail", "https://(www\\.)?tenor\\.com/view/(.)+", DEFAULT, GUILD)
				.blacklistGuilds(GUILD_C).executeStatus(
				message -> {
			if (!PermissionUtil.checkPermission(message.getMember(), Permission.MESSAGE_EMBED_LINKS)
				|| !PermissionUtil.checkPermission(message.getChannel().asTextChannel(), message.getMember(), Permission.MESSAGE_EMBED_LINKS)) {
					message.reply("Nice Embed Fail. And Before You Ask Why: We Don't Want To See Your Shitty Spam Gifs Here.").queue();
				return true;
			} else {
				return false;
			}
		}).build(),

		new CommandBuilder("Angie", "^When( I|')s The Best Discord Girl'?s Birthday[?]?", DEFAULT, GUILD).caseInsensitive().executeStatus(message ->
			booleanReturnStatus(message.getGuild().getIdLong() == GUILD_GS, () ->
					Chats.getBdayMessage(message.getChannel().asTextChannel()).queue())
		).build(),

		new CommandBuilder("Get Mods", getCommandRegex("(The|A|An)\\s+(Mod|Moderator)s?\\s*(\\s((In\\s+(This|The)\\s+(Server|Guild|Place))|Here))?[?.]*\\s*",
															   "((What|Who)\\s+(Are|Is)|(Whose|Who'?s|Who're))\\s+"), MOD, GUILD).execute(message ->
				reply(message, new EmbedBuilder().setDescription("Moderators In \"" + message.getGuild().getName() + "\"")
						.addField("Moderators", String.join("\n", DataBase.getMods(message.getGuild().getIdLong()).get()), true)
						.setThumbnail(message.getGuild().getIconUrl())
						.setColor((Color) Config.get("embedColor"))
						.build())
		).helpPanel("Get Mods In Server").deactivated().build(),

		new CommandBuilder("Give Mod", getCommandRegex("((((Make|Set|Give)\\s*)@.{1,32}\\s*(A\\s+)?(Mod|Moderator))|(((Make|Set|Give)\\s*)?(Mod|Moderator)\\s+@.{1,32}))"),
								   OWNER, GUILD).execute(message ->
				reply(message, DataBase.addMod(message.getGuild().getIdLong(),
						message.getMentions().getMembers().stream()
								.map(Member::getIdLong) // TODO or filter by if it isn't a bot
								.filter(memberID -> !memberID.equals(message.getJDA().getSelfUser().getIdLong()))
								.findFirst()
								.orElse(null),
						getUserName(message.getAuthor()))
				.getFeedback())
		).helpPanel("Give Moderator To User").deniable().deactivated().build(),

		new CommandBuilder("Remove Mod", getCommandRegex("(((Remove|Re Move|Take)\\s*@.{1,32}\\s*('?s\\s+)?(Mod|Moderator))|(((Remove|Re Move|Take)\\s*)?(Mod|Moderator)\\s*@.{1,32}))"),
								   OWNER, GUILD).execute(message -> reply(message, DataBase.removeMod(
						message.getGuild().getIdLong(),
						message.getMentions().getMembers().stream()
								.map(Member::getIdLong) // TODO or filter by if it isn't a bot
								.filter(memberID -> !memberID.equals(message.getJDA().getSelfUser().getIdLong()))
								.findFirst()
								.orElse(null))
				.getFeedback())
		).helpPanel("Remove A Moderator").deactivated().build(),

		new CommandBuilder("Main Channel", getCommandRegex("((((Make|Set)\\s*)#.{1,32}\\s*((The|A)\\s+)?(Main\\s+Channel))|((Make|Set)\\s+((The|A)\\s+)?(Main\\s+Channel)\\s*#.{1,32}))"),
								   MOD, GUILD).execute(message -> {
			Optional<TextChannel> channel = message.getMentions()
												   .getChannels(TextChannel.class)
												   .stream()
												   .findFirst();
			if (channel.isPresent()) {
				DataBase.setMainChannel(message.getGuild().getIdLong(), channel.get().getIdLong());
				Main.getBot().setGuildMainChannel(message.getGuild().getIdLong(), channel.get());
			}
		}).helpPanel("Set Main Channel").deniable().deactivated().build(),

		new CommandBuilder("Change Log", "^!l\\s+([\\w\\d]+)$", OWNER, GUILD_AND_PRIVATE).executeStatus((message, matcher) -> {
			String channel = matcher.group(1);
			if (channel.equalsIgnoreCase("console")) {
				Main.getBot().setConsoleIDE();
				return true;
			} else {
				if (channel.equalsIgnoreCase("default"))
					channel = "955111291272450048";
				TextChannel textChannel = message.getJDA().getTextChannelById(channel);
				if (textChannel != null) {
					Main.getBot().setConsole(textChannel);
					return true;
				} else
					return false;
			}
		}).build(),

		// new CommandBuilder("Censor Target", ".*" + censorChainRegex("special, words", "[\\s.,]*") + ".*",
		// 						   CENSORED, GUILD).executeStatus(message -> // TODO censor optimizer
		// 	booleanReturnStatus(message.getGuild().getIdLong() == 793333500303769600L, () ->
		// 		message.delete().queue())
		// ).helpPanel("Censor Targeted Users").deactivated().build(),

		new MessageReactionBuilder("Censor AAVE", aaveRegex,  //".*([Bb][.,\\s;:]*[Rr][.,\\s;:]*[Aa][.,\\s;:]*[Nn][.,\\s;:]*[Dd][.,\\s;:]*[Oo0]).*"
							DEFAULT, GUILD).caseInsensitive().executeStatus((message, matcher) -> // TODO censor optimizer
			booleanReturnStatus(message.getGuild().getIdLong() == GUILD_GS || message.getGuild().getIdLong() == GUILD_C, () ->
				reply(message, String.format(
						"Please Retract This Message, %s. Are You A Black Person Of Color? "
								+ "No? Then No, You Should **NOT** Be Using The Word \"%s\", "
								+ "As It Is From From The AAVE Dialect And Is **Racist** To "
								+ "Use It. Please Educate Your Self And Avoid Any Future "
								+ "Microaggressions Against Black Individuals At aavenb.carrd.co",
						CaseUtil.properCase(message.getAuthor().getName()),
						CaseUtil.properCase(matcher.reset().results().findFirst().get().group()))))
		).deactivated().build(),
		new MessageReactionBuilder("Welcome", "hey[\\w\\W]+", BOT, GUILD).executeStatus((message) -> {
					if (message.getMember().getIdLong() == 155149108183695360L
							&& message.getGuild().getIdLong() == GUILD_C) {
						Optional<Member> member = message.getMentions().getMembers().stream().findFirst();
						if (member.isEmpty())
							return false;
						else {
							reply(message, "Hello " + member.get().getAsMention() + " .");
							return true;
						}
					} else
						return false;
				}
		).deactivated().build(),
		new MessageReactionBuilder("Me&Whom", "(me\\s*and\\s*who)[^Mm][.,;:!?\\s]", DEFAULT, GUILD).caseInsensitive().execute(message -> {
			reply(message, "Me And Whom.*");
		}).deactivated().build(),
		new MessageReactionBuilder("Yawn", ".*", YAWN, GUILD_AND_PRIVATE).execute(message -> {
			// if (message.getAuthor().getIdLong() == 749625271937663027L)
			if (!message.isFromGuild() || hasPermission(message.getChannel().asTextChannel(), Permission.MESSAGE_EXT_EMOJI))
				for (CustomEmoji yawnEmoji : YAWN_EMOJIS) {
					message.addReaction(yawnEmoji).queue();
				}
			else {
//				if (hasPermission(message.getChannel().asTextChannel(), Permission.MESSAGE_ADD_REACTION))
//					message.addReaction("U+1F971").queue();
				logMissingChannelPermissions(message.getChannel().asTextChannel());
			}
		}).build(),
//		new MessageReactionBuilder("Gay", ".*", DEFAULT, GUILD_AND_PRIVATE).executeStatus(message ->
//			booleanReturnStatus(message.getAuthor().getIdLong() == 748583074073280532L, () -> // TheRealBrady
//				message.addReaction("U+1F3F3U+FE0FU+200DU+1F308").queue())
//		).deactivated().build(),
		new MessageReactionBuilder("Questions",
							// "^(" +
		//		"((.*(\\?|:(grey_)?question:|\u2753|\u2754))|([,;:<>&^%$#@!{}\\[\\]=/\\-.*+()_\\s]*(\\?|:(grey_)?question:|\u2753|\u2754)))"
		//		+ "[,;:<>&^%$#@!{}\\[\\]=/\\-.*+()_\\s]*"
		//		+ "|([,;:<>&^%$#@!{}\\[\\]=/\\-.*+()_\\s]*(([Oo]+[Mm]+[Gg]+|[Ww]+[Aa]+[Ii]+[Tt]+|[Oo]+[Hh]*)\\s+)?" //([Cc][\s]*[Aa][\s]*[Nn])
		//	  "((([Ww][Hh]([Ii][Cc][Hh]|[Oo]+|[Aa]+[Tt]|[Ee][Rr][Ee]|[Ee][Nn]+|[Yy]+))|[Hh][Oo]+[Ww])" // interrogative
		//	  + "('?[Ss]|[Ii][Ss]|[Aa][Rr][Ee])?([\\s+].*|[,;:<>&^%$#@!{}\\[\\]=/\\-.*+()_\\s])?)"
		//	  + "|([Rr][Ee][Aa][Ll]+[Yy]+[.?;:\\s]*))\\?",  // Hey Pimp, Can You Help Me

		"^((((wh(ich|o+|a+t|ere|en+|y+))|ho+w)('?s|is|are)?([\\s+].*|[,;:<>&^%$#@!{}\\[\\]=/\\-.*+()_\\s])?)|(real+y+[.?;:\\s]*))",
							DEFAULT, GUILD_AND_PRIVATE).caseInsensitive().execute(message -> {
			// if (!message.getContentRaw().matches("[,;:<>&^%$#@!{}\\[\\]=/\\-.*+()_\\s]*[Ww][Hh][Aa][Tt][.?]*\\s+([Aa][Nn]?[^?]*|[Tt][Hh][Ee]"
												 // + "\\s+([Ff][Uu]?[Cc]?[Kk]?|[Hh][Ee]+[Ll]+))[,;:<>&^%$#@!{}\\[\\]=/\\-.*+()_\\s]*")) {
				// qCount++;
				if (reactionQuestions) {
//					message.addReaction("U+1F1F3").queue();
//					message.addReaction("U+1F1ED").queue();
//					message.addReaction("U+1F1F9").queue();
//					message.addReaction("U+1F1E6").queue();
//					message.addReaction("U+1F1F6").queue();
					// message.addReaction("NHTAQ:864184033046298656").queue();
				} else
					reply(message, "Not Here To Answer Questions.");
			// }
		}).addGuildCooldown(10_000_000L).deactivated().build(),
		new CommandBuilder("Evaluate", "^Evaluate`*+(.+)`*+$", DEFAULT, GUILD_AND_PRIVATE).execute((message, matcher) -> {
			String response;
			try {
				Composition comp = CompositionParser.parse(matcher.group(1));
				if (comp instanceof Statement) {
//					throw new Exception("not an expression");
					response = String.valueOf(((Statement) comp).truth());
				} else {
					double evaluation = ((Expression) comp).evaluate();
					System.out.println(evaluation % 1);
					response = evaluation % 1 == 0 ? Integer.toString((int) evaluation) : Double.toString(evaluation);
				}
			} catch (Exception e) {
				response = "`" + e.getMessage() + '`';
			}
			message.reply(response).queue();
		}).caseInsensitive().build(),
		new CommandBuilder("Domain", "^Domain(\\s+((And\\s+)?(Then\\s+)?)?Simplify)?`*+(.+)`*+$", DEFAULT, GUILD_AND_PRIVATE).execute((message, matcher) -> {
			String response;
			try {
				Composition comp = CompositionParser.parse(matcher.group(5));
				if (!(comp instanceof Expression))
					throw new Exception("not an expression");
				Statement domain = ((Expression) comp).getDomainConditions();
				if (matcher.group(1) != null)
					domain = domain.simplified();
				response = domain.toString();
			} catch (Exception e) {
				response = "`" + e.getMessage() + '`';
			}
			message.reply(response).queue();
		}).caseInsensitive().build(),
		// new CommandBuilder("Range", "^Range(.+)$", DEFAULT, GUILD_AND_PRIVATE).execute((message, matcher) -> {
		// 	message.reply(String.valueOf(ExpressionParser.parseExpression(matcher.group(1)).range())).queue();
		// }).caseInsensitive().build(),
		new CommandBuilder("Derive", "^Deriv(e|ative)(\\s+((And\\s+)?(Then\\s+)?)?Simplify)?`*+(.+)`*+$", DEFAULT, GUILD_AND_PRIVATE).execute((message, matcher) -> {
			String response;
			try {
				Composition comp = CompositionParser.parse(matcher.group(6));
				if (!(comp instanceof Expression))
					throw new Exception("not an expression");
				Expression exp = ((Expression) comp).derive();
				if (matcher.group(2) != null)
					exp = exp.simplified();
				response = exp.toString();
			} catch (Exception e) {
				response = "`" + e.getMessage() + '`';
			}
			message.reply(response).queue();
		}).caseInsensitive().build(),
		new CommandBuilder("Inverse", "^Inverse(\\s+((And\\s+)?(Then\\s+)?)?Simplify)?`*+(.+)`*+$", DEFAULT, GUILD_AND_PRIVATE).execute((message, matcher) -> {
			String response;
			try {
				Composition parse = CompositionParser.parse(matcher.group(5));
				if (!(parse instanceof Expression))
					throw new Exception("not an expression");
				response = ((Expression) parse).inverse()
												   .stream()
												   .map(e -> matcher.group(1) != null ? e.simplified() : e)
												   .map(Expression::toString)
												   .collect(joining(", "));
			} catch (Exception e) {
				response = "`" + e.getMessage() + '`';
			}
			message.reply(response).queue();
		}).caseInsensitive().build(),
		new CommandBuilder("Truth", "^Truth(.+)$", DEFAULT, GUILD_AND_PRIVATE).execute((message, matcher) -> {
			String response;
			try {
				Composition parse = CompositionParser.parse(matcher.group(1));
				if (!(parse instanceof Statement))
					throw new Exception("not a statement");
				response = String.valueOf(((Statement) parse).truth());
			} catch (Exception e) {
				response = "`" + e.getMessage() + '`';
			}
			message.reply(response).queue();
		}).caseInsensitive().deactivated().build(),
		new CommandBuilder("Truth Table", "^Truth\\s+Table`*+(.+)`*+$", DEFAULT, GUILD_AND_PRIVATE).execute((message, matcher) -> {
			String response;
			try {
				Composition parse = CompositionParser.parse(matcher.group(1));
				if (!(parse instanceof Statement))
					throw new Exception("not a statement");
				response = "```" + ((Statement) parse).getTable() + "```";
			} catch (Exception e) {
				response = "`" + e.getMessage() + '`';
			}
			message.reply(response).queue();
		}).caseInsensitive().build(),
		new CommandBuilder("Simplify", "^Simpl(if)?y`*+(.+)`*+$", DEFAULT, GUILD_AND_PRIVATE).execute((message, matcher) -> {
			String response;
			String input = matcher.group(2);
			if (input.matches("[\\d\\w\\s',.\"?!]+"))
				return;
			try {
				response = CompositionParser.parse(input).simplified().toString();
			} catch (Exception e) {
				response = "`" + e.getMessage() + '`';
			}
			 message.reply(response).queue();
			// message.reply(String.valueOf(CompositionParser.parse(matcher.group(1)).simplified())).queue();
		}).caseInsensitive().build(),
		new CommandBuilder("Random", "Random\\s*(\\d+)", DEFAULT, GUILD_AND_PRIVATE).execute((message, matcher) -> {
			String response;
//			try {
				response = CompositionParser.generate(Integer.parseInt(matcher.group(1)));
//			} catch (Exception e) {
//				response = "`" + e.getMessage() + '`';
//			}
			message.reply(response).queue();
			// message.reply(String.valueOf(CompositionParser.parse(matcher.group(1)).simplified())).queue();
		}).caseInsensitive().build(),
		new CommandBuilder("Upsert Slash", "^!us\\s+(.+)$", OWNER, GUILD_AND_PRIVATE).caseInsensitive().executeResponse((message, matcher) -> {
			List<String> inputStrings = Arrays.asList(matcher.group(1).split("\\s+"));
			if (inputStrings.size() <= 1)
				return new ReactionResponse(false, "you must provide a command name and \"true\" at the end for it to be upserted globally");
			Set<String> upsertCommands = new HashSet<>(inputStrings.subList(1, inputStrings.size() - 1));
			return getSlashCommands(upsertCommands).stream()
											.map(s -> s.upsert(inputStrings.size() > 2 && inputStrings.get(inputStrings.size() - 1).equalsIgnoreCase("true")))
											.reduce(ReactionResponse::combine)
											.orElse(new ReactionResponse(false, "couldn't find any of those commands"));
		}).build(),
		new CommandBuilder("Delete Slash", "^!ds\\s+(.+)$", OWNER, GUILD_AND_PRIVATE).caseInsensitive().executeResponse((message, matcher) -> {
			Set<String> upsertCommands = new HashSet<>(Arrays.asList(matcher.group(1).split("\\s+")));
			return upsertCommands.stream()
						  .map(SlashCommand::delete)
						  .reduce(ReactionResponse::combine)
						  .orElse(new ReactionResponse(false, "couldn't find any of those commands"));
		}).build(),
		new SlashCommandBuilder("kill", "End Your Life.", DEFAULT, GUILD_AND_PRIVATE)
//				.addSubcommands(new SubcommandData("info", "Show Details.").addOption())
				.addOption(OptionType.STRING, "cause", "Cause Of Death.", true)
				.execute(event ->
					event.getInteraction().reply("Hope You Die By " + CaseUtil.properCase(event.getOption("cause").getAsString())).queue()
		).build(),
		((Supplier<SlashCommand>) () -> {
			final List<Command.Choice> eventsToOptions = CustomCommand.events.keySet().stream()
							.map(event -> new Command.Choice(CaseUtil.splitNoSpaceCase(event), event)).collect(toList());

			SlashCommandBuilder builder = new SlashCommandBuilder("custom-command", "Create Your Own Command", MOD, GUILD_AND_PRIVATE);
			final Map<String, String> names = builder.getData();
			return builder
				.addField("CREATE_NAME", "create")
				.addField("RUN_NAME", "run")
				.addField("INFO_NAME", "info")
				.addField("EVENT_INFO_NAME", "event-info")
				.addField("NAME_OPTION", "name")
				.addField("DESC_OPTION", "description")
				.addField("EVENT_OPTION", "event")
				.addField("CODE_OPTION", "code")

				.createSubcommandBranch(new SubcommandData(names.get("CREATE_NAME"), "Create A Command")
												.addOption(OptionType.STRING, names.get("NAME_OPTION"), "name of your command", true)
												.addOptions(new OptionData(OptionType.STRING, names.get("EVENT_OPTION"), "events", true)
																	.addChoices(eventsToOptions))
												.addOption(OptionType.STRING, names.get("CODE_OPTION"), "code to run when event occurs (see \"/info\")", true) // TODO is it /info?
												.addOption(OptionType.STRING, names.get("DESC_OPTION"), "describe what your command does", false),
						event -> {
							try {
								new CustomCommandBuilder(event.getOption(names.get("NAME_OPTION")).getAsString(),
														 event.getOption(names.get("EVENT_OPTION")).getAsString(),
														 event.getUser(),
														 event.getGuild(),
														 event.getOption(names.get("CODE_OPTION")).getAsString())
										.addDescription(event.getOption(names.get("DESC_OPTION")).getAsString())
										.build()
										.register();
							} catch (CustomCommandCompileErrorException exception) {
								event.getInteraction().reply(exception.getMessage());
							}
						}
				)
				.createSubcommandBranch(new SubcommandData(names.get("RUN_NAME"), "Run This Command Only Once And Now")
												.addOption(OptionType.STRING, names.get("CODE_OPTION"), "code to run now", true),
						event -> {
							try {
								CustomCommandCompiler.compile(event.getInteraction().getOption(names.get("CODE_OPTION")).getAsString(),
															  (ClassType<Guild>) CustomCommand.getType("guild"));
								event.getInteraction().reply("Running").setEphemeral(true).queue();
							} catch (CustomCommandCompileErrorException exception) {
								event.getInteraction().reply(exception.getMessage()).queue();
							}
				})
				.createSubcommandBranch(new SubcommandData(names.get("EVENT_INFO_NAME"), "Info On Listened Event")
												.addOptions(new OptionData(OptionType.STRING, names.get("EVENT_OPTION"), "events", true)
																	.addChoices(eventsToOptions)),
						event -> event.getInteraction().reply(CustomCommand.events.get(event.getOption(names.get("EVENT_OPTION")).getAsString()).toString()).queue())

				.createSubcommandBranch(new SubcommandData(names.get("INFO_NAME"), "Details On Usage"),
						event -> event.getInteraction().reply((String) JSONReader.getData().get("custom_command_info")).queue())

				.subcommandExecuter(event -> LOGGER.error("bad subcommand naming for Custom Command"))
			.build();
		}).get(),
		new SlashCommandBuilder("test", "test slash command", DEFAULT, GUILD, (data, names) -> {
				// data.addOptions(new OptionData(OptionType.STRING, "option-a", "test"));
				data.addSubcommands(new SubcommandData("sub-with-options", "test")
											.addOptions(new OptionData(OptionType.STRING, "option-1", "test"))
											.addOptions(new OptionData(OptionType.STRING, "option-2", "test")));
				// data.addSubcommandGroups(new SubcommandGroupData("sub-group", "test")
				// 								 .addSubcommands(new SubcommandData("sub-2-with-options", "test")
				// 														 .addOptions(new OptionData(OptionType.STRING, "option-1", "test"))
				// 														 .addOptions(new OptionData(OptionType.STRING, "option-2", "test"))));
						// .addSubcommands(new SubcommandData("sub", "test"))
				return event -> {

				};
			}).build()
		);

	  messageReactions =
	  		reactions.stream()
	  				.filter(r -> r instanceof MessageReaction)
	  				.map(r -> (MessageReaction) r)
	  				.collect(toList());
	}

	@NotNull
	private static Set<SlashCommand> getSlashCommands(Set<String> upsertCommands) {
		return Reactions.reactions.stream()
								  .filter(r -> r instanceof SlashCommand)
								  .filter(r -> upsertCommands.contains(r.getName()))
								  .map(r -> (SlashCommand) r)
								  .collect(toSet());
	}

	private static ReactionResponse parseCommand(Message message, Consumer<MessageReaction> successMessage, String... potentialMatches) {
		String commandName = null;
		for (String potentialMatch : potentialMatches) {
			if (potentialMatch != null) {
				commandName = potentialMatch;
			}
		}
		if (commandName == null) {
			LOGGER.error("Command Info - probable regex group number misplacement encountered");
			return FAILURE;
		}
		Reaction reaction = commandsByName.get(commandName);
		if (reaction == null) {
			return FAILURE.onFailureReply(message.getChannel().asTextChannel().sendMessage(truncate("That Command Doesn't Exist.")));
		} else {
			if (reaction instanceof MessageReaction) {
				successMessage.accept((MessageReaction) reaction);
				return SUCCESS;
			}
			else {
				return FAILURE.onFailureReply(message.getChannel().asTextChannel().sendMessage(truncate("Can't Use A Slash Command")));
			}
		}
	}

	private static Matcher charGroupSpecials = getMatcher("\\[.*(\\\\([sdDsSwW]).*)+\\]");

//	private static String fixCharGroups(String regex) {
//		charGroupSpecials.reset(regex).results().forEach(match -> replace(regex, match.start(), match.end(), extractToUnion(match)));
//		return regex;
//	}


	public static void flushAutoDeleteQueue() {
		// try catch ?
		for (AuditableRestAction<Void> voidAuditableRestAction : autoDeleteQueue) {
			try {
				voidAuditableRestAction.complete();
			} catch (ErrorResponseException | RejectedExecutionException ignored) { }
		}
	}

	private static final Deque<AuditableRestAction<Void>> autoDeleteQueue = new ArrayDeque<>();

	/**
	 * @return word(s) being censored
	 */
	private static String logWordCensor(final Message message, final Matcher matcher) {
		final String messageContent = message.getContentRaw();
		final String[] censoredWords = matcher.reset(messageContent).results().map(MatchResult::group).toArray(String[]::new);
		if (censoredWords.length == 0) {
			return null;
		}
		final AuditableRestAction<Void> delete = message.delete();
		autoDeleteQueue.addFirst(delete);
		delete.and(new GenericRestAction(message.getJDA(), autoDeleteQueue::pop))
			  .queueAfter(1, TimeUnit.HOURS, s -> {}, s -> {});
//					  () -> LOGGER.info("failed to delete already deleted message " + messageContent);
		final String words = String.join(", ", censoredWords);
		autodeleteLog.sendMessageEmbeds(
				new EmbedBuilder()
						.setAuthor(getUserName(message.getAuthor()), message.getJumpUrl(), message.getAuthor().getAvatarUrl())
						.addField("Message", truncateEnd(messageContent, 1024), false)
						.addField("ID", message.getId(), false)
						.addField("Censored Word" + (censoredWords.length > 1 ? "s" : ""), truncate(censoredWords, 1024), false)
						.setColor(Color.RED)
						.build()).queue();
		return words;
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
		for (String part : chain)
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

	public static String censorBasicRegexCaseInsensitive(String word) {
		return word;
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
				.collect(joining(joiner)), word.length(), joiner);
	}

	private static String notNumberRegex(final String regex, int wordLength, String joiner) {
		return "((?!(\\d){" + wordLength + "})" + regex + ")";
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

	public static <R extends Reaction> Map<ReactionChannelType, Map<UserCategory, Set<R>>>
	reactionsByCategoryChannel(Class<R> c) {
			return Stream.of(ReactionChannelType.values()).collect(toMap(Function.identity(), channelType ->
					Stream.of(UserCategory.values()).collect(toMap(Function.identity(), userCategory ->
							messageReactions.stream()
											.filter(r -> r.getChannelType().inRange(channelType))
											.filter(r -> r.getUserCategory().inRange(userCategory))
											.filter(r -> c.isAssignableFrom(r.getClass()))
											.map(r -> (R) r)
											.collect(toSet())))));
	}

	public static final Map<ReactionChannelType, Map<UserCategory, Set<MessageReaction>>> messageReactionsByCategoryChannel =
			reactionsByCategoryChannel(MessageReaction.class);

	public static final Map<ReactionChannelType, Map<UserCategory, Set<SlashCommand>>> slashCommandsByCategoryChannel =
			reactionsByCategoryChannel(SlashCommand.class);

	static {
		commandsByName = reactions.stream().collect(toMap(Reaction::getName, Function.identity()));
	}

	private static Set<MessageReaction> getReactionSet(final MessageReaction command) {
		return messageReactionsByCategoryChannel.get(command.getChannelType())
				.get(command.getUserCategory());
	}

	public static Set<MessageReaction> getMessageReactions(ReactionChannelType channelType, UserCategory userCategory, MessageType type) {
		if (!Main.getBot().getJDA().getGatewayIntents().contains(GatewayIntent.GUILD_MEMBERS) // can't detect when joined? find join message
				&& type == MessageType.GUILD_MEMBER_JOIN) {
//			getReactions(ReactionChannelType.GUILD, type.)
		}
		return getMessageReactions(channelType, userCategory);
	}

	public static Set<MessageReaction> getMessageReactions(ReactionChannelType channelType, UserCategory userCategory) {
		return messageReactionsByCategoryChannel.get(channelType).get(userCategory);
	}

	public static Set<SlashCommand> getSlashReactions(ReactionChannelType channelType, UserCategory userCategory) {
		return slashCommandsByCategoryChannel.get(channelType).get(userCategory);
	}

	static boolean addCommand(MessageReaction command) {
		final Set<MessageReaction> commandSet = getReactionSet(command);
		if (commandSet.stream().anyMatch(c -> c.equals(command) || c.getName().equals(command.getName())))
			return false;
		return commandSet.add(command);
	}

	static void removeCommand(MessageReaction command) {
		getReactionSet(command).remove(command);
	}

	public static void reply(final Message message, final String reply) {
		if (message.getChannelType() == ChannelType.TEXT)
			reply(message.getChannel().asTextChannel(), reply);
		else
			message.getChannel().sendMessage(truncate(reply)).queue(); // in DMs
	}

	public static void reply(final Message message, final MessageEmbed reply) {
		if (message.getChannelType() == ChannelType.TEXT)
			reply(message.getChannel().asTextChannel(), reply);
		else
			message.getChannel().sendMessageEmbeds(reply).queue();
	}

	public static void reply(final TextChannel channel, final String reply) {
		if (canUseChannel(channel))
			channel.sendMessage(truncate(reply)).queue();
		else
			logMissingChannelPermissions(channel);
	}

	public static String truncate(String text) {
		return truncateMiddle(text, 2000);
	}

	public static void main(String[] args) {
		truncate(new String[] { "abcde", "fgh", "ijklmnop" }, 10);
	}

	private static String truncate(String[] words, int maxSize) { // failure
		int joinedLength = Arrays.stream(words).mapToInt(String::length).sum() + (words.length - 1) * 2;
		if (joinedLength <= maxSize)
			return String.join(", ", words); // TODO truncate this just in case size is wrong? it shouldn't
		List<String> wordsByLength = Arrays.stream(words)
										   .sorted(Comparator.comparing(String::length).reversed())
										   .collect(toList());
		int totalLength = joinedLength;
		int ind = 0;
		while (totalLength > maxSize) {
			int wordLength = wordsByLength.get(ind).length();
			if (wordLength < 7)
				return truncateMiddle(String.join(", ", words), maxSize);
			/*
			totalLength aaaxxxxx
			wordLength  aaa
			take out between
			(x {1 to wL+7}) >= tL - maxSize
			dif = tL - maxSize;
			if (dif > wL+7)
			 */
			wordsByLength.set(ind, truncateMiddle(wordsByLength.get(ind), wordLength - maxSize));
			totalLength -= wordLength + 7;
			ind++;
		}
		return truncateEnd(String.join(", ", wordsByLength), maxSize);
	}

	public static String truncateMiddle(String text, int maxSize) {
		return text.length() > maxSize ? text.substring(0, maxSize / 2 - 1) + "..." + text.substring(text.length() - maxSize / 2 - 2) : text;
	}

	public static String truncateEnd(String text, int maxSize) {
		return text.length() > maxSize ? text.substring(0, maxSize - 3) + "..." : text;
	}

	public static void reply(final TextChannel channel, final MessageEmbed reply) {
		if (canUseChannel(channel))
			channel.sendMessageEmbeds(reply).queue();
		else
			logMissingChannelPermissions(channel);
	}

	private static final Matcher IDMatcher = Pattern.compile("[\\d]{18,21}").matcher("");

	/**
	 * @param tag the name of a server, channel, category, message, role, user, or emoji
	 * @param <T>
	 * @return
	 */
	public static <T extends ISnowflake> T tryGet(String tag, Function<String, T> getByID, RestAction<T> retrieveByID,
												  Function<String, T> getByName) {
		if (IDMatcher.reset(tag).matches()) {
			return Optional.ofNullable(getByID.apply(tag))
					.or(() -> Optional.ofNullable(retrieveByID.complete()))
					.orElse(getByName.apply(tag));
		} else {
			return getByName.apply(tag);
		}
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

	/**
	 * {@link java.util.Optional#ifPresentOrElse(Consumer, Runnable)} with a return type
	 */
	public static <T, R> R presentOrElseReturn(Optional<T> optional, Function<? super T, R> action, Supplier<R> emptySupplier) {
		if (optional.isPresent()) {
			return action.apply(optional.get());
		} else {
			return emptySupplier.get();
		}
	}

	public static <T> boolean presentOrElseReturnStatus(Optional<T> optional, Consumer<? super T> action) {
		return presentOrElseReturn(optional, t -> { action.accept(t); return true; }, () -> false);
	}

	public static boolean booleanReturnStatus(boolean present, Runnable action) {
		if (present) {
			action.run();
			return true;
		} else {
			return false;
		}
	}

}
