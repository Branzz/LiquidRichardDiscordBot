package com.wordpress.brancodes.messaging.reactions;

public class ReactionResponse {

	private final boolean status;
	private final String logResponse;

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

}
