package com.barzi.database;

import com.barzi.Main;
import com.barzi.database.config.ConfigTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;

public class DatabaseUtil {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseUtil.class); //Logger instance for logging.
    private static Connection connection=null; //Connection object.

    /**
     * <pre>
     *     This method will check if the connection is NULL or connection is closed then a new connection will be created else
     *     the same connection object will be returned.
     * </pre>
     *
     * @return connection Returns a connection object.
     */
    public static Connection getConnection(){
        try{
            logger.info("Confirming if connection is already exists...");
            if (connection==null ||  connection.isClosed()){
                logger.info("Creating new connection...");
                connection= DriverManager.getConnection(ConfigTag.getProperty("database.url"),ConfigTag.getProperty("database.user"),ConfigTag.getProperty("database.password"));
                logger.info("Created new connection");
            }
        }catch (Exception e){
            logger.error("Exception encountered while created new connection, please see details ",e);
        }

        return connection;
    }
}
