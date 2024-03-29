package com.wordpress.brancodes.messaging.reactions;

import bran.parser.CompositionParser;
import bran.tree.compositions.Composition;
import bran.tree.compositions.expressions.Expression;
import bran.tree.compositions.godel.GodelNumberFactors;
import bran.tree.compositions.statements.Statement;
import com.wordpress.brancodes.bot.Config;
import com.wordpress.brancodes.bot.LiquidRichardBot;
import com.wordpress.brancodes.database.DataBase;
import com.wordpress.brancodes.main.Main;
import com.wordpress.brancodes.messaging.PreparedMessages;
import com.wordpress.brancodes.messaging.reactions.message.MessageReaction;
import com.wordpress.brancodes.messaging.reactions.message.MessageReaction.MessageReactionBuilder;
import com.wordpress.brancodes.messaging.reactions.message.commands.Command.CommandBuilder;
import com.wordpress.brancodes.messaging.reactions.message.commands.SlashCommand;
import com.wordpress.brancodes.messaging.reactions.message.commands.SlashCommand.SlashCommandBuilder;
import com.wordpress.brancodes.messaging.reactions.message.commands.custom.CustomCommand;
import com.wordpress.brancodes.messaging.reactions.message.commands.custom.CustomCommand.CustomCommandBuilder;
import com.wordpress.brancodes.messaging.reactions.message.commands.custom.CustomCommandCompiler;
import com.wordpress.brancodes.messaging.reactions.message.commands.custom.exception.CustomCommandCompileErrorException;
import com.wordpress.brancodes.messaging.reactions.message.commands.custom.exception.InvalidCustomCommandException;
import com.wordpress.brancodes.messaging.reactions.message.commands.custom.types.obj.ClassType;
import com.wordpress.brancodes.messaging.reactions.unit.BMI;
import com.wordpress.brancodes.messaging.reactions.unit.UnitMatch;
import com.wordpress.brancodes.messaging.reactions.users.UserCategory;
import com.wordpress.brancodes.messaging.reactions.users.UserCategoryType;
import com.wordpress.brancodes.mybb.MyBBThread;
import com.wordpress.brancodes.mybb.MyBBUser;
import com.wordpress.brancodes.util.*;
import com.wordpress.brancodes.voice.PlayerManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.Message.MentionType;
import net.dv8tion.jda.api.entities.emoji.CustomEmoji;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;
import net.dv8tion.jda.api.entities.sticker.StickerItem;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;
import net.dv8tion.jda.internal.entities.emoji.CustomEmojiImpl;
import net.dv8tion.jda.internal.utils.PermissionUtil;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.math.BigInteger;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.wordpress.brancodes.bot.LiquidRichardBot.getUserName;
import static com.wordpress.brancodes.messaging.reactions.ReactionChannelType.*;
import static com.wordpress.brancodes.messaging.reactions.ReactionResponse.FAILURE;
import static com.wordpress.brancodes.messaging.reactions.ReactionResponse.SUCCESS;
import static com.wordpress.brancodes.messaging.reactions.users.UserCategoryType.*;
import static com.wordpress.brancodes.util.ImageUtil.sendSpeechBubbleImage;
import static com.wordpress.brancodes.util.RegexUtil.getCommandRegex;
import static com.wordpress.brancodes.util.RegexUtil.objectPronunsPart;
import static java.util.stream.Collectors.*;

