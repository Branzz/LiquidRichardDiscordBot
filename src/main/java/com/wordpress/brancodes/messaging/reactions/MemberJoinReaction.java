package com.wordpress.brancodes.messaging.reactions;

import com.wordpress.brancodes.messaging.reactions.users.UserCategory;
import net.dv8tion.jda.api.entities.Message;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Matcher;

public class MemberJoinReaction extends Reaction {

	public MemberJoinReaction(Reaction.Builder reactionBuilder) {
		super(reactionBuilder.name, reactionBuilder.matcher, reactionBuilder.deactivated, reactionBuilder.userCategory,
				ReactionChannelType.GUILD, reactionBuilder.executeResponse, reactionBuilder.executeMatcherResponse);
	}

	protected MemberJoinReaction(String name, Matcher matcher, boolean deactivated, UserCategory userCategory,
								 ReactionChannelType channelCategory, Function<Message, ReactionResponse> executeResponse,
								 BiFunction<Message, Matcher, ReactionResponse> executeMatcherResponse) {
		super(name, matcher, deactivated, userCategory, channelCategory, executeResponse, executeMatcherResponse);
	}

}
