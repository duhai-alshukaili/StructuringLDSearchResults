// FileName   : ConnectionFactory.java
// Date       : 20-10-2014
// Programmer : Duhai Alshukaili

package uk.ac.man.cs.rdb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Manage the connection to the underlying database.
 *
 * @author Duhai Alshukaili
 * @version 0.0.1
 */

public class ConnectionFactory {

    // the drive class string
    private static final String DRIVER
            = "org.apache.derby.jdbc.EmbeddedDriver";
    
    // the directory where the database will be created
    private static final String DB_HOME 
            = "/home/ispace/data/lod_search_db";

    // the name of the derby database
    private static final String DB_NAME = "LDPayGOLitDB";

    // the connection url
    private static final String connectionURL
            = "jdbc:derby:" + DB_NAME + ";create=true";

    private static ConnectionFactory connectionFactory = null;

    /**
     *
     */
    private ConnectionFactory() {
        // set the derby.system.home property
        System.setProperty("derby.system.home", DB_HOME);
        
        try {
            Class.forName(DRIVER);
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException("Error while loading driver class "
                    + DRIVER, ex);
        }
    }

    /**
     *
     * @return @throws SQLException
     */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(connectionURL);

    }

    /**
     *
     * @return
     */
    public static ConnectionFactory getInstance() {

        if (connectionFactory == null) {
            connectionFactory = new ConnectionFactory();
        }

        return connectionFactory;
    }

}

