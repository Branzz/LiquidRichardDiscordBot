package com.wordpress.brancodes.messaging.reactions.users;

import com.wordpress.brancodes.database.DataBase;
import com.wordpress.brancodes.main.Main;
import com.wordpress.brancodes.mybb.MyBBUser;
import com.wordpress.brancodes.util.Config;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum UserCategoryType {

	// user can be only one of these
	OWNER("Bot Owner", true, user -> user.equals(Config.get("ownerUser"))),
	MOD("Moderator", true, mixin -> mixin.isMember() && DataBase.userIsMod(mixin.getIdLong(), mixin.getGuild().getIdLong()).get()),
	DEFAULT("Public", true, user -> true),
	BOT("Other Bots", true, UserMemberMixin::isBot),
	SELF("Liquid Richard", true, user -> user.equals(Main.getBot().getJDA().getSelfUser())),

	// and any of these
	MYBB("User On MyBB", false, user -> MyBBUser.users.containsKey(user.getIdLong())),
	PING_CENSORED("Censor List", false, user -> Set.of(717416156897738782L).contains(user.getIdLong())),
	CENSORED("Censor List", false, user -> Set.of(915798454507307061L).contains(user.getIdLong())), // 192130507771871232L, 799953862949208114L
	YAWN("Yawn List", false, user -> Set.of().contains(user.getIdLong()));
	// TODO there's a lot of shared behavior here,
	//  but it may not be repetitive enough to convert UserCategoryType to a class yet...

	private final String displayName;
	private final boolean ranked;
	private Function<UserMemberMixin, Boolean> memberOfCategory;

	UserCategoryType(String displayName, boolean ranked, Function<UserMemberMixin, Boolean> memberOfCategory) {
		this.displayName = displayName;
		this.ranked = ranked;
		this.memberOfCategory = memberOfCategory;
	}

	UserCategoryType(String displayName, boolean ranked) {
		this.displayName = displayName;
		this.ranked = ranked;
	}

	public String getDisplayName() {
		return displayName;
	}

	public boolean inRange(UserCategoryType userCategoryType) {
		int compare = this.compareTo(userCategoryType);
		return this.ranked && userCategoryType.ranked ? compare >= 0 : compare == 0;
	}

	public boolean inCategory(UserMemberMixin mixin) {
		return memberOfCategory.apply(mixin);
	}

	@Override
	public String toString() {
		return displayName;
	}

}
