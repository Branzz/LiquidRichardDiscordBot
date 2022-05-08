package com.wordpress.brancodes.messaging.reactions.commands.custom;

import com.wordpress.brancodes.messaging.reactions.ReactionChannelType;
import com.wordpress.brancodes.messaging.reactions.commands.Command;
import com.wordpress.brancodes.messaging.reactions.commands.custom.CustomCommand.ClassType.ClassTypeInstance;
import com.wordpress.brancodes.messaging.reactions.commands.custom.CustomCommand.ClassType.Field;
import com.wordpress.brancodes.messaging.reactions.commands.custom.CustomCommand.ClassType.Method;
import com.wordpress.brancodes.messaging.reactions.commands.custom.CustomCommand.Type.Instance;
import com.wordpress.brancodes.messaging.reactions.commands.custom.types.InterfaceType;
import com.wordpress.brancodes.messaging.reactions.commands.custom.types.obj.ClassType;
import com.wordpress.brancodes.messaging.reactions.users.UserCategory;
import jdk.jfr.Event;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent;
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;
import net.dv8tion.jda.api.events.channel.GenericChannelEvent;
import net.dv8tion.jda.api.events.guild.*;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.events.user.GenericUserEvent;
import net.dv8tion.jda.api.events.user.UserActivityEndEvent;
import net.dv8tion.jda.api.events.user.UserActivityStartEvent;
import net.dv8tion.jda.api.events.user.UserTypingEvent;
import net.dv8tion.jda.api.events.user.update.GenericUserUpdateEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateDiscriminatorEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateNameEvent;
import org.springframework.data.util.Streamable;

import javax.annotation.RegEx;
import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.wordpress.brancodes.messaging.reactions.commands.custom.types.*;
import com.wordpress.brancodes.messaging.reactions.commands.custom.types.Void;
import com.wordpress.brancodes.messaging.reactions.commands.custom.types.Type.Instance;
import com.wordpress.brancodes.messaging.reactions.commands.custom.types.obj.ClassType;
import com.wordpress.brancodes.messaging.reactions.commands.custom.types.obj.ClassType.ClassTypeInstance;
import com.wordpress.brancodes.messaging.reactions.commands.custom.types.obj.Field;
import com.wordpress.brancodes.messaging.reactions.commands.custom.types.obj.Method;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import static com.wordpress.brancodes.messaging.reactions.commands.custom.tokens.Operator.*;

//@SuppressWarnings("ALL")

public class CustomCommand extends Command {

	public static abstract class Builder<T extends Command, B extends Command.Builder<T, B>> extends Command.Builder<T, B> {
		public Builder(String name, @RegEx String regex, UserCategory userCategory, ReactionChannelType channelCategory) {
			super(name, regex, userCategory, channelCategory);
		}
	}

	public static final class CustomCommandBuilder extends CustomCommand.Builder<CustomCommand, CustomCommandBuilder> {
		public CustomCommandBuilder(Message message, String name, String event, String text) {
			super(name, ".+", UserCategory.DEFAULT, ReactionChannelType.GUILD);
			object.message = message;
			// CustomCommandCompiler
			executeStatus(message1 -> {
				return true;
			});
		}
		@Override public CustomCommand build() { return object; }
		@Override protected CustomCommand createObject() { return new CustomCommand(); }
		@Override protected CustomCommandBuilder thisObject() { return this; }
	}

	Message message;

	protected CustomCommand() { }

	public CustomCommand(String name, UserCategory category, BiFunction<Message, Matcher, Boolean> executer) {
//		super(regex, name, category, channelCategory);
		executer = (message, matcher) -> {
			final Type<Message>.Instance request = getType("message").create(message);
			Map<String, Instance> variables = new HashMap<>();
			variables.put("request", request);
			ClassTypeInstance user = ((ClassType) getType("user")).create(message.getAuthor());
			user.getField("name");
			user.callMethod("ban");
			return true;
		};
	}

	public static <T extends Nameable> Map<String, T> namedMap(Streamable<T> streamable) {
		return streamable.stream().collect(Collectors.toMap(Nameable::getName, Function.identity()));
	}

	public static <T extends Nameable> Map<String, T> namedMap(T[] ts) {
		return namedMap(Streamable.of(ts));
	}

	Member getAuthor(User user) {
		return message.getGuild().getMember(user);
	}

