package com.wordpress.brancodes.messaging;

import com.mifmif.common.regex.Generex;
import com.wordpress.brancodes.bot.LiquidRichardBot;
import com.wordpress.brancodes.messaging.reactions.commands.Command;
import com.wordpress.brancodes.messaging.reactions.commands.Commands;
import com.wordpress.brancodes.messaging.reactions.*;
import com.wordpress.brancodes.database.DataBase;
import com.wordpress.brancodes.messaging.reactions.users.UserCategory;
import com.wordpress.brancodes.util.Config;
import com.wordpress.brancodes.util.Util;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;

import javax.annotation.RegEx;
import java.awt.*;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;

public class PreparedMessages {

	private static final Map<String, DynamicMessage> preparedMessages = Map.of(
			"positive",		new UniformMessages(new Object[] { "Got It.", "Got It, Pimp.", "Ok.", "Ok, Pimp.", "Okay.", "Yes.", "Yes, Pimp.", ":thumbsup_tone5:" }), //new ServerDependentSingleMessageEmoji("thumbsup")
			"talk back",	new UniformMessages(new String[] { "No.", "Not Here To Take Commands.", "No I Won't.", "Shut Up.", "Don't Talk To Me That Way.", "Don't Think I Will." }),
			"moderation",	new UniformMessages(new String[] { ".Warn @everyone" }),
			"images",		new UniformMessages(new String[] { "https://i.imgur.com/V2IMs46.jpeg", "https://i1.sndcdn.com/artworks-000593732349-ifevuv-t500x500.jpg", "https://i1.sndcdn.com/artworks-000525995070-tbs2ac-t500x500.jpg", "https://lastfm.freetls.fastly.net/i/u/avatar170s/6ccb4e1d055ce06608f46084c5ae7c83.webp" }),
			"missing",		new GenerexMessage("(Searching My Data Base(\\.\\.)?\\. )?(Sorry[.,]? )?(Pimp[,] )?((I )?(Could(n'| No)|Can('| No)t) (Locate|Find) ((This|That|Your) )?(Provided )?\\{}( In My Data Base)?|((The|This|That|Your) )?(Provided )?\\{} ((Was(n'| No)t|(Could(n'| No)|(Can('| No)))t Be) Found|Does(n'| No)t Exist)( In My Data Base)?)\\."),
			"userNotA",		new GenerexMessage("(Th(at|is) )?User (Was|Is)( Not|n't)( A)? (Mod|Moderator)\\.?"),
			"userAlreadyA",	new GenerexMessage("(Th(at|is) )?User (Was|Is) Already( A)? (Mod|Moderator)\\.?"));

	private static final Map<String, MessageEmbed> embedMessages = Map.of(
			"help",			addFieldsTo(new EmbedBuilder().setAuthor("Lil Richie Help Panel", "https://www.youtube.com/watch?v=95DcuRua25s")
														  .appendDescription("Commandments. Include My Name And Your Request And You Might Get An Answer. All Commands Can Be Requested In -- --- .-. ... . .-.-.- \"/\" For Spaces.")
														  .setImage("https://i.imgur.com/V2IMs46.jpeg")
														  .setFooter("Original creator: " + LiquidRichardBot.getUserName((User) Config.get("creatorUser")))
														  .setColor((Color) Config.get("embedColor")))
									   .build());

	private static EmbedBuilder addFieldsTo(final EmbedBuilder embedBuilder) {
		Stream.of(ReactionChannelType.values()).forEach(channelType -> {
				embedBuilder.addField("\u2550\u2550\u2550\u2550\u2550 " + channelType.toString() + " Commands" + " \u2550\u2550\u2550\u2550\u2550", "", false);
				Stream.of(UserCategory.values()).forEach(userCategory ->
							 addNonEmptyFieldTo(embedBuilder, userCategory.toString(),
												Commands.commands.stream()
																 .filter(cT -> cT.getChannelType() == channelType)
																 .filter(uC -> uC.getUserCategory() == userCategory)
																 .map(Command::getDescription)
																 .filter(Objects::nonNull)
																 .collect(joining(", ")),
												true));
				});
		// embedBuilder
		// embedBuilder.addField(Category::getDisplayName())
		// , collectingAndThen(mapping(Command::getDescription, toList()), Integer::parseInt)
		// final Map<Category, String> collect = Commands.commands.collect(groupingBy(Reaction::getCategory, flatMapping(Command::getDescription, toList())))
		// 		.entrySet().forEach(entry -> embedBuilder.addField(entry.getKey().getDisplayName(), entry.getValue(), true));
																						  // , collectingAndThen(mapping(Command::getDescription, toList()), Integer::parseInt)
		return embedBuilder;
	}

