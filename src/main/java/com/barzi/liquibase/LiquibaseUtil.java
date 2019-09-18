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

import java.sql.Connection;

/**
 * <pre>
 *     This class is Liquibase utility class; helps providing customized ways for using different
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
     *     if any change file encounters any exception/error all changes will be roll backed.
     * </pre>
     */
    public void integrateChanges() {
        logger.info("Getting connection of the configured database...");
        java.sql.Connection connection = DatabaseUtil.getConnection("target");
        logger.info("Connection received");
        Liquibase liquibase = null;
        boolean failed = false;
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
            failed = true;
        } catch (LiquibaseException liquibaseException) {
            logger.error("Exception encountered while attempting to integrate change logs, please see details ", liquibaseException);
            failed = true;
        } catch (Exception exception) {
            logger.error("Exception encountered while attempting to integrate change logs, please see details ", exception);
            failed = true;
        } finally {
            close(connection, liquibase, failed);
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
                close(targetConnection, liquibase, false);
            } catch (Exception exception) {
                logger.error("Exception encountered while closing connections, please see details ", exception);
            }
        }

        return diffResult;
    }

    /**
     * <pre>
     *     This method will demonstrate how can preconditions in liquibase be utilized.
     *     There are several reasons to use preconditions, including:
     *
     *     <ul>
     *
     *      <li>Document what assumptions the writers of the changelog had when creating it. </li>
     *      <li>Enforce that those assumptions are not violated by users running the changelog</li>
     *      <li>Perform data checks before performing an unrecoverable change such as drop_Table</li>
     *      <li>Control what changesets are run and not run based on the state of the database</li>
     *
     *      Reference: Liquibase documentation.
     *    </ul>
     *
     * </pre>
     */
    public void preConditions() {

        logger.info("Getting connection of the configured database...");
        java.sql.Connection connection = DatabaseUtil.getConnection("target");
        logger.info("Connection received");
        Liquibase liquibase = null;
        boolean failed = false;
        JdbcConnection jdbcConn = null;
        try {
            jdbcConn = new JdbcConnection(connection);
            logger.info("Getting database object from connection...");
            Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(jdbcConn);
            logger.info("Database object received");
            logger.info("Configuring liquibase...");
            liquibase = new Liquibase(ConfigTag.getProperty("changelog.file.path", "db.changelog.xml"), new FileSystemResourceAccessor(), database);
            logger.info("Liquibase configured");
            logger.info("Updating change as with added precondition...");
            liquibase.update("");
            logger.info("Changes updated");
        } catch (DatabaseException databaseException) {
            logger.error("Exception encountered while attempting to integrate change logs, please see details ", databaseException);
            failed = true;
        } catch (LiquibaseException liquibaseException) {
            logger.error("Exception encountered while attempting to integrate change logs, please see details ", liquibaseException);
            failed = true;
        } catch (Exception exception) {
            logger.error("Exception encountered while attempting to integrate change logs, please see details ", exception);
            failed = true;
        } finally {
            close(connection, liquibase, failed);
        }
    }

    /**
     * <pre>
     *     For reusability purposes this method will help closing all opened objects.
     * </pre>
     *
     * @param connection The connection object.
     * @param liquibase  The liquibase object.
     * @param isFailed    The success/failure flag.
     * @return flag Returns true after successful close operation false otherwise.
     */
    private boolean close(Connection connection, Liquibase liquibase, boolean isFailed) {
        boolean closed = false;
        int rollBackIndex=Integer.parseInt(ConfigTag.getProperty("liquibase.rollbackto"));
        if (connection != null) {
            try {
                if (isFailed) {
                    logger.info("Reverting partially integrated changes...");
                    liquibase.rollback(rollBackIndex, "");
                    logger.info("Reverted partially integrated changes");
                }
                liquibase.forceReleaseLocks();
                connection.close();
                closed = true;
            } catch (Exception exception) {
                closed = false;
                logger.error("Exception encountered while attempting to revert partially integrated changes, please see details ", exception);
            }
        }
        return closed;
    }

}