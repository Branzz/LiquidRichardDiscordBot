package com.wordpress.brancodes.bot;

import com.wordpress.brancodes.database.DataBase;
import com.wordpress.brancodes.main.Main;
import com.wordpress.brancodes.messaging.reactions.ReactionChannelType;
import com.wordpress.brancodes.messaging.reactions.users.UserCategory;
import com.wordpress.brancodes.messaging.reactions.commands.Commands;
import com.wordpress.brancodes.util.Config;
import com.wordpress.brancodes.util.MorseUtil;
import com.wordpress.brancodes.util.CaseUtil;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.emote.update.EmoteUpdateNameEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.internal.entities.AbstractMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.stream.Collectors.joining;

public class Listener extends ListenerAdapter {

	private static final Logger LOGGER = LoggerFactory.getLogger(Listener.class);
	private boolean pause = false;


	@Override
	public void onGuildVoiceLeave(@NotNull final GuildVoiceLeaveEvent event) {
		super.onGuildVoiceLeave(event);
		if (event.getMember().getUser().equals(event.getJDA().getSelfUser())) {
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			Commands.commandsByName.get("Join Voice").execute(new AbstractMessage("!J " + event.getChannelLeft().getId(), "", false) {
				@NotNull @Override public JDA getJDA() { return event.getJDA(); }
				@Override protected void unsupported() { }
				@Nullable @Override public MessageActivity getActivity() { return null; }
				@Override public long getIdLong() { return 0; }
			});
		}
	}

	@Override
	public void onReady(@NotNull final ReadyEvent event) {
		LOGGER.info("{} is ready", event.getJDA().getSelfUser());
		Config.createJDADependantProperties(event.getJDA());
		Main.getBot().cacheDependantInit();
	}

	@Override
	public void onGuildJoin(@NotNull final GuildJoinEvent event) {
		DataBase.addGuild(event.getGuild().getIdLong());
		// DataBase.setMainChannel(event.getGuild().getIdLong(), event.getGuild().getDefaultChannel().getIdLong());
		Main.getBot().addChats(event.getGuild().getIdLong(), event.getGuild().getSystemChannel());
		LOGGER.info("JOINED GUILD: {}", event.getGuild().getName());
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

	@Override
	public void onMessageReceived(@NotNull final MessageReceivedEvent event) {
		if (event.isFromType(ChannelType.PRIVATE)) {
			/*
			 * a private user can't be a mod, as that is guild dependant.
			 */
			///DataBase.respondToBotPrivate()
			if (event.getAuthor().equals(Config.get("ownerUser")) && event.getMessage().getContentRaw().equals("!p"))
				pause ^= true; // overrides every other command
			if (!pause) {
				messageReceived(event.getChannel().getType(), UserCategory.getUserCategory(event), event.getMessage());
				if (!event.getAuthor().equals(event.getJDA().getSelfUser()) && !event.getAuthor().equals(Config.get("ownerUser")))
					LOGGER.info("DM from {}: \"{}\"{} in {} DM's", LiquidRichardBot.getUserName(event.getAuthor()),
								event.getMessage().getContentRaw(), event.getMessage().getAttachments().stream().map(Message.Attachment::getUrl).collect(joining("\n,")),
								LiquidRichardBot.getUserName(event.getAuthor()));
			}
		} else if (event.isFromGuild()) {
			// if (!event.getAuthor().isBot())
			// 	event.getChannel().sendMessage(Util.properCase(event.getMessage().getContentRaw())).queue();
			// DataBase.respondToBots(event.getGuild().getIdLong()).get()
			if (!pause) {
				messageReceived(event.getChannel().getType(), checkMod(event, UserCategory.getUserCategory(event)), event.getMessage()); // TODO refactor extract
			}
		}
	}

	private static UserCategory checkMod(@NotNull final MessageReceivedEvent event, final UserCategory userCategory) {
		return (userCategory == UserCategory.DEFAULT
				&& event.isFromGuild()
				&& DataBase.userIsMod(event.getAuthor().getIdLong(), event.getGuild().getIdLong()).get())
					   ? UserCategory.MOD : userCategory;
	}

	private void messageReceived(final ChannelType channelType, final UserCategory userCategory, final Message message) {
		// LOGGER.info("Message \"{}\" by {} in #{} ChannelType: {} UserCategory: {}",
		// 			message.getContentRaw(),
		// 			LiquidRichardBot.getUserName(message.getAuthor()),
		// 			message.getChannel().getName(),
		// 			channelType,
		// 			userCategory);
		final String contentDisplay = message.getContentDisplay();
		final String messageContent = MorseUtil.isMorse(contentDisplay)
							  ?	CaseUtil.properCaseExcludeNumbers(MorseUtil.fromMorse(contentDisplay))
							  : message.getContentRaw();
		Commands.commandsByCategoryChannel
				.get(ReactionChannelType.of(channelType))
				.get(userCategory)
				.stream()
				.filter(command -> command.execute(message, messageContent))
				.findFirst() // forEach(
				.ifPresent(command -> LOGGER.info("Ran {} command by {} in #{} in {}", //Commands.qCount +
									  command,
									  LiquidRichardBot.getUserName(message.getAuthor()),
									  message.getChannel().getName(),
									  message.isFromGuild() ? message.getGuild().getName() : "DMs"));
	}

}