	static Void VOID = Void.getInstance();
//	static Null NULL = Null.getInstance();

	public static Instance getInstance(Object obj) {
		for (Type type : types.values())
			if (type.getClass().getGenericSuperclass().equals(obj.getClass()))
				return type.create(obj);
		return new VoidType().create();
	}

	final static Type[] NO_ARGS = new Type[] { };

	static Map<String, Type<?>> types = namedMap(Streamable.of(
			new VoidType(),
			new ClassType<Object>("object"),
			new PrimitiveType<Long>("num", String::valueOf, n -> n != 0),
			new PrimitiveType<String>("str", Long::valueOf, s -> !s.equals("false") && !s.equals("")),
			new PrimitiveType<Boolean>("bool", String::valueOf),
			new ClassType<JDA>("bot"),
			new ClassType<Guild>("server"),
			new ClassType<Channel>("channel"),
			new ClassType<Message>("message"),
			new ClassType<Member>("user"),
			new ClassType<Role>("role"),
			new ClassType<Emoji>("emoji"),
			new InterfaceType<IMentionable>("identifiable",
					new Field<>("id", "num", true, ISnowflake::getIdLong),
					new Field<>("mention", "str", false, IMentionable::getAsMention)),
			new InterfaceType<>("nameable", new Field<Object, String>("name", "str", true)),
			new InterfaceType<>("gettable", new Method<Long, Object>("get", "any"))
	));

	static {
		((ClassType<Object>) getType("object"))
				.registerOperators(EQUALS, TYPED_EQUALS, NOT_EQUALS, NOT_TYPED_EQUALS);
		((PrimitiveType<Long>) getType("num"))
						.registerOperators(LESS, GREATER, LESS_EQUAL, GREATER_EQUAL)
				.extend("object")
				.tryAddRealMethods(Long.class);
		((PrimitiveType<String>) getType("str"))
				.extend("object")
				.addField(new Field<>("length", "num", false, str -> (long) str.length()))
				.tryAddRealMethods(String.class);
		((PrimitiveType<Boolean>) getType("bool"))
						.registerOperators(NOT, AND, OR)
				.extend("object");
//		putType(new ClassType<JDA>("bot")); TODO
		putType(new ClassType<Guild>("server",
				new Field<>("owner", "user", false, Guild::getOwner))
				.implement((InterfaceType<Object>) getType("nameable"), (m, args) -> m.getName())
				.implement((InterfaceType<ISnowflake>) getType("identifiable"))

		);
		putType(new ClassType<TextChannel>("channel", // TODO tryCast() ?
				new Method<>("send", "void", (channel, args) -> {
					channel.sendMessage(((String) args[0].get())).queue(); return VOID; }, "str"))
				.extend("object")
				.implement((InterfaceType<Object>) getType("nameable"), (m, args) -> m.getName())
				.implement((InterfaceType<ISnowflake>) getType("identifiable"))
//				.override(new Method<TextChannel, Long>("get", "num", NO_ARGS,
//						(id, args) -> Main.getBot().getJDA().getTextChannelById(id)))
		);
		putType(new ClassType<Message>("message",
				new Field<>("channel", "channel", true, Message::getTextChannel),
				new Field<>("author", "user", false, Message::getAuthor),
				new Field<>("server", "server", false, Message::getGuild),
				new Field<>("time", "str", false, Message::getTimeCreated),
				new Field<>("jump", "url", false, Message::getJumpUrl),
				new Field<>("mentioned", "list", false, Message::getMentionedMembers),
				new Method<>("react", "void", (message, args) -> {
					message.addReaction((Emote) args[0].get()); return VOID; }, "emoji")
				).extend("object")
				.implement((InterfaceType<ISnowflake>) getType("identifiable"))
		);
		putType(new ClassType<Member>("user",
				new Field<>("pfp", "str", true, Member::getEffectiveAvatarUrl),
				new Field<>("banner", "str", true, (Member m) -> m.getUser().retrieveProfile().complete().getBannerUrl()),
				new Method<>("giveRole", "void", (user, args) -> { user.getRoles().add((Role) (args[0].get())); return VOID; }, "role"),
				new Method<>("nick", "void", (user, args) -> { user.modifyNickname(((String) (args[0].get()))).queue(); return VOID; }, "str"),
				new Method<>("kick", "void", (user, args) -> { user.kick().queue(); return VOID; }),
				new Method<>("ban", "void", (user, args) -> { user.ban(0).queue(); return VOID; })
				).extend("object")
//				.implement((InterfaceType<Member>) getType("gettable"), (m, args) -> Main.getBot().getJDA().retrieveUserById(m.getIdLong()).complete())
				.implement((InterfaceType<Object>) getType("nameable"), (m, args) -> m.getEffectiveName())
				.implement((InterfaceType<ISnowflake>) getType("identifiable"))

		);
		putType(new ClassType<Emote>("emoji"));
		putType(new ClassType<List<Instance>>("list"));
	}

