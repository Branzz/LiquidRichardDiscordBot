package com.wordpress.brancodes.messaging.reactions.users;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.wordpress.brancodes.messaging.reactions.users.UserCategoryType.*;

/**
 * A user's category consists of 1 of a ranked category types and a set of any number of non-ranked category types
 * <p>
 * Example: PUBLIC, CENSORED, YAWN
 * Example: MOD
 */
public class UserCategory {

	UserCategoryType baseUserCategoryType;
	Set<UserCategoryType> extraTypes;

	public UserCategory(UserCategoryType baseUserCategoryType, Set<UserCategoryType> extraTypes) {
		this.baseUserCategoryType = baseUserCategoryType;
		this.extraTypes = extraTypes;
	}

	public static UserCategory from(MessageReceivedEvent messageReceivedEvent) {
		return userCategoryReduce(new UserMemberMixin(messageReceivedEvent.getAuthor(), messageReceivedEvent.getMember()));
	}

	public static UserCategory from(SlashCommandInteractionEvent slashCommandEvent) {
		return userCategoryReduce(new UserMemberMixin(slashCommandEvent.getUser(), slashCommandEvent.getMember()));
	}

	public static UserCategory of(User user) {
		return userCategoryReduce(new UserMemberMixin(user));
	}

	public static UserCategory of(Member member) {
		return userCategoryReduce(new UserMemberMixin(member));
	}

	private static UserCategory userCategoryReduce(UserMemberMixin mixin) { // should cache?? but, it is dynamic
		return new UserCategory(List.of(SELF, BOT, OWNER, MOD).stream().filter(u -> u.inCategory(mixin)).findFirst().orElse(DEFAULT),
								List.of(MYBB, PING_CENSORED, CENSORED, YAWN).stream().filter(u -> u.inCategory(mixin)).collect(Collectors.toSet()));
	}

	public boolean isPartOf(UserCategoryType key) {
		return key.inRange(baseUserCategoryType) || extraTypes.contains(key);
	}

	@Override
	public String toString() {
		return baseUserCategoryType + ", " + extraTypes.stream()
													   .map(UserCategoryType::toString)
													   .collect(Collectors.joining(", "));
	}

}
