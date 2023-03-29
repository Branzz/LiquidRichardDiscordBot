package com.wordpress.brancodes.database;

import com.wordpress.brancodes.messaging.PreparedMessages;
import com.wordpress.brancodes.messaging.reactions.ReactionManager;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Users  : DM back proper case                 | dmcase (userid,          / contains userid
 * Users  : DM back proper case with numbers	|                 numbers) / numbers is true
 * Server : Users  : are a mod               	| mods (guildid, userid)
 * Server : Emojis : yawn, thumbsup,            | emojis (guildid, default, overridden)
 * Server : Main channel						| main_channel (guildid, main)
 * Server : Ignored channels					| ignored_channels (guildid, ignored)
 */
public class DataBase {

	private static final Logger LOGGER = LoggerFactory.getLogger(DataBase.class);

	// public static void initializeTables() {
	// 	try (PoolConnection.ConnectionQuery connectionQuery = new PoolConnection.ConnectionQuery()) {
	// 		LOGGER.info("Connected to database");
	// 		Statement statement = connectionQuery.getStatement();
	// 		final String[] statements = new String[] {
	// 			"CREATE TABLE IF NOT EXISTS users (`id` INTEGER PRIMARY KEY, dmcase boolean, dmnumbers boolean, relationship INTEGER);",
	// 			"CREATE TABLE IF NOT EXISTS guilds (`id` INTEGER PRIMARY KEY, main_channel INTEGER,"
	// 			+ "FOREIGN KEY (`id`) REFERENCES emojis (guild_id));",
	// 			"CREATE TABLE IF NOT EXISTS ignored_channels (channel_id INTEGER, guild_id INTEGER,"
	// 			+ "FOREIGN KEY (guild_id) REFERENCES guilds (`id`));",
	// 			"CREATE TABLE IF NOT EXISTS channels (`id` INTEGER,"
	// 			+ "FOREIGN KEY (`id`) REFERENCES ignored_channels (channel_id));",
	// 			"CREATE TABLE IF NOT EXISTS emojis (guild_id INTEGER PRIMARY KEY, `id` INTEGER, replaced_name varchar(255));",
	// 			"CREATE TABLE IF NOT EXISTS mods (user_id INTEGER, guild_id INTEGER,"
	// 			+ "FOREIGN KEY (user_id) REFERENCES users (`id`),"
	// 			+ "FOREIGN KEY (guild_id) REFERENCES guilds (`id`));",
	// 		};
	// 		DatabaseMetaData dm = connectionQuery.getConnection().getMetaData();
	// 		// TODO add servers that it has now joined
	// 		LOGGER.info("Driver name: {} Driver version: {} Product name: {} Product version: {} "
	// 					+ dm.getDriverName(), dm.getDriverVersion(), dm.getDatabaseProductName(), dm.getDatabaseProductVersion());
	// 	} catch (SQLException e) {
	// 		LOGGER.warn("Error connecting to database");
	// 		e.printStackTrace();
	// 	}
	// }

	// private static Data updateDatabase(String sql) {
	// 	try (PoolConnection.PreparedStatementQuery preparedStatementQuery = new PoolConnection.PreparedStatementQuery("?")) {
	//
	// 	}
	// 	return null;
	// }


	// private final static Set<Long> cryUniMods = Set.of(289894216354758657L, 850200778740072458L, 841201063637549076L);
	// guildID == 929974932417437726L && (cryUniMods.contains(userID))
	/**
	 * Owner set with command: In server, "p, Look. Listen To <@ID>."
	 */
	public static Data<Boolean> userIsMod(final Long userID, final Long guildID) {
		return new Data<>(
			(guildID != null && userID != null)
			&& (guildID == ReactionManager.GUILD_C
				&& (userID == 1006323394997919774L || userID == 956135678893252628L
					|| userID == 928336417514479646L || userID == 936433911486087208L)
			|| guildID == 973797632436760606L && (userID == 1006323394997919774L || userID == 445655234308603914L)),
			guildID);
		// return new Data<>(false);
		// if (guildID != null && userID != null)
		// 	try (PoolConnection.PreparedStatementQuery preparedStatementQuery =
		// 			 new PoolConnection.PreparedStatementQuery("SELECT * FROM mods WHERE users.id = ? AND mods.guild_id = ?")) {
		// 	PreparedStatement query = preparedStatementQuery.getPreparedStatement();
		// 	query.setLong(1, userID);
		// 	query.setLong(2, guildID);
		// 	return new Data<Boolean>(preparedStatementQuery.executeResultSet().next());
		// } catch (SQLException e) {
		// 	LOGGER.warn("Error establishing database query");
		// 	// e.printStackTrace();
		// }
		// return new Data<Boolean>(false);
	}

	public static Map<Long, Long> guildAutoDeleteChannelMap = Map.of(
			// 907042440924528662L, 920653763130310706L
			// 973797632436760606L, 1001374877539893298L
			1043398817816526878L, 1068637316618404040L
	);

	public static Data<Long> autodeleteChannelId(@Nullable final Long guildID) {
		return new Data<>(guildAutoDeleteChannelMap.get(guildID));
	}

