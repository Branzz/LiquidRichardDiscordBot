package com.wordpress.brancodes.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.intellij.lang.annotations.Language;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

public class PoolConnection {

	private static final Logger LOGGER = LoggerFactory.getLogger(PoolConnection.class);
	protected static HikariDataSource dataSource;

	public static void begin() {
		HikariConfig hConfig = new HikariConfig();
		hConfig.addDataSourceProperty("dataSourceClassName", "org.sqlite.JDBC");
		hConfig.setJdbcUrl("jdbc:sqlite:dbot.db");
		dataSource = new HikariDataSource(hConfig);
	}

	public static void end() {
		dataSource.close();
	}

	abstract static class Query implements AutoCloseable {

		protected Connection connection;

		public void close() {
			try { if (connection != null) connection.close(); } catch (Exception ignored) { }
		}

		public Connection getConnection() {
			return connection;
		}

		// public abstract void get();

	}

	static class PreparedStatementQuery extends Query implements AutoCloseable {

		private PreparedStatement preparedStatement;
		private ResultSet resultSet;

		public PreparedStatementQuery(@Language("SQLITE-SQL") String prepareStatement) {
			try {
				connection = dataSource.getConnection();
				preparedStatement = connection.prepareStatement(prepareStatement);
			} catch (SQLException e) {
				LOGGER.warn("Error connecting to database");
				e.printStackTrace();
			}
		}

		public PreparedStatement getPreparedStatement() {
			return preparedStatement;
		}

		public ResultSet executeResultSet() throws SQLException {
			return resultSet = preparedStatement.executeQuery();
		}

		public void close() {
			try { if (resultSet != null) resultSet.close(); } catch (Exception ignored) { }
			try { if (preparedStatement != null) preparedStatement.close(); } catch (Exception ignored) { }
			super.close();
		}

	}

	static class ConnectionQuery extends Query implements AutoCloseable {

		private Statement statement;

		public ConnectionQuery() {
			try {
				connection = dataSource.getConnection();
				statement = connection.createStatement();
			} catch (SQLException e) {
				LOGGER.warn("Error connecting to database");
				e.printStackTrace();
			}
		}

		public Statement getStatement() {
			return statement;
		}

		public void close() {
			try { if (statement != null) statement.close(); } catch (Exception ignored) { }
			super.close();
		}

	}
}
