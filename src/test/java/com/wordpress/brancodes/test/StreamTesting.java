package com.wordpress.brancodes.test;

import java.util.stream.Stream;
public class StreamTesting {

	public static void main(String[] args) {

		Stream.of(new Getter(false, 0), new Getter(false, 1), new Getter(true, 2), new Getter(false, 4))
					  .filter(Getter::get).findFirst();
// x.get();

		// for (Command command : Commands.commandsByCategoryChannel.get(type).get(type1)) {
		// 	if (command.execute(message))
		// 		break;
		// }

		// UserCategory[] userCategories = UserCategory.values();
		// Arrays.sort(userCategories);
		// System.out.println(Arrays.toString(userCategories));

		// for (Map.Entry<ChannelType, Map<UserCategory, Set<Command>>> entry0 : Commands.commandsByCategoryChannel.entrySet()) {
		// 	System.out.println(entry0.getKey() + ":");
		// 	for (Map.Entry<UserCategory, Set<Command>> entry1 : entry0.getValue().entrySet()) {
		// 		System.out.println("\t" + entry1.getKey() + ":");
		// 		entry1.getValue().forEach(v -> System.out.println("\t\t" + v.getDescription()));
		// 	}
		// }

		// List<Integer> integerList = IntStream.range(1,20).boxed().collect(Collectors.toList());
		// integerList.stream().map(i -> i % 4).forEach(System.out::println);
		// integerList.stream().flatMap(i -> new Integer(i % 4));

		// Stream<List<String>> stream = Stream.of("Cat", "Dog", "Whale", "Cactus")
		// 									.collect(Collectors.collectingAndThen(
		// 											Collectors.partitioningBy(a -> a.charAt(0) == 'C'),
		// 											map -> Stream.of(map.get(true), map.get(false))
		// 									));
		// stream.forEach(System.out::println);

		// IntStream.range(0, 10)
		// 		 .mapToObj(n -> IntStream.of(n, n / 2, n / 3))
		// 		 .reduce(IntStream.empty(), IntStream::concat)
		// 		 .forEach(System.out::println);

		// Map<String, List<Person>> result
		// 		= Stream.of(new Person("A", "1"), new Person("A", "2"), new Person("A", "3"), new Person("B", "2"), new Person("B", "3"), new Person("C", "1"), new Person("C", "2"))
		// 				.flatMap(p -> Stream.of(p.getFirstName(), p.getLastName()).map(n -> new AbstractMap.SimpleEntry<>(n, p)))
		// 				.collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.mapping(Map.Entry::getValue, Collectors.toList())));
		// for (Map.Entry<String, List<Person>> entry : result.entrySet()) {
		// 	System.out.println(entry.getKey() + ":");
		// 	entry.getValue().forEach(System.out::println);
		// }

	}

	static class Getter {
		private boolean val;
		private int ID;
		public Getter(boolean val, int ID) {
			this.val = val;
			this.ID = ID;
		}
		public boolean get() {
			System.out.println(ID + " was got");
			return val;
		}
	}
	// static class Person {
	// 	private final String firstName;
	// 	private String lastName;
	// 	public Person(String firstName, String lastName) {
	// 		this.firstName = firstName;
	// 		this.lastName = lastName;
	// 	}
	// 	public String getFirstName() {
	// 		return firstName;
	// 	}
	// 	public String getLastName() {
	// 		return lastName;
	// 	}
	// 	public String toString() {
	// 		return firstName + " " + lastName;
	// 	}
	// }

	// public class GroupBy<X> implements Function<X, Stream<X>> {
	//
	// 	private final BiPredicate<X, X> groupBorder;
	// 	private final BinaryOperator<X> combiner;
	// 	private X latest = null;
	//
	// 	public GroupBy(BiPredicate <X, X> groupBorder,
	// 				   BinaryOperator<X> combiner) {
	// 		this.groupBorder = groupBorder;
	// 		this.combiner = combiner;
	// 	}
	//
	// 	@Override
	// 	public Stream<X> apply(X elem) {
	// 		// TODO: add test on end marker as additonal parameter for constructor
	// 		if (elem==null) {
	// 			return latest==null ? Stream.empty() : Stream.of(latest);
	// 		}
	// 		if (latest==null) {
	// 			latest = elem;
	// 			return Stream.empty();
	// 		}
	// 		if (groupBorder.test(latest, elem)) {
	// 			Stream<X> result = Stream.of(latest);
	// 			latest = elem;
	// 			return result;
	// 		}
	// 		latest = combiner.apply(latest,  elem);
	// 		return Stream.empty();
	// 	}
	// }
}
