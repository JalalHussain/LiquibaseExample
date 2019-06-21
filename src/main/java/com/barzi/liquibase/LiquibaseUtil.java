package com.barzi.liquibase;

import com.barzi.database.DatabaseUtil;
import com.barzi.database.config.ConfigTag;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.resource.FileSystemResourceAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class LiquibaseUtil {
    private static final Logger logger = LoggerFactory.getLogger(LiquibaseUtil.class);  //Logger instance for logging

    /**
     * <pre>
     *     This method will attempt to read the script file(s)(as configured) and execute the database changes,
     *     if any change file encounters any exception/error the changes will be roll backed.
     * </pre>
     */
    public void attempt() {
        logger.info("Getting connection of the configured database...");
        java.sql.Connection connection = DatabaseUtil.getConnection();
        logger.info("Connection received");
        Liquibase liquibase = null;
        boolean failure = false;
        JdbcConnection jdbcConn = null;
        try {
            jdbcConn = new JdbcConnection(connection);
            logger.info("Getting database object from connection...");
            Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(jdbcConn);
            logger.info("Database object received");
            logger.info("Configuring liquibase...");
            liquibase = new Liquibase(ConfigTag.getProperty("changelog.file.path", "db.changelog.xml"), new FileSystemResourceAccessor(), database);
            logger.info("Liquibase configured");
            logger.info("Integrating changes...");
            liquibase.update("");
            logger.info("Changes integrated");
        } catch (DatabaseException databaseException) {
            logger.error("Exception encountered while attempting to integrate change logs, please see details ", databaseException);
            failure = true;
        } catch (LiquibaseException liquibaseException) {
            logger.error("Exception encountered while attempting to integrate change logs, please see details ", liquibaseException);
            failure = true;
        } catch (Exception exception) {
            logger.error("Exception encountered while attempting to integrate change logs, please see details ", exception);
            failure = true;
        } finally {
            if (connection != null) {
                try {
                    if (failure) {
                        logger.info("Reverting partially integrated changes...");
                        liquibase.rollback(Integer.parseInt(ConfigTag.getProperty("liquibase.rollbackto")), "");
                        logger.info("Reverted partially integrated changes");
                    }
                    connection.close();
                } catch (Exception sqlException) {
                    logger.error("Exception encountered while attempting to revert partially integrated changes, please see details ", sqlException);
                }
            }
        }
    }
}
