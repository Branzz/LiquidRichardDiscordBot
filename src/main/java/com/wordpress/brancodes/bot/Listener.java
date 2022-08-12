package com.wordpress.brancodes.bot;

import com.wordpress.brancodes.database.DataBase;
import com.wordpress.brancodes.main.Main;
import com.wordpress.brancodes.messaging.reactions.MessageReaction;
import com.wordpress.brancodes.messaging.reactions.ReactionChannelType;
import com.wordpress.brancodes.messaging.reactions.ReactionResponse;
import com.wordpress.brancodes.messaging.reactions.Reactions;
import com.wordpress.brancodes.messaging.reactions.commands.Command;
import com.wordpress.brancodes.messaging.reactions.commands.SlashCommand;
import com.wordpress.brancodes.messaging.reactions.users.UserCategory;
import com.wordpress.brancodes.util.CaseUtil;
import com.wordpress.brancodes.util.Config;
import com.wordpress.brancodes.util.MorseUtil;
import com.wordpress.brancodes.util.Pair;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMuteEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.internal.entities.AbstractMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.AbstractMap;

import static java.util.stream.Collectors.joining;

public class Listener extends ListenerAdapter {

	private static final Logger LOGGER = LoggerFactory.getLogger(Listener.class);
	private boolean pause = false;

	public void pause() {
		pause = true;
	}

