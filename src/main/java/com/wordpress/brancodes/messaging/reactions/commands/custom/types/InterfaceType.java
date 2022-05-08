package com.wordpress.brancodes.messaging.reactions.commands.custom.types;

import com.wordpress.brancodes.messaging.reactions.commands.custom.CustomCommandCompileErrorException;
import com.wordpress.brancodes.messaging.reactions.commands.custom.types.obj.ClassType;
import com.wordpress.brancodes.messaging.reactions.commands.custom.types.obj.Method;

import java.util.Map;

public class InterfaceType<T> extends ClassType<T> { // optional abstract methods/fields

	private Map<String, Method> methods;

	public InterfaceType(String name, Method... methods) {
		super(name, methods);
	}

	public Map<String, Method> getMethods() {
		return methods;
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