	private static void addNonEmptyFieldTo(final EmbedBuilder embedBuilder, String name, String value, boolean inLine) {
		if (!value.equals(""))
			embedBuilder.addField(name, value, inLine);
	}

	public static void reply(final Message message, final Long guildID, final String request) {
		Commands.reply(message, getMessage(guildID, request));
	}

	public static void replyEmbedMessage(final Message message, final String request) {
		Commands.reply(message, getEmbedMessage(request));
	}

	/**
	 * For guildless, it will use:
	 * default emoji name
	 */
	public static String getMessage(String request) {
		return getMessage(null, request);
	}

	public static String getMessage(final Long guildID, String request) {
		return preparedMessages.get(request).get(guildID);
	}

	public static MessageEmbed getEmbedMessage(final String request) {
		return embedMessages.get(request);
	}

	public static Map<String, DynamicMessage> preparedMessages() {
		return preparedMessages;
	}

	public static abstract class DynamicMessage {
		public abstract String get(final Long guildID);
	}

	private static class GenerexMessage extends DynamicMessage {

		protected Generex generex;

		public GenerexMessage(@RegEx String generex) {
			this.generex = new Generex(generex);
		}

		@Override
		public String get(final Long guildID) {
			return generex.random();
		}

	}

	private static class SingleMessage extends DynamicMessage {
		protected String value;

		public SingleMessage(String value) {
			this.value = value;
		}

		public SingleMessage() {
			this.value = null;
		}

		@Override
		public String get(final Long guildID) {
			return value;
		}

	}

	private static class ServerDependentSingleMessage extends SingleMessage {

		public ServerDependentSingleMessage(String value) {
			super(value);
		}

		public ServerDependentSingleMessage() {
			super();
		}

		@Override
		public String get(final Long guildID) {
			return value;
		}
	}

	private static class ServerDependentSingleMessageEmoji extends ServerDependentSingleMessage {

		private String emojiName;

		public ServerDependentSingleMessageEmoji(final String emojiName) {
			this.emojiName = emojiName;
		}

		@Override
		public String get(final Long guildID) { // null guild gives default emoji
			return Util.asEmoji(DataBase.getEmoji(guildID, emojiName));
		}

	}

	private static class UniformMessages extends DynamicMessage {

		private static final Random random = new Random();
		final SingleMessage[] messages;

		public UniformMessages(final SingleMessage[] messages) {
			this.messages = messages;
		}

		public UniformMessages(final String[] messages) {
			this.messages = Stream.of(messages).map(SingleMessage::new).toArray(SingleMessage[]::new);
		}

		public UniformMessages(final Object[] messages) {
			this.messages = Stream.of(messages).map(object -> {
				if (object instanceof String)
					return new SingleMessage((String) object);
				else if (object instanceof SingleMessage)
					return (SingleMessage) object;
				else
					return null;
			}).toArray(SingleMessage[]::new);
		}

		@Override
		public String get(final Long guildID) {
			return messages[random.nextInt(messages.length)].get(guildID);
		}

	}

	private static class CustomDistributionMessages extends DynamicMessage {

		private static final Random random = new Random();
		final SingleMessage[] messages;
		final double[] probabilities;
		private final double sum;

		public CustomDistributionMessages(final SingleMessage[] messages, final double[] probabilities) {
			this.messages = messages;
			this.probabilities = IntStream.range(0, messages.length)
										  .boxed()
										  .map(i -> (double) (probabilities[i] + (i == 0 ? 0 : probabilities[i - 1])))
										  .mapToDouble(Double::valueOf)
										  .toArray();
			sum = DoubleStream.of(probabilities).sum();
		}

		@Override
		public String get(final Long guildID) {
			double bound = 0;
			do bound = random.nextDouble() * sum; while (bound != 0);
			assert (bound != 0);
			int i = 0;
			while (i < messages.length)
				if (probabilities[i] > bound)
					return messages[i - 1].get(guildID);
			return messages[messages.length - 1].get(guildID);
		}

	}

	private static class OnlineImageMessage extends DynamicMessage {

		protected String lookUpValue;

		public OnlineImageMessage(String lookUpValue) {
			this.lookUpValue = lookUpValue;
		}

		@Override
		public String get(final Long guildID) {
			return null;
		}

	}

}
