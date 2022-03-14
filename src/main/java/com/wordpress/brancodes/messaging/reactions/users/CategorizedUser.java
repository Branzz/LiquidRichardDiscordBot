package com.wordpress.brancodes.messaging.reactions.users;

import com.wordpress.brancodes.database.DataBase;
import com.wordpress.brancodes.util.Config;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class CategorizedUser {

	private final boolean owner, mod, bot, self;
	private final Map<String, Boolean> customCategories;

	private static final Map<String, Function<MessageReceivedEvent, Boolean>> categoryMapper = new HashMap<>();

	static {
		// my default categories
		// categoryMapper.put("censored", event -> event.getAuthor().getIdLong() == );
		// categoryMapper.put("yawn", event -> event.getAuthor().getIdLong() == 717416156897738782L);
	}

	public CategorizedUser(MessageReceivedEvent event) {
		this(event, event.getAuthor().equals(Config.get("ownerUser")),
			 event.isFromGuild() && DataBase.userIsMod(event.getAuthor().getIdLong(), event.getGuild().getIdLong()).get(),
			 event.getAuthor().isBot(),
			 event.getAuthor().equals(event.getJDA().getSelfUser()));
	}

	public CategorizedUser(MessageReceivedEvent event, final boolean owner, final boolean mod, final boolean bot, final boolean self) {
		this.owner = owner;
		this.mod = mod;
		this.bot = bot;
		this.self = self;
		customCategories = new HashMap<>();
		for (Map.Entry<String, Function<MessageReceivedEvent, Boolean>> entry : categoryMapper.entrySet()) {
			customCategories.put(entry.getKey(), entry.getValue().apply(event));
		}
	}

	public static void addCategory(String key, Function<MessageReceivedEvent, Boolean> categoryTester) {
		categoryMapper.put(key, categoryTester);
	}

	public boolean isOwner() {
		return owner;
	}

	public boolean isMod() {
		return mod;
	}

	public boolean isBot() {
		return bot;
	}

	public boolean isSelf() {
		return self;
	}

}
