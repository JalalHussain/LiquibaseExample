package com.barzi.database;

import com.barzi.database.config.ConfigTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * <pre>
 *     This class is Database utility class helps by providing customized ways for using different
 *     functionality provided by underlying database Database.
 * </pre>
 *
 * @author barzi
 */
public class DatabaseUtil {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseUtil.class); //Logger instance for logging.

    /**
     * <pre>
     *     This method will check if the connection is NULL or connection is closed then a new connection will be created else
     *     the same connection object will be returned.
     * </pre>
     *
     * @return connection Returns a connection object.
     */
    public static Connection getConnection(String database) {
        Connection connection = null;
        try {

            logger.info("Creating new connection...");
            connection = DriverManager.getConnection(ConfigTag.getProperty(database + ".database.url"),
                    ConfigTag.getProperty(database + ".database.user"),
                    ConfigTag.getProperty(database + ".database.password"));
            logger.info("Created new connection for " + database + " database ");

        } catch (Exception e) {
            logger.error("Exception encountered while created new connection, please see details ", e);
        }

        return connection;
    }
}
