package net.xtrafrancyz.degustator.mysql;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface PreparedStatementFiller {
	void fill(PreparedStatement statement) throws SQLException;
}
