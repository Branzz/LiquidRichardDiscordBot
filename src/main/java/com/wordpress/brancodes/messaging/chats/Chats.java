package com.wordpress.brancodes.messaging.chats;

import com.wordpress.brancodes.util.NumberToText;
import com.wordpress.brancodes.util.CaseUtil;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.YearMonth;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class Chats {

	private static final Logger LOGGER = LoggerFactory.getLogger(Chats.class);

	private List<Chat> chats;
	private TextChannel mainChannel;

	// public Chats(final Long guildID) {
	// 	this.mainChannel = Main.getBot().getJDA().getTextChannelById(DataBase.getMainChannel(guildID).get());
	// 	initChats();
	// }

	public Chats(TextChannel mainChannel) {
		this.mainChannel = mainChannel;
		initChats();
	}

	public void setMainChannel(TextChannel mainChannel) {
		this.mainChannel = mainChannel;
	}

	private static final Date angelitteBirthday = new Date(122, Calendar.JULY, 16);

	private void initChats() {
		chats = List.of(
				// new PeriodicChat(() -> mainChannel.sendMessage(String.valueOf(System.currentTimeMillis())).queue(), 10_000L)
				// new VariatedChat(() -> Images.send(mainChannel, "mgtow", "DAILY REMINDER MGTOW", "MGTOW"),			 5_000L, 100L),
				// new VariatedChat(() -> Images.send(mainChannel, "stretch", "DAILY REMINDER STRETCHES", "STRETCHES"), 3_000L, 100L),
				// new PeriodicChat(() -> {
				// 	mainChannel.sendMessage("@everyone").queue();
				// 	mainChannel.getIterableHistory().stream().findFirst().get().delete().queue();
				// }, 43_200_000L)
				// new VariatedChat(channel -> {
				// 	getBdayMessage((TextChannel) channel).queue(message -> {
				// 					message.addReaction("U+1F449U+1F3FF").queue();
				// 					message.addReaction("U+1F44CU+1F3FF").queue();
				// 			   }
				// 	);
				// }, "Angelitte Countdown")

		);
	}

	public List<Chat> getChats() {
		return chats;
	}

	public static MessageAction getBdayMessage(TextChannel channel) {
		Date current = new Date();
		int days = angelitteBirthday.getDate() - current.getDate();
		int months = angelitteBirthday.getMonth() - current.getMonth();
		if (days < 0) {
			days += YearMonth.of(current.getYear(), current.getMonth()).lengthOfMonth();
			months--;
		}
		return channel.sendMessage((months != 0 ? CaseUtil.properCase(NumberToText.numberToString(months))
											   + " Month" + (months != 1 ? "s" : "") : "")
								+ (days != 0 ? (months != 0 ? " And " : "")
											   + CaseUtil.properCase(NumberToText.numberToString(days))
											   + " Day" + (days != 1 ? "s" : "") : "") + " U");
	}

}
