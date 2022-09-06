package com.wordpress.brancodes.messaging.reactions.message.commands;

import com.wordpress.brancodes.main.Main;
import com.wordpress.brancodes.messaging.reactions.Reaction;
import com.wordpress.brancodes.messaging.reactions.ReactionChannelType;
import com.wordpress.brancodes.messaging.reactions.ReactionResponse;
import com.wordpress.brancodes.messaging.reactions.users.UserCategory;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.*;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.security.InvalidParameterException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.*;
import java.util.stream.Collectors;

import static com.wordpress.brancodes.messaging.reactions.ReactionResponse.FAILURE;
import static com.wordpress.brancodes.messaging.reactions.ReactionResponse.SUCCESS;

public class SlashCommand extends Reaction<SlashCommandInteractionEvent> {

	private static final Logger LOGGER = LoggerFactory.getLogger(SlashCommand.class);

	protected SlashCommandData commandData;
	protected net.dv8tion.jda.api.interactions.commands.Command jdaCommand;
	protected Consumer<SlashCommandInteractionEvent> executer; // TODO default to replying with that it's not implemented?
	private long commandId;

	public SlashCommand() {
	}

	/**
	 * to be called upon modification
	 */
	public ReactionResponse upsert(boolean global) {
		if (global) {
			Main.getBot().getJDA().upsertCommand(commandData).queue(s -> jdaCommand = s); // TODO global vs guild slash command
			return new ReactionResponse(String.format("Upserted Command %s to global", name));
		} else {
			List<Guild> guilds = Main.getBot().getJDA().getGuilds();
			guilds.forEach(g -> g.upsertCommand(commandData).queue(s -> jdaCommand = s));
			return new ReactionResponse(String.format("Upserted Command %s to %s", name, guilds.stream().map(Guild::getName).collect(Collectors.joining(", "))));
		}
	}

	public static ReactionResponse delete(String name) {
		StringBuilder response = new StringBuilder();
		AtomicBoolean success = new AtomicBoolean(false);
		List<Guild> guilds = Main.getBot().getJDA().getGuilds();
		guilds.forEach(g -> g.retrieveCommands().queue(
				commands -> commands.stream()
									.filter(c -> c.getName().equals(name))
									.findFirst()
									.ifPresentOrElse(
											c -> {
												c.delete().queue(v -> response.append(name).append(" deleted in ").append(g.getName()).append(", "));
												success.set(true);
											},
											() -> response.append(name).append(" wasn't found in ").append(g.getName()).append(", ")
									)));
		return new ReactionResponse(success.get(), response.toString());
	}

	@Override
	public ReactionResponse execute(SlashCommandInteractionEvent options) {
		if (canExecute(options)) {
			executer.accept(options);
			return SUCCESS;
		} else {
			return FAILURE;
		}
	}

	public static abstract class Builder<T extends SlashCommand, B extends Builder<T, B>> extends Reaction.Builder<T, B> {

		Map<String, String> communicationMap;
		boolean subcommandBranching = false;
		private Map<String, Consumer<SlashCommandInteractionEvent>> subcommandExecuters;
		private Consumer<SlashCommandInteractionEvent> defaultExecuter;

		private Consumer<SlashCommandInteractionEvent> mainCommandExecuter;
		private Consumer<SlashCommandInteractionEvent> subcommandExecuter;

		public Builder(String name, String description, UserCategory userCategory, ReactionChannelType channelCategory) {
			super(name, userCategory, channelCategory);
			object.commandData = new CommandDataImpl(name, description);
			communicationMap = new HashMap<>() {
				@Override
				public String get(final Object key) {
					String val = super.get(key);
					if (val == null)
						throw new InvalidParameterException(String.format("the %s key wasn't found in the field look up table", key));
					return val;
				}
			};
		}

		public Builder(String name, String description, UserCategory userCategory, ReactionChannelType channelCategory,
					   BiFunction<SlashCommandData, Map<String, String>, Consumer<SlashCommandInteractionEvent>> slashCreator) {
			this(name, description, userCategory, channelCategory);
			defaultExecuter = slashCreator.apply(object.commandData, communicationMap);
		}

		public B addData(BiConsumer<SlashCommandData, Map<String, String>> commandDataCreator) {
			commandDataCreator.accept(object.commandData, communicationMap);
			return thisObject;
		}

		public B execute(BiConsumer<SlashCommandInteractionEvent, Map<String, String>> executerWithMap) { // TODO confirm: no need for success logic because it always got ran
			defaultExecuter = event -> executerWithMap.accept(event, communicationMap);
			return thisObject;
		}

		public B mainCommandExecuter(BiConsumer<SlashCommandInteractionEvent, Map<String, String>> mainCommandExecuter) {
			this.mainCommandExecuter = event -> mainCommandExecuter.accept(event, communicationMap);
			return thisObject;
		}

		public B subcommandExecuter(Consumer<SlashCommandInteractionEvent> subcommandExecuter) {
			this.subcommandExecuter = subcommandExecuter;
			return thisObject;
		}

		public B subcommandExecuter(BiConsumer<SlashCommandInteractionEvent, Map<String, String>> subcommandExecuter) {
			this.subcommandExecuter = event -> subcommandExecuter.accept(event, communicationMap);
			return thisObject;
		}

