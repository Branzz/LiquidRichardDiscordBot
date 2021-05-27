package com.wordpress.brancodes.test;

import com.wordpress.brancodes.messaging.reactions.Reaction;
import com.wordpress.brancodes.messaging.reactions.ReactionChannelType;
import com.wordpress.brancodes.messaging.reactions.UserCategory;
public class ReactionTest extends Reaction {

	public ReactionTest(final String regex, final String nam, final UserCategory category, final ReactionChannelType channelCategory) {
		super(regex, nam, category, channelCategory);
		name = "";
	}

}
