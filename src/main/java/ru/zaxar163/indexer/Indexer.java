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
import ru.zaxar163.indexer.command.manage.SwearFilterCommand;
import ru.zaxar163.indexer.command.standard.HelpCommand;
import ru.zaxar163.indexer.command.standard.JokeCommand;
import ru.zaxar163.indexer.module.SwearFilter;

public class Indexer {
	private static Indexer instance = null;

	public static Indexer instance() {
		return instance;
	}

	public static void main(String[] args) throws Exception {
		new Indexer();
		BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
		while (true) {
			if (r.readLine().contains("stop"))
				System.exit(0);
		}
	}

	public final Gson gson = new Gson();
	public final Config config;

	public final DiscordApi client;
	public final CommandManager commandManager;

	public final SwearFilter swearFilter;

	private Indexer() throws Exception {
		instance = this;
		config = readConfig();

		client = new DiscordApiBuilder().setToken(config.token).login().join();

		commandManager = new CommandManager(this);
		commandManager.registerCommand(new HelpCommand(commandManager));
		commandManager.registerCommand(new JokeCommand());
		commandManager.registerCommand(new SwearFilterCommand());
		swearFilter = new SwearFilter(this);

		client.addMessageCreateListener(ev -> {
			if (ev.getMessage().getContent().startsWith(config.messageToken))
				commandManager.process(ev.getMessage());
		});
	}

	private Config readConfig() throws IOException {
		File confFile = new File("config.json");
		Config config = null;
		if (!confFile.exists()) {
			config = new Config();
			JsonWriter writer = new JsonWriter(new FileWriter(confFile));
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
