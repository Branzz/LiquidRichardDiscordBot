package com.wordpress.brancodes.messaging.reactions.message.commands.custom;

import com.wordpress.brancodes.messaging.reactions.Reaction;
import com.wordpress.brancodes.messaging.reactions.ReactionChannelType;
import com.wordpress.brancodes.messaging.reactions.ReactionResponse;
import com.wordpress.brancodes.messaging.reactions.message.commands.custom.exception.CustomCommandCompileErrorException;
import com.wordpress.brancodes.messaging.reactions.message.commands.custom.tokens.tree.TokenTreeNode;
import com.wordpress.brancodes.messaging.reactions.message.commands.custom.types.*;
import com.wordpress.brancodes.messaging.reactions.message.commands.custom.types.Void;
import com.wordpress.brancodes.messaging.reactions.message.commands.custom.types.Type.Instance;
import com.wordpress.brancodes.messaging.reactions.message.commands.custom.types.obj.ClassType;
import com.wordpress.brancodes.messaging.reactions.message.commands.custom.types.obj.Field;
import com.wordpress.brancodes.messaging.reactions.message.commands.custom.types.obj.Method;
import com.wordpress.brancodes.messaging.reactions.users.UserCategoryType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.Event;
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

import java.lang.reflect.ParameterizedType;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import static com.wordpress.brancodes.messaging.reactions.message.commands.custom.tokens.Operator.*;

//@SuppressWarnings("ALL")

public class CustomCommand extends Reaction<Event> {

	protected CustomCommand() { }

	protected String description;
	protected ClassType<? extends GenericEvent> event;
	protected User creator;
	protected Guild guild;
	protected String text;

	protected TokenTreeNode program;

	@Override
	public ReactionResponse execute(final Event message) {
		return null;
	}

	public void register() {
		GenericListener.put(name, guild.getIdLong(), name, this);
	}

	@Override
	protected EmbedBuilder getMessageEmbed() {
		return super.getMessageEmbed()
					.addField("", text, true)
					.addField("For Event", event.getName(), true)
					.addField("Description", description, true)
					.addField("Created By", creator.getName(), false);
	}

	public static <T extends Nameable> Map<String, T> namedMap(Streamable<T> streamable) {
		return streamable.stream().collect(Collectors.toMap(Nameable::getName, Function.identity()));
	}

	public static <T extends Nameable> Map<String, T> namedMap(T[] ts) {
		return namedMap(Streamable.of(ts));
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

	static Map<String, Type<?>> types;

	static {
		types = namedMap(Streamable.of(
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
				new InterfaceType<IMentionable>("identifiable"),
				new InterfaceType<>("nameable"),
				new InterfaceType<>("gettable")
		));

		putType(new InterfaceType<IMentionable>("identifiable",
												new Field<>("id", "num", true, ISnowflake::getIdLong),
												new Field<>("mention", "str", false, IMentionable::getAsMention)));
		putType(new InterfaceType<>("nameable", new Field<Object, String>("name", "str", true)));
		putType(new InterfaceType<>("gettable", new Method<Long, Object>("get", "any")));

		((ClassType<Object>) getType("object"))
				.registerOperators(EQUALS, TYPED_EQUALS, NOT_EQUALS, TYPED_NOT_EQUALS);
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
				new Field<>("channel", "channel", true, m -> m.getChannel().asTextChannel()),
				new Field<>("author", "user", false, Message::getAuthor),
				new Field<>("server", "server", false, Message::getGuild),
				new Field<>("time", "str", false, Message::getTimeCreated),
				new Field<>("jump", "url", false, Message::getJumpUrl),
				new Field<>("mentioned", "list", false, m -> m.getMentions().getUsers()),
				new Method<>("react", "void", (message, args) -> {
					message.addReaction((Emoji) args[0].get()); return VOID; }, "emoji")
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
		putType(new ClassType<Emoji>("emoji"));

		// putType(new ClassType<List<Instance>>("list"));
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

	public static final Map<String, ClassType<? extends GenericEvent>> events = namedMap(Streamable.of(
		    new InterfaceType<GenericEvent>("generic"),
			  new InterfaceType<Event>("event").extend("generic"),
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

	public static abstract class Builder<T extends CustomCommand, B extends Builder<T, B>> extends Reaction.Builder<T, B> {

		public Builder(String name, String event, User creator, Guild guild, String text) {
			super(name, UserCategoryType.DEFAULT, ReactionChannelType.GUILD);
			object.event = events.get(event);
			object.creator = creator;
			object.guild = guild;
			object.text = text;
		}

		public B addDescription(String description) {
			object.description = description;
			return thisObject;
		}

		@Override
		public T build() throws CustomCommandCompileErrorException {
			object.program = CustomCommandCompiler.compile(object.text, object.event);
			return super.build();
		}

	}

	public static final class CustomCommandBuilder extends Builder<CustomCommand, CustomCommandBuilder> {

		public CustomCommandBuilder(String name, String event, User creator, Guild guild, String codeText) {
			super(name, event, creator, guild, codeText);
		}

		@Override public CustomCommand build() { return object; }
		@Override protected CustomCommand createObject() { return new CustomCommand(); }
		@Override protected CustomCommandBuilder thisObject() { return this; }

	}

}
