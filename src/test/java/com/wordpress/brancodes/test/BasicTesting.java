package com.wordpress.brancodes.test;

public class BasicTesting {

	public static void main(String[] args) {
		funct(5 * 5 - 1);
		funct(5 * 5);
		funct(46340 * 46340 - 1);
		funct(46340 * 46340);
	}

	private static void funct(int square) {
		int i = 1;
		while (i * i < square)
			i++;
		System.out.println(i);
		int j = 1;
		while (j * j <= square)
			j++;
		System.out.println(j);

	}

	protected static int x;
}
