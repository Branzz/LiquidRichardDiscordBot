package com.wordpress.brancodes.messaging.cooldown;

import com.wordpress.brancodes.util.CaseUtil;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class CooldownPool<B, T> {

	private final Map<T, Long> cooldownPool;
	private final Function<B, T> messageTypeConverter;
	private final long cooldown;
	private final Class<T> type; // not really an anti-pattern, just want for toString TODO get class name of T

	public CooldownPool(long cooldown, Function<B, T> messageTypeConverter, Class<T> type) {
		cooldownPool = new HashMap<>();
		this.cooldown = cooldown;
		this.messageTypeConverter = messageTypeConverter;
		this.type = type;
	}

	public void addConverted(B message) {
		add(messageTypeConverter.apply(message));
	}

	public void add(T t) {
		cooldownPool.put(t, System.currentTimeMillis());
	}

	public boolean checkConverted(B message) {
		return check(messageTypeConverter.apply(message));
	}

	private boolean check(T locker) {
		Long creationTime = cooldownPool.get(locker);
		if (creationTime == null)
			return true;
		if (System.currentTimeMillis() > creationTime + cooldown) {
			cooldownPool.remove(locker);
			return true;
		}
		return false;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		final CooldownPool<?, ?> that = (CooldownPool<?, ?>) o;

		if (cooldown != that.cooldown)
			return false;
		return type.equals(that.type);
	}

	@Override
	public int hashCode() {
		int result = (int) (cooldown ^ (cooldown >>> 32));
		result = 31 * result + type.getName().hashCode();
		return result;
	}

	@Override
	public String toString() {
		return ((cooldown < 1000) ? (new BigDecimal(cooldown).movePointLeft(3).stripTrailingZeros().toString()) : (cooldown / 1000))
			   + " Seconds For " + CaseUtil.addSpacesToProper(type.getSimpleName()) + "s";
	}

}
