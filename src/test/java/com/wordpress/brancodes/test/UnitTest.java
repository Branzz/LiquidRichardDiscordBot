package com.wordpress.brancodes.test;

import com.wordpress.brancodes.messaging.reactions.message.MessageReaction;
import com.wordpress.brancodes.messaging.reactions.ReactionResponse;
import com.wordpress.brancodes.messaging.reactions.ReactionManager;
import com.wordpress.brancodes.test.proxy.TesterMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class UnitTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(UnitTest.class);

	private static final String[] TEST_CASES =
			{ "5'", "5.4'", "5'3", "5.4'3", "5.4'3.2", "5.'", "5'3.", "3\"", "3.3\"", ".3\"", // feet inch
			  "5's", "5 foot", "5 foot 5", "5 foot 5's", "5 foot's", "5\"", "5''", "5''s",
			  "150lbs 5'10", "5'10 5 inches 80kg", "180cm 152lbs", // bmi
			  "161cm 25kg", "161cm 25.1kg", "161cm 25.2kg", "161cm 25.3kg", "161cm 25.4kg", // accuracy / rounding
			};

	public static void test() {
		final MessageReaction convertUnit = (MessageReaction) ReactionManager.reactionsByName.get("Convert Units");
		Arrays.stream(TEST_CASES).map(testCase -> new TesterMessage(testCase, false)).map(convertUnit::execute).forEachOrdered(ReactionResponse::logResponse);
	}

	public static void main(String[] args) {
		test();
	}

}
