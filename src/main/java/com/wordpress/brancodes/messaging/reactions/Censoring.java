package com.wordpress.brancodes.messaging.reactions;

import com.wordpress.brancodes.util.GenericRestAction;
import com.wordpress.brancodes.util.JSONReader;
import com.wordpress.brancodes.util.JavaUtil;
import com.wordpress.brancodes.util.RegexUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;

import javax.annotation.RegEx;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import static com.wordpress.brancodes.bot.LiquidRichardBot.autodeleteLogMap;
import static com.wordpress.brancodes.bot.LiquidRichardBot.getUserName;
import static com.wordpress.brancodes.util.RegexUtil.getMatcher;
import static java.util.stream.Collectors.joining;
public class Censoring {

	private static final Map<Character, String> homoglyphs = new HashMap<>();

	static {
		Map<String, String> joHoms = ((Map<String, String>) JSONReader.getData().get("homoglyphs"));
		if (joHoms == null) {
			for (char c = 'a'; c <= 'z'; c++)
				homoglyphs.put(c, (String.valueOf(c) + Character.toUpperCase(c)));
		} else {
			joHoms.forEach((k, v) -> {
				homoglyphs.put(k.charAt(0), v);
			});

		}
	}

	// public static final @RegEx String censoredWordsGeneRegex =
	// 		censoredWords.stream()
	// 					 .map(word -> breakCharMatcher.reset(censorGeneRegex(word))
	// 												  .replaceAll(String.valueOf((char) Integer.parseInt(breakCharMatcher.group(1), 16))))
	// 					 .collect(Commands.orChainRegex());

	// private static final @RegEx String censoredUsersMatcher = orChainRegex(Stream.of("a", "x", "e")
	// 																			 .map(Commands::censorRegex)
	// 																			 .toArray(String[]::new));

	private static final List<String> censoredWords = (List<String>) JSONReader.getData().get("censored_words");
	private static final Map<String, Matcher> censoredWordsMatchers
			= censoredWords.stream()
						   .collect(Collectors.toMap(Function.identity(),
													 word -> getMatcher(censorRegex(word))));
	public static final @RegEx String censoredWordsRegex = censoredWords.stream()
																		.map(Censoring::censorRegex)
																		.collect(RegexUtil.orChainRegex());
	static final Matcher censoredWordsMatcher = getMatcher(censoredWordsRegex);
	private static final @RegEx String fullCensorBuffer = "[!@#$%^&*()\\[\\]/=\\-\\\\;',.{}?+|S_:\"\\s]*"; // >= 5
	private static final @RegEx String noSpaceCensorBuffer = "[!@#$%^&*()\\[\\]/=\\-\\\\;',.{}?+|S_:\"]*"; // 3-4
	private static final @RegEx String acronymCensorBuffer = "\\.*"; // <= 2
	private static final Deque<AuditableRestAction<Void>> autoDeleteQueue = new ArrayDeque<>();
	private static ScheduledExecutorService censorScheduler = new ScheduledThreadPoolExecutor(1);

	private static String getCensor(int length) {
		return length <= 2 ? acronymCensorBuffer : (length <= 4 ? noSpaceCensorBuffer : fullCensorBuffer);
	}

	public static void flushAutoDeleteQueue() {
		// try catch ?
		int shutdown = 0;
		for (AuditableRestAction<Void> voidAuditableRestAction : autoDeleteQueue) {
			try {
				voidAuditableRestAction.complete();
				shutdown++;
			} catch (ErrorResponseException | RejectedExecutionException ignored) { }
		}
		System.out.printf("auto deleted %s/%s queued messages\n", shutdown, autoDeleteQueue.size());
	}

	/**
	 * @return word(s) being censored
	 */
	static String logWordCensor(Message message, Matcher matcher) {
		final String messageContent = message.getContentRaw();
		final String[] censoredWords = matcher.reset(messageContent).results().map(MatchResult::group).toArray(String[]::new);
		if (censoredWords.length == 0) {
			return null;
		}
		final AuditableRestAction<Void> delete = message.delete();
		autoDeleteQueue.addFirst(delete);
		delete.and(new GenericRestAction(message.getJDA(), autoDeleteQueue::pop))
			  .queueAfter(1, TimeUnit.HOURS, s -> {}, s -> {});
//					  () -> LOGGER.info("failed to delete already deleted message " + messageContent);
		final String words = String.join(", ", censoredWords);
		autodeleteLogMap.get(message.getGuild().getIdLong())
				.sendMessageEmbeds(new EmbedBuilder()
						.setAuthor(getUserName(message.getAuthor()), message.getJumpUrl(), message.getAuthor().getAvatarUrl())
						.addField("Message", JavaUtil.truncateEnd(messageContent, 1024), false)
						.addField("ID", message.getId(), false)
						.addField("Censored Word" + (censoredWords.length > 1 ? "s" : ""), JavaUtil.truncate(censoredWords, 1024), false)
						.setColor(Color.RED)
						.build()).queue();
		return words;
	}

	private static String censorChainRegex(String words, String joiner) {
		return censorChainRegex(words.split("\\s*,\\s*"), joiner);
	}

	private static String censorChainRegex(String[] words, String joiner) {
		return RegexUtil.orChainRegex(Arrays.stream(words).map(w -> censorRegex(w, joiner)).toArray(String[]::new));
	}

	public static String censorBasicRegex(String word) {
		final String collect = word.chars()
				.mapToObj(c -> c == '+' || c == '*'
						? String.valueOf((char) c)
						: ("[" + (c == ' ' ? "\\s" : (String.valueOf((char) Character.toUpperCase(c))
						+ ((char) Character.toLowerCase(c)))) + "]"))
				.collect(joining(""));
		return collect;
	}

	public static String censorBasicRegexCaseInsensitive(String word) {
		return word;
	}

	public static String censorRegex(String word) {
		return censorRegex(word, getCensor(word.length()));
	}

	public static String censorGeneRegex(String word) {
		return censorGeneRegex(word, getCensor(word.length()));
	}

	private static String censorRegex(String word, String joiner) {
		return notNumberRegex(word.chars()
				.mapToObj(c -> "[" + homoglyphs.getOrDefault((char) c, c == ' ' ? "\\s" : String.valueOf((char) c)) + "]+")
				.map(s -> word.length() <= 2 ? RegexUtil.noLetterEndsRegex(s) : s)
				.collect(joining(joiner)), word.length(), joiner);
	}

	private static String notNumberRegex(final String regex, int wordLength, String joiner) {
		return "((?!(\\d){" + wordLength + "})" + regex + ")";
	}

	private static String censorGeneRegex(String word, String joiner) {
		return word.chars()
				.mapToObj(c -> "[" + homoglyphs.getOrDefault((char) c, c == ' ' ? "\\s" : String.valueOf((char) c)) + "]+")
				.map(s -> word.length() <= 2 ? RegexUtil.noLetterEndsRegex(s) : s)
				.collect(joining(joiner));
	}

}
