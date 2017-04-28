package org.alexdev.roseau.dao.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.alexdev.roseau.dao.MessengerDao;
import org.alexdev.roseau.game.messenger.MessengerUser;
import org.alexdev.roseau.log.Log;

import com.google.common.collect.Lists;

public class MySQLMessengerDao implements MessengerDao {

	private MySQLDao dao;

	public MySQLMessengerDao(MySQLDao dao) {
		this.dao = dao;
	}

	@Override
	public List<MessengerUser> getFriends(int userId) {

		List<MessengerUser> friends = Lists.newArrayList();

		Connection sqlConnection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;

		try {

			sqlConnection = this.dao.getStorage().getConnection();
			preparedStatement = this.dao.getStorage().prepare("SELECT * FROM messenger_friendships WHERE (sender = " + userId + ") OR (receiver = " + userId + ")", sqlConnection);
			resultSet = preparedStatement.executeQuery();

			while (resultSet.next()) {

				MessengerUser friend = null;

				if (resultSet.getInt("sender") != userId) {
					friend = new MessengerUser(resultSet.getInt("sender"));
				} else {
					friend = new MessengerUser(resultSet.getInt("receiver"));
				}

				friends.add(friend);
			}

		} catch (Exception e) {
			Log.exception(e);
		} finally {
			Storage.closeSilently(resultSet);
			Storage.closeSilently(preparedStatement);
			Storage.closeSilently(sqlConnection);
		}

		return friends;
	}


	@Override
	public List<MessengerUser> getRequests(int userId) {

		List<MessengerUser> users = Lists.newArrayList();

		Connection sqlConnection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;

		try {

			sqlConnection = this.dao.getStorage().getConnection();
			preparedStatement = this.dao.getStorage().prepare("SELECT * FROM messenger_requests WHERE to_id = " + userId, sqlConnection);
			resultSet = preparedStatement.executeQuery();

			while (resultSet.next()) {
				users.add(new MessengerUser(resultSet.getInt("from_id")));
			}

		} catch (Exception e) {
			Log.exception(e);
		} finally {
			Storage.closeSilently(resultSet);
			Storage.closeSilently(preparedStatement);
			Storage.closeSilently(sqlConnection);
		}

		return users;
	}

	@Override
	public boolean newRequest(int fromId, int toId) {

		Connection sqlConnection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;

		if (!this.requestExists(fromId, toId)) {

			try {

				sqlConnection = this.dao.getStorage().getConnection();
				preparedStatement = dao.getStorage().prepare("INSERT INTO messenger_requests (to_id, from_id) VALUES (?, ?)", sqlConnection);
				preparedStatement.setInt(1, toId);
				preparedStatement.setInt(2, fromId);
				preparedStatement.execute();

				return true;

			} catch (SQLException e) {
				Log.exception(e);
			} finally {
				Storage.closeSilently(resultSet);
				Storage.closeSilently(preparedStatement);
				Storage.closeSilently(sqlConnection);
			}
		}

		return false;
	}

	@Override
	public boolean requestExists(int fromId, int toId) {

		Connection sqlConnection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;

		try {

			sqlConnection = this.dao.getStorage().getConnection();
			preparedStatement = this.dao.getStorage().prepare("SELECT * FROM messenger_requests WHERE (to_id = '" + toId + "') AND (from_id = '" + fromId + "') OR (from_id = '" + toId + "') AND (to_id = '" + fromId + "')", sqlConnection);
			resultSet = preparedStatement.executeQuery();

			if (resultSet.next()) {
				return true;
			}

		} catch (Exception e) {
			Log.exception(e);
		} finally {
			Storage.closeSilently(resultSet);
			Storage.closeSilently(preparedStatement);
			Storage.closeSilently(sqlConnection);
		}

		return false;
	}

	@Override
	public boolean removeRequest(int fromId, int toId) {
		return this.dao.getStorage().execute("DELETE FROM messenger_requests WHERE from_id = " + fromId + " AND to_id = " + toId);
	}

	@Override
	public boolean removeFriend(int friendId, int userId) {
		return this.dao.getStorage().execute("DELETE FROM messenger_friendships WHERE (sender = " + userId + " AND receiver = " + friendId + ") OR (receiver = " + userId + " AND sender = " + friendId + ")");
	}

	@Override
	public boolean newFriend(int sender, int receiver) {

		Connection sqlConnection = null;
		PreparedStatement preparedStatement = null;

		try {

			sqlConnection = this.dao.getStorage().getConnection();
			preparedStatement = dao.getStorage().prepare("INSERT INTO messenger_friendships (sender, receiver) VALUES (?, ?)", sqlConnection);
			preparedStatement.setInt(1, sender);
			preparedStatement.setInt(2, receiver);
			preparedStatement.executeUpdate();

		} catch (SQLException e) {
			Log.exception(e);
		} finally {
			Storage.closeSilently(preparedStatement);
			Storage.closeSilently(sqlConnection);
		}

		return false;
	}

	public MySQLDao getDao() {
		return dao;
	}
}