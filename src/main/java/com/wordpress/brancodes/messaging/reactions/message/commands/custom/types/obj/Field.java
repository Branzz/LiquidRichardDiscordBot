package com.wordpress.brancodes.messaging.reactions.message.commands.custom.types.obj;

import com.wordpress.brancodes.messaging.reactions.message.commands.custom.CustomCommand;
import com.wordpress.brancodes.messaging.reactions.message.commands.custom.types.Type;

import java.util.function.Function;

public class Field<T, R> extends Method<T, R> { // no-arg method
	public boolean castable; // allow this field to be converted to its class type

//			Field(String name, String type, Function<T, ? extends Instance<R>> call) {
//				super(name, type, null, (T t, Instance[] args) -> call.apply(t));
//			}

	public Field(String name, String type) {
		super(name, type);
	}

	public Field(String name, String type, boolean castable) {
		super(name, type);
	}

	public Field(String name, String type, boolean castable, Function<T, R> call) {
		this(name, type);
		this.returnType = CustomCommand.getType(type);
		this.parameterTypes = null;
		this.call = (T t, Type<?>.Instance[] args) -> returnType.create(call.apply(t));
	}

}