	static Map<String, Method<?, ?>> publicMethods = namedMap(Streamable.of(
			new Method<Matcher, Object>("group", "any", (Matcher m, Type<?>.Instance[] args) ->
								m.group(((Long) args[0].get()).intValue()), "num")
	));

	public static Type getType(String name) {
		return types.get(name);
	}

	public static <T> Type<?>[] getType(Class<T> tClass) {
		return types.values()
				.stream()
				.filter(type -> tClass.equals(
						((ParameterizedType) type.getClass().getGenericSuperclass()).getActualTypeArguments()[0]))
				.toArray(Type<?>[]::new);
	}

	private static <T> void putType(Type<T> type) {
		Type<T> existing = (Type<T>) types.get(type);
		if (existing != null)
			existing.set(type);
		else
			types.put(type.getName(), type);
	}

	static final Map<String, ClassType<?>> events = namedMap(Streamable.of(
		    new InterfaceType<Event>("event"),
		     new InterfaceType<GenericEvent>("generic").extend("event"),
			 new InterfaceType<GenericUserEvent>("genericUser").extend("event"),
			   new InterfaceType<GenericUserUpdateEvent<?>>("genericUserUpdate").extend("genericUser").extend("generic"),
			     new ClassType<UserUpdateNameEvent>("userUpdateName").extend("genericUserUpdate"),
			     new ClassType<UserUpdateDiscriminatorEvent>("userUpdateDiscriminator").extend("genericUserUpdate"),
			   new ClassType<UserTypingEvent>("userTyping").extend("genericUser"),
			   new ClassType<UserActivityStartEvent>("userActivityStart").extend("genericUser"),
			   new ClassType<UserActivityEndEvent>("userActivityEnd").extend("genericUser"),
			 new InterfaceType<GenericMessageEvent>("genericMessage").extend("event"),
			   new ClassType<MessageReceivedEvent>("messageReceived").extend("genericMessage"),
			   new ClassType<MessageUpdateEvent>("messageUpdate").extend("genericMessage"),
			   new ClassType<MessageDeleteEvent>("messageDelete").extend("genericMessage"),
			 new InterfaceType<GenericMessageReactionEvent>("genericMessageReaction").extend("genericMessage"),
			   new ClassType<MessageReactionAddEvent>("messageReactionAdd").extend("genericMessageReaction"),
			   new ClassType<MessageReactionRemoveEvent>("messageReactionRemove").extend("genericMessageReaction"),
			 new InterfaceType<GenericChannelEvent>("genericChannel").extend("event"),
			   new ClassType<ChannelCreateEvent>("channelCreate").extend("genericChannel"),
			   new ClassType<ChannelDeleteEvent>("channelDelete").extend("genericChannel"),
			 new InterfaceType<GenericGuildEvent>("genericGuild"),
			   new ClassType<GuildJoinEvent>("guildJoin").extend("genericGuild"),
			   new ClassType<GuildLeaveEvent>("guildLeave").extend("genericGuild"),
			   new ClassType<GuildBanEvent>("guildBan").extend("genericGuild"),
			   new ClassType<GuildUnbanEvent>("guildUnban").extend("genericGuild")
	));
//	static class JointType<T> extends Type<T> {
//
//		public JointType(String name) {
//			super(name);
//		}
//
//		@Override
//		public void set(Type type) {
//
//		}
//
//		@Override
//		Type<T>.Instance create(T real) {
//			return new JointInstance(real);
//		}
//
////		public JointType(String name, Cast<?, T>... casts) {
////			super(name, casts);
////		}
//
//		class JointInstance extends Instance {
//
//			public JointInstance(T real) {
//				super(real);
//			}
//
//		}
//	}

	// TODO separate out into separate files, correct visibilities?
	// T is the jda type it is based on

	interface Cacheable<T> {
		boolean isCached();

		T getCache();
	}

}
