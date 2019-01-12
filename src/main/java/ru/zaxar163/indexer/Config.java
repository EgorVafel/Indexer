package ru.zaxar163.indexer;

/**
 * @author xtrafrancyz
 */
public class Config {
	public static class Mysql {
		public String url = "jdbc:mysql://127.0.0.1/degustator?useUnicode=true&characterEncoding=utf-8";
		public String user = "root";
		public String pass = "";
		public int port = 336;
		public String host = "";
		public boolean useSSL = false;
		public boolean verifyCertificates = false;
		public String database = "bot";
	}

	public String token = "MY_BOT_TOKEN";

	public Mysql mysql = new Mysql();
}
