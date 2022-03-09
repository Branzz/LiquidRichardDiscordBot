package com.wordpress.brancodes.messaging.reactions;

import net.dv8tion.jda.api.entities.Message;

import java.util.function.BiConsumer;
import java.util.regex.Matcher;

@FunctionalInterface
public interface ExecuteMatcherResponse extends BiConsumer<Message, Matcher> {

}
