package ru.zaxar163.indexer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;

import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;

import ru.zaxar163.indexer.command.CommandManager;
import ru.zaxar163.indexer.command.basic.SrvOwnersManageCmd;
import ru.zaxar163.indexer.command.manage.ExecChannelCommand;
import ru.zaxar163.indexer.command.manage.SwearFilterCommand;
import ru.zaxar163.indexer.command.standard.HelpCommand;
import ru.zaxar163.indexer.module.PrivateWorker;
import ru.zaxar163.indexer.module.SwearFilter;

public class Indexer {

	public static void main(final String[] args) throws Exception {
		new Indexer();
		final BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
		while (true)
			if (r.readLine().contains("stop"))
				System.exit(0);
	}

	public final Gson gson = new Gson();
	public final Config config;

	public final DiscordApi client;
	public final CommandManager commandManager;
	public final RoleManager roler;
	public final SwearFilter swearFilter;
	public final PrivateWorker privateWorker;

	private Indexer() throws Exception {
		config = readConfig();

		client = new DiscordApiBuilder().setToken(config.token).login().join();
		roler = new RoleManager(client);
		commandManager = new CommandManager(this);
		privateWorker = new PrivateWorker();
		swearFilter = new SwearFilter(this);
		commandManager.registerCommand(new HelpCommand(commandManager));
		commandManager.registerCommand(new SrvOwnersManageCmd(this));
		commandManager.registerCommand(new SwearFilterCommand(this));
		commandManager.registerCommand(new ExecChannelCommand(this));
	}

	private Config readConfig() throws IOException {
		final File confFile = new File("config.json");
		Config config = null;
		if (!confFile.exists()) {
			config = new Config();
			final JsonWriter writer = new JsonWriter(new FileWriter(confFile));
			writer.setIndent("  ");
			writer.setHtmlSafe(false);
			gson.toJson(config, Config.class, writer);
			writer.close();
			System.out.println("Created config.json");
			System.exit(0);
		} else
			config = gson.fromJson(
					Files.readAllLines(confFile.toPath()).stream().map(String::trim)
							.filter(s -> !s.startsWith("#") && !s.isEmpty()).reduce((a, b) -> a += b).orElse(""),
					Config.class);
		return config;
	}
}
