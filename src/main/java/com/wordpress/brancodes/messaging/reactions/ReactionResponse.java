package com.wordpress.brancodes.messaging.reactions;

import net.dv8tion.jda.api.requests.RestAction;

public class ReactionResponse {

	public static ReactionResponse SUCCESS = new ReactionResponse(true);
	public static ReactionResponse FAILURE = new ReactionResponse(false);

	private final boolean status;
	private final String logResponse;
	private RestAction response;

	public ReactionResponse(final boolean status, final String logResponse) {
		this.status = status;
		this.logResponse = logResponse;
	}

	public ReactionResponse(final String logResponse) {
		this.status = true;
		this.logResponse = logResponse;
	}

	public ReactionResponse(final boolean status) {
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

	public ReactionResponse onFailureReply(RestAction response) {
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
