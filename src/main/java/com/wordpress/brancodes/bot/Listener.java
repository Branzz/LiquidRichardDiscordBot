package com.wordpress.brancodes.bot;

import com.wordpress.brancodes.database.DataBase;
import com.wordpress.brancodes.main.Main;
import com.wordpress.brancodes.messaging.reactions.Reaction;
import com.wordpress.brancodes.messaging.reactions.ReactionChannelType;
import com.wordpress.brancodes.messaging.reactions.ReactionResponse;
import com.wordpress.brancodes.messaging.reactions.Reactions;
import com.wordpress.brancodes.messaging.reactions.users.UserCategory;
import com.wordpress.brancodes.util.CaseUtil;
import com.wordpress.brancodes.util.Config;
import com.wordpress.brancodes.util.MorseUtil;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageActivity;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.emote.update.EmoteUpdateNameEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMuteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.internal.entities.AbstractMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.AbstractMap;
import java.util.Comparator;

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
			Reactions.commandsByName.get("Join Voice").execute(new AbstractMessage("!J " + event.getChannelLeft().getId(), "", false) {
				@NotNull @Override public JDA getJDA() { return event.getJDA(); }
				@Override protected void unsupported() { }
				@Nullable @Override public MessageActivity getActivity() { return null; }
				@Override public long getIdLong() { return 0; }
			});
		}
	}

	@Override
	public void onGuildVoiceMute(@NotNull GuildVoiceMuteEvent event) {
		super.onGuildVoiceMute(event);
		if (event.getMember().getUser().getIdLong() == Main.getBot().getJDA().getSelfUser().getIdLong()) {
			if (event.isMuted())
			event.getMember().mute(false).queue();
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
	public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
		super.onGuildMemberJoin(event);
//		if (event.getGuild().getIdLong() == 953143574453706792L)
//			event.getMember().modifyNickname("Tommy.").queue();
//		if (event.getGuild().getIdLong() == 907042440924528662L) {
//			event.getJDA().getTextChannelById(907111693446950912L).sendMessage("Hello " + event.getMember().getAsMention() + " Due To Recent Developments It Is Of The Utmost Importance That All Of Our Members Are Of Heterosexual Nature. We Can And Will Not Allow Any Member Of The LGBT Community Here In Fact Our Server Shuns Their Sodomitic Lifestyle. To Assure Our Members Conform To Said Ideals We Require Proof. I Would Kindly Ask You To Send A Senior Moderator A Picture Of Your Butthole. We Assure That The Pictures Will Be Handled With Utmost Privacy.");
//		.queue);
// }
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
//		 LOGGER.info("Message \"{}\" by {} in #{} ChannelType: {} UserCategory: {}",
//		 			message.getContentRaw(),
//		 			LiquidRichardBot.getUserName(message.getAuthor()),
//		 			message.getChannel().getName(),
//		 			channelType,
//		 			userCategory);
		final String contentDisplay = message.getContentDisplay();
		final String messageContent = MorseUtil.isMorse(contentDisplay)
							  ?	CaseUtil.properCaseExcludeNumbers(MorseUtil.fromMorse(contentDisplay))
							  : message.getContentRaw();
		Reactions.getReactions(ReactionChannelType.of(channelType), userCategory)
				.stream()
				.filter(reaction -> !reaction.isDeactivated())
				.map(reaction -> new AbstractMap.SimpleEntry<>(reaction, reaction.execute(message, messageContent)))
				.sorted(Comparator.comparing(r -> !r.getValue().status()))
				// .forEach(r -> System.out.println(r.getKey().getName()));
				.findFirst() // method 3
				// .ifPresent(r -> System.out.println(r.getKey().getName()));
				.ifPresent(reactionAndResponse -> logReactionResponse(message, reactionAndResponse.getKey(), reactionAndResponse.getValue()));

	}

	static void logReactionResponse(Message message, Reaction reaction, ReactionResponse response) {
		if (response.status()) {
			String log = String.format("%s %s %s by %s in #%s in %s", //Commands.qCount +
									   response.status() ? "Ran" : "Failed to run",
									   reaction,
									   reaction.getClass().getSimpleName(),
									   LiquidRichardBot.getUserName(message.getAuthor()),
									   message.getChannel().getName(),
									   message.isFromGuild() ? message.getGuild().getName() : "'s DMs");
			if (response.status() || response.hasFailureResponse())
				log = response.hasLogResponse() ? log + ": " + response.getLogResponse() : log;
			LOGGER.info(log);
		}
	}

}
