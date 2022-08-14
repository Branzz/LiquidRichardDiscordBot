package com.wordpress.brancodes.test;

import com.wordpress.brancodes.messaging.reactions.message.commands.custom.CustomCommand;
import com.wordpress.brancodes.messaging.reactions.message.commands.custom.CustomCommandCompiler;

import java.util.stream.Collectors;
public class CustomCommandTest {

	public static void main(String[] args) {
		CustomCommand.getType("bool");
		System.out.println(CustomCommandCompiler.tokenize(" if event.user == \"abc\"\n"
									 + "    event.user.kick\n"
									 + "else if event.user equals defg {event.user.ban}"
									 + "  reply h ")
							 .stream().map(Object::toString).collect(Collectors.joining(" ")));
	}

}
