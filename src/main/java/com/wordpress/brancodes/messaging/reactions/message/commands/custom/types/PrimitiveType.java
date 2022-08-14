package com.wordpress.brancodes.messaging.reactions.message.commands.custom.types;

import com.wordpress.brancodes.messaging.reactions.message.commands.custom.types.obj.ClassType;

import java.util.List;

import static com.wordpress.brancodes.messaging.reactions.message.commands.custom.CustomCommand.getInstance;

public class PrimitiveType<T> extends ClassType<T> {

	private Iterable<Cast<?, T>> casts;

	public PrimitiveType(String name) {
		super(name);
	}

	public PrimitiveType(String name, Cast<?, T>... casts) {
		super(name);
		this.casts = List.of(casts);
	}

	public class PrimitiveInstance extends ClassTypeInstance {

		public PrimitiveInstance(T real) {
			super(real);
		}

		@Override
		public Type<?>.Instance tryCast(Type other) {
			if (casts != null)
				for (Cast<?, T> cast : casts)
					if (cast.getClass().getGenericSuperclass().equals(other.getClass())) {
						Instance instance = getInstance(cast.tryCast(real));
						if (instance.exists())
							return instance;
					}
			return super.tryCast(other);
		}

	}

	@Override
	public PrimitiveInstance create(T real) {
		return new PrimitiveInstance(real);
	}

	@Override
	public void set(Type<T> type) {
		if (type instanceof PrimitiveType) {
			this.casts = ((PrimitiveType) type).casts;
		}
	}

}
