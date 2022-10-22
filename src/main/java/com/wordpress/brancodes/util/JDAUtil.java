package com.wordpress.brancodes.util;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.internal.utils.PermissionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JDAUtil {

	private static final Logger LOGGER = LoggerFactory.getLogger(JDAUtil.class);

	public static final Matcher IDMatcher = Pattern.compile("[\\d]{18,21}").matcher("");

	public static void reply(Message message, String reply) {
		if (message.getChannelType() == ChannelType.TEXT)
			reply(message.getChannel().asTextChannel(), reply);
		else
			message.getChannel().sendMessage(JavaUtil.truncate(reply)).queue(); // in DMs
	}

	public static void reply(Message message, final MessageEmbed reply) {
		if (message.getChannelType() == ChannelType.TEXT)
			reply(message.getChannel().asTextChannel(), reply);
		else
			message.getChannel().sendMessageEmbeds(reply).queue();
	}

	public static void reply(TextChannel channel, final String reply) {
		if (canUseChannel(channel))
			channel.sendMessage(JavaUtil.truncate(reply)).queue();
		else
			logMissingChannelPermissions(channel);
	}

	public static void reply(TextChannel channel, final MessageEmbed reply) {
		if (canUseChannel(channel))
			channel.sendMessageEmbeds(reply).queue();
		else
			logMissingChannelPermissions(channel);
	}

	private static Member getMember(Message message) {
		return message.getGuild().getMember(message.getAuthor());
	}

	private static boolean canUseChannel(final TextChannel channel) {
		return hasPermission(channel, Permission.MESSAGE_SEND);
	}

	public static boolean hasPermission(final TextChannel channel, Permission permission) {
		return PermissionUtil.checkPermission(channel, Objects.requireNonNull(channel.getGuild().getMember(channel.getJDA().getSelfUser())),
											  permission);
	}

	public static void logMissingChannelPermissions(final TextChannel channel) {
		LOGGER.info("Tried to do command in {} in {}, but is missing permissions", channel, channel.getGuild());
	}

	public static Channel reduceThreadChannels(MessageChannelUnion channel) {
		return channel instanceof ThreadChannel ? channel.asThreadChannel().getParentChannel() : channel;
	}

	public static IPermissionContainer toPermissionContainer(MessageChannelUnion channel) {
		return (IPermissionContainer) (channel instanceof ThreadChannel ? channel.asThreadChannel().getParentChannel() : channel);
	}

}
