package com.wordpress.brancodes.messaging.reactions.commands;

import com.wordpress.brancodes.main.Main;
import com.wordpress.brancodes.messaging.reactions.Reaction;
import com.wordpress.brancodes.messaging.reactions.ReactionChannelType;
import com.wordpress.brancodes.messaging.reactions.users.UserCategory;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Collection;

public class SlashCommand extends Reaction {

	private static final Logger LOGGER = LoggerFactory.getLogger(SlashCommand.class);

	protected CommandData commandData;
	protected net.dv8tion.jda.api.interactions.commands.Command jdaCommand;

	public SlashCommand() {
	}

	/**
	 * to be called upon modification
	 */
	public void upsertCommand() {
		Main.getBot().getJDA().upsertCommand(commandData).queue(s -> jdaCommand = s);
		LOGGER.info("Upserted Command " + jdaCommand.getName());
	}

	public static abstract class Builder<T extends SlashCommand, B extends Builder<T, B>> extends Reaction.Builder<T, B> {
		public Builder(String name, String description, UserCategory userCategory, ReactionChannelType channelCategory) {
			super(name, userCategory, channelCategory);
			object.commandData = new CommandData(name, description);
		}

//		public B execute(Consumer<SlashCommandEvent> event) {
////			object.
//			return thisObject;
//		}

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
