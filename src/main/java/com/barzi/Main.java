package com.barzi;

import com.barzi.liquibase.LiquibaseUtil;
import liquibase.diff.DiffResult;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.DiffToChangeLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <pre>
 *     Main class.
 * </pre>
 *
 * @author barzi
 */

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);  //Logger instance for logging

    /**
     * <pre>
     *     Main method.
     * </pre>
     *
     * @param args Command Line arguments.
     */
    public static void main(String args[]) {
        logger.info("Starting liquibase example...");


        integrateChanges();
        //compareDatabase();
        logger.info("finished liquibase example");
    }

    /**
     * Method to call Database up-grader.
     */
    public static void integrateChanges() {

        logger.info("Creating liquibase Utility...");
        LiquibaseUtil liquibaseUtil = new LiquibaseUtil();
        logger.info("Liquibase Utility created");
        logger.info("Started liquibase example");
        logger.info("Attempting to integrate change log...");
        liquibaseUtil.integrateChanges();
        logger.info("Attempt to integrate change log finished");
    }

    /**
     * Method to call Database difference checker.
     */
    public static void compareDatabase() {
        logger.info("Creating liquibase Utility...");
        LiquibaseUtil liquibaseUtil = new LiquibaseUtil();
        logger.info("Liquibase Utility created");
        logger.info("Started liquibase example");
        logger.info("Attempting to compare Databases...");
        DiffResult diffResult = liquibaseUtil.fetchDifferences();
        logger.info("Attempt to compare Databases finished");
    }

    /**
     * Method to call Liquibase Precondition demo.
     */
    public static void preConditions(){
        logger.info("Creating liquibase Utility...");
        LiquibaseUtil liquibaseUtil = new LiquibaseUtil();
        logger.info("Liquibase Utility created");
        logger.info("Started liquibase example");
        logger.info("Attempting call Liquibase Precondition demo...");
        liquibaseUtil.preConditions();
        logger.info("Liquibase Precondition demo finished");
    }
}
