package com.wordpress.brancodes.messaging.reactions;

import com.wordpress.brancodes.util.Config;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;
public enum UserCategory {

	OWNER("Bot Owner", true),
	MOD("Moderator", true),
	DEFAULT("Public", true),
	BOT("Other Bots", true),
	SELF("Liquid Richard", true),

	CENSORED("Censor List", false),
	YAWN("Yawn List", false);

	private final String displayName;
	private final boolean ranked;

	UserCategory(final String displayName, final boolean ranked) {
		this.displayName = displayName;
		this.ranked = ranked;
	}

	public String getDisplayName() {
		return displayName;
	}

	public boolean inRange(final UserCategory userCategory) {
		int compare = this.compareTo(userCategory);
		return this.ranked && userCategory.ranked ? compare >= 0 : compare == 0;
	}

	// static final Set<Long> CENSORED_USERS = Set.of(192130507771871232L, 799953862949208114L);
	static final Map<Long, UserCategory> TRACKED_USERS = Map.of(
			192130507771871232L, CENSORED,
			799953862949208114L, YAWN, 749625271937663027L, YAWN, 708038453509619734L, YAWN);

	public static UserCategory getUserCategory(@NotNull final PrivateMessageReceivedEvent privateMessageReceivedEvent) {
		return getUserCategory(privateMessageReceivedEvent.getJDA(), privateMessageReceivedEvent.getAuthor());
	}

	public static UserCategory getUserCategory(@NotNull final GuildMessageReceivedEvent guildMessageReceivedEvent) {
		return getUserCategory(guildMessageReceivedEvent.getJDA(), guildMessageReceivedEvent.getAuthor());
	}

	private static UserCategory getUserCategory(JDA jda, User author) {
		return author.equals(jda.getSelfUser())					   ? UserCategory.SELF
					: author.isBot()							   ? UserCategory.BOT
					: author.equals(Config.get("ownerUser"))	   ? UserCategory.OWNER
					: TRACKED_USERS.getOrDefault(author.getIdLong(), UserCategory.DEFAULT);
	}

	@Override
	public String toString() {
		return displayName;
	}

}
