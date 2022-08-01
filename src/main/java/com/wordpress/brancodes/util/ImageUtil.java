package com.wordpress.brancodes.util;

import com.wordpress.brancodes.main.Main;
import com.wordpress.brancodes.messaging.reactions.Reactions;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.MessageEmbed.AuthorInfo;
import net.dv8tion.jda.api.entities.MessageEmbed.ImageInfo;
import net.dv8tion.jda.api.entities.MessageEmbed.Thumbnail;
import net.dv8tion.jda.internal.JDAImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;

import static java.util.Optional.*;
import static java.util.Optional.ofNullable;

public class ImageUtil {

	private static final Logger LOGGER = LoggerFactory.getLogger(ImageUtil.class);

	private static final ClassLoader loader = Thread.currentThread().getContextClassLoader();

	public static void sendSpeechBubbleImage(Message message, Attachment image) {
		try (InputStream inputStream = image.retrieveInputStream().get()) {
			BufferedImage bufImage = ImageIO.read(inputStream);
			Graphics2D graphics = bufImage.createGraphics();
			graphics.setColor(new Color(54, 57, 63, 0));
			graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC, 0F));
			final int height = bufImage.getHeight();
			final int width = bufImage.getWidth();
			double ovalHeightRatio = 3.0 / 8;
			double ovalWidthRatio = 5.0 / 4;
			graphics.fillOval(-width / 8, (int) (-height * ovalHeightRatio + (ovalHeightRatio * height / 2) * Math.sqrt(1 - (1 / (ovalWidthRatio * ovalWidthRatio)))),
					(int) (width * ovalWidthRatio), (int) (ovalHeightRatio * height));
			graphics.fillPolygon(new Polygon(new int[] { width / 2, width / 2 + width / 10, width / 2 + width / 3 },
					new int[] { height / 4, 0, 0 }, 3));
			File output = new File(loader.getResource("image_processing").getPath() + "/" + (
					image.isSpoiler() ? "SPOILER_SpeechBubble_" + image.getFileName().substring(8)
							: "SpeechBubble_" + image.getFileName()));
			String fileExtension = image.getFileExtension();
			if (fileExtension == null)
				fileExtension = "";
			ImageIO.write(bufImage, fileExtension, output);
			message.getChannel().sendFile(output)
					.queue(s -> { if (!output.delete())
						LOGGER.error("file not deleted!"); });
		} catch (InterruptedException | ExecutionException | IOException e) {
			e.printStackTrace();
		}
	}

	public static void sendSpeechBubbleVideo(Message message, Attachment video) {

	}

	@NotNull
	public static Optional<Attachment> getFirstAttachment(Message message) {
		return getFirstImage(message)
			.or(() -> getFirstEmbedImage(message))
			.or(() -> getFirstSticker(message))
			.or(() -> getFirstEmote(message));
	}

	@NotNull
	public static Optional<Attachment> getFirstImage(Message message) {
		return message.getAttachments().stream().filter(Attachment::isImage).findFirst();
	}

	@NotNull
	public static Optional<Attachment> getFirstSticker(Message message) {
		return message.getStickers().stream().findFirst().map(sticker -> ImageUtil.URLToAttachment(sticker.getIconUrl()));
	}

	@Nullable
	private static ImageInfo thumbnailToImageInfo(Thumbnail thumbnail) {
		return thumbnail == null ? null : new ImageInfo(thumbnail.getUrl(), thumbnail.getProxyUrl(), thumbnail.getWidth(), thumbnail.getHeight());
	}

	@Nullable
	private static ImageInfo authorToImageInfo(AuthorInfo authorInfo) {
		return authorInfo == null ? null : new ImageInfo(authorInfo.getProxyIconUrl(), authorInfo.getProxyIconUrl(), 0, 0);
	}
	@NotNull
	public static Optional<Attachment> getFirstEmbedImage(Message message) {
		return message.getEmbeds().stream()
				.map(embed -> embed.getImage() != null ? embed.getImage()
									  : embed.getAuthor() != null ? authorToImageInfo(embed.getAuthor())
												: thumbnailToImageInfo(embed.getThumbnail()))
				.filter(Objects::nonNull)
				.findFirst()
				.map(imageInfo -> {
					String url = imageInfo.getUrl();
					String name = "name";
					if (url != null)
						name = url.substring(url.lastIndexOf('/') + 1);
					return new Attachment(1L, url, imageInfo.getProxyUrl(), name, null, "", 0, imageInfo.getHeight(), imageInfo.getWidth(), false, (JDAImpl) Main.getBot().getJDA());
				});
	}

	public static Optional<Attachment> getFirstEmote(Message message) {
		// Optional.ofNullable(message.getEmotes().get(0))
		return message.getMentions().getCustomEmojis().stream().findFirst().map(emote -> URLToAttachment(emote.getImageUrl()));
	}

	private static final Matcher jumpMessageMatcher = Message.JUMP_URL_PATTERN.matcher("");

	public static synchronized Optional<Attachment> getFirstMessageLink(Message message) {
			return jumpMessageMatcher.reset(message.getContentRaw()).find() ?
						  ofNullable(message.getJDA().getGuildById(jumpMessageMatcher.group("guild")))
								.map(m -> m.getTextChannelById(jumpMessageMatcher.group("channel")))
								.map(c -> c.retrieveMessageById(jumpMessageMatcher.group("message")).complete())
								.flatMap(ImageUtil::getFirstAttachment)
					  : Optional.empty();
	}

	@NotNull
	public static Optional<Message.Attachment> getMentionedMemberPFP(Message message) {
		return message.getMentions().getMembers()
					  .stream()
					  .filter(member -> !message.isFromGuild()
										|| (message.getReferencedMessage() == null // dont include @ of referenced user
												|| (message.getReferencedMessage().getAuthor().getIdLong() != member.getUser().getIdLong())))
					  .findFirst()
					  .map(member -> ImageUtil.URLToAttachment(member.getEffectiveAvatarUrl()));
	}

	@NotNull
	public static Optional<Attachment> searchFirstAttachment(Message message) {
		AtomicReference<Attachment> optionalAttachment = new AtomicReference<>();
		try {
			message.getChannel().getIterableHistory().takeUntilAsync(10, m ->
				  Reactions.presentOrElseReturnStatus(getFirstAttachment(m), optionalAttachment::set)).get();
		}
		catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		return of(optionalAttachment.get());
	}

	public static Attachment URLToAttachment(String URL) {
		String name = URL.substring(URL.lastIndexOf('/') + 1);
		return new Attachment(1L, URL, URL, name, null, "", 0, 100, 100, false, (JDAImpl) Main.getBot().getJDA());
	}

	public static <T> Optional<T> booleanToOptional(boolean exists, Optional<T> optional) {
		return exists ? optional : Optional.empty();
	}

}
