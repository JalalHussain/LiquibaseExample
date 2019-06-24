package com.barzi.liquibase;

import com.barzi.database.DatabaseUtil;
import com.barzi.database.config.ConfigTag;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.diff.DiffResult;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.DiffToChangeLog;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.resource.FileSystemResourceAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <pre>
 *     This class is Liquibase utility class helps by providing customized ways for using different
 *     functionality provided by Liquibase.
 * </pre>
 *
 * @author barzi
 */
public class LiquibaseUtil {
    private static final Logger logger = LoggerFactory.getLogger(LiquibaseUtil.class);  //Logger instance for logging

    /**
     * <pre>
     *     This method will attempt to read the script file(s)(as configured) and execute the database changes,
     *     if any change file encounters any exception/error the changes will be roll backed.
     * </pre>
     */
    public void integrateChanges() {
        logger.info("Getting connection of the configured database...");
        java.sql.Connection connection = DatabaseUtil.getConnection("target");
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
                    liquibase.forceReleaseLocks();
                    connection.close();
                } catch (Exception exception) {
                    logger.error("Exception encountered while attempting to revert partially integrated changes, please see details ", exception);
                }
            }
        }
    }

    /**
     * <pre>
     *     This method will check for any differences between the target Database and the Reference Database.
     * </pre>
     *
     * @return diffResult Returns an instance of DiffResults.
     */
    public DiffResult fetchDifferences() {
        Liquibase liquibase = null;
        DiffResult diffResult = null;
        Database targetDatabase = null;
        Database referenceDatabase = null;
        logger.info("Getting connection for target and reference databases...");
        java.sql.Connection targetConnection = DatabaseUtil.getConnection("target");
        java.sql.Connection referenceConnection = DatabaseUtil.getConnection("reference");
        logger.info("Connections received");
        try {
            logger.info("Getting database object from connection...");
            targetDatabase = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(targetConnection));
            referenceDatabase = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(referenceConnection));
            logger.info("Database objects received");
            logger.info("Configuring liquibase...");
            liquibase = new Liquibase("", new FileSystemResourceAccessor(), referenceDatabase);
            logger.info("Liquibase configured");
            logger.info("fetching differences...");
            diffResult = liquibase.diff(referenceDatabase, targetDatabase, new CompareControl());
            logger.info("fetched differences...");
            logger.info("Differences:");
            logger.info("________________________________________________________________________________________");
            new DiffToChangeLog(diffResult, new DiffOutputControl()).print(System.out);
            logger.info("________________________________________________________________________________________");
            logger.info(new DiffToChangeLog(diffResult, new DiffOutputControl()).toString());
            logger.info("________________________________________________________________________________________");
        } catch (Exception exception) {
            logger.error("Exception encountered attempting to extract differences, please see details ", exception);
        } finally {
            try {
                liquibase.forceReleaseLocks();
                referenceConnection.close();
                targetConnection.close();
            } catch (Exception exception) {
                logger.error("Exception encountered while closing connections, please see details ", exception);
            }
        }

        return diffResult;
    }
}
