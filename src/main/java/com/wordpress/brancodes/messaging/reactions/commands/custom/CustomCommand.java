package com.wordpress.brancodes.messaging.reactions.commands.custom;

import com.wordpress.brancodes.main.Main;
import com.wordpress.brancodes.messaging.reactions.Reaction;
import com.wordpress.brancodes.messaging.reactions.ReactionChannelType;
import com.wordpress.brancodes.messaging.reactions.commands.Command;
import com.wordpress.brancodes.messaging.reactions.commands.custom.CustomCommand.ClassType.ClassTypeInstance;
import com.wordpress.brancodes.messaging.reactions.commands.custom.CustomCommand.ClassType.Field;
import com.wordpress.brancodes.messaging.reactions.commands.custom.CustomCommand.ClassType.Method;
import com.wordpress.brancodes.messaging.reactions.commands.custom.CustomCommand.Type.Instance;
import com.wordpress.brancodes.messaging.reactions.commands.custom.CustomCommandCompiler.TokenType;
import com.wordpress.brancodes.messaging.reactions.users.UserCategory;
import net.dv8tion.jda.api.entities.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.util.Streamable;

import javax.annotation.RegEx;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

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

		public CustomCommand build() {
			return object;
		}

		@Override
		protected CustomCommand createObject() {
			return new CustomCommand();
		}

		@Override
		protected CustomCommandBuilder thisObject() {
			return this;
		}

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

	static <T extends Nameable> Map<String, T> namedMap(Streamable<T> streamable) {
		return streamable.stream().collect(Collectors.toMap(Nameable::getName, Function.identity()));
	}

	static <T extends Nameable> Map<String, T> namedMap(T[] ts) {
		return namedMap(Streamable.of(ts));
	}

	Member getAuthor(User user) {
		return message.getGuild().getMember(user);
	}

	static Void VOID = Void.getInstance();
