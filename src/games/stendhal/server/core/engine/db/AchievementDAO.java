package games.stendhal.server.core.engine.db;

import games.stendhal.server.core.events.achievements.Achievement;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import marauroa.server.db.DBTransaction;
import marauroa.server.db.TransactionPool;
/**
 * DAO to handle achievements for the stendhal website
 * @author madmetzger
 *
 */
public class AchievementDAO {
	
	/**
	 * logs a reached achievement into the database
	 *
	 * @param achievementId
	 * @param playerName
	 * @throws SQLException
	 */
	public void saveReachedAchievement(Integer achievementId, String playerName) throws SQLException {
		DBTransaction transaction = TransactionPool.get().beginWork();
		saveReachedAchievement(achievementId, playerName, transaction);
		TransactionPool.get().commit(transaction);
	}
	
	/**
	 * logs a reached achievement into the database
	 *
	 * @param achievementId
	 * @param playerName
	 * @param transaction
	 * @throws SQLException
	 */
	public void saveReachedAchievement(Integer achievementId, String playerName, DBTransaction transaction) throws SQLException {
		String query  = "INSERT INTO reached_achievement " +
						"(charname, achievement_id) VALUES" +
						"('[charname]','[achievement_id]');";
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("charname", playerName);
		parameters.put("achievement_id", achievementId);
		transaction.execute(query, parameters);
	}

	/**
	 * Saves the base data of an achievement
	 *
	 * @param achievement Achievement to save
	 * @return the id of the stored achievement
	 * @throws SQLException
	 */
	public int saveAchievement(Achievement achievement) throws SQLException {
		DBTransaction transaction = TransactionPool.get().beginWork();
		int achievementId = saveAchievement(achievement, transaction);
		TransactionPool.get().commit(transaction);
		return achievementId;
	}

	/**
	 * Saves the base data of an achievement
	 *
	 * @param achievement Achievement to save
	 * @param transaction a database transaction to execute the save operation in
	 * @return the id of the stored achievement
	 * @throws SQLException
	 */
	public int saveAchievement(Achievement achievement,
			DBTransaction transaction) throws SQLException {
		int achievementId = 0;
		String query = 	"INSERT INTO achievement " +
						"(identifier, title, category, description, base_score) VALUES " +
						"('[identifier]','[title]','[category]', '[description]', [base_score])";
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("identifier", achievement.getIdentifier());
		parameters.put("title", achievement.getTitle());
		parameters.put("category", achievement.getCategory().toString());
		parameters.put("description", achievement.getDescription());
		parameters.put("base_score", achievement.getBaseScore());
		transaction.execute(query, parameters);
		achievementId = transaction.getLastInsertId("achievement", "id");
		return achievementId;
	}
	
	/**
	 * Updates the achievement with the given id
	 *
	 * @param id
	 * @param achievement
	 * @throws SQLException 
	 */
	public void updateAchievement(Integer id, Achievement achievement) throws SQLException {
		DBTransaction transaction = TransactionPool.get().beginWork();
		updateAchievement(id, achievement, transaction);
		TransactionPool.get().commit(transaction);
	}

	/**
	 * Updates the achievement with the given id
	 *
	 * @param id
	 * @param achievement
	 * @param transaction
	 * @throws SQLException 
	 */
	public void updateAchievement(Integer id, Achievement achievement,
			DBTransaction transaction) throws SQLException {
		String query = "UPDATE achievement SET " +
						"identifier='[identifier]', " +
						"title='[title]', " +
						"category = '[category]', " +
						"description = '[description]', " +
						"base_score=[base_score] " +
						"WHERE id = [id];";
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("identifier", achievement.getIdentifier());
		parameters.put("title", achievement.getTitle());
		parameters.put("category", achievement.getCategory().toString());
		parameters.put("description", achievement.getDescription());
		parameters.put("base_score", achievement.getBaseScore());
		parameters.put("id", id);
		transaction.execute(query, parameters);
	}

	/**
	 * Loads a map from achievement identifier to database serial
	 *
	 * @return map with key identifier string and value database id
	 * @throws SQLException
	 */
	public Map<String, Integer> loadIdentifierIdPairs() throws SQLException {
		DBTransaction transaction = TransactionPool.get().beginWork();
		Map<String, Integer> map = loadIdentifierIdPairs(transaction);;
		TransactionPool.get().commit(transaction);
		return map;
	}

	/**
	 * Loads a map from achievement identifier to database serial
	 *
	 * @param transaction
	 * @return map with key identifier string and value database id
	 * @throws SQLException
	 */
	public Map<String, Integer> loadIdentifierIdPairs(DBTransaction transaction) throws SQLException {
		Map<String, Integer> map = new HashMap<String, Integer>();
		String query = "SELECT identifier, id FROM achievement;";
		ResultSet set = transaction.query(query, new HashMap<String, Object>());
		while (set.next()) {
			String identifier = set.getString("identifier");
			Integer id = set.getInt("id");
			map.put(identifier, id);
		}
		return map;
	}
	
	/**
	 * Loads all achievements a player has reached
	 * @param playerName
	 * @return set identifiers of achievements reached by playerName
	 * @throws SQLException 
	 */
	public Set<String> loadAllReachedAchievementsOfPlayer(String playerName) throws SQLException {
		DBTransaction transaction = TransactionPool.get().beginWork();
		Set<String> set = loadAllReachedAchievementsOfPlayer(playerName, transaction);
		TransactionPool.get().commit(transaction);
		return set;
	}
	
	/**
	 * Loads all achievements a player has reached
	 * @param playerName
	 * @param transaction
	 * @return set identifiers of achievements reached by playerName
	 * @throws SQLException 
	 */
	public Set<String> loadAllReachedAchievementsOfPlayer(String playerName, DBTransaction transaction) throws SQLException {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("playername", playerName);
		String query = "SELECT identifier FROM achievement a JOIN reached_achievement ra ON ra.achievement_id = a.id WHERE ra.charname = '[playername]';";
		ResultSet resultSet = transaction.query(query, params);
		Set<String> identifiers = new HashSet<String>();
		while(resultSet.next()) {
			identifiers.add(resultSet.getString(1));
		}
		return identifiers;
	}

}
