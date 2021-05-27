package com.wordpress.brancodes.test;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
class Example {
    public static void main(String[] args) {
        List<Integer> myList = List.of(5, 10, 15, 20, 25, 30);
 
        Map<Range, List<Integer>> result
                = Stream.of(Range.values())
                        .collect(Collectors.toMap(range -> range,
                                                  range -> myList.stream()
                                                                 .filter(range::inRange)
                                                                 .collect(Collectors.toList())));

        System.out.println(result);
    }
}

enum Range {
    BOTTOM(0, 20), MIDDLE(10, 30), TOP(20, 40);
 
    int start;
    int end;
 
    Range(int start, int end) {
        this.start = start;
        this.end = end;
    }
 
    boolean inRange(int value) {
        return value >= start && value <= end;
    }
}
 
class Person {
    private int age;

    public Person(int age) {
        this.age = age;
    }
 
    public int getAge() {
        return age;
    }
 
    public void setAge(int age) {
        this.age = age;
    }
 

    public String toString() {
        return String.valueOf(age);
    }
}