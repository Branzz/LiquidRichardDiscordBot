package com.wordpress.brancodes.messaging.reactions.commands.custom;

import com.wordpress.brancodes.messaging.reactions.commands.custom.CustomCommand.ClassType.Field;
import com.wordpress.brancodes.messaging.reactions.commands.custom.CustomCommand.ClassType.Method;
import com.wordpress.brancodes.messaging.reactions.commands.custom.CustomCommand.ClassType.ClassTypeInstance;
import com.wordpress.brancodes.messaging.reactions.commands.custom.CustomCommand.Type.Instance;
import com.wordpress.brancodes.messaging.reactions.users.UserCategory;
import net.dv8tion.jda.api.entities.*;
import org.springframework.data.util.Streamable;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

@SuppressWarnings("ALL")
public class CustomCommand {

	Message message;

	public CustomCommand(Message message, Matcher matcher, String input) {
		this.message = message;
	}

	public CustomCommand(final String regex, final String name, final UserCategory category,
						 BiFunction<Message, Matcher, Boolean> executer) {
//		super(regex, name, category, channelCategory);
		executer = (message, matcher) -> {
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

	static Map<String, Type> types = namedMap(Streamable.of(
			new VoidType(),
			new ListType("list"),
			new PrimitiveType<Long>("num", String::valueOf, n -> n != 0),
			new PrimitiveType<String>("str", Long::valueOf, Boolean::valueOf),
			new PrimitiveType<Boolean>("bool", String::valueOf),
			new ClassType<Member>("user"),
			new ClassType<Channel>("channel"),
			new ClassType<Message>("message"),
			new ClassType<Guild>("guild"),
			new ClassType<Emoji>("emoji")
	));

	static {
		addType(new ClassType<Member>("user",
				Streamable.of(new Field<>("id", "num", true, Member::getIdLong),
						new Field<>("name", "str", true, Member::getEffectiveName)),
				Streamable.of(new Method<Member, Void>("ban", "void", new Type[] {},
						(user, args) -> { user.ban(0).queue(); return VOID; }))));
//			new ObjType("user", new Field[] {}, new Method[] {}),

	}

	static Map<String, Method> publicMethods = namedMap(Streamable.of(
			new Method<Matcher, Object>("group", "any", new Type[] { getType("num") },
					(Matcher m, Instance[] args) -> m.group(((Long) args[0].get()).intValue()))
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

		public abstract void set(Type type);

		public class Instance<S extends T> {
			protected T real;

			public Instance(T real) {
				this.real = real;
			}

			public T get() {
				return real;
			}

			Type<?>.Instance<?> tryCast(Type other) {
				return new VoidType().create();
			}

			boolean exists() {
				return true;
			}

		}

		abstract Instance<T> create(T real);

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

		class PrimitiveInstance extends Instance<T> {

			public PrimitiveInstance(T real) {
				super(real);
			}

			Type<?>.Instance<?> tryCast(Type other) {
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
		public void set(Type type) {
			if (type instanceof PrimitiveType) {
				this.casts = ((PrimitiveType) type).casts;
			}
		}

	}

	// T is the jda type it is based on
	static class ClassType<T> extends Type<T> {

		static class Field<T, R> extends Method<T, R> { // no-arg method
			public boolean castable; // allow this field to be converted to its class type

//			Field(String name, String type, Function<T, ? extends Instance<R>> call) {
//				super(name, type, null, (T t, Instance[] args) -> call.apply(t));
//			}

			Field(String name, String type, boolean castable, Function<T, R> call) {
				super(name);
				this.returnType = getType(type);
				this.parameterTypes = null;
				this.call = (T t, Instance[] args) -> returnType.create(call.apply(t));
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
		static class Method<T, R> implements Nameable {
			String name;
			Type<R> returnType;
			Type[] parameterTypes;
			BiFunction<T, Instance[], ? extends Type<R>.Instance<R>> call;

			protected Method(String name) {
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
						  BiFunction<T, Instance[], R> call) {
				this.name = name;
				this.returnType = getType(returnTypeStr);
				this.parameterTypes = parameterTypes;
				this.call = (T t, Instance[] args) -> returnType.create(call.apply(t, args));
			}

			Type<R>.Instance<R> call(T inst, Instance[] args) {
				return call.apply(inst, args);
			}

			@Override
			public String getName() {
				return name;
			}

		}

		Map<String, Field> fields;
		Map<String, Method> methods;
		boolean gettable;
		Supplier<T> get; // static method

		public ClassType(String name) {
			super(name);
		}

		public ClassType(String name, Streamable<Field> fields, Streamable<Method> methods, Supplier<T> get) {
			super(name);
			this.fields = namedMap(fields);
			this.methods = namedMap(methods);
			this.get = get;
			this.gettable = true;
		}

		public ClassType(String name, Streamable<Field> fields, Streamable<Method> methods) {
			super(name);
			this.fields = namedMap(fields);
			this.methods = namedMap(methods);
			this.get = null;
			this.gettable = false;
		}

		class ClassTypeInstance extends Instance<T> {

			public ClassTypeInstance(T real) {
				super(real);
			}

			Object getField(String name) {
				return fields.get(name).call(real, null);
			}

			Instance callMethod(String name, Instance... args) {
				return methods.get(name).call(real, args);
			}

			@Override
			Type<?>.Instance<?> tryCast(Type other) {
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
		public void set(Type type) {
			if (type instanceof ClassType) {
				ClassType classType = (ClassType) type;
				this.fields = classType.fields;
				this.methods = classType.methods;
				this.gettable = classType.gettable;
				this.get = classType.get;
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

		class ListInstance extends Instance<List<?>> {
			public ListInstance(List<?> real) {
				super(real);
			}
		}

		@Override
		public void set(Type type) { }

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

		class VoidInstance extends Instance<Void> {

			VoidInstance() {
				super(null);
			}

			@Override
			boolean exists() {
				return false;
			}

		}

		@Override
		public void set(Type type) {

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
