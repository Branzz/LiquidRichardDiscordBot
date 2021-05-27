package com.wordpress.brancodes.test;

import com.wordpress.brancodes.util.MorseUtil;
public class MorseTest {

	public static void main(String[] args) {
		for (String s : new String[] {"Help Her Out Here Richard.", "abc a AOEUCHAOER CUU"})
			System.out.println(s + "=" + MorseUtil.toMorse(s));
		// for (String s : new String[] {".- -... -.-. .-.-.-", ".... .. / - .... . .-. . .-.-.-"})
		// 	System.out.println(s + "=" + MorseUtil.fromMorse(s));
	}

}
