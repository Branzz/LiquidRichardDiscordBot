package com.wordpress.brancodes.messaging.reactions.commands;

import com.wordpress.brancodes.messaging.reactions.ReactionChannelType;
import com.wordpress.brancodes.messaging.reactions.users.UserCategory;
import net.dv8tion.jda.api.entities.Message;

import java.util.function.BiFunction;
import java.util.regex.Matcher;

//public class CustomCommand extends Command {

	/**
	 * types:
	 * num (int), str, bool, id (long/String), user, channel, message, emoji (String, ??), list
	 * vars: declaration, global
	 * message: id, channel, author, mentionedUsers, time, jumpURL // admin: delete, pin
	 * user: id, name, pfpURL, bannerURL
	 * channel: id, name, sendMessage(str)
	 * guild /l can't do these things if declare it non-guild command and similar
	 * functions:
	 * num group(num), addReaction(emoji), user getUser(id)
	 * channel getChannel(id), sendMessage(str), sendMessage(id, str)
	 * code control:
	 * if, else
	 * @param input must specify the regex / periodic, name, user category, channel, and code
	 */
	// public CustomCommand(Message message, Matcher matcher, String input) {
	// }

//	public CustomCommand(final String regex, final String name, final UserCategory category, final ReactionChannelType channelCategory,
//						 final BiFunction<Message, Matcher, Boolean> executer) {
////		super(regex, name, category, channelCategory);
//	}

//}
