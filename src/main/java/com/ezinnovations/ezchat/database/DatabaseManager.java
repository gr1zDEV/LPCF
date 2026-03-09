package com.ezinnovations.ezchat.database;

import java.sql.Connection;
import java.sql.SQLException;

public interface DatabaseManager {

    Connection getConnection() throws SQLException;

    void initialize();

    void close();
}
