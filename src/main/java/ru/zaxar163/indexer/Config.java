package ru.zaxar163.indexer;

public class Config {
	public static class Mysql {
		public String user = "db_user";
		public String pass = "db_pass";
		public int port = 336;
		public String host = "db.host";
		public boolean useSSL = false;
		public boolean verifyCertificates = false;
		public String database = "indexer";
	}

	public String token = "MY_BOT_TOKEN";
	public Mysql mysql = new Mysql();
	public String messageToken = "!";
}
