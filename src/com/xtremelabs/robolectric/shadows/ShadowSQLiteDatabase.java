package com.xtremelabs.robolectric.shadows;

import static com.xtremelabs.robolectric.Robolectric.newInstanceOf;
import static com.xtremelabs.robolectric.Robolectric.shadowOf;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;

import com.xtremelabs.robolectric.util.Implementation;
import com.xtremelabs.robolectric.util.Implements;

/**
 * Shadow for {@code SQLiteDatabase} that simulates the movement of a {@code Cursor} through database tables.
 */
@Implements(SQLiteDatabase.class)
public class ShadowSQLiteDatabase {
    private static Connection conn;

    @Implementation
    public static SQLiteDatabase openDatabase(String path, SQLiteDatabase.CursorFactory factory, int flags) {
    	try {
			Class.forName("org.h2.Driver").newInstance();
			conn = DriverManager.getConnection("jdbc:h2:mem:");
		} catch (Exception e) {
			rethrowException( e, "SQL exception in openDatabase" );
		}
        return newInstanceOf(SQLiteDatabase.class);
    }
    
    @Implementation
    public long insert(String table, String nullColumnHack, ContentValues values) {
    	// TODO
    	return -1;
    }

    @Implementation
    public Cursor query(final String table, final String[] columns, String selection,
                        String[] selectionArgs, String groupBy, String having,
                        String orderBy) {
    	ResultSet rs = null;
    	
    	// TODO build SQL
    	String sql = "SELECT * from " + table;
    	
    	try {
	    	Statement statement = conn.createStatement( ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY );
	    	rs = statement.executeQuery(sql);
    	} catch( SQLException e ) {
			rethrowException( e, "SQL exception in query" );
    	}
    	
    	SQLiteCursor cursor = new SQLiteCursor(null, null, null, null);
    	shadowOf(cursor).setResultSet( rs );
    	return cursor;
    }
    
    @Implementation
    public int update(String table, ContentValues values, String whereClause, String[] whereArgs) {
    	// TODO
    	return 0;
    }
    
    @Implementation
    public int delete(String table, String whereClause, String[] whereArgs) {
    	// TODO
    	return 0;
    }
    
    @Implementation
    public void execSQL(String sql) throws SQLException {  	
    	if (!isOpen()) {
            throw new IllegalStateException("database not open");
        }

    	Statement statement = conn.createStatement();
    	statement.execute(sql);
    }
    
    @Implementation
    public boolean isOpen() {
    	return (conn != null);
    }
    
    @Implementation
    public void close() {
    	if (!isOpen()) {
    		return;
    	}
    	try {
			conn.close();
			conn = null;
		} catch (SQLException e) {
			rethrowException( e, "SQL exception in close" );
		}
    }
    
    /**
     * Allows test cases access to the underlying JDBC connection, for use in
     * setup or assertions.
     * 
     * @return
     */
    public Connection getConnection() {
    	return conn;
    }
    
    // DRY out with ShadowSQLiteCursor
	private static void rethrowException( Exception e, String msg ) {
		AssertionError ae = new AssertionError( msg );
		ae.initCause(e);
		throw ae;
	}
}
