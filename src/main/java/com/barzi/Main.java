package com.barzi;

import com.barzi.liquibase.LiquibaseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <pre>
 *     Main class.
 * </pre>
 *@author barzi
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
        logger.info("Creating liquibase Utility...");
        LiquibaseUtil liquibaseUtil=new LiquibaseUtil();
        logger.info("Liquibase Utility created");
        logger.info("Started liquibase example");
        logger.info("Attempting to integrate change log...");
        liquibaseUtil.attempt();
        logger.info("Attempt to integrate change log finished");
        logger.info("finished liquibase example");
    }
}
