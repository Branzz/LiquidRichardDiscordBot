package com.wordpress.brancodes.messaging.reactions.message.commands.custom.types;

import com.wordpress.brancodes.messaging.reactions.message.commands.custom.types.obj.ClassType;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class ListType<E> extends ClassType<List<E>> {

	public ListType(String name, Type<E> elementType) {
		super(name);
	}

	//		@Override
	public ListType<E>.ListInstance create(List<E> real) {
		return new ListType<E>.ListInstance(real);
	}

	public class ListInstance extends ClassType<List<E>>.ClassTypeInstance implements List<E> {
		public ListInstance(List<E> real) {
			super(real);
		}

		@Override public int size() { return real.size(); }
		@Override public boolean isEmpty() { return real.isEmpty(); }
		@Override public boolean contains(Object o) { return real.contains(o); }
		@Override @NotNull
		public Iterator<E> iterator() { return real.iterator(); }
		@Override @NotNull public Object[] toArray() { return real.toArray(); }
		@Override @NotNull public <T> T[] toArray(@NotNull T[] a) { return real.toArray(a); }
		@Override public boolean add(E e) { return real.add(e); }
		@Override public boolean remove(Object o) { return real.remove(o); }
		@Override public boolean containsAll(@NotNull Collection<?> c) { return real.containsAll(c); }
		@Override public boolean addAll(@NotNull Collection<? extends E> c) { return real.addAll(c); }
		@Override public boolean addAll(int index, @NotNull Collection<? extends E> c) { return real.addAll(index, c); }
		@Override public boolean removeAll(@NotNull Collection<?> c) { return real.removeAll(c); }
		@Override public boolean retainAll(@NotNull Collection<?> c) { return real.retainAll(c); }
		@Override public void clear() { real.clear(); }
		@Override public E get(int index) { return real.get(index); }
		@Override public E set(int index, E element) { return real.set(index, element); }
		@Override public void add(int index, E element) { real.add(index, element); }
		@Override public E remove(int index) { return real.remove(index); }
		@Override public int indexOf(Object o) { return real.indexOf(o); }
		@Override public int lastIndexOf(Object o) { return real.lastIndexOf(o); }
		@Override @NotNull public ListIterator<E> listIterator() { return real.listIterator(); }
		@Override @NotNull public ListIterator<E> listIterator(int index) { return real.listIterator(); }
		@Override @NotNull public List<E> subList(int fromIndex, int toIndex) { return real.subList(fromIndex, toIndex); }

	}

}