		public B execute(Consumer<SlashCommandInteractionEvent> executer) {
			defaultExecuter = executer;
			return thisObject;
		}

		public B addOptions(@Nonnull OptionData... options) {
			object.commandData = object.commandData.addOptions(options); return thisObject;
		}
		public B addOptions(Function<Map<String, String>, OptionData[]> optionsWithMap) {
			object.commandData = object.commandData.addOptions(optionsWithMap.apply(communicationMap)); return thisObject;
		}
		public B addOptions(@Nonnull Collection<? extends OptionData> options) {
			object.commandData = object.commandData.addOptions(options); return thisObject;
		}
		public B addOption(@Nonnull OptionType type, @Nonnull String name, @Nonnull String description, boolean required) {
			object.commandData = object.commandData.addOption(type, name, description, required); return thisObject;
		}
		public B addOptionDataName(@Nonnull OptionType type, @Nonnull String name, @Nonnull String description, boolean required) {
			object.commandData = object.commandData.addOption(type, communicationMap.get(name), description, required); return thisObject;
		}
		public B addOption(@Nonnull OptionType type, @Nonnull String name, @Nonnull String description) {
			object.commandData = object.commandData.addOption(type, name, description); return thisObject;
		}
		public B addSubcommands(@Nonnull SubcommandData... subcommands) {
			object.commandData = object.commandData.addSubcommands(subcommands); return thisObject;
		}
		public B addSubcommands(@Nonnull Collection<? extends SubcommandData> subcommands) {
			object.commandData = object.commandData.addSubcommands(subcommands); return thisObject;
		}
		public B addSubcommandGroups(@Nonnull SubcommandGroupData... groups) {
			object.commandData = object.commandData.addSubcommandGroups(groups); return thisObject;
		}
		public B addSubcommandGroups(@Nonnull Collection<? extends SubcommandGroupData> groups) {
			object.commandData = object.commandData.addSubcommandGroups(groups); return thisObject;
		}

		public B createSubcommandBranch(SubcommandData subcommand, Consumer<SlashCommandInteractionEvent> subcommandExecuter) {
			if (!subcommandBranching) {
				subcommandExecuters = new HashMap<>();
				subcommandBranching = true;
			}
			object.commandData.addSubcommands(subcommand);
			subcommandExecuters.put(subcommand.getName(), subcommandExecuter);
			return thisObject;
		}

		public B createSubcommandBranch(Function<Map<String, String>, SubcommandData> subcommandWithData,
												BiConsumer<SlashCommandInteractionEvent, Map<String, String>> subcommandExecuter) {
			if (!subcommandBranching) {
				subcommandExecuters = new HashMap<>();
				subcommandBranching = true;
			}
			SubcommandData subcommand = subcommandWithData.apply(communicationMap);
			object.commandData.addSubcommands(subcommand);
			subcommandExecuters.put(subcommand.getName(), event -> subcommandExecuter.accept(event, communicationMap));
			return thisObject;
		}

		private void addSubcommandBranchesToExecuter() {
			if (subcommandBranching) {
				object.executer = event -> {
					String subcommandName = event.getSubcommandName();
					if (subcommandName == null) { // isn't a subcommand / doesn't exist
						if (mainCommandExecuter != null)
							mainCommandExecuter.accept(event);
						else
							defaultExecuter.accept(event);
					} else {
						Consumer<SlashCommandInteractionEvent> subcommandExecuter = subcommandExecuters.get(subcommandName);
						if (subcommandExecuter == null) { // wasn't added by #subcommand
							if (this.subcommandExecuter != null)
								this.subcommandExecuter.accept(event);
							else
								defaultExecuter.accept(event);
						} else {
							subcommandExecuter.accept(event);
						}
					}
				};
			} else {
				object.executer = defaultExecuter;
			}
		}

		public B addCommunicationMap(Map<String, String> table) {
			communicationMap = table;
			return thisObject;
		}

		public B addField(String key, String value) {
			// if (communicationMap == null)
			// 	communicationMap = new HashMap<>() {
			// 		@Override
			// 		public String get(final Object key) {
			// 			String val = super.get(key);
			// 			if (val == null)
			// 				throw new InvalidParameterException(String.format("the %s key wasn't found in the field look up table", key));
			// 			return val;
			// 		}
			// 	};
			communicationMap.put(key, value);
			return thisObject;
		}

		public Map<String, String> getData() {
			return communicationMap;
		}

		@Override
		public T build() {
			addSubcommandBranchesToExecuter();
			return super.build();
		}

	}

	public static final class SlashCommandBuilder extends Builder<SlashCommand, SlashCommandBuilder> {
		public SlashCommandBuilder(String name, String description, UserCategory userCategory, ReactionChannelType channelCategory) {
			super(name, description, userCategory, channelCategory);
		}
		public SlashCommandBuilder(String name, String description, UserCategory userCategory, ReactionChannelType channelCategory,
								   BiFunction<SlashCommandData, Map<String, String>, Consumer<SlashCommandInteractionEvent>> slashCreator) {
			super(name, description, userCategory, channelCategory, slashCreator);
		}
		@Override public SlashCommand build() { return object; }
		@Override protected SlashCommand createObject() { return new SlashCommand(); }
		@Override protected SlashCommandBuilder thisObject() { return this; }
	}

}
