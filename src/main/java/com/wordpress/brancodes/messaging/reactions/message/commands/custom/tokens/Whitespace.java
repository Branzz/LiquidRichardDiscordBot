package com.wordpress.brancodes.messaging.reactions.message.commands.custom.tokens;

public class Whitespace implements TokenType {

	private final int width;
	private final boolean hasNewLine;

	/**
	 * @param whitespace String containing characters only true by #isWhiteSpace
	 */
	public Whitespace(String whitespace) {
		int width = 0;
		boolean hasNewLine = false;
		for (char c : whitespace.toCharArray()) {
			switch (c) {
				case '\t':
					width += 4;
					break;
				case '\n':
				case '\r':
					width = 0;
					hasNewLine = true;
					break;
				default:
					width++;
			}

		}
		this.width = width;
		this.hasNewLine = hasNewLine;
	}

	public Whitespace(int width, boolean hasNewLine) {
		this.width = width;
		this.hasNewLine = hasNewLine;
	}

	public int getWidth() {
		return width;
	}

	public boolean isHasNewLine() {
		return hasNewLine;
	}

	/**
	 * Different from Character#isWhitespace,
	 * as parsing some of those should be as text
	 */
	public static boolean isWhitespace(char c) {
		return c == '\t' || c == '\n' || c == '\r' || c == ' ';
	}

}
