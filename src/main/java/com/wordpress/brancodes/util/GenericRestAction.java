package com.wordpress.brancodes.util;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.exceptions.RateLimitedException;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.RestFuture;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class GenericRestAction implements RestAction<Void> {

	private final JDA jda;
	private final Runnable runnable;

	public GenericRestAction(JDA jda, Runnable runnable) {
		this.jda = jda;
		this.runnable = runnable;
	}

	@NotNull
	@Override
	public JDA getJDA() {
		return jda;
	}

	@NotNull
	@Override
	public RestAction<Void> setCheck(@Nullable final BooleanSupplier checks) {
		return this;
	}

	@Override
	public void queue(@Nullable final Consumer<? super Void> success, @Nullable final Consumer<? super Throwable> failure) {
		runnable.run();
	}

	@Override
	public Void complete(final boolean shouldQueue) {
		runnable.run();
		return null;
	}

	@NotNull
	@Override
	public CompletableFuture<Void> submit(final boolean shouldQueue) {
		return new RestFuture<>((Void) null);
	}

}
