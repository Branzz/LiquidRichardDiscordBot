package com.wordpress.brancodes.messaging.reactions.commands.custom.types.obj;

import com.wordpress.brancodes.messaging.reactions.commands.custom.types.Nameable;
import com.wordpress.brancodes.messaging.reactions.commands.custom.types.Type;

import java.util.function.BiFunction;

import static com.wordpress.brancodes.messaging.reactions.commands.custom.CustomCommand.getType;

/**
 * @param <T> real thing this is a member of
 * @param <R> return real type
 */
public class Method<T, R> implements Nameable { // methods must be implemented
	String name;
	Type<R> returnType;
	Type[] parameterTypes;
	BiFunction<T, Type<?>.Instance[], ? extends Type<R>.Instance> call;

	public Method(String name, String returnTypeStr) { // declared and not initialize
		this.name = name;
		this.returnType = getType(returnTypeStr);
	}

//	public Method(String name, String returnType, Type[] parameterTypes,
//				  BiFunction<T, Instance[], ? extends Type<R>.Instance<R>> call) {
//		this.name = name;
//		this.returnType = getType(returnType);
//		this.parameterTypes = parameterTypes;
//		this.call = call;
//	}

//	public Method(String name, String returnTypeStr, Type[] parameterTypes,
//				  BiFunction<T, Type<?>.Instance[], R> call) {
//		this.name = name;
//		this.returnType = getType(returnTypeStr);
//		this.parameterTypes = parameterTypes;
//		this.call = (T t, Type<?>.Instance[] args) -> returnType.create(call.apply(t, args));
//	}

	public Method(String name, String returnTypeStr, BiFunction<T, Type<?>.Instance[], R> call, String... parameterTypeNames) {
		this.name = name;
		this.returnType = getType(returnTypeStr);
		this.parameterTypes = new Type[parameterTypeNames.length];
		for (int i = 0; i < parameterTypeNames.length; i++) {
			parameterTypes[i] = getType(parameterTypeNames[i]);
		}
		this.call = (T t, Type<?>.Instance[] args) -> returnType.create(call.apply(t, args));
	}

	public Type<R>.Instance call(T inst, Type<?>.Instance[] args) {
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
