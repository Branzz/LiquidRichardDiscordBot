package com.wordpress.brancodes.util;

import com.wordpress.brancodes.messaging.chat.Chats;
import net.dv8tion.jda.api.entities.MessageChannel;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toMap;

public class Images {

	private static final Logger LOGGER = LoggerFactory.getLogger(Images.class);

	// private static final Map<String, ArrayDeque<File>> IMAGE_FILES = Map.of(
	// 		"mgtow", new ArrayDeque<>(List.of(
	// 				new File("")
	// 		)),
	// 		"mgtow2", Stream.of("file1", "file2").map(File::new).collect(Collectors.toCollection(ArrayDeque::new))
	//
	// );
	//
	// private static final Map<String, ArrayDeque<String>> IMAGE_URLS = Map.of(
	// 		"stretch", new ArrayDeque<>(List.of(
	// 				"https://www.default.com"
	// 		))
	// );

	private static final Map<String, Deque<Object>> IMAGES =
			Stream.of("mgtow", "stretches")
				  .collect(toMap(Function.identity(),
								 key -> getFiles(key).collect(toCollection(ArrayDeque::new))));

	// private static final Map<String, Deque<Object>> IMAGES =
	// 		Stream.of("mgtow", "stretches")
	// 			  .collect(toMap(Function.identity(),
	// 							 key -> getDeque(getURLs(key), getFiles(key))));

	private static ArrayDeque<Object> getDeque(Stream<String> urls, Stream<File> files) {
		return Stream.concat(urls, files).collect(toCollection(ArrayDeque::new));
	}

	private static final ClassLoader loader = Thread.currentThread().getContextClassLoader();

	private static Stream<File> getFiles(String directory) {
		try {
			URL path = loader.getResource("images/" + directory);
			return path == null ? Stream.empty() : Arrays.stream(new File(path.getFile()).listFiles());
		} catch (Exception e) {
			LOGGER.info("Failed to get files");
		}
		return Stream.empty();
	}

	private static Stream<String> getURLs(String directory) {
		try (InputStream path = loader.getResourceAsStream("urls/" + directory)) {
			if (path != null)
				return IOUtils.readLines(path, StandardCharsets.UTF_8).stream();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return Stream.empty();
		// Stream<String> urls = null;
		// try {
		// 	urls =
		// 	IOUtils.readLines(Images.class.getClassLoader().getResourceAsStream("/urls/" + directory + ".txt"), StandardCharsets.UTF_8)
		// 		   .stream();
		// } catch (NullPointerException | IOException e) {
		// 	e.printStackTrace();
		// 	urls = Stream.empty();
		// }
	}

	public static void send(final MessageChannel mainChannel, String key, String chat, String fileName) {
		Deque<Object> images = IMAGES.get(key);
		Object nextImage = images.pop();
		images.addLast(nextImage);

		Object val = IMAGES.get(key).pop();
		if (val == null)
			LOGGER.info("Failed to send chat: null file/url");
		else if (val instanceof File)
			sendFile(mainChannel, (File) val, chat, fileName);
		else if (val instanceof String)
			sendURL(mainChannel, (String) val, chat);
	}

	private static void sendFile(final MessageChannel mainChannel, File file, String chat, final String fileName) {
		mainChannel.sendFile(file, fileName + FilenameUtils.getExtension(file.getPath()))
				   .append(chat)
				   .queue();
	}

	private static void sendURL(final MessageChannel mainChannel, String url, String chat) {
		mainChannel.sendMessage(chat + "\n\r" + url)
				   .queue();
	}

	// public static File getFile(String key) {
	// 	ArrayDeque<File> files = IMAGE_FILES.get(key);
	// 	if (files != null) {
	// 		File nextFile = files.pop();
	// 		files.addLast(nextFile);
	// 		return nextFile;
	// 	}
	// 	else
	// 		return null;
	// }
	//
	// public static String getURL(String key) {
	// 	ArrayDeque<String> urls = IMAGE_URLS.get(key);
	// 	if (urls != null) {
	// 		String nextURL = urls.pop();
	// 		urls.addLast(nextURL);
	// 		return nextURL;
	// 	}
	// 	else
	// 		return null;
	//
	// }

}