//	static Null NULL = Null.getInstance();

	static Instance getInstance(Object obj) {
		for (Type type : types.values())
			if (type.getClass().getGenericSuperclass().equals(obj.getClass()))
				return type.create(obj);
		return new VoidType().create();
	}

	final static Type[] NO_ARGS = new Type[] { };

	static Map<String, Type<?>> types = namedMap(Streamable.of(
			new VoidType(),
			new PrimitiveType<Long>("num", String::valueOf, n -> n != 0),
			new PrimitiveType<String>("str", Long::valueOf, Boolean::valueOf),
			new PrimitiveType<Boolean>("bool", String::valueOf),
			new ClassType<Guild>("server"),
			new ClassType<Channel>("channel"),
			new ClassType<Message>("message"),
			new ClassType<Member>("user"),
			new ClassType<Role>("role"),
			new ClassType<Emoji>("emoji"),
			new InterfaceType<IMentionable>("identifiable",
					new Field<>("id", "num", true, ISnowflake::getIdLong),
					new Field<>("mention", "str", false, IMentionable::getAsMention)
			),
			new InterfaceType<>("nameable", new Field<Object, String>("name", "str", true)),
			new InterfaceType<>("gettable", new Method<Long, Object>("get", "any"))
	));

	static {
		putType(new ClassType<Guild>("server",
				new Field<>("owner", "user", false, Guild::getOwner))
				.implement((InterfaceType<Object>) getType("nameable"), (m, args) -> m.getName())
				.implement((InterfaceType<ISnowflake>) getType("identifiable"))

		);
		putType(new ClassType<TextChannel>("channel",
				new Method<TextChannel, Void>("send", "void", new Type[] { getType("str") },
						(channel, args) -> { channel.sendMessage(((String) args[0].get())).queue(); return VOID; })) // TODO tryCast() ?
				.implement((InterfaceType<Object>) getType("nameable"), (m, args) -> m.getName())
				.implement((InterfaceType<ISnowflake>) getType("identifiable"))
				.override(new Method<Long, TextChannel>("get", "num", NO_ARGS,
						(id, args) -> Main.getBot().getJDA().getTextChannelById(id)))
		);
		putType(new ClassType<Message>("message",
				new Field<>("channel", "channel", true, Message::getTextChannel),
				new Field<>("author", "user", false, Message::getAuthor),
				new Field<>("server", "server", false, Message::getGuild),
				new Field<>("time", "str", false, Message::getTimeCreated),
				new Field<>("jump", "url", false, Message::getJumpUrl),
				new Field<Message, List<Member>>("mentioned", "list", false, Message::getMentionedMembers),
				new Method<Message, Void>("react", "void", new Type[] { getType("emoji") },
				(message, args) -> { message.addReaction((Emote) args[0].get()); return VOID; }))
						.implement((InterfaceType<ISnowflake>) getType("identifiable"))
		);
		putType(new ClassType<Member>("user",
				new Field<>("pfp", "str", true, Member::getEffectiveAvatarUrl),
				new Field<>("banner", "str", true, (Member m) -> m.getUser().retrieveProfile().complete().getBannerUrl()),
				new Method<Member, Void>("giveRole", "void", new Type[] { getType("role") },
						(user, args) -> { user.getRoles().add((Role) (args[0].get())); return VOID; }),
				new Method<Member, Void>("nick", "void", new Type[] { getType("str") },
						(user, args) -> { user.modifyNickname(((String) (args[0].get()))).queue(); return VOID; }),
				new Method<Member, Void>("kick", "void", NO_ARGS,
						(user, args) -> { user.kick().queue(); return VOID; }),
				new Method<Member, Void>("ban", "void", NO_ARGS,
						(user, args) -> { user.ban(0).queue(); return VOID; })
				)
//				.implement((InterfaceType<Member>) getType("gettable"), (m, args) -> Main.getBot().getJDA().retrieveUserById(m.getIdLong()).complete())
				.implement((InterfaceType<Object>) getType("nameable"), (m, args) -> m.getEffectiveName())
				.implement((InterfaceType<ISnowflake>) getType("identifiable"))
		);
		putType(new ClassType<Emote>("emoji"));
		putType(new ClassType<List<Instance>>("list"));
	}

	static Map<String, Method> publicMethods = namedMap(Streamable.of(
			new Method<Matcher, Object>("group", "any", new Type[] { getType("num") },
					(Matcher m, Type<?>.Instance[] args) -> m.group(((Long) args[0].get()).intValue()))
	));

	static Type getType(String name) {
		return types.get(name);
	}

	private static <T> void putType(Type<T> type) {
		Type<T> existing = (Type<T>) types.get(type);
		if (existing != null)
			existing.set(type);
		else
			types.put(type.getName(), type);
	}

	interface Cast<T, I> {
		T tryCast(I inst);
	}

	public static abstract class Type<T> implements Nameable, TokenType {
		String name;

		public Type(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public abstract void set(Type<T> type);

		public class Instance {
			protected T real;

			public Instance(T real) {
				this.real = real;
			}

			public T get() {
				return real;
			}

			Type<?>.Instance tryCast(Type<?> other) {
				return new VoidType().create();
			}

			boolean exists() {
				return true;
			}

		}

		abstract Instance create(T real);

	}

	static class PrimitiveType<T> extends Type<T> {

		public PrimitiveType(String name) {
			super(name);
		}

		public PrimitiveType(String name, Cast<?, T>... casts) {
			super(name);
			this.casts = List.of(casts);
		}

		Iterable<Cast<?, T>> casts;

		class PrimitiveInstance extends Type<T>.Instance {

			public PrimitiveInstance(T real) {
				super(real);
			}

			Type<?>.Instance tryCast(Type other) {
				if (casts != null)
					for (Cast cast : casts)
						if (cast.getClass().getGenericSuperclass().equals(other.getClass())) {
							Instance instance = getInstance(cast.tryCast(real));
							if (instance.exists())
								return instance;
						}
				return super.tryCast(other);
			}

		}

		@Override
		PrimitiveInstance create(T real) {
			return new PrimitiveInstance(real);
		}

		@Override
		public void set(Type<T> type) {
			if (type instanceof PrimitiveType) {
				this.casts = ((PrimitiveType) type).casts;
			}
		}

	}

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
	static class ClassType<T> extends Type<T> {

		static class Field<T, R> extends Method<T, R> { // no-arg method
			public boolean castable; // allow this field to be converted to its class type

//			Field(String name, String type, Function<T, ? extends Instance<R>> call) {
//				super(name, type, null, (T t, Instance[] args) -> call.apply(t));
//			}

			Field(String name, String type) {
				super(name, type);
			}

			public Field(String name, String type, boolean castable) {
				super(name, type);
			}

			Field(String name, String type, boolean castable, Function<T, R> call) {
				this(name, type);
				this.returnType = getType(type);
				this.parameterTypes = null;
				this.call = (T t, Type<?>.Instance[] args) -> returnType.create(call.apply(t));
			}

		}

		/**
		 * @param <T> real thing this is a member of
//		 * @param <Y> Type that this is a member of
//		 * @param <S> Instance of the Type that this is a member of
		 * @param <R> return real type
//		 * @param <A> argument real types
		 *
		 * Y and S pertain to the method being called with an actual instance of its parent
		 */
		static class Method<T, R> implements Nameable { // methods must be implemented
			String name;
			Type<R> returnType;
			Type[] parameterTypes;
			BiFunction<T, Type<?>.Instance[], ? extends Type<R>.Instance> call;

			public Method(String name, String returnTypeStr) { // declared and not initializide
				this.name = name;
				this.returnType = getType(returnTypeStr);
			}

//			public Method(String name, String returnType, Type[] parameterTypes,
//						  BiFunction<T, Instance[], ? extends Type<R>.Instance<R>> call) {
//				this.name = name;
//				this.returnType = getType(returnType);
//				this.parameterTypes = parameterTypes;
//				this.call = call;
//			}

			public Method(String name, String returnTypeStr, Type[] parameterTypes,
						  BiFunction<T, Type<?>.Instance[], R> call) {
				this.name = name;
				this.returnType = getType(returnTypeStr);
				this.parameterTypes = parameterTypes;
				this.call = (T t, Type<?>.Instance[] args) -> returnType.create(call.apply(t, args));
			}

			Type<R>.Instance call(T inst, Type<?>.Instance[] args) {
				return call.apply(inst, args);
			}

			@Override
			public String getName() {
				return name;
			}

			public void override(BiFunction<T, Type<?>.Instance[], R> call) {
				this.call = (T t, Type<?>.Instance[] args) -> returnType.create(call.apply(t, args));
			}

		}

		ClassType<? super T> parentClass;
		Set<InterfaceType<? super T>> interfaces;
		Map<String, Method> methods;
		boolean gettable;
		BiFunction<Message, ?, T> get; // static method

		public ClassType(String name) {
			super(name);
		}

		public ClassType(String name, Method... methods) {
			super(name);
			this.methods = namedMap(methods);
			this.get = null;
			this.gettable = false;
		}

		public ClassType<T> extend(ClassType<? super T> parent) {
			this.parentClass = parent;
//			fields.putAll(parent.fields); TODO why?
			return this;
		}

		public ClassType<T> implement(InterfaceType<? super T> parent) {
			interfaces.add(parent);
			return this;
		}

		/**
		 * @param parent a Functional Interface (one method)
		 */
		public ClassType<T> implement(InterfaceType<? super T> parent, BiFunction<T, Type<?>.Instance[], ?> methodBody) {
			Collection<Method> methods = parent.methods.values();
			if (methods.size() > 1)
				throw new CustomCommandCompileErrorException("Can't do single method implementation on a non-functional interface");
			methods.stream().findFirst().get().override(methodBody);
			return this;
		}

		public ClassType<T> implement(InterfaceType<? super T> parent, Method... overridingMethods) {
			interfaces.add(parent);
			for (Method method : overridingMethods) {
				parent.override(method);
			}
			return this;
		}

		public ClassType<T> override(Method method) throws CustomCommandCompileErrorException {
			Method original = methods.get(method.getName());
			if (original == null)
				throw new CustomCommandCompileErrorException("Method does not override method from its superclass");
//			original.getClass().getGenericSuperclass()
			methods.put(method.getName(), method);
			return this;
		}

		public ClassType<T> override(String methodName, BiFunction<T, Type<?>.Instance[], ?> methodBody) throws CustomCommandCompileErrorException {
			Method original = methods.get(methodName);
			if (original == null)
				throw new CustomCommandCompileErrorException("Method does not override method from its superclass");
//			original.getClass().getGenericSuperclass()
			original.override(methodBody);
			return this;
		}

		class ClassTypeInstance extends Type<T>.Instance {

			ClassType<? super T>.ClassTypeInstance parentInstance;
			Set<InterfaceType<? super T>.InterfaceInstance> interfaceInstances;

			public ClassTypeInstance(T real) {
				super(real);
				parentInstance = parentClass.create(real);
				interfaces.stream().map(i -> i.create(real));
			}

			Instance getField(String name) {
				return methods.get(name).call(real, null);
			}

			Instance callMethod(String name, Instance... args) {
				return methods.get(name).call(real, args);
			}

			@Override
			Type<?>.Instance tryCast(Type other) {
				for (Method method : methods.values())
					if (method instanceof Field) {
						Field field = ((Field) method);
						if (field.castable && field.returnType.equals(other)) {
							Instance instance = field.call(real, null);
							if (instance.exists())
								return instance;
						}
					}
				return super.tryCast(other);
			}

		}

		ClassTypeInstance create(T real) {
			return new ClassTypeInstance(real);
		}

		@Override
		public void set(Type<T> type) {
			if (type instanceof ClassType) {
				ClassType classType = (ClassType) type;
				this.methods = classType.methods;
				this.gettable = classType.gettable;
				this.get = classType.get;
			}
		}

	}

	static class InterfaceType<T> extends ClassType<T> { // optional abstract methods/fields

		Map<String, Method> methods;

		public InterfaceType(String name, Method... methods) {
			super(name, methods);
		}

		public InterfaceType override(Method method) throws CustomCommandCompileErrorException {
			Method original = methods.get(method.getName());
			if (original == null)
				throw new CustomCommandCompileErrorException("Method does not override method from its superclass");
//			original.getClass().getGenericSuperclass()
			methods.put(method.getName(), method);
			return this;
		}

		@Override
		public void set(Type<T> type) {
			if (type instanceof InterfaceType) {
				InterfaceType inter = (InterfaceType<T>) type;
				methods = inter.methods;
			}
		}

		@Override
		InterfaceInstance create(T real) { // can this be called?
			// return NULL
			return new InterfaceInstance(real);
		}

		class InterfaceInstance extends ClassType<T>.ClassTypeInstance {

			public InterfaceInstance(T real) {
				super(real);
			}

		}
	}

	static class ListType<E> extends ClassType<List<E>> {

		public ListType(String name, Type<E> elementType) {
			super(name);
		}

//		@Override
		ListType<E>.ListInstance create(List<E> real) {
			return new ListType<E>.ListInstance(real);
		}

		class ListInstance extends ClassType<List<E>>.ClassTypeInstance implements List<E> {
			public ListInstance(List<E> real) {
				super(real);
			}

			@Override public int size() { return real.size(); }
			@Override public boolean isEmpty() { return real.isEmpty(); }
			@Override public boolean contains(Object o) { return real.contains(o); }
			@Override @NotNull public Iterator<E> iterator() { return real.iterator(); }
			@Override @NotNull public Object[] toArray() { return real.toArray(); }
			@Override @NotNull public <T> T[] toArray(@NotNull T[] a) { return real.toArray(a); }
			@Override public boolean add(E e) { return real.add(e); }
			@Override public boolean remove(Object o) { return real.remove(o); }
			@Override public boolean containsAll(@NotNull Collection<?> c) { return real.containsAll(c); }
			@Override public boolean addAll(@NotNull Collection<? extends E> c) { return real.addAll(c); }
			@Override public boolean addAll(int index, @NotNull Collection<? extends E> c) { return real.addAll(index, c); }
			@Override public boolean removeAll(@NotNull Collection<?> c) { return real.removeAll(c); }
			@Override public boolean retainAll(@NotNull Collection<?> c) { return real.retainAll(c); }
			@Override public void clear() { real.clear(); }
			@Override public E get(int index) { return real.get(index); }
			@Override public E set(int index, E element) { return real.set(index, element); }
			@Override public void add(int index, E element) { real.add(index, element); }
			@Override public E remove(int index) { return real.remove(index); }
			@Override public int indexOf(Object o) { return real.indexOf(o); }
			@Override public int lastIndexOf(Object o) { return real.lastIndexOf(o); }
			@Override @NotNull public ListIterator<E> listIterator() { return real.listIterator(); }
			@Override @NotNull public ListIterator<E> listIterator(int index) { return real.listIterator(); }
			@Override @NotNull public List<E> subList(int fromIndex, int toIndex) { return real.subList(fromIndex, toIndex); }

		}

	}

	private static class Void {
		static Void VOID;
		public static Void getInstance() {
			return VOID;
		}
		private Void() {}
	}

	static class VoidType extends Type<Void> {

		private final VoidInstance VOIDINSTANCE = new VoidInstance();

		public VoidType() {
			super("void");
		}

		@Override
		VoidInstance create(Void real) {
			return create();
		}

		VoidInstance create() {
			return VOIDINSTANCE;
		}

		class VoidInstance extends Type<Void>.Instance {

			VoidInstance() {
				super(null);
			}

			@Override
			boolean exists() {
				return false;
			}

		}

		@Override
		public void set(Type<Void> type) {

		}

	}

	static interface Cacheable<T> {
		boolean isCached();

		T getCache();
	}

	static interface Nameable {
		String getName();
	}

}