	public static Data<Long> addMod(@Nullable final Long guildID, @Nullable final Long userID, final String userName) {
		return null;
	// 	if (guildID != null && userID != null && !userIsMod(guildID, userID).get())
	// 		try (PoolConnection.PreparedStatementQuery preparedStatementQuery
	// 					 = new PoolConnection.PreparedStatementQuery("INSERT INTO mods (user_id, guild_id) VALUES(?, ?)")) {
	// 			PreparedStatement query = preparedStatementQuery.getPreparedStatement();
	// 			query.setLong(1, userID);
	// 			query.setLong(2, guildID);
	// 			preparedStatementQuery.executeResultSet();
	// 			return new Data<>(null, guildID);
	// 		} catch (SQLException e) {
	// 			LOGGER.warn("Error establishing database query");
	// 			// e.printStackTrace();
	// 			return new Data<>(null, "Data Base Error.");
	// 		}
	// 	else {
	// 		if (guildID == null || userID == null)
	// 			return new Data<>(null, PreparedMessages.getMessage(guildID, "missing").replaceAll("\\{}",
	// 					guildID == null ? userID == null ? "Guild And User" : "Guild" : "User"));
	// 		else
	// 			return new Data<>(null, PreparedMessages.getMessage(guildID, "userNotA"));
	// 	}
	}

	public static Data<Long> removeMod(@Nullable final Long guildID, @Nullable final Long userID) {
		return new Data<Long>(null, "remove mod");
		// if (guildID != null && userID != null && !userIsMod(guildID, userID).get())
		// 	try (PoolConnection.PreparedStatementQuery preparedStatementQuery
		// 				 = new PoolConnection.PreparedStatementQuery("DELETE INTO mods (user_id, guild_id) VALUES(?, ?)")) { // TODO remove mod
		// 		PreparedStatement query = preparedStatementQuery.getPreparedStatement();
		// 		query.setLong(1, userID);
		// 		query.setLong(2, guildID);
		// 		preparedStatementQuery.executeResultSet();
		// 		return new Data<>(null, guildID);
		// 	} catch (SQLException e) {
		// 		LOGGER.warn("Error establishing database query");
		// 		// e.printStackTrace();
		// 		return new Data<>(null, "Data Base Error.");
		// 	}
		// else {
		// 	if (guildID == null || userID == null)
		// 		return new Data<>(null, PreparedMessages.getMessage(guildID, "missing").replaceAll("\\{}",
		// 				guildID == null ? userID == null ? "Guild And User" : "Guild" : "User"));
		// 	else
		// 		return new Data<>(null, PreparedMessages.getMessage(guildID, "userAlreadyA"));
		// }
	}

	public static Data<Collection<String>> getMods(final long guildID) {
		Collection<String> mods = new ArrayList<>();
		// if (mods.isEmpty())
		// 	mods.add("No Moderators");
		return new Data<>(mods);
	}

	public static boolean respondToBotPrivate() {
		return false;
	}

	public enum UserDMLevel {
		NoDMProperCase, DMProperCaseNoNumbers, DMProperCaseNumbers;
	}

	/**
	 * User set with command: In DM, "Speak To Me, p."
	 */
	public static UserDMLevel userDMsProperCase(final Long userID) {
		return UserDMLevel.NoDMProperCase;
	}

	public static void addUserDMsProperCase(final Long userID) {

	}

	// /**
	//  * User set with command: In DM, "No Numbers, p."
	//  */
	// public static boolean userDMsProperCaseWithNumbers(final User author) {
	// 	return false;
	// }

	public static void addUserDMsProperCaseWithNumbers(final Long userID) {

	}

	/**
	 * Owner set with command: In server, !p :default_name: :overridden:
	 */
	public static String getEmoji(final Long guildID, String defaultEmojiName) {
		// not guild == null AND: if guild OR emoji not exist, return defaultEmojiName
		return "<:Thumbs_Up:856453032317681674>";
	}

	public static void setEmoji(final Long guildID, String defaultEmojiName, String overriddenName) {

	}

	public static void resetEmoji(final Long guildID, String defaultEmojiName, String overriddenName) {

	}

	/**
	 * guild table
	 */
	public static void addGuild(final Long guildID) {
	}

	public static void removeGuild(final Long guildID) {
	}

	private static final Map<Long, Long> guildMainChannel = new HashMap<>();

	public static Data<Long> getMainChannel(final Long guildID) {
		return new Data<>(guildMainChannel.get(guildID));
	}

	public static Data<Long> setMainChannel(@Nullable final Long guildID, @Nullable final Long channelID) {
		if (guildID!= null && channelID != null) {
			guildMainChannel.put(guildID, channelID);
			return new Data<Long>(null, guildID);
		}
		return new Data<>();
	}

	public static Data<Boolean> respondToBots(final Long guildID) {

		return new Data<Boolean>(true);
	}

	public static class Data<T> {
		private T value;
		private final String feedback;

		public Data() {
			this.feedback = PreparedMessages.getMessage("positive");
		}

		public Data(final T value) {
			this.value = value;
			this.feedback = PreparedMessages.getMessage("positive");
		}

		public Data(final T value, final Long guildID) {
			this.value = value;
			this.feedback = PreparedMessages.getMessage(guildID, "positive");
		}

		public Data(T value, final String feedback) {
			this.value = value;
			this.feedback = feedback;
		}

		public T get() {
			return value;
		}

		public String getFeedback() {
			return feedback;
		}

	}

}
