package com.wordpress.brancodes.main;

import net.dv8tion.jda.api.entities.TextChannel;

import java.io.OutputStream;
import java.io.PrintStream;

public class ChannelOutputStream extends OutputStream {


	private TextChannel outputChannel;
	private final char[] buffer;
	private int index;

	public ChannelOutputStream(TextChannel outputChannel) {
		this.outputChannel = outputChannel;
		buffer = new char[2000];
		// buffer[0] = '`';
		index = 0;
	}

	@Override
	public void write(final int b) {
		final char c = (char) b;
		if (c == '\n')
			return;
		if (index >= 2000) {
			// buffer[1999] = '`';
			flush();
		}
		else if (c == '\r') {
			// buffer[--index] = '`';
			flush();
		} else
			buffer[index++] = c;
	}

	@Override
	public void flush() {
		outputChannel.sendMessage(new String(buffer, 0, index)).queue();
		index = 0;
	}

}
