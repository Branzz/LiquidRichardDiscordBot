package com.wordpress.brancodes.messaging.reactions.commands.custom.types.obj;

import com.wordpress.brancodes.messaging.reactions.commands.custom.exception.CustomCommandCompileErrorException;
import com.wordpress.brancodes.messaging.reactions.commands.custom.tokens.Operator;
import com.wordpress.brancodes.messaging.reactions.commands.custom.types.InterfaceType;
import com.wordpress.brancodes.messaging.reactions.commands.custom.types.Type;
import net.dv8tion.jda.api.entities.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

import static com.wordpress.brancodes.messaging.reactions.commands.custom.CustomCommand.getType;
import static com.wordpress.brancodes.messaging.reactions.commands.custom.CustomCommand.namedMap;

public class ClassType<T> extends Type<T> {

	private static final Logger LOGGER = LoggerFactory.getLogger(ClassType.class);

	private ClassType<? super T> parentClass;
	private Set<InterfaceType<? super T>> interfaces;
	private Map<String, Method<? super T, ?>> methods;
	private boolean gettable;
	private BiFunction<Message, ?, T> get; // static method

	public ClassType(String name) {
		super(name);
	}

	public ClassType(String name, Method<T, ?>... methods) {
		super(name);
		this.methods = namedMap(methods);
		this.get = null;
		this.gettable = false;
	}

	public ClassType<T> registerOperators(Operator... operators) {
		for (Operator operator : operators)
			for (Method method : operator.getMethods()) {
				addMethod(method);
			}
		return this;
	}

	public ClassType<T> addMethod(Method<T, ?> method) {
		methods.put(method.getName(), method);
		return this;
	}

	public ClassType<T> addField(Field<T, ?> field) {
		methods.put(field.getName(), field);
		return this;
	}

	public ClassType<T> extend(ClassType<? super T> parent) {
		this.parentClass = parent;
//			fields.putAll(parent.fields); TODO why?
		return this;
	}

	public ClassType<T> extend(String parentName) {
		this.parentClass = (ClassType<? super T>) getType(parentName);
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
		Collection<Method> methods = parent.getMethods().values();
		if (methods.size() > 1)
			throw new CustomCommandCompileErrorException("Can't do single method implementation on a non-functional interface");
		methods.stream().findFirst().get().override(methodBody);
		return this;
	}

	public ClassType<T> implement(InterfaceType<? super T> parent, Method<? super T, ?>... overridingMethods) {
		interfaces.add(parent);
		for (Method<? super T, ?> method : overridingMethods) {
			parent.override(method);
		}
		return this;
	}

	public ClassType<T> override(Method<? super T, ?> method) throws CustomCommandCompileErrorException {
		Method<? super T, ?> original = methods.get(method.getName());
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

	public ClassType<T> tryAddRealMethods(Class<? extends T> tClass) {
		Arrays.stream(tClass.getMethods())
				.filter(realMethod -> realMethod.canAccess(this))
				.map(realMethod -> new Method<>(
						realMethod.getName(),
						getType(realMethod.getReturnType())[0].getName(),
						(BiFunction<T, Type<?>.Instance[], Object>) (x, args) -> {
							try {
								return realMethod.invoke(x, Arrays.stream(args).map(i -> i.get()).toArray());
							} catch (IllegalAccessException | InvocationTargetException ignored) {
								LOGGER.info("failed a real method");
								return null;
							}
						},
						Arrays.stream(realMethod.getParameterTypes())
								.map(p -> getType(p)[0].getName()).toArray(String[]::new)))
				.forEach(this::addMethod);
		return this;
	}

	public class ClassTypeInstance extends Type<T>.Instance {

		ClassType<? super T>.ClassTypeInstance parentInstance;
		Set<InterfaceType<? super T>.InterfaceInstance> interfaceInstances;

		public ClassTypeInstance(T real) {
			super(real);
			parentInstance = parentClass.create(real);
			interfaces.stream().map(i -> i.create(real));
		}

		public Type<?>.Instance getField(String name) {
			return methods.get(name).call(real, null);
		}

		public Type<?>.Instance callMethod(String name, Instance... args) {
			return methods.get(name).call(real, args);
		}

		@Override
		public Type<?>.Instance tryCast(Type other) {
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

	public ClassTypeInstance create(T real) {
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
