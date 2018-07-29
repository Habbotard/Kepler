package org.alexdev.kepler.dao.mysql;

import org.alexdev.kepler.dao.Storage;
import org.alexdev.kepler.game.player.Player;
import org.alexdev.kepler.game.player.PlayerDetails;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public class CurrencyDao {

    /**
     * Atomically increase credits.
     */
    public static void increaseCredits(Map<PlayerDetails, Integer> playersToSave) {
        Connection conn = null;
        PreparedStatement updateQuery = null;
        PreparedStatement fetchQuery = null;
        ResultSet row = null;

        try {
            conn = Storage.getStorage().getConnection();

            // We disable autocommit to make sure the following queries share the same atomic transaction
            conn.setAutoCommit(false);

            // Increase credits
            updateQuery = Storage.getStorage().prepare("UPDATE users SET credits = credits + ? WHERE id = ?", conn);

            for (var kvp : playersToSave.entrySet()) {
                PlayerDetails playerDetails = kvp.getKey();
                int increaseAmount = kvp.getValue();

                updateQuery.setInt(1, increaseAmount);
                updateQuery.setInt(2, playerDetails.getId());

                updateQuery.addBatch();
            }

            updateQuery.executeBatch();

            for (var kvp : playersToSave.entrySet()) {
                PlayerDetails playerDetails = kvp.getKey();

                // Fetch increased amount
                fetchQuery = Storage.getStorage().prepare("SELECT credits FROM users WHERE id = ?", conn);
                fetchQuery.setInt(1, playerDetails.getId());

                row = fetchQuery.executeQuery();

                // Set amount
                if (row != null && row.next()) {
                    int updatedAmount = row.getInt("credits");
                    playerDetails.setCredits(updatedAmount);
                }
            }

            // Commit these queries
            conn.commit();
        } catch (Exception e) {
            try {
                // Rollback these queries
                conn.rollback();
            } catch(SQLException re) {
                Storage.logError(re);
            }

            Storage.logError(e);
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException ce) {
                Storage.logError(ce);
            }

            Storage.closeSilently(row);
            Storage.closeSilently(updateQuery);
            Storage.closeSilently(fetchQuery);
            Storage.closeSilently(conn);
        }
    }

    /**
     * Atomically increase credits.
     *
     * @param details the player details
     */
    public static void increaseCredits(PlayerDetails details, int amount) {
        Connection conn = null;
        PreparedStatement updateQuery = null;
        PreparedStatement fetchQuery = null;
        ResultSet row = null;

        try {
            conn = Storage.getStorage().getConnection();

            // We disable autocommit to make sure the following queries share the same atomic transaction
            conn.setAutoCommit(false);

            // Increase credits
            updateQuery = Storage.getStorage().prepare("UPDATE users SET credits = credits + ? WHERE id = ?", conn);
            updateQuery.setInt(1, amount);
            updateQuery.setInt(2, details.getId());
            updateQuery.execute();

            // Fetch increased amount
            fetchQuery = Storage.getStorage().prepare("SELECT credits FROM users WHERE id = ?", conn);
            fetchQuery.setInt(1, details.getId());
            row = fetchQuery.executeQuery();

            // Commit these queries
            conn.commit();

            // Set amount
            if (row != null && row.next()) {
                int updatedAmount = row.getInt("credits");
                details.setCredits(updatedAmount);
            }

        } catch (Exception e) {
            try {
                // Rollback these queries
                conn.rollback();
            } catch(SQLException re) {
                Storage.logError(re);
            }

            Storage.logError(e);
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException ce) {
                Storage.logError(ce);
            }

            Storage.closeSilently(row);
            Storage.closeSilently(updateQuery);
            Storage.closeSilently(fetchQuery);
            Storage.closeSilently(conn);
        }
    }

    /**
     * Atomically decrease credits.
     *
     * @param details the player details
     */
    public static void decreaseCredits(PlayerDetails details, int amount) {
        Connection conn = null;
        PreparedStatement updateQuery = null;
        PreparedStatement fetchQuery = null;
        ResultSet row = null;

        try {
            conn = Storage.getStorage().getConnection();

            // We disable autocommit to make sure the following queries share the same atomic transaction
            conn.setAutoCommit(false);

            // Decrease credits
            updateQuery = Storage.getStorage().prepare("UPDATE users SET credits = credits - ? WHERE id = ?", conn);
            updateQuery.setInt(1, amount);
            updateQuery.setInt(2, details.getId());
            updateQuery.execute();

            // Fetch increased amount
            fetchQuery = Storage.getStorage().prepare("SELECT credits FROM users WHERE id = ?", conn);
            fetchQuery.setInt(1, details.getId());
            row = fetchQuery.executeQuery();

            // Commit these queries
            conn.commit();

            // Set amount
            if (row != null && row.next()) {
                int updatedAmount = row.getInt("credits");
                details.setCredits(updatedAmount);
            }

        } catch (Exception e) {
            try {
                // Rollback these queries
                conn.rollback();
            } catch(SQLException re) {
                Storage.logError(re);
            }

            Storage.logError(e);
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException ce) {
                Storage.logError(ce);
            }

            Storage.closeSilently(row);
            Storage.closeSilently(updateQuery);
            Storage.closeSilently(fetchQuery);
            Storage.closeSilently(conn);
        }
    }

    /**
     * Atomically increase tickets.
     *
     * @param details the player details
     */
    public static void increaseTickets(PlayerDetails details, int amount) {
        Connection conn = null;
        PreparedStatement updateQuery = null;
        PreparedStatement fetchQuery = null;
        ResultSet row = null;

        try {
            conn = Storage.getStorage().getConnection();

            // We disable autocommit to make sure the following queries share the same atomic transaction
            conn.setAutoCommit(false);

            // Increase credits
            updateQuery = Storage.getStorage().prepare("UPDATE users SET tickets = tickets + ? WHERE id = ?", conn);
            updateQuery.setInt(1, amount);
            updateQuery.setInt(2, details.getId());
            updateQuery.execute();

            // Fetch increased amount
            fetchQuery = Storage.getStorage().prepare("SELECT tickets FROM users WHERE id = ?", conn);
            fetchQuery.setInt(1, details.getId());
            row = fetchQuery.executeQuery();

            // Commit these queries
            conn.commit();

            // Set amount
            if (row != null && row.next()) {
                int updatedAmount = row.getInt("tickets");
                details.setTickets(updatedAmount);
            }

        } catch (Exception e) {
            try {
                // Rollback these queries
                conn.rollback();
            } catch(SQLException re) {
                Storage.logError(re);
            }

            Storage.logError(e);
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException ce) {
                Storage.logError(ce);
            }

            Storage.closeSilently(row);
            Storage.closeSilently(updateQuery);
            Storage.closeSilently(fetchQuery);
            Storage.closeSilently(conn);
        }
    }

    /**
     * Atomically decrease tickets.
     *
     * @param details the player details
     */
    public static void decreaseTickets(PlayerDetails details, int amount) {
        Connection conn = null;
        PreparedStatement updateQuery = null;
        PreparedStatement fetchQuery = null;
        ResultSet row = null;

        try {
            conn = Storage.getStorage().getConnection();

            // We disable autocommit to make sure the following queries share the same atomic transaction
            conn.setAutoCommit(false);

            // Decrease credits
            updateQuery = Storage.getStorage().prepare("UPDATE users SET tickets = tickets - ? WHERE id = ?", conn);
            updateQuery.setInt(1, amount);
            updateQuery.setInt(2, details.getId());
            updateQuery.execute();

            // Fetch increased amount
            fetchQuery = Storage.getStorage().prepare("SELECT tickets FROM users WHERE id = ?", conn);
            fetchQuery.setInt(1, details.getId());
            row = fetchQuery.executeQuery();

            // Commit these queries
            conn.commit();

            // Set amount
            if (row != null && row.next()) {
                int updatedAmount = row.getInt("tickets");
                details.setTickets(updatedAmount);
            }

        } catch (Exception e) {
            try {
                // Rollback these queries
                conn.rollback();
            } catch(SQLException re) {
                Storage.logError(re);
            }

            Storage.logError(e);
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException ce) {
                Storage.logError(ce);
            }

            Storage.closeSilently(row);
            Storage.closeSilently(updateQuery);
            Storage.closeSilently(fetchQuery);
            Storage.closeSilently(conn);
        }
    }
}
