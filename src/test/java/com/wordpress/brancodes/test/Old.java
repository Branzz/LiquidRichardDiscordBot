package com.wordpress.brancodes.test;

public class Old {


	/* Commands */

	// public static final Map<ChannelType, Map<UserCategory, Set<Command>>> commandsByCategoryChannel
	// 		= commands.collect(
	// 				groupingBy(Reaction::getChannelType,
	// 				groupingBy(Reaction::getUserCategory,
	// 				toSet())));

	// Map<UserCategory, Set<Command>> ref = commandsByCategoryChannel.get(ReactionChannelType.TEXT);
		// ref.get(UserCategory.OWNER).addAll(ref.get(UserCategory.MOD));
		// ref.get(UserCategory.MOD).addAll(ref.get(UserCategory.DEFAULT));
		// commandsByCategoryChannel.get(ReactionChannelType.TEXT_AND_PRIVATE).putAll(commandsByCategoryChannel.get(ReactionChannelType.TEXT));
		// commandsByCategoryChannel.get(ReactionChannelType.TEXT_AND_PRIVATE).putAll(commandsByCategoryChannel.get(ReactionChannelType.PRIVATE));

	// foodRepository.findAll().stream()
	//  .map(Food::getFoodToFoodGroup)
	//  .flatMap(Collection::stream)
	//  .collect(Collectors.groupingBy(
	// 									   f -> f.getFoodGroup().getName(),
	// HashMap::new,
	// 			   Collectors.mapping(FoodToFoodGroup::getFood, Collectors.toList())));
	// public static final Map<ChannelType, Set<Command>> commandsTest
	// 		= commands.map(Reaction::getChannelType)
	// 				  .flatMap(Collection::stream)
	// 				  .collect(groupingBy())
	// public static final Stream<Command> ownerCommands = commands;
	// public static final Stream<Command> modCommands = commands.filter(command -> command.getChannelCategory().compareTo(UserCategory.MOD) <= 0);
	// public static final Stream<Command> defaultCommands = commands.filter(command -> command.getChannelCategory().compareTo(UserCategory.DEFAULT) <= 0);
	// public static final Stream<Command> botCommands = commands.filter(command -> command.getChannelCategory().compareTo(UserCategory.BOT) <= 0);
	// public static final Stream<Command> jdaSelfCommands = commands.filter(command -> command.getChannelCategory().compareTo(UserCategory.SELF) <= 0);


	/* Listener */

	// executeCommandIn(event.getAuthor().equals(event.getJDA().getSelfUser()) ? UserCategory.SELF
	// 						: DataBase.respondToBotPrivate() ? UserCategory.BOT
	// 						: event.getAuthor().equals(Config.get("ownerUser")) ? UserCategory.OWNER
	// 						: UserCategory.MOD, // Treat all users like Mods in DMs until they have server specific request
	// 				 ChannelType.PRIVATE, event.getMessage());

	// if (!event.getAuthor().equals(event.getJDA().getSelfUser()))
	// if (DataBase.respondToBotPrivate())
	// 		MessageReaction.privateBotMessage(event, event.getMessage().getContentRaw());
	// 	else if (event.getAuthor().equals(Config.get("ownerUser")))
	// 		MessageReaction.privateOwnerMessage(event, event.getMessage().getContentRaw());
	// 	else
	// 		MessageReaction.privateMessage(event, event.getMessage().getContentRaw());

	// Commands.reply(event.getChannel(), event.getMessage().getContentRaw());
	// executeCommandIn(event.getAuthor().equals(event.getJDA().getSelfUser()) ? Category.SELF
	// 						: DataBase.respondToBots(event.getGuild().getIdLong()).get() ? Category.BOT
	// 						: event.getAuthor().equals(Config.get("ownerUser")) ? Category.OWNER
	// 						: DataBase.userIsMod(event.getGuild().getIdLong(), event.getAuthor().getIdLong()).get() ? Category.MOD
	// 						: Category.DEFAULT,
	// 		ChannelType.TEXT, event.getMessage());
	// if (DataBase.respondToBots(event.getGuild().getIdLong()).get())
	// 	MessageReaction.botMessage(event, event.getMessage().getContentRaw());
	// else if (event.getAuthor().equals(Config.get("ownerUser")))
	// 	MessageReaction.ownerMessage(event, event.getMessage().getContentRaw());
	// else if (DataBase.userIsMod(event.getGuild().getIdLong(), event.getAuthor().getIdLong()).get())
	// 	MessageReaction.modMessage(event, event.getMessage().getContentRaw());
	// else
	// 	MessageReaction.message(event, event.getMessage().getContentRaw());

	// private void executeCommandIn(UserCategory category, ChannelType channelType, Message message) {
	// 	Commands.commands.filter(command -> command.getUserCategory().compareTo(category) >= 0 && command.getChannelType().equals(channelType))
	// 					 .findFirst()
	// 					 .ifPresent(command -> command.executeIfMatches(message));
	// }

}
