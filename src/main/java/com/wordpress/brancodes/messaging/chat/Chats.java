package com.wordpress.brancodes.messaging.chat;

import com.wordpress.brancodes.database.DataBase;
import com.wordpress.brancodes.main.Main;
import com.wordpress.brancodes.util.Images;
import net.dv8tion.jda.api.entities.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	private void initChats() {
		chats = List.of(
				// new PeriodicChat(() -> mainChannel.sendMessage(String.valueOf(System.currentTimeMillis())).queue(), 10_000L)
				new VariatedChat(() -> Images.send(mainChannel, "mgtow", "DAILY REMINDER MGTOW", "MGTOW"),			 5_000L, 100L),
				new VariatedChat(() -> Images.send(mainChannel, "stretch", "DAILY REMINDER STRETCHES", "STRETCHES"), 3_000L, 100L)
		);
	}

	public List<Chat> getChats() {
		return chats;
	}

}
