package com.wordpress.brancodes.messaging.reactions;

import net.dv8tion.jda.api.entities.Member;

public class MemberJoinReaction extends Reaction<Member> {

	@Override
	public ReactionResponse execute(Member member) {
		return ReactionResponse.SUCCESS;
	}

}