public class ReactionManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(ReactionManager.class);

	final static boolean reactionQuestions = false;

	private static final CustomEmoji[] YAWN_EMOJIS = Arrays.stream(new String[] { "yawn~1:864990663438106624", "yawn~2:864990672209444864",
			"yawn~3:864990679645814825", "yawn~4:864990712919883804", "yawn~5:864990744786763797"/*, "yawn~6:864990758711721995",
			"yawn~7:864990776757190656", "yawn~8:864990782707204108", "yawn~9:864990787028647966", "yawn~10:864990791700578345",
			"yawn~11:864990796088737792", "yawn~12:864990799086747668", "yawn~13:864990801867964468", "yawn~14:864990805293793310", "yawn~15:864990808766414899"*/ }
	).map(e -> e.split(":")).map(s -> new CustomEmojiImpl(s[0], Long.parseLong(s[1]), false)).toArray(CustomEmoji[]::new);



	public static List<Reaction> reactions;
	public static List<MessageReaction> messageReactions; // TODO to Unmodifiable?

	public static Map<String, Reaction> reactionsByName;

	static final String aaveRegex = "(^|\\s)" + ((List<String>) JSONReader.getData().get("aave_terms"))
			.stream()
			.map(Censoring::censorBasicRegexCaseInsensitive)
			.collect(RegexUtil.orChainRegex()) + "($|\\s)";

	public static final long GUILD_GS = 910004207120183326L;
	public static final long GUILD_C = 1036455284308185139L;
	public static final long GUILD_DW = 959299477729079328L;

	final static Consumer<Throwable> LOG_EXCEPTION = f -> LOGGER.info(f.getMessage());

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
		}).helpPanel("Shut Me Off").docs("Complete the queued auto-deletes, then shut down the bot and the server").build(),
		new CommandBuilder("Restart", getCommandRegex("Restart"), OWNER, GUILD_AND_PRIVATE).execute(message -> {
			message.getJDA().cancelRequests();
			message.getChannel()
					.sendMessage(PreparedMessages.preparedMessages().get("positive").get(message.getGuild().getIdLong()))
					.complete();
			Main.reset();
		}).helpPanel("Restart Me").build(),
		new CommandBuilder("Help", getCommandRegex("Help(\\s+" + objectPronunsPart + "(\\s+Out)?)?(\\s+Here)?(\\s+Right\\s+Now)?"),
								   DEFAULT, GUILD_AND_PRIVATE).deniable().addMessageChannelCooldown(10_000L).execute(message ->
				PreparedMessages.replyEmbedMessage(message, "help")
		).helpPanel("Help On Commands (This Panel)").docs("Send compilation of any commands that have a help panel option by sections of \"(Role) in (Channel)\"").build(),
		new CommandBuilder("Say In", "!s\\s*(\\d{18,20})(\\D[\\S\\s]+)", MOD, GUILD_AND_PRIVATE).caseInsensitive().matchFull().executeStatus((message, matcher) -> {
			final TextChannel textChannelById = message.getJDA().getTextChannelById(matcher.group(1));
			if (textChannelById == null) {
				JDAUtil.reply(message, "I Was Not Able To Find That Channel \"" + matcher.group(1) + "\".");
				return false;
			} else {
				textChannelById.sendMessage(matcher.group(2)).queue();
				JDAUtil.reply(message, PreparedMessages.getMessage("positive") + " Sent To " + textChannelById.getAsMention()
									   + " In " + textChannelById.getGuild().getName());
				return true;
			}
		}).build(),
		new CommandBuilder("Say Here", "!s[\\S\\s]+", MOD, GUILD).caseInsensitive().matchFull().execute(message -> {
			String response = JavaUtil.truncate(message.getContentRaw().substring(2));
			message.delete().queue();
			if (message.getMessageReference() != null) {
				message.getReferencedMessage().reply(response).queue();
			} else {
				JDAUtil.reply(message, response);
			}
		}).docs("Send a matching message").build(),
		new CommandBuilder("Say Proper", "^!p[\\S\\s]+", MOD, GUILD).caseInsensitive().execute(message -> {
			String response = JavaUtil.truncate(CaseUtil.properCase(message.getContentRaw().substring(2)));
			message.delete().queue();
			if (message.getMessageReference() != null) {
				message.getReferencedMessage().reply(response).queue();
			} else {
				JDAUtil.reply(message, response);
			}
		}).docs("Send matching message converted to Proper Case").build(),
		new CommandBuilder("DM", "^!d\\s*(\\d{17,20})(\\D[\\S\\s]+)", MOD, GUILD_AND_PRIVATE).caseInsensitive().execute((message, matcher) ->  // (@?.{2,32})#(\d{4})
				message.getJDA().retrieveUserById(matcher.group(1)).queue(
						/*success*/    user -> user.openPrivateChannel().queue(privateChannel ->
								privateChannel.sendMessage(matcher.group(2)).queue(s -> {
									message.reply(JavaUtil.truncate(PreparedMessages.getMessage("positive") + " Sent Message To " + getUserName(user))).queue();
									LOGGER.info("Sent " + matcher.group(2) + " To " + getUserName(user) + " By " + getUserName(message.getAuthor()));
								})),
						/*failure*/    user -> message.reply(JavaUtil.truncate(PreparedMessages.getMessage("negative") + " Failed To Find User.")).queue())
		).docs("Send message to user").examples("!D 885535338415556989 Yo").build(),
		new CommandBuilder("Join Voice", "^!j\\s*(\\d{18,20})[\\S\\s]*", MOD, GUILD_AND_PRIVATE).caseInsensitive().executeStatus((message, matcher) -> {
			VoiceChannel voiceChannel = message.getJDA().getVoiceChannelById(matcher.group(1));
			if (voiceChannel == null) {
				message.reply(JavaUtil.truncate("Couldn't Find That Voice Channel ID.")).queue();
				return false;
			}
			voiceChannel.getGuild().getAudioManager().openAudioConnection(voiceChannel);
			return true;
			// reply(message, PreparedMessages.getMessage("positive") + " Will Attempt To Join.");
		}).docs("Join a voice channel by ID").examples("!J 439422552544123982").build(),
		new CommandBuilder("Disconnect", "!di|Disconnect\\.?", MOD, GUILD).caseInsensitive().matchFull().executeStatus((Message message) -> {
			final AudioManager audioManager = message.getGuild().getAudioManager();
			if (audioManager.isConnected()) {
				audioManager.closeAudioConnection();
				return true;
			}
			return false;
		}).docs("Leave its current voice channel in that guild").examples("!di").build(),

		new CommandBuilder("Servers", "!v\\s*", OWNER, PRIVATE).caseInsensitive().matchFull().execute(message ->
				message.reply(JavaUtil.truncate("`" + message.getJDA().getGuilds().stream().map(Guild::getName).collect(joining("\n")) + "`")).queue()).build(),
		new CommandBuilder("Get Channels", "!c\\s*", OWNER, GUILD).caseInsensitive().matchFull().execute(message ->
			System.out.printf("Channels in %s: %s\n", message.getGuild(),
					message.getGuild()
							.getTextChannels()
							.stream()
							.map(c -> c.getName() + ":" + JDAUtil.hasPermission(c, Permission.VIEW_CHANNEL))
							.collect(joining(", ")))
		).build(),

		new CommandBuilder("Get Commands", "!m|" + getCommandRegex("(Get|Tell|Show|Give)\\s+(" + objectPronunsPart + "\\s+)?((Every|All)\\s+)?(The\\s+)?Commands"),
						   MOD, GUILD_AND_PRIVATE).caseInsensitive().matchFull().execute(message -> {
			Map<Boolean, String> deactivatedGroupReactions = reactions.stream()//.filter(r -> r instanceof Command)
					.collect(groupingBy(Reaction::isDeactivated, mapping(Reaction::getName, joining(", ", "", "."))));
			message.reply(JavaUtil.truncate("All Commands: " + deactivatedGroupReactions.get(false) + "\nDeactivated:\n" + deactivatedGroupReactions.get(true))).queue();
		}).docs("Show all the commands").examples("Yo Pimp Show Us All The Commands", "Pimp Get Commands").build(),

		new CommandBuilder("Disable Command", "!dc\\s+([\\W\\w]+)", MOD, GUILD).caseInsensitive().matchFull().executeResponse((message, matcher) ->
				parseCommand(message, command -> {
					if (command.getName().equals("Disable Command"))
						return;
					command.deactivate();
					message.reply(JavaUtil.truncate(PreparedMessages.getMessage(message.getGuild().getIdLong(), "positive") + " Disabled " + command)).queue();
				}, matcher.group(1))
		).build(),
		new CommandBuilder("Enable Command", "!ec\\s+([\\W\\w]+)", OWNER, GUILD).caseInsensitive().matchFull().executeResponse((message, matcher) ->
				parseCommand(message, command -> {
					command.activate();
					message.reply(JavaUtil.truncate(PreparedMessages.getMessage(message.getGuild().getIdLong(), "positive") + " Enabled " + command)).queue();
				}, matcher.group(1))
		).build(),

		new CommandBuilder("Get Role", "(!r)|(" + getCommandRegex("((Get|Tell|Show|Give)\\s+" + objectPronunsPart + "?|What(\\s+I|')s)\\s+(My\\s+)?(Role|Position)") + ")",
						   DEFAULT, GUILD).caseInsensitive().matchFull().execute(message ->
				message.reply(JavaUtil.truncate("You Are " + UserCategory.of(message.getMember()) + ".")).queue()
		).build(),

		new CommandBuilder("DM History", "!h\\s+\\d{1,20}\\s+\\d+\\s*", OWNER, GUILD_AND_PRIVATE).caseInsensitive().matchFull().execute(message -> {
			String[] messageParts = message.getContentRaw().split("\\s+");
			message.getJDA().retrieveUserById(messageParts[1]).queue(user -> {
				if (user.equals(message.getJDA().getSelfUser())) // TODO FAILURE
					return;
				user.openPrivateChannel().queue(privateChannel -> {
					int amount = Integer.parseInt(messageParts[2]);
					privateChannel.getHistory().retrievePast(amount).queue(messageHistory -> {
						EmbedBuilder embedBuilder = new EmbedBuilder().setDescription("Message History With " + getUserName(user))
								.setThumbnail(user.getAvatarUrl())
								.setColor((Color) Config.get("embedColor"));
						messageHistory.forEach(otherMessage -> embedBuilder.addField(
								otherMessage.getAuthor().getName() + " " + JavaUtil.timeStampOf(otherMessage.getTimeCreated()),
								otherMessage.getContentDisplay() + "\n" +
										otherMessage.getAttachments().stream().map(Message.Attachment::getUrl).collect(joining(", ")),
								false));
						JDAUtil.reply(message, embedBuilder.build());
					});
				});
			});
		}).build(),

		new CommandBuilder("Nick All", "Nick\\s*\"([\\w]{2,32})\"[\\s\\S]+", MOD, GUILD).matchFull().executeStatus((message, matcher) -> {
			message.getMentions().getMembers().forEach(member -> { // TODO bad return status... needs to wait for queue?
				try {
					member.modifyNickname(matcher.group(1)).queue();
				} catch (InsufficientPermissionException ignored) {
					// return false;
				}
			});
			return true;
		}).docs("Give nick name to all mentioned members").examples("Nick \"Spammers\" @THEg#1534 @bottttt#9999").build(),

		new CommandBuilder("Kick All", "Kick[\\s\\S]+", MOD, GUILD).matchFull().executeStatus((message, matcher) -> {
			message.getMentions().getMembers().forEach(member -> { // TODO bad return status... needs to wait for queue? (if found nobody?)
				try {
					member.kick().queue();
				} catch (InsufficientPermissionException ignored) {
				}
			});
			return true;
		}).docs("Kick all mentioned members").examples("Kick @hijkl89#1508 @conor#0009 @aoeu#4035").build(),

		new CommandBuilder("Info", getCommandRegex("(Tell\\s+" + objectPronunsPart + "(\\s+What|\\s+About)?|Define|What\\s+Is)\\s+(The\\s+)?(Command\\s+(.{1,16})|(.{1,16})\\s+Command)(\\s+Is)?"),
								   MOD, GUILD).matchFull()
				.executeResponse((message, matcher) -> parseCommand(message, command -> JDAUtil.reply(message, command.toFullString()), matcher.group(22), matcher.group(23), matcher.group(38), matcher.group(39)))
				.andChainable()
				.helpPanel("Guide On Activating A Command")
				.docs("Send info about a command, i.e. its required role, location, RegEx (the message that activates it), any cooldowns, and info from the help panel, docs, or examples")
				.examples("Ok Pimp, Tell Me About The Greeting Command. Thanks.", "Pimp Define Command Info")
				.build(),

		new CommandBuilder("Example", "(!e)|(" + getCommandRegex("(Tell|Show|Give)\\s+(" + objectPronunsPart + "\\s+)?(An?\\s+)?Example\\s+((For|Of)\\s+)?(The\\s+)?(Command\\s+(.{1,16})|(.{1,16})\\s+Command)") + ")",
								   DEFAULT, GUILD_AND_PRIVATE).matchFull()
				.executeResponse((message, matcher) -> parseCommand(message, command -> message.reply(JavaUtil.truncate(command.getGenerex().random())).queue(), matcher.group(27), matcher.group(28), matcher.group(45), matcher.group(46)))
				.andChainable()
				.helpPanel("Give Example On How To Activate A Command").build(),

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

		new MessageReactionBuilder("Greeting", "((" + (Config.get("aliasesRegex") + "\\s*(\\?+|\\.+|,|!+)?\\s+" + "(Greetings|Sup|Hi|Hey|Hello|Yo)"
												+ ")|(" + "(Greetings|Sup|Hi|Hey|Hello|Yo)" + "\\s*[,.]?\\s+" + (Config.get("aliasesRegex"))) + "))\\s*[.!\\s]*",
							DEFAULT, GUILD_AND_PRIVATE).matchFull().execute(message ->
				JDAUtil.reply(message.getChannel().asTextChannel(), Math.random() < .5 ? "Not Here To Greet." : "Yo.")
		)
													   // .addMessageChannelCooldown(5_000L)
													   .build(),

		new MessageReactionBuilder("Auto Delete", "^?" + Censoring.censoredWordsRegex + "$?", SELF, GUILD) // TODO ^ $ ??
				.disableLogging()
				.whitelistGuilds(1043398817816526878L) // guildAutoDeleteChannelMap.keySet()
				.executeResponse((message, matcher) -> {
			if (!message.isPinned()) { // TODO guild subscribed to this command
				final String censoredWords = Censoring.logWordCensor(message, matcher);
				if (censoredWords == null)
					return FAILURE;
				// LOGGER.info("Censoring: " + message.getContentDisplay());
				return new ReactionResponse(SUCCESS, censoredWords);
			} else
				return FAILURE;
		}).build(),

		new MessageReactionBuilder("Censor Japanese", "[\u4E00-\u9FA0\u3041-\u3094\u30A1-\u30F4\u30FC\u3005\u3006\u3024]", DEFAULT, GUILD)
				.whitelistGuilds(GUILD_C).execute(message ->
				message.delete().queue()
		).build(),

		new CommandBuilder("Purge All", getCommandRegex("Purge(\\s+(Every)\\s+(Chat|Channel))?"), MOD, GUILD).matchFull().execute(message -> {
	//			message.reply(truncate(PreparedMessages.getMessage("positive"))).queue();
			final AtomicLong count = new AtomicLong(0L);
			message.getGuild().getChannels().forEach(channel -> {
				if (channel.getType().isMessage()) {
					((MessageChannel) channel).getIterableHistory().takeAsync(300).thenAccept(list -> list.forEach(m -> {
						if (m.getContentRaw().length() > 0 && Censoring.censoredWordsMatcher.reset(m.getContentRaw()).matches() && !m.isPinned()) {
							System.out.println(m.getContentRaw());
							m.delete().complete();
							// logWordCensor(message, matcher);
						}
					})).whenComplete((n, t) ->
							count.getAndIncrement()
					);
				}
			});
			message.reply(JavaUtil.truncate(PreparedMessages.getMessage("positive"))).queue();
		}).helpPanel("Purge Censor All Channels").build(),

		new CommandBuilder("Purge Current", getCommandRegex("Purge\\s+(The|This)?\\s+(Chat|Channel|(Right )?Here)"), MOD, GUILD).execute(message -> {
			message.reply(JavaUtil.truncate(PreparedMessages.getMessage("positive"))).queue();
			final AtomicLong count = new AtomicLong(0L);
			message.getChannel().getIterableHistory().forEach(m -> {
				// LOGGER.info("Checking message to purge: " + m.getContentRaw() + " - " + censoredWordsMatcher.reset(m.getContentRaw()).matches());
				if (m.getContentRaw().length() > 0 && Censoring.censoredWordsMatcher.reset(m.getContentRaw()).matches() && !m.isPinned()) {
					m.delete().queue();
					count.getAndIncrement();
					// logWordCensor(message, matcher);
				}
			});
			message.reply(JavaUtil.truncate(PreparedMessages.getMessage("positive") + " Deleted " + count + " Messages.")).queue();
		}).helpPanel("Purge Censor Current Channel").build(),

		new CommandBuilder("Prune", "pimp\\s+prune", MOD, GUILD).caseInsensitive().matchFull().executeResponse(commandMessage -> {
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
					Set<Member> needsProcessingMembers = membersWithRole.stream()
																		.filter(member -> member.getRoles()
																								.stream()
																								.anyMatch(r -> r .equals(roles.get(995451990777811024L))))
																		.collect(toSet());
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
					// nonRecentMembers.forEach(member -> member.kick("PRUNE (HASN'T TALKED IN 2 WEEKS)").queue());
					// LOGGER.info("PRUNED " + nonRecentMembers.size() + " MEMBERS.");
					System.out.printf("ALL USERS (%d):\n%s\nRECENT USERS(%d):\n%s\nNON RECENT USERS(%d)\n%s\n",
									  guildMembers.size(),
									  guildMembers.stream().map(Member::getUser).map(LiquidRichardBot::getUserName).collect(joining(", ")),
									  recentMembers.size(),
									  recentMembers.stream().filter(Objects::nonNull).map(Member::getUser).map(LiquidRichardBot::getUserName).collect(joining(", ")),
									  nonRecentMembers.size(), nonRecentMembers.stream().map(Member::getUser).map(LiquidRichardBot::getUserName).collect(joining(", ")));
					// commandMessage.getChannel().sendMessage(JDAUtil.nonEmpty(
					LOGGER.info(
							needsProcessingMembers.stream()
												  .sorted(Comparator.<Member, OffsetDateTime>comparing(m -> m.getUser().getTimeCreated()).reversed())
												  .map(member -> LiquidRichardBot.getUserName(member.getUser()) + ": " + member.getUser().getTimeCreated())
												  .collect(joining(", "))
					);
															// )).queue();
					/////// put pruneable users into needs processing:
					// Role replacement = commandMessage.getGuild().getRoleById(995451990777811024L);
					// Role remove = commandMessage.getGuild().getRoleById(907451275807981638L);
					// nonRecentMembers.forEach(m -> {
					// 	commandMessage.getGuild().removeRoleFromMember(m, remove).queue();
					// 	commandMessage.getGuild().addRoleToMember(m, replacement).queue();
					// });
				});
			});
			return SUCCESS;
		}).build(),
			  //|(\d+(\.(\d*))?("|''|[ ]?[Ii][Nn]([.CcSs\s]|$)?)?)
		new MessageReactionBuilder("Convert Units", ("(?<!^[?.!]mute\\s{1,5}\\S{1,30}\\s{1,5}\\d{0,10})(?<!https://\\S{0,1990})" // TODO delimit like 1x1 1-1
					+ "(?<negs>-*)(?<!\\$)(?:(?<feetInch>(?<base>(?<feet>\\d+)(?:[" + RegexUtil.apostrophes + "]|\\s*(?:foot|feet|ft\\.?)\\s*))"
					+ "(?:(?<inch>(?<whole>\\d+)(?:\\.(?<inchDec1>\\d*))?|\\.(?<inchDec2>\\d+))|[^" + RegexUtil.apostrophes + "s]))"
					+ "|" // (?<fromValue>(\\d)\\s*(by|x|-|to)\\s*)?
					+ "(?<value>(?<valWhole>\\d+)(?:\\.(?<valDec1>\\d*))?|\\.(?<valDec2>\\d+))\\s*(?:(?:something|ish|~)\\s*)?" // 12 12. 12.34 .34 .00 .0200
					+ "(?<unit>(kg|lb|meter|cm)s?([\\W]+|$)|kilo([sg\\s]|$)\\w*|([" + RegexUtil.apostrophes + "]{2}|[" + RegexUtil.doubleQuotes + "]|in(\\.|ch)|pound)\\w*)?)"),
			// 		    + "kgs?([^\\w]+|$)"
			// + "|kilo([sg\\s]|$)\\w*"
			// 		   + "|lbs?([^\\w]|$)"
			// 		  + "|pound\\w*"  //m([^\w]+|$)|
			// 		+ "|meters?([^\\w]+|$)"
			// 		   + "|cms?([^\\w]+|$)"
			// 	 + "|in(\\.|ch)\\w*)?)"),
								   ////|feet([^\w]+[\w\W]*|$)
							////|[\d]+\\s*'\\s*([\d]+(\.[\d]*)?)
					DEFAULT, GUILD_AND_PRIVATE)
				.caseInsensitive()
				.blacklistGuilds(GUILD_DW)
				.addMessageChannelCooldown(1_000L)
				.disableLogging()
				.executeResponse((message, matcher) -> { // TODO detect number without unit
			List<UnitMatch> matches = matcher.reset().results().map(UnitMatch::new).collect(toList());
			if (matches.size() == 0) {
				LOGGER.error("failed to convert " + message.getContentRaw());
				return FAILURE;
			}
			BMI bmi = new BMI(matches);
			matches = bmi.getActualMatches();
			if (matches.size() == 0) {
				return FAILURE;
			} else {
				StringJoiner converted = new StringJoiner(", ");
				StringJoiner conversionArrow = new StringJoiner(", ");
				for (UnitMatch match : matches) {
				// if (match.unit() != null && match.unit().unitSystem == UnitSystem.METRIC) // only convert some types
					converted.add(match.convertUnit());
					conversionArrow.add(match.fullMatch() + " -> " + match.convertUnit());
				}
				if (bmi.couldCalculate()) {
					converted.add(bmi.getConvertedString());
					conversionArrow.add(bmi.getLogString());
				}
				final String reply = converted.toString();
				if (!reply.trim().isEmpty())
					message.reply(JavaUtil.truncate(reply)).queue();
				return new ReactionResponse(SUCCESS, conversionArrow.toString());
			}
		}).examples("120 lbs", "5.2\'0.3\"", "2ish foot 3.56", "10 kilograms").build(),

		new MessageReactionBuilder("Delete Ping", ".*", PING_CENSORED, GUILD_AND_PRIVATE).executeStatus(message -> {
			if (message.getMentions()
					   .getMentions(MentionType.USER)
					   .stream()
					   .anyMatch(i -> i.getIdLong() == 849711011456221285L)) {
				message.delete().queue();
			} else {
				final Message referenceMessage = message.getReferencedMessage();
				if (referenceMessage != null && referenceMessage.getAuthor().getIdLong() == 849711011456221285L)
					message.delete().queue();
				else {
					return false;
				}
			}
			return true;
		}).deactivated().build(),

		new CommandBuilder("Birthday", "It'?s Time To Celebrate\\.?", DEFAULT, GUILD).caseInsensitive().matchFull().executeStatus(message -> {
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

		new CommandBuilder("As Image", "^(pimp\\s*image|pi($|\\s*(\\d{18,20}|https)))", DEFAULT, GUILD_AND_PRIVATE).caseInsensitive().executeStatus((Message message) ->
				JavaUtil.presentOrElseReturnStatus(
					ImageUtil.findImage(message),
				image -> message.getChannel().sendMessage(image.getProxyUrl()).queue()))
			.helpPanel("Add Speech Bubble To Image")
			.docs("Resend any image, predicted in order by (if multiple: the first of): "
				  + "Sticker, Custom Emote, Attachment From Message Reference/Message URL/Message ID, Mentioned Member PFP, "
				  + "Mentioned Role Icon, If There's No ID: The Last Image Out Of The Most Recent 10 Messages, First Attachment. "
				  + "Default Emojis And Stickers Don't Embed (Because They Are SVG/JSON)")
			.build(),

		new CommandBuilder("Speech Bubble", "^(speech\\s*bubble|sb$)", DEFAULT, GUILD_AND_PRIVATE).caseInsensitive().executeStatus((Message message) ->
				(message.getGuild().getIdLong() != GUILD_GS || message.getChannel().getIdLong() == 910004207610904673L)
				&& JavaUtil.presentOrElseReturnStatus(
					ImageUtil.findImage(message),
				image -> sendSpeechBubbleImage(message, image)))
			.helpPanel("Add Speech Bubble To Image").docs("See \"As Image\" docs, but adds scaled transparent speech bubble")
			.examples("Speech Bubble", "sb")
			.build(),

		new MessageReactionBuilder("Harita", "^haritard$", DEFAULT, GUILD)
				.caseInsensitive()
				.whitelistGuilds(GUILD_C)
				.execute(message -> {
			message.getChannel().sendMessage("https://media.discordapp.net/attachments/722001554944819202/965120982480224296/Screenshot_20220415-020148_Chrome.png.jpg").queue();
			message.delete().queue();
		}).build(),

		new MessageReactionBuilder("Embed Fail", "https://(www\\.)?tenor\\.com/view/(.)+", DEFAULT, GUILD)
				.blacklistGuilds(GUILD_C).executeStatus(
				message -> JavaUtil.booleanReturnStatus(!PermissionUtil.checkPermission(message.getMember(), Permission.MESSAGE_EMBED_LINKS)
														|| (message.getChannel() instanceof IMemberContainer
									&& !PermissionUtil.checkPermission(JDAUtil.toPermissionContainer(message.getChannel()), message.getMember(), Permission.MESSAGE_EMBED_LINKS)),
														() -> message.reply("Nice Embed Fail. And Before You Ask Why: We Don't Want To See Your Shitty Spam Gifs Here.").queue())
		).build(), // TODO to pr

		new CommandBuilder("Get Mods", getCommandRegex("(The|A|An)\\s+(Mod|Moderator)s?\\s*(\\s((In\\s+(This|The)\\s+(Server|Guild|Place))|Here))?[?.]*\\s*",
															   "((What|Who)\\s+(Are|Is)|(Whose|Who'?s|Who're))\\s+"), MOD, GUILD).execute(message ->
				JDAUtil.reply(message, new EmbedBuilder().setDescription("Moderators In \"" + message.getGuild().getName() + "\"")
														 .addField("Moderators", String.join("\n", DataBase.getMods(message.getGuild().getIdLong()).get()), true)
														 .setThumbnail(message.getGuild().getIconUrl())
														 .setColor((Color) Config.get("embedColor"))
														 .build())
		).helpPanel("Get Mods In Server").deactivated().build(),

		new CommandBuilder("Give Mod", getCommandRegex("((((Make|Set|Give)\\s*)@.{1,32}\\s*(A\\s+)?(Mod|Moderator))|(((Make|Set|Give)\\s*)?(Mod|Moderator)\\s+@.{1,32}))"),
								   OWNER, GUILD).execute(message ->
				JDAUtil.reply(message, DataBase.addMod(message.getGuild().getIdLong(),
													   message.getMentions().getMembers().stream()
								.map(Member::getIdLong) // TODO or filter by if it isn't a bot
								.filter(memberID -> !memberID.equals(message.getJDA().getSelfUser().getIdLong()))
								.findFirst()
								.orElse(null),
													   getUserName(message.getAuthor()))
											   .getFeedback())
		).helpPanel("Give Moderator To User").deniable().deactivated().build(),

		new CommandBuilder("Remove Mod", getCommandRegex("(((Remove|Re Move|Take)\\s*@.{1,32}\\s*('?s\\s+)?(Mod|Moderator))|(((Remove|Re Move|Take)\\s*)?(Mod|Moderator)\\s*@.{1,32}))"),
								   OWNER, GUILD).execute(message -> JDAUtil.reply(message, DataBase.removeMod(
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

		new CommandBuilder("Change Log", "!l\\s+([\\w\\d]+)", OWNER, GUILD_AND_PRIVATE).matchFull().executeStatus((message, matcher) -> {
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
		}).docs("Change where the log output is channeled").build(),

		// new CommandBuilder("Censor Target", ".*" + censorChainRegex("special, words", "[\\s.,]*") + ".*",
		// 						   CENSORED, GUILD).executeStatus(message -> // TODO censor optimizer
		// 	booleanReturnStatus(message.getGuild().getIdLong() == 793333500303769600L, () ->
		// 		message.delete().queue())
		// ).helpPanel("Censor Targeted Users").deactivated().build(),

		new MessageReactionBuilder("Censor AAVE", aaveRegex,  //".*([Bb][.,\\s;:]*[Rr][.,\\s;:]*[Aa][.,\\s;:]*[Nn][.,\\s;:]*[Dd][.,\\s;:]*[Oo0]).*"
							DEFAULT, GUILD).caseInsensitive().executeStatus((message, matcher) -> // TODO censor optimizer
			JavaUtil.booleanReturnStatus(message.getGuild().getIdLong() == GUILD_GS || message.getGuild().getIdLong() == GUILD_C, () ->
				JDAUtil.reply(message, String.format(
						"Please Retract This Message, %s. Are You A Black Person Of Color? "
								+ "No? Then No, You Should **NOT** Be Using The Word \"%s\", "
								+ "As It Is From From The AAVE Dialect And Is **Racist** To "
								+ "Use It. Please Educate Your Self And Avoid Any Future "
								+ "Microaggressions Against Black Individuals At aavenb.carrd.co",
						CaseUtil.properCase(message.getAuthor().getName()),
						CaseUtil.properCase(matcher.reset().results().findFirst().get().group()))))
		).deactivated().build(),
		new MessageReactionBuilder("Me&Whom", "(me\\s*and\\s*who)[^Mm][.,;:!?\\s]", DEFAULT, GUILD).caseInsensitive().execute(message -> {
			JDAUtil.reply(message, "Me And Whom.*");
		}).deactivated().build(),
		new MessageReactionBuilder("Yawn", ".*", YAWN, GUILD_AND_PRIVATE).execute(message -> {
			// if (message.getAuthor().getIdLong() == 749625271937663027L)
			if (!message.isFromGuild() || JDAUtil.hasPermission(message.getChannel().asTextChannel(), Permission.MESSAGE_EXT_EMOJI))
				for (CustomEmoji yawnEmoji : YAWN_EMOJIS) {
					message.addReaction(yawnEmoji).queue();
				}
			else {
//				if (hasPermission(message.getChannel().asTextChannel(), Permission.MESSAGE_ADD_REACTION))
//					message.addReaction("U+1F971").queue();
				JDAUtil.logMissingChannelPermissions(message.getChannel().asTextChannel());
			}
		}).docs("Yawn").build(),

		new MessageReactionBuilder("Reddit", "(reddit.com/|\\s+|/|^)r/[\\w\\d]+", DEFAULT, GUILD)
				.caseInsensitive()
				.disableLogging()
				.execute(message ->
					JavaUtil.booleanReturnStatus(
						message.getChannel().asGuildMessageChannel()
							   .getPermissionContainer() != null,
						() -> message.addReaction(Emoji.fromCustom("redditor", 953332619687395338L, false)).queue(s -> {}, LOG_EXCEPTION))
		).build(),

		new MessageReactionBuilder("Emoji Reaction", "^pimp\\s+(.+)", MOD, GUILD).caseInsensitive().matchFull().executeResponse((message, matcher) -> {
			Message referencedMessage = message.getReferencedMessage();
			if (referencedMessage == null) {
				return new ReactionResponse(FAILURE, "need to reply to a message");
			}
			final String code = matcher.group(1);
			final EmojiUnion emoji;
			try {
				emoji = Emoji.fromFormatted(code);
			} catch (IllegalArgumentException e) {
				return new ReactionResponse(FAILURE, e.getMessage());
			}
			// if (emoji instanceof UnicodeEmojiImpl && !(emoji.getAsReactionCode().startsWith("U+") || emoji.getAsReactionCode().startsWith("u+"))) {
			// 	return new ReactionResponse(FAILURE, "bad emoji");
			// }
			message.delete().queue();
			referencedMessage.addReaction(emoji).queue(s -> {}, LOG_EXCEPTION);
			return SUCCESS;
		}).docs("Add emoji to referenced message").build(),

		new CommandBuilder("Verify", "^\\?v(e(r(i(f)?)?)?)?", DEFAULT, GUILD_AND_PRIVATE).caseInsensitive().executeResponse((message, matcher) -> { // insert space after
			if (message.getMember().hasPermission(Permission.MANAGE_ROLES)) {
				BiConsumer<AtomicBoolean, CustomEmoji> callback = (anyCalled, emoji) -> { // this is a job for Scala
					if (!anyCalled.get()) {
						anyCalled.set(true);
						message.addReaction(emoji).queue(s -> {}, LOG_EXCEPTION);
					}
				};
				AtomicBoolean anySucceeded = new AtomicBoolean(false);
				AtomicBoolean anyFailed = new AtomicBoolean(false);
				Runnable succeed = () -> callback.accept(anySucceeded, Emoji.fromCustom("nigup", 910308835808395384L, false));
				Runnable fail = () -> callback.accept(anyFailed, Emoji.fromCustom("nigdown", 910308878766473256L, false));
				List<Member> members = message.getMentions().getMembers();
				if (members.isEmpty()) {
					fail.run();
					return SUCCESS;
				}
				Guild guild = message.getGuild();
				Role remove = guild.getRoleById(995451990777811024L);
				Role replacement = guild.getRoleById(907451275807981638L);
				if (remove == null || replacement == null) {
					return new ReactionResponse(FAILURE, "relevant verification role was deleted");
				}
				Set<Role> defaultMemberRoles = Set.of(replacement, guild.getRoleById(907468017280090122L)); // p alr
				TextChannel verificationChannel = guild.getChannelById(TextChannel.class, 907042441478164562L);
				if (verificationChannel == null)
					return new ReactionResponse(FAILURE, "verification channel was deleted");
				members.forEach(member -> {
					List<Role> currentRoles = member.getRoles();
					if (!Collections.disjoint(currentRoles, defaultMemberRoles)
						|| !currentRoles.contains(remove)
								|| !PermissionUtil.checkPermission(verificationChannel, member, Permission.VIEW_CHANNEL))
						fail.run();
					else
						guild.modifyMemberRoles(member, Collections.singleton(replacement), Collections.singleton(remove))
							 .queue(s -> succeed.run(), f -> fail.run());
				});
				return SUCCESS;
			} else {
				return new ReactionResponse(FAILURE, "member is not allowed to verify"); // TODO command collision problems?
			}
		}).debugReactionResponse().build(),

		new MessageReactionBuilder("Questions",
							// "^(" +
		//		"((.*(\\?|:(grey_)?question:|\u2753|\u2754))|([,;:<>&^%$#@!{}\\[\\]=/\\-.*+()_\\s]*(\\?|:(grey_)?question:|\u2753|\u2754)))"
		//		+ "[,;:<>&^%$#@!{}\\[\\]=/\\-.*+()_\\s]*"
		//		+ "|([,;:<>&^%$#@!{}\\[\\]=/\\-.*+()_\\s]*(([Oo]+[Mm]+[Gg]+|[Ww]+[Aa]+[Ii]+[Tt]+|[Oo]+[Hh]*)\\s+)?" //([Cc][\s]*[Aa][\s]*[Nn])
		//	  "((([Ww][Hh]([Ii][Cc][Hh]|[Oo]+|[Aa]+[Tt]|[Ee][Rr][Ee]|[Ee][Nn]+|[Yy]+))|[Hh][Oo]+[Ww])" // interrogative
		//	  + "('?[Ss]|[Ii][Ss]|[Aa][Rr][Ee])?([\\s+].*|[,;:<>&^%$#@!{}\\[\\]=/\\-.*+()_\\s])?)"
		//	  + "|([Rr][Ee][Aa][Ll]+[Yy]+[.?;:\\s]*))\\?",  // Hey Pimp, Can You Help Me

		"^((((wh(ich|o+|a+t|ere|en+|y+))|ho+w)('?s|is|are)?([\\s+].*|[,;:<>&^%$#@!{}\\[\\]=/\\-.*+()_\\s])?)|(real+y+[.?;:\\s]*))",
							DEFAULT, GUILD_AND_PRIVATE)
				.caseInsensitive()
				.addGuildCooldown(10_000_000L)
				.deactivated()
				.execute(message -> {
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
					JDAUtil.reply(message, "Not Here To Answer Questions.");
			// }
		}).docs("Detect when user asks a question").build(),

		new CommandBuilder("Evaluate", "Evaluate`*+(.+)`*+", DEFAULT, GUILD_AND_PRIVATE).matchFull().execute((message, matcher) -> {
			String response;
			try {
				Composition comp = CompositionParser.parse(matcher.group(1));
				if (comp instanceof Statement) {
//					throw new Exception("not an expression");
					response = String.valueOf(((Statement) comp).truth());
				} else {
					double evaluation = ((Expression) comp).evaluate();
					System.out.println(evaluation % 1);
					response = evaluation <= Integer.MAX_VALUE && evaluation % 1 == 0 ? Integer.toString((int) evaluation) : Double.toString(evaluation);
				}
			} catch (Exception e) {
				response = "`" + e.getMessage() + '`';
			}
			message.reply(response).queue();
		}).caseInsensitive().build(),
		new CommandBuilder("Domain", "Domain(\\s+((And\\s+)?(Then\\s+)?)?Simplify)?`*+(.+)`*+", DEFAULT, GUILD_AND_PRIVATE).matchFull().execute((message, matcher) -> {
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
		}).caseInsensitive().docs("Find the domain of an expression").build(),
		// new CommandBuilder("Range", "^Range(.+)$", DEFAULT, GUILD_AND_PRIVATE).execute((message, matcher) -> {
		// 	message.reply(String.valueOf(ExpressionParser.parseExpression(matcher.group(1)).range())).queue();
		// }).caseInsensitive().build(),
		new CommandBuilder("Derive", "Deriv(e|ative)(\\s+((And\\s+)?(Then\\s+)?)?Simplify)?`*+(.+)`*+", DEFAULT, GUILD_AND_PRIVATE).matchFull().execute((message, matcher) -> {
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
		}).caseInsensitive().docs("Find a math derivative").build(),
		new CommandBuilder("Inverse", "Inverse(\\s+((And\\s+)?(Then\\s+)?)?Simplify)?`*+(.+)`*+", DEFAULT, GUILD_AND_PRIVATE).matchFull().execute((message, matcher) -> {
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
		new CommandBuilder("Truth", "Statement\\s+Truth(.+)", DEFAULT, GUILD_AND_PRIVATE).matchFull().execute((message, matcher) -> {
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
		}).caseInsensitive().deactivated().docs("Find the truthness of a statement (can use Evaluate)").build(),
		new CommandBuilder("Truth Table", "Truth\\s+Table`*+(.+)`*+", DEFAULT, GUILD_AND_PRIVATE).matchFull().execute((message, matcher) -> {
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
		new CommandBuilder("Godel", "G[oö]del\\s*`*+(.+)`*+", DEFAULT, GUILD_AND_PRIVATE).matchFull().execute((message, matcher) -> {
			String response;
			String input = matcher.group(1);
			if (input.matches("[\\s',.\"?!]+"))
				return;
			try {
				if (input.matches("[\\d]+")) {
					response = new GodelNumberFactors(new BigInteger(input)).symbols();
				} else {
					final Composition parsed = CompositionParser.parse(input);
					response = parsed.godelNumber().toString();
				}
			} catch (Exception e) {
				response = "`" + e.getMessage() + '`';
			}
			if (response.length() == 0)
				response = "Empty";
			message.reply(response).queue();
		}).caseInsensitive().docs("Convert composition to Gödel representation and Gödel number or from a number").build(),
		new CommandBuilder("Simplify", "Simpl(if)?y`*+(.+)`*+", DEFAULT, GUILD_AND_PRIVATE).matchFull().execute((message, matcher) -> {
			String response;
			String input = matcher.group(2);
			if (input.matches("[\\w\\s',.\"?!]+"))
				return;
			try {
				response = CompositionParser.parse(input).simplified().toString();
			} catch (Exception e) {
				response = "`" + e.getMessage() + '`';
			}
			 message.reply(JavaUtil.truncate(response)).queue();
			// message.reply(String.valueOf(CompositionParser.parse(matcher.group(1)).simplified())).queue();
		}).caseInsensitive().docs("Simplify a math expression or statement").build(),
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
		new MessageReactionBuilder("Post On MyBB", "^Pimp Post\\s+(.+)?", MYBB, GUILD_AND_PRIVATE)
				.caseInsensitive().disableLogging().execute((message, matcher) -> {
			MyBBUser myBBUser = MyBBUser.users.get(message.getAuthor().getIdLong());
			MyBBThread.myBBDiscordThread.post(myBBUser, matcher.group(1), message.getAttachments());
		}).andReactPositive().build(),
		new MessageReactionBuilder("Repost On MyBB", "^Pimp Repost\\s+(.+)", MYBB, GUILD_AND_PRIVATE)
				.caseInsensitive().executeStatus((message, matcher) -> {
			Message referencedMessage = message.getReferencedMessage();
			if (referencedMessage == null)
				return false;
			MyBBUser myBBUser = MyBBUser.users.get(message.getAuthor().getIdLong());
			// MyBBUser referencedMyBBUser = MyBBUser.users.get(referencedMessage.getAuthor().getIdLong());
			String extraPostText = matcher.group(1);
			// Message primaryMessage;
			// if (referencedMyBBUser == null) { // TODO will allow users to see who the actual account is on MyBB
			// 	postText += "\n\nOriginal User: ";
			// 	primaryMessage = referencedMessage;
			// } else {
			// 	postText += "\n\nReposted By: ";
			// 	primaryMessage = message;
			// }
			// Member member = primaryMessage.getMember();
			// postText += member != null ? member.getEffectiveName() : LiquidRichardBot.getUserName(primaryMessage.getAuthor());
			MyBBThread.myBBDiscordThread.post(myBBUser, extraPostText != null ? extraPostText : referencedMessage.getContentDisplay(), referencedMessage.getAttachments());
			return true;
		}).andReactPositive().build(),
		new CommandBuilder("Upsert Slash", "^!us\\s+(.+)$", OWNER, GUILD_AND_PRIVATE).caseInsensitive().executeResponse((message, matcher) -> {
			List<String> inputStrings = Arrays.asList(matcher.group(1).split("\\s+"));
			if (inputStrings.size() <= 1)
				return new ReactionResponse(FAILURE, "you must provide a command name and \"true\" at the end for it to be upserted globally");
			Set<String> upsertCommands = new HashSet<>(inputStrings.subList(0, inputStrings.size() - 1));
			return getSlashCommands(upsertCommands).stream()
											.map(s -> s.upsert(inputStrings.size() > 2 && inputStrings.get(inputStrings.size() - 1).equalsIgnoreCase("true")))
											.reduce(ReactionResponse::combine)
											.orElse(new ReactionResponse(FAILURE, "couldn't find any of those commands"));
		}).build(),
		new CommandBuilder("Delete Slash", "^!ds\\s+(.+)$", OWNER, GUILD_AND_PRIVATE).caseInsensitive().executeResponse((message, matcher) -> {
			Set<String> upsertCommands = new HashSet<>(Arrays.asList(matcher.group(1).split("\\s+")));
			return upsertCommands.stream()
						  .map(SlashCommand::delete)
						  .reduce(ReactionResponse::combine)
						  .orElse(new ReactionResponse(FAILURE, "couldn't find any of those commands"));
		}).build(),
		new SlashCommandBuilder("kill", "End Your Life.", DEFAULT, GUILD_AND_PRIVATE)
//				.addSubcommands(new SubcommandData("info", "Show Details.").addOption())
				.addOption(OptionType.STRING, "cause", "Cause Of Death.", true)
				.execute(event ->
					event.getInteraction().reply("Hope You Die By " + CaseUtil.properCase(event.getOption("cause").getAsString())).queue()
		).build(),
		new SlashCommandBuilder("trig", "Evaluate Trig Expression.", DEFAULT, GUILD_AND_PRIVATE)
				.createSubcommandBranch(new SubcommandData("info", "Show Details."),
						event -> event.reply("Input Sin, Cos, Or Tan And A Number As Its Argument To Get A Calculation.").queue())
				.createSubcommandBranch(new SubcommandData("calc", "Execute Trig Function.")
								.addOptions(new OptionData(OptionType.STRING, "func", "function", true)
													.addChoice("sin", "sin")
													.addChoice("cos", "cos")
													.addChoice("tan", "tan"))
								.addOption(OptionType.NUMBER, "arg", "valued to be calculated upon", true),
						event -> {
							double arg = event.getOption("arg").getAsDouble();
							double res = switch (event.getOption("func").getAsString()) {
								case "sin" -> Math.sin(arg);
								case "cos" -> Math.cos(arg);
								case "tan" -> Math.tan(arg);
								default -> 0;
							};
							event.reply(String.valueOf(res)).queue();})
				.subcommandExecuter(event -> LOGGER.error("bad subcommand naming for Custom Command"))
				.docs("This is just a test slash command")
				.build(),

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
			}).deactivated().build(),

		new MessageReactionBuilder("Tkin", ".*", DEFAULT, GUILD).executeStatus(message -> {
			 if (message.getAuthor().getIdLong() == 991751124308730036L
				 	&& (Set.of(968670122082455623L, 1035267648130396232L, 910004207610904666L).contains(message.getChannel().getIdLong()))) {
				 ThreadChannel channel = message.getJDA().getChannelById(ThreadChannel.class, 1046297727555874846L);
				 boolean sentAny = false;
				 for (Message.Attachment attachment: message.getAttachments()) {
					 channel.sendMessage(attachment.getProxyUrl()).queue();
					 sentAny = true;
				 }
				 for (StickerItem sticker: message.getStickers()) {
					 channel.sendMessage(sticker.getIconUrl()).queue();
					 sentAny = true;
				 }
				 return sentAny;
			}
			return false;
		}).build(),

		new MessageReactionBuilder("Logger", ".*", DEFAULT, GUILD_AND_PRIVATE).executeResponse(message -> {
			Set<Long> loggedChannels = Set.of(1037977597272924190L);
			if (loggedChannels.contains(message.getChannel().getIdLong())) {
				return new ReactionResponse(SUCCESS, "\"" + message.getContentRaw() + "\" from " + message.getAuthor().getIdLong() + " " + message.getAuthor().getName());
			} else {
				return FAILURE;
			}
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
		upsertCommands.forEach(System.out::println);
		return ReactionManager.reactions.stream()
										.filter(r -> r instanceof SlashCommand)
										.filter(r -> upsertCommands.contains(r.getName()))
										.map(r -> (SlashCommand) r)
										.collect(toSet());
	}

	private static ReactionResponse parseCommand(Message message, Consumer<MessageReaction> successMessage, String... potentialMatches) {
		String commandName = null;
		for (String potentialMatch : potentialMatches) {
			System.out.println(potentialMatch);
			if (potentialMatch != null) {
				commandName = potentialMatch;
			}
		}
		if (commandName == null) {
			LOGGER.error("Command Info - probable regex group number misplacement encountered");
			return FAILURE;
		}
		Reaction reaction = reactionsByName.get(commandName);
		if (reaction == null) {
			return FAILURE.onFailureReply(message.getChannel().asTextChannel().sendMessage(JavaUtil.truncate("That Command Doesn't Exist.")));
		} else {
			if (reaction instanceof MessageReaction) {
				successMessage.accept((MessageReaction) reaction);
				return SUCCESS;
			}
			else {
				return FAILURE.onFailureReply(message.getChannel().asTextChannel().sendMessage(JavaUtil.truncate("Can't Use A Slash Command")));
			}
		}
	}

	/*
	another layout idea:
	master method which will take some condition for a command to be run
	Context context(context -> condition, context -> Context);
	then, we flatten all of these with DiscreteMath Statement#simplify
	 */

	public static <R extends Reaction> Map<ReactionChannelType, Map<UserCategoryType, Set<R>>> reactionsByCategoryChannel(Class<R> c) {
			return Stream.of(ReactionChannelType.values()).collect(toMap(Function.identity(), channelType ->
					Stream.of(UserCategoryType.values()).collect(toMap(Function.identity(), userCategory ->
							messageReactions.stream()
											.filter(r -> r.getChannelType().inRange(channelType))
											.filter(r -> r.getUserCategory().inRange(userCategory))
											.filter(r -> c.isAssignableFrom(r.getClass()))
											.map(r -> (R) r)
											.collect(toSet())))));
	}

	public static final Map<ReactionChannelType, Map<UserCategoryType, Set<MessageReaction>>> messageReactionsByCategoryChannel =
			reactionsByCategoryChannel(MessageReaction.class);

	public static final Map<ReactionChannelType, Map<UserCategoryType, Set<SlashCommand>>> slashCommandsByCategoryChannel =
			reactionsByCategoryChannel(SlashCommand.class);

	static {
		reactionsByName = reactions.stream().collect(toMap(Reaction::getName, Function.identity()));
	}

	private static Set<MessageReaction> getReactionSet(final MessageReaction command) {
		return messageReactionsByCategoryChannel.get(command.getChannelType())
				.get(command.getUserCategory());
	}

	public static Set<MessageReaction> getMessageReactions(ReactionChannelType channelType, UserCategoryType userCategoryType, MessageType type) {
		if (!Main.getBot().getJDA().getGatewayIntents().contains(GatewayIntent.GUILD_MEMBERS) // can't detect when joined? find join message
				&& type == MessageType.GUILD_MEMBER_JOIN) {
//			getReactions(ReactionChannelType.GUILD, type.)
		}
		return getMessageReactions(channelType, userCategoryType);
	}

	public static Set<MessageReaction> getMessageReactions(ReactionChannelType channelType, UserCategoryType userCategoryType) {
		return messageReactionsByCategoryChannel.get(channelType).get(userCategoryType);
	}

	public static Set<MessageReaction> getMessageReactions(ReactionChannelType channelType, UserCategory userCategory) {
		return messageReactionsByCategoryChannel.get(channelType)
												.entrySet()
												.stream()
												.filter(p -> userCategory.isPartOf(p.getKey()))
												.flatMap(p -> p.getValue().stream())
												.collect(Collectors.toSet());
	}

	public static Set<SlashCommand> getSlashReactions(ReactionChannelType channelType, UserCategoryType userCategoryType) {
		return slashCommandsByCategoryChannel.get(channelType).get(userCategoryType);
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

}
