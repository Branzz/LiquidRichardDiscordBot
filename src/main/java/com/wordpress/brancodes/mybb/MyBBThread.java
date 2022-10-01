package com.wordpress.brancodes.mybb;

import net.dv8tion.jda.api.entities.Message;

import java.util.List;
import java.util.Map;

public class MyBBThread {

	public static Map<String, MyBBThread> threads;

	public static final MyBBThread myBBDiscordThread = MyBBThread.threads.get("Discord Direct Posts");

	public void post(MyBBUser asUser, String postText, List<Message.Attachment> attachments) {

	}

}