	@Override
	public void onGuildVoiceLeave(@NotNull final GuildVoiceLeaveEvent event) {
		super.onGuildVoiceLeave(event);
		if (event.getMember().getUser().equals(event.getJDA().getSelfUser())) {
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			((MessageReaction) Reactions.commandsByName.get("Join Voice"))
					.execute(new AbstractMessage("!J " + event.getChannelLeft().getId(), "", false) {
				@NotNull @Override public JDA getJDA() { return event.getJDA(); }
				@Override protected void unsupported() { }
				@Nullable @Override public MessageActivity getActivity() { return null; }
				@Override public long getIdLong() { return 0L; }
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

//	@Override
//	public void onEmoteUpdateName(@NotNull final EmoteUpdateNameEvent event) {
//		DataBase.setEmoji(event.getGuild().getIdLong(), event.getOldName(), event.getNewName());
//		super.onEmoteUpdateName(event);
//	}

	@Override
	public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
		super.onGuildMemberJoin(event);
		final User user = event.getMember().getUser();
		if (!user.isBot() && event.getJDA().getGuildById(907042440924528662L).getMember(user) != null
		 && event.getJDA().getGuildById(910004207120183326L).getMember(user) != null) {
//			event.getMember().ban(0).queue();
			LOGGER.info("found crossover user:" + event.getMember().getUser().getName() + " " + event.getMember().getIdLong());
		}
	}

	@Override
	public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
		if (event.getChannel().getType() == ChannelType.PRIVATE) {
			if (!pause) {
				slashCommand(event, ChannelType.PRIVATE, UserCategory.getUserCategory(event));
				if (!event.getUser().equals(event.getJDA().getSelfUser()) && !event.getUser().equals(Config.get("ownerUser")))
					LOGGER.info("DM from {}: \"{}\" in {} DM's", LiquidRichardBot.getUserName(event.getUser()),
							event.getCommandString(),
							LiquidRichardBot.getUserName(event.getUser()));
			}
		} else if (event.isFromGuild()) {
			if (!pause) {
				slashCommand(event, event.getChannel().getType(), checkMod(event, UserCategory.getUserCategory(event))); // TODO refactor extract
			}
		}
	}

	private void slashCommand(SlashCommandInteractionEvent event, ChannelType channelType, UserCategory userCategory) {
		String name = event.getName();

		SlashCommand slashCommand = (SlashCommand) Reactions.commandsByName.get(name);
		boolean channelInRange = slashCommand.getChannelType().inRange(channelType);
		boolean userInRange = slashCommand.getUserCategory().inRange(userCategory);

		if (channelInRange && userInRange) {
			slashCommand.execute(event);
		} else {
			StringBuilder failureMessageBuilder = new StringBuilder();
			if (!channelInRange)
				failureMessageBuilder.append("You Can't Use ").append(name).append(" Here");
			if (!channelInRange && !userInRange)
				failureMessageBuilder.append(" And ");
			if (!userInRange)
				failureMessageBuilder.append("You Don't Have Permission To Use ").append(name);
			String failureMessage = failureMessageBuilder.toString();
			event.getInteraction()
					.reply(failureMessage)
					.setEphemeral(true)
					.queue(s -> LOGGER.info("slash command {} user failure {}", name, failureMessage));
		}
	}

	@Override
	public void onMessageReceived(@NotNull final MessageReceivedEvent event) {
		if (event.isFromType(ChannelType.PRIVATE)) { // a private user can't be a mod, as that is guild dependant.
			///DataBase.respondToBotPrivate()
			if (event.getAuthor().equals(Config.get("ownerUser")) && event.getMessage().getContentRaw().equals("!p"))
				pause ^= true; // overrides every other command
			if (!pause) {
				messageReceived(event.getChannel().getType(), UserCategory.getUserCategory(event), event.getMessage()); //TODO replace channel Type with ReactionChannelType argument
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
		return checkMod(userCategory, event.isFromGuild(), event.getAuthor(), event.isFromGuild() ? event.getGuild() : null);
	}

	private static UserCategory checkMod(@NotNull final SlashCommandInteractionEvent event, final UserCategory userCategory) {
		return checkMod(userCategory, event.isFromGuild(), event.getUser(), event.isFromGuild() ? event.getGuild() : null);
	}

	private static UserCategory checkMod(final UserCategory userCategory, boolean isFromGuild, User user, Guild guild) {
		return (userCategory == UserCategory.DEFAULT
				&& isFromGuild
				&& DataBase.userIsMod(user.getIdLong(), guild.getIdLong()).get())
					   ? UserCategory.MOD : userCategory;
	}

	private static boolean chainable(MessageReaction reaction) {
		return reaction instanceof Command && ((Command) reaction).chainable();
	}

	static final int FAILURE = 0, SUCCESS = 1, CHAINABLE = 2;
	private void messageReceived(final ChannelType channelType, final UserCategory userCategory, final Message message) {
//		 LOGGER.info("Message \"{}\" by {} in #{} ChannelType: {} UserCategory: {}",
//		 			message.getContentRaw(),
//		 			LiquidRichardBot.getUserName(message.getAuthor()),
//		 			message.getChannel().getName(),
//		 			channelType,
//		 			userCategory);
		String contentDisplay = message.getContentDisplay();
		String messageContent = MorseUtil.isMorse(contentDisplay)
										? CaseUtil.properCaseExcludeNumbers(MorseUtil.fromMorse(contentDisplay))
										: message.getContentRaw();
		// .forEach(r -> System.out.println(r.getKey().getName()));
		// List<AbstractMap.SimpleEntry<Reaction, ReactionResponse>> reactions =
		// Map<Integer, List<Pair<Reaction, ReactionResponse>>> reactionsTyped =

		// execute until find first true status and non-chainable// TODO doesn't log all.

		// if (userCategory != UserCategory.OWNER)
		// 	return;

		// Reactions.getReactions(ReactionChannelType.of(channelType), userCategory)
		// 		 .stream()
			//	 // .sorted(comparing(Listener::chainable))

				 // .map(reaction -> Pair.of(reaction, reaction.matches(messageContent)))
				 // .sorted(comparing(r -> !r.getValue()))
				 // .takeWhile(r -> chainable(r.getKey()))
				 // .forEach(r -> logReactionResponse(message, r));
		//
		// System.out.println(channelType + " " + userCategory + " " + contentDisplay);
		Reactions.getMessageReactions(ReactionChannelType.of(channelType), userCategory)
				.stream()
				// .peek(System.out::println)
				.filter(reaction -> !reaction.isDeactivated())
				.map(reaction -> new AbstractMap.SimpleEntry<>(reaction, reaction.execute(message, messageContent)))
				.filter(response -> response.getValue().status()).findFirst() // method 1
				// .min(Comparator.comparing(r -> r.getValue().status())) // method 3
				.ifPresent(reactionAndResponse -> logReactionResponse(message, reactionAndResponse.getKey(), reactionAndResponse.getValue()));

				 // .stream()
				 // .sorted(comparing(Listener::chainable))
				 // // .peek(x -> System.out.println("SrtByChn: " + x.getName()))
				 // .map(reaction -> Pair.of(reaction, reaction.execute(message, messageContent)))
				 // // .peek(x -> System.out.println("ToPr: " + x.getKey().getName() + ", " + x.getValue().status()))
				 // .sorted(comparing(r -> !r.getValue().status()))
				 // // .peek(x -> System.out.println("SrtPr: " + x.getKey().getName() + ", " + x.getValue().status()))
				 // .takeWhile(r -> chainable(r.getKey()))
				 // // .peek(x -> System.out.println("TkChn: " + x.getKey().getName() + ", " + x.getValue().status()))
				 // .forEach(r -> logReactionResponse(message, r));

		// put all the trues first TODO how does interact with chainable ?
				 // .sorted(comparing(r -> !(r.getValue().status() && !chainable(r.getKey()))))
				 // .findFirst()
				 // .ifPresent(r -> logReactionResponse(message, r));
				 //
		// List<Reaction> activated = new ArrayList<>();
		// for (Reaction reaction : Reactions.getReactions(ReactionChannelType.of(channelType), userCategory)) {
		// 	if (!reaction.isDeactivated()) {
		// 		activated.add(reaction);
		// 	}
		// }
		// List<Reaction> chainable = new ArrayList<>();
		// List<Reaction> nonChainable = new ArrayList<>();
		// for (Reaction reaction : activated) {
		// 	if ((reaction instanceof Command) && ((Command) reaction).chainable()) {
		// 		chainable.add(reaction);
		// 	} else {
		// 		nonChainable.add(reaction);
		// 	}
		// }
		// if (chainable.size() >= 2) {
		// 	chainable.stream()
		// 			 .map(reaction -> Pair.of(reaction, reaction.execute(message, messageContent))) // *execute all the failures*
		// 			 .filter(r -> !r.getValue().status()) // execute all of them
		// 			 .forEach(r -> logReactionResponse(message, r));
		// } else {
		// 	if (chainable.size() == 1) {
		// 		nonChainable.add(chainable.get(0));
		// 	}
		// 	nonChainable.stream()
		// 				.map(reaction -> Pair.of(reaction, reaction.execute(message, messageContent)))
		// 				.min(comparing(r -> !r.getValue().status())) // get the first success or the first failure IFF they all failed (some aren't executed)
		// 				.ifPresent(r -> logReactionResponse(message, r));



			// if (chainable.size() == 1) {
			// 	ReactionResponse reactionResponse = chainable.get(0).execute(message);
			// 	if (reactionResponse.status()) {
			// 		logReactionResponse(message,chainable.get(0),reactionResponse);
			// 	} else {
			// 		nonChainable.stream()
			// 					.map(reaction -> Pair.of(reaction, reaction.execute(message)))
			// 					.min(Comparator.comparing(r -> !r.getValue().status())) // get the first success or the first failure IFF they all failed (some aren't executed)
			// 					.ifPresent(r -> logReactionResponse(message, r.getKey(), r.getValue()));
			// 	}
			// }
		// }

		// .map(reaction -> new Pair<>(reaction, reaction.execute(message, messageContent)))
						 // .takeWhile(r -> r.getValue().status())
		// 				 .collect(groupingBy(r -> r.getValue().status() ?
		// 												  ((r.getKey() instanceof Command) && ((Command) r.getKey()).chainable() ?
		// 														   CHAINABLE : SUCCESS)
		// 												  : FAILURE));
		// List<Pair<Reaction, ReactionResponse>> chainable = reactionsTyped.get(CHAINABLE);
		//  else { // size == 0
		// 	List<Pair<Reaction, ReactionResponse>> successes = reactionsTyped.get(SUCCESS);
		// 	if (successes.size() > 0)
		// 		;
		// 	else
		// 		reactionsTyped.get(FAILURE)
		// 				;
		// }
		// .sorted(Comparator.comparing((AbstractMap.SimpleEntry<Reaction, ReactionResponse> r) ->
							// 								  (r.getKey() instanceof Command) && ((Command) r.getKey()).chainable())
						 // 				   .thenComparing(r -> !r.getValue().status()))
						 // .collect(Collectors.toList());

		// .ifPresent(reactionAndResponse -> logReactionResponse(message, reactionAndResponse.getKey(), reactionAndResponse.getValue()));

		// .forEach(r -> System.out.println(r.getKey().getName()));
		// List<AbstractMap.SimpleEntry<Reaction, ReactionResponse>> reactions =
		// Map<Integer, List<Pair<Reaction, ReactionResponse>>> reactionsTyped =

		/* demo question */

// List<Reaction> reactions = getSomeReactions();
// List<Reaction> chainable = new ArrayList<>();
// List<Reaction> nonChainable = new ArrayList<>();
// for (Reaction reaction : reactions) { // essentially a grouping by
// 	if (reaction.chainable()) {
// 		chainable.add(reaction);
// 	} else {
// 		nonChainable.add(reaction);
// 	}
// }
// if (chainable.size() >= 2) {
// 	chainable.stream()
// 			 .map(reaction -> reaction.execute(message))
// 			 // .filter(r -> !r.status()) // try to execute all of this type
// 			 .forEach(r -> log(r));
// } else {
// 	if (chainable.size() == 1) {
// 		ReactionResponse reactionResponse = chainable.get(0).execute(message);
// 		if (reactionResponse.status()) {
// 			log(reactionResponse);
// 		} else {
// 			nonChainable.stream()
// 						.map(reaction -> reaction.execute(message))
// 			// get the first success or the first failure IFF they all failed (some aren't executed)
// 						.min(Comparator.comparing(r -> !r.status()))
// 						.ifPresent(r -> log(r));
// 		}
// 	}
// }

	}

	static void logReactionResponse(Message message, Pair<MessageReaction, ReactionResponse> responsePair) {
		logReactionResponse(message, responsePair.getKey(), responsePair.getValue());
//		Map<Boolean, List<Pair<Command, ReactionResponse>>> byChainableSuccess =
//		Map<Boolean, List<Reaction>> byChainable =
//			Reactions.getReactions(ReactionChannelType.of(channelType), userCategory)
//				.stream()
//				.filter(reaction -> !reaction.isDeactivated())
//				.collect(Collectors.groupingBy(reaction -> reaction instanceof Command && reaction.getName().equals("and chainable"),
//						Collectors.));
//		Map<Boolean, List<Pair<Reaction, ReactionResponse>>> chainableResponses = byChainable.get(true)
//				.stream()
////				.map(reaction -> ((Command) reaction))
//				.map(command -> Pair.of(command, command.execute(message, messageContent)))
//				.collect(Collectors.groupingBy(e -> e.getRight().status()));
//		List<Pair<Reaction, ReactionResponse>> successes = chainableResponses.get(true);
//		if (successes.size() >= 2) {
//			// TODO execute On success
//		} else {
//			Stream.concat(byChainable.get(false)
//									 .stream()
//									 .map(reaction -> Pair.of(reaction, reaction.execute(message, messageContent))),
//						  chainableResponses.get(false)
//								  	 .stream())
//					.min(Comparator.comparing(r -> r.getRight().status())) // method 3
//					.ifPresent(reactionAndResponse -> logReactionResponse(message, reactionAndResponse.getLeft(), reactionAndResponse.getRight()));
//		}
		// method 2

//		List<SimpleEntry<Reaction, ReactionResponse>> reactions = Reactions.commandsByCategoryChannel
//				.get(ReactionChannelType.of(channelType))
//				.get(userCategory)
//				.stream()
////				.peek(System.out::println)
//				.filter(reaction -> !reaction.isDeactivated())
//				.map(reaction -> new SimpleEntry<>(reaction, reaction.execute(message, messageContent)))
//				.filter(response -> response.getValue().status())
//				.collect(Collectors.toList());
//		Optional<SimpleEntry<Reaction, ReactionResponse>> firstSuccess = reactions.stream().filter(reaction -> reaction.getValue().status()).findFirst();
//		if (firstSuccess.isPresent()) {
//			logReactionResponse(message, firstSuccess.get().getKey(), firstSuccess.get().getValue());
//		} else {
//			reactions.stream().findFirst().ifPresent(reaction -> logReactionResponse(message, reaction.getKey(), reaction.getValue()));
//		}
	}

	static void logReactionResponse(Message message, MessageReaction reaction, ReactionResponse response) {
		if (response.status()) {
			String log = String.format("%s %s %s by %s in %s in #%s%s", //Commands.qCount +
									   response.status() ? "Ran" : "Failed to run",
									   reaction,
									   reaction.getClass().getSimpleName(),
									   LiquidRichardBot.getUserName(message.getAuthor()),
									   message.isFromGuild() ? message.getGuild().getName() : "'s DMs",
									   message.getChannel().getName(),
									   (response.status() || response.hasFailureResponse()) && response.hasLogResponse()
											   ? (": " + response.getLogResponse()) : "");
			LOGGER.info(log);
		}
	}

}
