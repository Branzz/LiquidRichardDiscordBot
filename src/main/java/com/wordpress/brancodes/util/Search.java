package com.wordpress.brancodes.util;

import net.dv8tion.jda.internal.requests.Method;

public class Search {

	public static final Method searchMethod = Method.GET;
	public static final String searchRoute = "guilds/{guild_id}/messages/search";

	// public static void request() {
	// 	return new DeferredRestAction<>(this, User.class,
	// 									() -> isIntent(GatewayIntent.GUILD_MEMBERS) || isIntent(GatewayIntent.GUILD_PRESENCES) ? getUserById(id) : null,
	// 									() -> {
	// 										if (id == getSelfUser().getIdLong())
	// 											return new CompletedRestAction<>(this, getSelfUser());
	// 										Route.CompiledRoute route = Route.Users.GET_USER.compile(Long.toUnsignedString(id));
	// 										return new RestActionImpl<>(this, route,
	// 																	(response, request) -> getEntityBuilder().createUser(response.getObject()));
	// 									});
	// }
	//
	// public Search() {
	//
	// }

}
