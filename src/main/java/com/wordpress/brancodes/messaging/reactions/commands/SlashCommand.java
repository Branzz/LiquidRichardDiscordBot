package com.wordpress.brancodes.messaging.reactions.commands;

import com.wordpress.brancodes.main.Main;
import com.wordpress.brancodes.messaging.reactions.Reaction;
import com.wordpress.brancodes.messaging.reactions.ReactionChannelType;
import com.wordpress.brancodes.messaging.reactions.users.UserCategory;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class SlashCommand extends Reaction {

	private static final Logger LOGGER = LoggerFactory.getLogger(SlashCommand.class);

	protected SlashCommandData commandData;
	protected net.dv8tion.jda.api.interactions.commands.Command jdaCommand;
	protected Consumer<SlashCommandInteractionEvent> executer;

	public SlashCommand() {
	}

	/**
	 * to be called upon modification
	 */
	public void upsertCommand(boolean global) {
		if (global) {
			Main.getBot().getJDA().upsertCommand(commandData).queue(s -> jdaCommand = s); // TODO global vs guild slash command
			LOGGER.info("Upserted Command {} to global", name);
		} else {
			List<Guild> guilds = Main.getBot().getJDA().getGuilds();
			guilds.forEach(g -> g.upsertCommand(commandData).queue(s -> jdaCommand = s));
			LOGGER.info("Upserted Command {} to {}", name, guilds.stream().map(Guild::getName).collect(Collectors.joining(", ")));
		}
	}

	public void execute(SlashCommandInteractionEvent options) {
		executer.accept(options);
	}

	public static abstract class Builder<T extends SlashCommand, B extends Builder<T, B>> extends Reaction.Builder<T, B> {
		public Builder(String name, String description, UserCategory userCategory, ReactionChannelType channelCategory) {
			super(name, userCategory, channelCategory);
			object.commandData = new CommandDataImpl(name, description);
		}

		public B execute(Consumer<SlashCommandInteractionEvent> executer) { // no need for success logic because it always got ran
			object.executer = executer;
			return thisObject;
		}

		public B addOptions(@Nonnull OptionData... options) {
			object.commandData = object.commandData.addOptions(options); return thisObject; }
		public B addOptions(@Nonnull Collection<? extends OptionData> options) {
			object.commandData = object.commandData.addOptions(options); return thisObject; }
		public B addOption(@Nonnull OptionType type, @Nonnull String name, @Nonnull String description, boolean required) {
			object.commandData = object.commandData.addOption(type, name, description, required); return thisObject; }
		public B addOption(@Nonnull OptionType type, @Nonnull String name, @Nonnull String description) {
			object.commandData = object.commandData.addOption(type, name, description); return thisObject; }
		public B addSubcommands(@Nonnull SubcommandData... subcommands) {
			object.commandData = object.commandData.addSubcommands(subcommands); return thisObject; }
		public B addSubcommands(@Nonnull Collection<? extends SubcommandData> subcommands) {
			object.commandData = object.commandData.addSubcommands(subcommands); return thisObject; }
		public B addSubcommandGroups(@Nonnull SubcommandGroupData... groups) {
			object.commandData = object.commandData.addSubcommandGroups(groups); return thisObject; }
		public B addSubcommandGroups(@Nonnull Collection<? extends SubcommandGroupData> groups) {
			object.commandData = object.commandData.addSubcommandGroups(groups); return thisObject; }

	}

	public static final class SlashCommandBuilder extends Builder<SlashCommand, SlashCommandBuilder> {
		public SlashCommandBuilder(String name, String description, UserCategory userCategory, ReactionChannelType channelCategory) {
			super(name, description, userCategory, channelCategory);
		}
		@Override public SlashCommand build() { return object; }
		@Override protected SlashCommand createObject() { return new SlashCommand(); }
		@Override protected SlashCommandBuilder thisObject() { return this; }
	}

}
