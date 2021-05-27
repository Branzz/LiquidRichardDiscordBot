package com.wordpress.brancodes.bot;

import com.wordpress.brancodes.database.DataBase;
import com.wordpress.brancodes.main.Main;
import com.wordpress.brancodes.messaging.reactions.commands.Commands;
import com.wordpress.brancodes.messaging.reactions.UserCategory;
import com.wordpress.brancodes.util.Config;
import com.wordpress.brancodes.util.MorseUtil;
import com.wordpress.brancodes.util.Util;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.emote.EmoteAddedEvent;
import net.dv8tion.jda.api.events.emote.update.EmoteUpdateNameEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Listener extends ListenerAdapter {

	private static final Logger LOGGER = LoggerFactory.getLogger(Listener.class);

	@Override
	public void onReady(@NotNull final ReadyEvent event) {
		LOGGER.info("{} is ready", event.getJDA().getSelfUser());
		Config.createJDADependantProperties(event.getJDA());
		Main.getBot().cacheDependantInit();
		// How to leave a server:
		// Main.getBot().getJDA().getGuilds().stream()
		// 	.filter(guild -> guild.getIdLong() == 813184719369535528L).findFirst().get().leave().queue();
	}

	@Override
	public void onGuildJoin(@NotNull final GuildJoinEvent event) {
		DataBase.addGuild(event.getGuild().getIdLong());
		// DataBase.setMainChannel(event.getGuild().getIdLong(), event.getGuild().getDefaultChannel().getIdLong());
		Main.getBot().addChats(event.getGuild().getIdLong(), event.getGuild().getDefaultChannel());
	}

	@Override
	public void onGuildLeave(@NotNull final GuildLeaveEvent event) {
		DataBase.removeGuild(event.getGuild().getIdLong());
		Main.getBot().removeChats(event.getGuild().getIdLong());
	}

	@Override
	public void onEmoteUpdateName(@NotNull final EmoteUpdateNameEvent event) {
		DataBase.setEmoji(event.getGuild().getIdLong(), event.getOldName(), event.getNewName());
		super.onEmoteUpdateName(event);
	}

	// @Override
	// public void onEmoteAdded(@NotNull final EmoteAddedEvent event) {
	// 	super.onEmoteAdded(event);
	// }

	/**
	 * a private user can't be a mod, as that is guild dependant.
	 */
	@Override
	public void onPrivateMessageReceived(@NotNull final PrivateMessageReceivedEvent event) {
		messageReceived(event.getChannel().getType(),
						event.getAuthor().equals(event.getJDA().getSelfUser())  ? UserCategory.SELF
							: event.getAuthor().isBot()							? UserCategory.BOT //DataBase.respondToBotPrivate()
							: event.getAuthor().equals(Config.get("ownerUser"))	? UserCategory.OWNER : UserCategory.DEFAULT,
						event.getMessage());
		// if (!event.getAuthor().equals(event.getJDA().getSelfUser()))
		if (!event.getChannel().getUser().equals(Config.get("ownerUser")))
			LOGGER.info("DM sender {}: \"{}\" in {} DM's", LiquidRichardBot.getUserName(event.getAuthor()), event.getMessage().getContentRaw(), LiquidRichardBot.getUserName(event.getChannel().getUser()));
	}

	@Override
	public void onGuildMessageReceived(@NotNull final GuildMessageReceivedEvent event) {
		messageReceived(event.getChannel().getType(),
						event.getAuthor().equals(event.getJDA().getSelfUser())	? UserCategory.SELF
							: event.getAuthor().isBot()							? UserCategory.BOT //DataBase.respondToBots(event.getGuild().getIdLong()).get()
							: event.getAuthor().equals(Config.get("ownerUser"))	? UserCategory.OWNER
							: DataBase.userIsMod(event.getGuild().getIdLong(), event.getAuthor().getIdLong()).get() ? UserCategory.MOD : UserCategory.DEFAULT,
						event.getMessage());
	}

	private void messageReceived(final ChannelType channelType, final UserCategory userCategory, final Message message) {
		// LOGGER.info("Message \"{}\" by {} in #{} ChannelType: {} UserCategory: {}",
		// 			message.getContentRaw(),
		// 			LiquidRichardBot.getUserName(message.getAuthor()),
		// 			message.getChannel().getName(),
		// 			channelType,
		// 			userCategory);
		String messageContent = MorseUtil.isMorse(message.getContentRaw())
							  ?	Util.properCaseExcludeNumbers(MorseUtil.fromMorse(message.getContentRaw()))
							  : message.getContentRaw();
		Commands.commandsByCategoryChannel.get(channelType)
										  .get(userCategory)
										  .stream()
										  .filter(command -> command.execute(message, messageContent))
										  .findFirst()
										  .ifPresent(command -> LOGGER.info("Ran {} command by {} in {}",
																			command.toString(),
																			LiquidRichardBot.getUserName(message.getAuthor()),
																			message.getChannel().getName()));
	}

}
