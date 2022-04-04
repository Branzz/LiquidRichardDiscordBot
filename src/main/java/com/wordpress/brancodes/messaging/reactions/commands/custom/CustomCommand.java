package com.wordpress.brancodes.messaging.reactions.commands.custom;

import com.wordpress.brancodes.messaging.reactions.commands.custom.CustomCommand.ClassType.ClassTypeInstance;
import com.wordpress.brancodes.messaging.reactions.commands.custom.CustomCommand.ClassType.Field;
import com.wordpress.brancodes.messaging.reactions.commands.custom.CustomCommand.ClassType.Method;
import com.wordpress.brancodes.messaging.reactions.commands.custom.CustomCommand.Type.Instance;
import com.wordpress.brancodes.messaging.reactions.users.UserCategory;
import net.dv8tion.jda.api.entities.*;
import org.springframework.data.util.Streamable;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

//@SuppressWarnings("ALL")
public class CustomCommand {

	Message message;

	public CustomCommand(Message message, Matcher matcher, String input) {
		this.message = message;
	}

	public CustomCommand(final String regex, final String name, final UserCategory category,
						 BiFunction<Message, Matcher, Boolean> executer) {
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

	static Map<String, Type> types = namedMap(Streamable.of(
			new VoidType(),
			new ListType("list"),
			new PrimitiveType<Long>("num", String::valueOf, n -> n != 0),
			new PrimitiveType<String>("str", Long::valueOf, Boolean::valueOf),
			new PrimitiveType<Boolean>("bool", String::valueOf),
			new ClassType<Guild>("guild"),
			new ClassType<Channel>("channel"),
			new ClassType<Message>("message"),
			new ClassType<Member>("user"),
			new ClassType<Role>("role"),
			new ClassType<Emoji>("emoji"),
			new InterfaceType<ISnowflake>("identifiable",
					Streamable.of(new Field<ISnowflake, Long>("id", "num", true, ISnowflake::getIdLong)),
					Streamable.empty()),
			new InterfaceType<Object>("gettable", Streamable.empty(), Streamable.of(new Method<Object, Long>("get")))
	));

	static {
		addType(new ClassType<Member>("user",
			Streamable.of(new Field<>("id", "num", true, Member::getIdLong),
						  new Field<>("name", "str", true, Member::getEffectiveName),
						  new Field<>("pfp", "str", true, Member::getEffectiveAvatarUrl),
						  new Field<>("banner", "str", true, (Member m) -> m.getUser().retrieveProfile().complete().getBannerUrl())),
			Streamable.of(new Method<Member, Void>("giveRole", "void", new Type[] { getType("role") },
						(user, args) -> { user.getRoles().add((Role) (args[0].get())); return VOID; }),
						  new Method<Member, Void>("nick", "void", new Type[] { getType("str") },
						(user, args) -> { user.modifyNickname(((String) (args[0].get()))).queue(); return VOID; }),
						  new Method<Member, Void>("kick", "void", NO_ARGS,
						(user, args) -> { user.kick().queue(); return VOID; }),
						  new Method<Member, Void>("ban", "void", NO_ARGS,
						(user, args) -> { user.ban(0).queue(); return VOID; })
			)).implement(((InterfaceType) getType("gettable"))
				.override(new Method<Message, Long>("get", "num", true,
						(msg, id) -> msg.getGuild().getMember(msg.getJDA().retrieveUserById((Long) id).complete())))
				.implement(((InterfaceType) getType("identifiable"))));
//			new ObjType("user", new Field[] {}, new Method[] {}),

	}

	static Map<String, Method> publicMethods = namedMap(Streamable.of(
			new Method<Matcher, Object>("group", "any", new Type[] { getType("num") },
					(Matcher m, Type<?>.Instance[] args) -> m.group(((Long) args[0].get()).intValue()))
	));

	static Type getType(String name) {
		return types.get(name);
	}

	private static void addType(Type type) {
		Type existing = types.get(type);
		if (existing != null)
			existing.set(type);
		else
			types.put(type.getName(), type);
	}

	interface Cast<T, I> {
		T tryCast(I inst);
	}

	public static abstract class Type<T> implements Nameable {
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

			Type<?>.Instance tryCast(Type other) {
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

			Field(String name) {
				super(name);
			}

			Field(String name, String type, boolean castable, Function<T, R> call) {
				this(name);
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

			public Method(String name) { // declared and not initializide
				this.name = name;
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

		}

		ClassType<? super T> parentClass;
		Set<InterfaceType<? super T>> interfaces;
		Map<String, Field> fields;
		Map<String, Method> methods;
		boolean gettable;
		BiFunction<Message, ?, T> get; // static method

		public ClassType(String name) {
			super(name);
		}

		public ClassType(String name, Streamable<Field> fields, Streamable<Method> methods) {
			super(name);
			this.fields = namedMap(fields);
			this.methods = namedMap(methods);
			this.get = null;
			this.gettable = false;
		}

		public ClassType<T> extend(ClassType<? super T> parent) {
			this.parentClass = parent;
			fields.putAll(parent.fields);
			return this;
		}

		public ClassType<T> implement(InterfaceType<? super T> parent) {
			interfaces.add(parent);
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

			Object getField(String name) {
				return fields.get(name).call(real, null);
			}

			Instance callMethod(String name, Instance... args) {
				return methods.get(name).call(real, args);
			}

			@Override
			Type<?>.Instance tryCast(Type other) {
				for (Field field : fields.values())
					if (field.castable && field.returnType.equals(other)) {
						Instance instance = field.call(real, null);
						if (instance.exists())
							return instance;
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
				this.fields = classType.fields;
				this.methods = classType.methods;
				this.gettable = classType.gettable;
				this.get = classType.get;
			}
		}

	}

	static class InterfaceType<T> extends Type<T> { // optional abstract methods

		Map<String, Field> fields;
		Map<String, Method> methods;

		public InterfaceType(String name, Streamable<Field> fields, Streamable<Method> methods) {
			super(name);
			this.fields = namedMap(fields);
			this.methods = namedMap(methods);
		}

		public void override(Method method) throws CustomCommandCompileErrorException {
			Method original = methods.get(method.getName());
			if (original == null)
				throw new CustomCommandCompileErrorException("Method does not override method from its superclass");
//			original.getClass().getGenericSuperclass()
			methods.put(method.getName(), method);
		}

		@Override
		public void set(Type<T> type) {
			if (type instanceof InterfaceType) {
				InterfaceType inter = (InterfaceType<T>) type;
				fields = inter.methods;
				methods = inter.methods;
			}
		}

		@Override
		InterfaceInstance create(T real) {
			return new InterfaceInstance(real);
		}

		class InterfaceInstance extends Type<T>.Instance {

			public InterfaceInstance(T real) {
				super(real);
			}

		}
	}

	static class ListType extends Type<List<?>> {
		public ListType(String name) {
			super(name);
		}

		@Override
		ListInstance create(List<?> real) {
			return new ListInstance(real);
		}

		class ListInstance extends Type<List<?>>.Instance {
			public ListInstance(List<?> real) {
				super(real);
			}
		}

		@Override
		public void set(Type<List<?>> type) { }

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

	static Map<String, Operator> operatorNames = Arrays.stream(Operator.values())
			.flatMap(o -> Arrays.stream(o.getSymbols()) // .distinct()
					.map(s -> new AbstractMap.SimpleEntry<>(s, o)))
			.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

	static Operator getOperator(String name) {
		return operatorNames.get(name);
	}

	enum Operator {
		CALL("."), LEFT_PAREN("("), RIGHT_PAREN(")"), LEFT_BRACKET("{"), RIGHT_BRACKET("}"),
		NOT("!", "~"), EQUALS("equals", "==", "is"), NOT_EQUALS("!="), LESS("<"), GREATER(">"),
		LESS_EQUAL("<="), GREATER_EQUAL(">="), QUOTE("\"")
		;
		private String[] symbols;

		Operator(String... symbols) {
			this.symbols = symbols;
		}

		public String[] getSymbols() {
			return symbols;
		}
	}
}
