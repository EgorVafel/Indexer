package ru.zaxar163.indexer.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import com.mysql.cj.jdbc.MysqlDataSource;

import ru.zaxar163.indexer.Config;
import ru.zaxar163.indexer.Indexer;

public class MysqlPool {
	private MysqlDataSource pool;

	public MysqlPool(Indexer indexer) throws Exception {
		Config.Mysql config = indexer.config.mysql;

		pool = new MysqlDataSource();
		pool.setServerName(config.host);
		pool.setPort(config.port);
		pool.setUser(config.user);
		pool.setPassword(config.pass);
		// Prep statements cache
		pool.setPrepStmtCacheSize(250);
		pool.setPrepStmtCacheSqlLimit(2048);
		pool.setCachePrepStmts(true);
		pool.setUseServerPrepStmts(true);

		// General optimizations
		pool.setCacheServerConfiguration(true);
		pool.setUseLocalSessionState(true);
		pool.setRewriteBatchedStatements(true);
		pool.setMaintainTimeStats(false);
		pool.setUseUnbufferedInput(false);
		pool.setUseReadAheadInput(false);
		pool.setUseSSL(config.useSSL);
		pool.setVerifyServerCertificate(config.verifyCertificates);

		pool.setDatabaseName(config.database);
		pool.setTcpNoDelay(true);
	}

	public int query(String query) throws SQLException {
		return query(query, null);
	}

	public int query(String query, PreparedStatementFiller filler) throws SQLException {
		Connection conn = null;
		Statement st = null;
		try {
			conn = pool.getConnection();
			if (filler == null) {
				st = conn.createStatement();
				return st.executeUpdate(query);
			} else {
				st = conn.prepareStatement(query);
				filler.fill((PreparedStatement) st);
				return ((PreparedStatement) st).executeUpdate();
			}
		} catch (SQLException e) {
			if (e.getMessage() != null && e.getMessage().contains("try restarting transaction"))
				return query(query, filler);
			throw e;
		} finally {
			if (st != null)
				try {
					st.close();
				} catch (Exception ignored) {
				}
			if (conn != null)
				try {
					conn.close();
				} catch (Exception ignored) {
				}
		}
	}

	public SelectResult select(String query) throws SQLException {
		return select(query, null);
	}

	public SelectResult select(String query, PreparedStatementFiller filler) throws SQLException {
		Connection conn = null;
		Statement st = null;
		ResultSet rs = null;
		try {
			conn = pool.getConnection();
			if (filler == null) {
				st = conn.createStatement();
				rs = st.executeQuery(query);
			} else {
				st = conn.prepareStatement(query);
				filler.fill((PreparedStatement) st);
				rs = ((PreparedStatement) st).executeQuery();
			}

			SelectResult result = new SelectResult();
			ResultSetMetaData metadata = rs.getMetaData();
			int columnCount = metadata.getColumnCount();
			result.columns = new ArrayList<>(columnCount);
			for (int i = 1; i <= columnCount; i++)
				result.columns.add(new Column(metadata.getColumnLabel(i)));
			result.rows = new ArrayList<>();
			while (rs.next()) {
				Row row = new Row(result);
				for (int i = 1; i <= columnCount; i++)
					row.values.add(rs.getObject(i));
				result.rows.add(row);
			}
			return result;
		} catch (SQLException e) {
			if (e.getMessage() != null && e.getMessage().contains("try restarting transaction"))
				return select(query, filler);
			throw e;
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (Exception ignored) {
				}
			if (st != null)
				try {
					st.close();
				} catch (Exception ignored) {
				}
			if (conn != null)
				try {
					conn.close();
				} catch (Exception ignored) {
				}
		}
	}
}
