package com.wordpress.brancodes.messaging.reactions;

import com.wordpress.brancodes.database.DataBase;
import com.wordpress.brancodes.messaging.reactions.commands.Commands;
import com.wordpress.brancodes.util.CaseUtil;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageReaction {

	private static final Logger LOGGER = LoggerFactory.getLogger(MessageReaction.class);
	// private static final String aliasesRegexPart = orChainRegex((String[]) Config.get("aliases"));
	// private static final String questionRegexPart = "(((Can\\s+|Could\\s+)You\\s+)|(You\\s+Should\\s+))?(Please\\s+)*\\s*";
	// private static final Matcher helpCommandRegex = getCommandRegex(questionRegexPart + "Help(\\s+(Me|Him|Her|Them|It|Every(\\s+One)?)(\\s+Out)?)?(\\s+Here)?(Right\\s+Now)?");
	// private static final Matcher makeModCommandRegex = getCommandRegex(questionRegexPart + "((((Make|Set|Give)\\s*)@.{1,32}\\s*(A\\s+)?(Mod|Moderator))|(((Make|Set|Give)\\s*)?(Mod|Moderator)\\s+@.{1,32}))\\s*");
	// private static final Matcher removeModCommandRegex = getCommandRegex("");
	// private static final Matcher setEmojiCommandRegex = getCommandRegex("");
	// private static final Matcher resetEmojiCommandRegex = getCommandRegex("");
	// private static final Matcher shutDownCommandRegex = getCommandRegex("(((Turn)?\\s*Off)|(Shut\\s*(Down|Off|Up)))");
	// private static final Matcher IDParameterRegex = getMatcher("[\\d]{1,20}");
	// private static final Matcher userIDParameterRegex = getMatcher("(([\\d]{1,20})|(<@[\\d]{1,20}>))");
	// private static final Matcher interrogativesRegex = getMatcher(orChainRegex(new String[] { "Which", "What", "Whose", "Who", "Whose", "What", "Which", "Where", "When", "How", "Why" }));
	// private static final Matcher sendMessageCommandRegex = getCommandRegex("\\s*!s.+");
	// private static final String shutDownCommandRegex = getCommandRegex("(Turn|Shut)?\\s*(Off|Down)");

	// private static String orChainRegex(String[] chain) {
	// 	StringBuilder sb = new StringBuilder();
	// 	sb.append('(');
	// 	for (String part: chain)
	// 		sb.append(part)
	// 		  .append('|');
	// 	sb.delete(sb.length() - 1, sb.length());
	// 	sb.append(')');
	// 	return sb.toString();
	// }

	// private static Matcher getCommandRegex(String regexQuestion) {
	// 	return getMatcher("\\s*((Yo|Hey|Ok|Alright|All\\s+Right|Hi|Hello)\\s*(,|\\.+|!+|\\s)\\s*)?((" + aliasesRegexPart
	// 					  + "\\s*(,|\\.+|!+|\\s)\\s*" + regexQuestion + ")|(" + regexQuestion + "\\s*(,|\\s)\\s*" + aliasesRegexPart
	// 					  + "))\\s*(\\?+|\\.+|!+)?\\s*((Thanks|Thank\\s+You)\\s*(\\.+|!+)?)?\\s*");
	// }

	// private static Matcher getMatcher(String regex) {
	// 	return Pattern.compile(regex).matcher("");
	// }

	// private static boolean matchesCommand(final Matcher regexCommand) {
	// 	return regexCommand.reset(message).matches();
	// }

	// public static void ownerMessage(final @NotNull GuildMessageReceivedEvent event) {
	// 	if (Reactions.makeModCommand.executeIfMatches(event.getMessage())) {
	// 		DataBase.addMod(event.getGuild().getIdLong(), event.getAuthor().getIdLong());
	// 	} else if (Reactions.makeModCommand.removeModCommandRegex(message))
	// 		DataBase.removeMod(event.getGuild().getIdLong(), event.getAuthor().getIdLong());
	// 	else
	// 		Reactions.reply(event.getChannel(), message);
	// 	// message(event, message);
	// 	modMessage(event);
	// }

	public static void modMessage(final @NotNull MessageReceivedEvent event) {
		message(event);
	}

	// public static void botMessage(final @NotNull GuildMessageReceivedEvent event) {
	// 	if (DataBase.respondToBots(event.getGuild().getIdLong()).get())
	// 		message(event);
	// }

	public static void message(final @NotNull MessageReceivedEvent event) {
		// Commands.commands.filter(command -> command.getChannelType().equals(UserCategory.DEFAULT)).findFirst().ifPresent(command -> command.executeIfMatches(event.getMessage()));
		// if (!Commands.helpCommand.executeIfMatches(event.getMessage()))
		// 	;

	}
	// LOGGER.info("{} tried to run a command, but they are not an admin in {}", event.getAuthor(), event.getGuild());
	// event.getAuthor().openPrivateChannel().queue(channel -> channel.sendMessage("Hey. :slight_smile:").queue());
	// LOGGER.info("{} in {} said {}", event.getAuthor(), event.getGuild(), event.getMessage().getContentRaw());

	public static void privateOwnerMessage(final @NotNull MessageReceivedEvent event, final String message) {
		// if (Commands.sendMessageCommand.executeIfMatches(event.getMessage())) {
		//
		// } else
		// 	privateMessage(event, message); // If owner wants to log self messages
	}

	public static void privateBotMessage(final @NotNull MessageReceivedEvent event, final String message) {
		if (DataBase.respondToBotPrivate())
			privateMessage(event, message);
	}

	public static void privateMessage(final @NotNull MessageReceivedEvent event, final String message) {
		LOGGER.info("DM from {}: \"{}\"", event.getAuthor().getName(), event.getMessage().getContentRaw());
		switch (DataBase.userDMsProperCase(event.getAuthor().getIdLong())) {
			case DMProperCaseNumbers:
				Commands.reply(event.getMessage(), CaseUtil.properCase(message)); break;
			case DMProperCaseNoNumbers:
				Commands.reply(event.getMessage(), CaseUtil.properCaseExcludeNumbers(message)); break;
			case NoDMProperCase:
				Commands.reply(event.getMessage(), "Don't DM Me."); break; // Taper?
			default:
		}
	}

}
