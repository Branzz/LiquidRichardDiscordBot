package com.wordpress.brancodes.messaging.reactions.commands.custom.types;

public class VoidType extends Type<Void> {

	private final VoidInstance VOIDINSTANCE = new VoidInstance();

	public VoidType() {
		super("void");
	}

	@Override
	public VoidInstance create(Void real) {
		return create();
	}

	public VoidInstance create() {
		return VOIDINSTANCE;
	}

	public class VoidInstance extends Type<Void>.Instance {

		VoidInstance() {
			super(null);
		}

		@Override
		public boolean exists() {
			return false;
		}

	}

	@Override
	public void set(Type<Void> type) {

	}

}

