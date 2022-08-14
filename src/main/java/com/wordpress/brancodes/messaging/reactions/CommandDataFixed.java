package com.wordpress.brancodes.messaging.reactions;

import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import net.dv8tion.jda.internal.utils.Checks;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.stream.Stream;

public class CommandDataFixed extends CommandDataImpl {

	public CommandDataFixed(@NotNull final String name, @NotNull final String description) {
		super(name, description);
	}

	public CommandDataFixed(@NotNull final Command.Type type, @NotNull final String name) {
		super(type, name);
	}

	@NotNull
	@Override
	public CommandDataImpl addOptions(@NotNull final OptionData... options) {
		Checks.noneNull(options, "Option");
		if (options.length == 0)
			return this;
		checkType(Command.Type.SLASH, "add options");
		Checks.check(options.length + this.options.length() <= 25, "Cannot have more than 25 options for a command!");
		for (OptionData option : options)
		{
			Checks.check(option.getType() != OptionType.SUB_COMMAND, "Cannot add a subcommand with addOptions(...). Use addSubcommands(...) instead!");
			Checks.check(option.getType() != OptionType.SUB_COMMAND_GROUP, "Cannot add a subcommand group with addOptions(...). Use addSubcommandGroups(...) instead!");
		}

		Checks.checkUnique(
				Stream.concat(getOptions().stream(), Arrays.stream(options)).map(OptionData::getName),
				"Cannot have multiple options with the same name. Name: \"%s\" appeared %d times!",
				(count, value) -> new Object[]{ value, count }
		);

		for (OptionData option : options)
			this.options.add(option);
		return this;
	}

}
