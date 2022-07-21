package com.wordpress.brancodes.messaging.reactions;

import net.dv8tion.jda.api.requests.RestAction;

public class ReactionResponse {

	public static final ReactionResponse SUCCESS = new ReactionResponse(true);
	public static final ReactionResponse FAILURE = new ReactionResponse(false);

	private final boolean status;
	private final String logResponse;
	private RestAction response;

	public ReactionResponse(MessageReaction forReaction, boolean status, String logResponse) {
		this.status = status;
		this.logResponse = logResponse;
	}

	public ReactionResponse(MessageReaction forReaction, String logResponse) {
		this.status = true;
		this.logResponse = logResponse;
	}

	public ReactionResponse(MessageReaction forReaction, boolean status) {
		this.status = status;
		this.logResponse = null;
	}


	public ReactionResponse(boolean status, final String logResponse) { // TODO
		this.status = status;
		this.logResponse = logResponse;
	}

	public ReactionResponse(String logResponse) {
		this.status = true;
		this.logResponse = logResponse;
	}

	public ReactionResponse(boolean status) {
		this.status = status;
		this.logResponse = null;
	}

	public boolean status() {
		return status;
	}

	public boolean hasLogResponse() {
		return logResponse != null;
	}

	public String getLogResponse() {
		return logResponse;
	}

	public ReactionResponse onFailureReply(RestAction<?> response) {
		this.response = response;
		return this;
	}

	public void queueFailureResponse() {
		response.queue();
	}

	public boolean hasFailureResponse() {
		return response != null;
	}

}
