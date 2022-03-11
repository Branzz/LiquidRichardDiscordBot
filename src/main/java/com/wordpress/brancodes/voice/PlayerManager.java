package com.wordpress.brancodes.voice;


import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Guild;
import org.json.simple.JSONObject;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class PlayerManager {

	private static final Map<Long, GuildMusicManager> musicManagers;
	private static final AudioPlayerManager audioPlayerManager;

	static {
		musicManagers = new HashMap<>();
		audioPlayerManager = new DefaultAudioPlayerManager();

		AudioSourceManagers.registerRemoteSources(audioPlayerManager);
		AudioSourceManagers.registerLocalSource(audioPlayerManager);
	}

	public static GuildMusicManager getMusicManager(Guild guild) {
		return musicManagers.computeIfAbsent(guild.getIdLong(), (guildId) -> {
			final GuildMusicManager guildMusicManager = new GuildMusicManager(audioPlayerManager);
			guild.getAudioManager().setSendingHandler(guildMusicManager.getSendHandler());
			return guildMusicManager;
		});
	}
	private static final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();


	public static void loadAndPlay(Guild guild, String trackFileName) {
		URL path = contextClassLoader.getResource(trackFileName);
		if (path == null) {
			System.out.println("couldn't find audio file");
			return;
		}
		final GuildMusicManager musicManager = getMusicManager(guild);
		audioPlayerManager.loadItemOrdered(musicManager, path.getPath(), new AudioLoadResultHandlerImpl(musicManager));
	}

	private static class AudioLoadResultHandlerImpl implements AudioLoadResultHandler {

		private final GuildMusicManager musicManager;

		public AudioLoadResultHandlerImpl(final GuildMusicManager musicManager) {
			this.musicManager = musicManager;
		}

		@Override public void trackLoaded(final AudioTrack track) {
			musicManager.scheduler.queue(track);
		}

		@Override public void playlistLoaded(final AudioPlaylist playlist) {
			for (AudioTrack track : playlist.getTracks()) {
				musicManager.scheduler.queue(track);
			}
		}
		@Override public void noMatches() { }
		@Override public void loadFailed(final FriendlyException exception) { }

	}
}
