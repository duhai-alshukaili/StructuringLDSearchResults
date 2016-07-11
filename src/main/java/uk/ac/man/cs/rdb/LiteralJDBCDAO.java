package uk.ac.man.cs.rdb;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * @author ispace
 */
public class LiteralJDBCDAO {

	private Connection connection = null;

	private static final String SQL_CREATE_TABLE = "CREATE TABLE LITERAL (\n"
			+ "  ID INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),\n"
			+ "  MD5HASH VARCHAR(50) UNIQUE, \n" + "  STRING  LONG VARCHAR\n"
			+ ")";

	public LiteralJDBCDAO() {
		try {

			connection = getConnection();

			if (!tableExsits("LITERAL")) {
				// create table
				Statement stmnt = connection.createStatement();
				stmnt.execute(SQL_CREATE_TABLE);
				stmnt.close();
				System.err.println("Creating new table LITERAL");
			} else {
				System.err.println("Table LITERAL already exisits");
			}

		} catch (SQLException ex) {

			Logger.getLogger(LiteralJDBCDAO.class.getName()).log(Level.SEVERE,
					null, ex);

			throw new RuntimeException("Error while intitlizing "
					+ LiteralJDBCDAO.class.getCanonicalName(), ex);
		}

	}

	public LiteralBean get(Integer id) {

		try {
			LiteralBean bean = new LiteralBean();

			bean.setId(id);

			String sqlSelect = "select id, MD5HASH, STRING from app.literal "
					+ "where  id = ?";

			PreparedStatement pStmtSelect = connection
					.prepareStatement(sqlSelect);

			pStmtSelect.setInt(1, bean.getId());

			ResultSet record = pStmtSelect.executeQuery();

			if (record.next()) {
				bean.setMd5Hash(record.getString(2));
				bean.setLiteralString(record.getString(3));
				return bean;
			} else {
				return null;
			}

		} catch (SQLException ex) {

			Logger.getLogger(LiteralJDBCDAO.class.getName()).log(Level.WARNING,
					null, ex);

			return null;
		}

	}

	/**
	 * 
	 * @param literalString
	 * @return
	 */
	public LiteralBean add(String literalString) {
		try {
			LiteralBean bean = new LiteralBean();

			bean.setMd5Hash(md5Hash(literalString));
			bean.setLiteralString(literalString);

			// try to find if the string already in the DB
			String sqlSelect = "select id, MD5HASH, STRING from app.literal where  MD5HASH = ?";

			PreparedStatement pStmtSelect = connection
					.prepareStatement(sqlSelect);

			pStmtSelect.setString(1, bean.getMd5Hash());

			ResultSet oldRecord = pStmtSelect.executeQuery();

			if (oldRecord.next()) {

				// get the ID from the previous record
				bean.setId(oldRecord.getInt(1));

			} else {

				// insert new record
				String sqlInsert = "insert into app.literal(MD5HASH, STRING) values(?,?)";

				PreparedStatement stmt = connection.prepareStatement(sqlInsert,
						new String[]{"ID"});

				stmt.setString(1, bean.getMd5Hash());
				stmt.setString(2, bean.getLiteralString());

				stmt.executeUpdate();

				ResultSet generatedKeys = stmt.getGeneratedKeys();

				if (generatedKeys != null && generatedKeys.next()) {
					bean.setId(generatedKeys.getInt(1));
				}

				// clean up resource
				if (generatedKeys != null)
					generatedKeys.close();
				stmt.close();
			}

			// clean up resource
			pStmtSelect.close();
			oldRecord.close();

			return bean;
		} catch (SQLException ex) {

			Logger.getLogger(LiteralJDBCDAO.class.getName()).log(Level.WARNING,
					null, ex);

			return null;
		}
	}

	private Connection getConnection() throws SQLException {
		return ConnectionFactory.getInstance().getConnection();
	}

	/**
	 * 
	 * @param table
	 * @return
	 */
	private boolean tableExsits(String table) {
		int numRows = 0;
		try {
			DatabaseMetaData dbmd = connection.getMetaData();
			// Note the args to getTables are case-sensitive!
			ResultSet rs = dbmd.getTables(null, "APP", table.toUpperCase(),
					null);
			while (rs.next()) {
				++numRows;
			}
		} catch (SQLException e) {
			String theError = e.getSQLState();
			System.out.println("Can't query DB metadata: " + theError);
			System.exit(1);
		}
		return numRows > 0;
	}

	private String md5Hash(String s) {
		try {
			MessageDigest m = MessageDigest.getInstance("MD5");
			m.update(s.getBytes(), 0, s.length());
			return new BigInteger(1, m.digest()).toString(16);
		} catch (NoSuchAlgorithmException ex) {
			Logger.getLogger(LiteralJDBCDAO.class.getName()).log(Level.SEVERE,
					null, ex);

			return null;
		}
	}
}
