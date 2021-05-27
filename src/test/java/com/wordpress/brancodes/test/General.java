package com.wordpress.brancodes.test;

import net.dv8tion.jda.api.entities.Guild;

import java.util.*;
public class General {

	//  0110100110010110100101100110100110010110011010010110100110010110100101100110100101101001100101100110
//		0	1	1	0	1	0	0	1	1	0	0	1	0	1	1	0	1	0	0	1	0	1	1	0	0


	public static void main(String[] args) {

		// String[] str = { "", "", "" };
		// for (int i = 0; i < 100; i++)  {
		// 	str[0] += String.valueOf(i % 3 == 0 ? 1 : 0);
		// 	final int parity = Integer.toBinaryString(i)
		// 						  .replaceAll("0", "")
		// 						  .length() % 2;
		// 	str[1] += String.valueOf(i % 3 == 0 && (parity == 0) ? 1 : 0);
		// 	// str[1] += String.valueOf(i % 5 == 0 ? 1 : 0);
		// 	str[2] += parity;
		// }
		// Arrays.stream(str).forEach(System.out::println);
		// int[] z = {};
		// z.clone();
		// for (int num = 0; num <= 0b111111; num++) {
		// 	System.out.println(String.format("%06d", Integer.parseInt(Integer.toBinaryString((num)))) + " " + ((0x96696996 & (1 << num)) == 0 ? 0 : 1) + " " +
		// 					   (Integer.toBinaryString(( ((num + 16) >> 5) ^ ((num + 4) >> 3) ^ ((num + 1) >> 1) )&1))); // OR 12, 1 // & 0b1010
		// }
		// System.out.println(lowestIndexOf(new int[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10}, 5));
		// System.out.println(lowestIndexOf(new int[] {5, 7, 9, 10, 1, 3, 2, 4, 6, 8}, 5));
		// System.out.println(lowestIndexOf(new int[] {-5, 0, 3, 3, 5, 9, 9, 9, 9, 15}, 5));
		// System.out.println(lowestIndexOf(new int[] {9, 15, 9, 5, 3, 9, 0, 3, -5, 9}, 5));
	}

	private static String removeDuplicates(String s) {
		return s.length() <= 1 ? s : s.charAt(0) == s.charAt(1) ? removeDuplicates(s.substring(1)) : s.charAt(0) + removeDuplicates(s.substring(1));
	}

	private static int lowestIndexOf(int[] arr, int k) {
		return (int) Arrays.stream(arr).filter(i -> i < k).count();
	}

	private static int not(int i) {
		return i == 0 ? 1 : 0;
	}

	static class Position {
		final int x;
		final int y;
		final int dir;

		public Position(final int x, final int y, final int dir) {
			this.x = x;
			this.y = y;
			this.dir = dir;
		}

		@Override
		public boolean equals(final Object o) {
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;
			final Position position = (Position) o;
			return x == position.x && y == position.y && dir == position.dir;
		}
	}

	public static boolean isRobotBounded(String s) {
		int x=0,y=0,d=0;
			for(int i:s.toCharArray()){
				switch(i){
					case 71:
						switch(d){
							case 0:
								y++;
								break;
							case 1:
								x--;
								break;
							case 2:
								y--;
								break;
							case 3:
								x++;
								break;
						}
						break;
					case 76:
						d = (d + 1) % 4;
						break;
					case 82:
						d = (d - 1) % 4;
						if (d == -1)
							d = 3;
						break;
				}
			}
			System.out.printf("(%d, %d) %d\n", x, y, d);
		return d!=0||x==0&&y==0;
	}

}
