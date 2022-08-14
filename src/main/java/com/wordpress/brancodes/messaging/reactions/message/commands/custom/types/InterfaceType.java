package com.wordpress.brancodes.messaging.reactions.message.commands.custom.types;

import com.wordpress.brancodes.messaging.reactions.message.commands.custom.exception.CustomCommandCompileErrorException;
import com.wordpress.brancodes.messaging.reactions.message.commands.custom.types.obj.ClassType;
import com.wordpress.brancodes.messaging.reactions.message.commands.custom.types.obj.Method;

import java.util.HashMap;
import java.util.Map;

public class InterfaceType<T> extends ClassType<T> { // optional abstract methods/fields

	public InterfaceType(String name, Method<? super T, ?>... methods) {
		super(name, methods);
	}

	public InterfaceType<T> override(Method<? super T, ?> method) throws CustomCommandCompileErrorException {
		Method<? super T, ?> original = methods.get(method.getName());
		if (original == null)
			throw new CustomCommandCompileErrorException("Method does not override method from its superclass");
//			original.getClass().getGenericSuperclass()
		methods.put(method.getName(), method);
		return this;
	}

	@Override
	public void set(Type<T> type) {
		if (type instanceof InterfaceType) {
			InterfaceType<T> inter = (InterfaceType<T>) type;
			methods = inter.methods;
		}
	}

	@Override
	public InterfaceInstance create(T real) { // can this be called?
		// return NULL
		return new InterfaceInstance(real);
	}

	public class InterfaceInstance extends ClassType<T>.ClassTypeInstance {

		public InterfaceInstance(T real) {
			super(real);
		}

	}
}
