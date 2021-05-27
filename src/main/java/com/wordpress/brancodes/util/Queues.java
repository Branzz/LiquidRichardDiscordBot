package com.wordpress.brancodes.util;

/**
 * Some methods will queue an event to the JDA such that the precedence of this event must come before its usage in
 * order for it to be completed, otherwise it would use unloaded or null data.
 */
public @interface Queues {

}
