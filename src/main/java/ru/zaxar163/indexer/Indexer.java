package ru.zaxar163.indexer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.concurrent.atomic.AtomicReference;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import ru.zaxar163.indexer.command.CommandManager;
import ru.zaxar163.indexer.command.basic.HelpCommand;
import ru.zaxar163.indexer.command.manage.ExecChannelCommand;
import ru.zaxar163.indexer.command.manage.RemoveMsgCommand;
import ru.zaxar163.indexer.command.manage.RemoveMsgCommand1;
import ru.zaxar163.indexer.command.manage.RemoveMsgCommand2;
import ru.zaxar163.indexer.command.manage.SwearFilterCommand;
import ru.zaxar163.indexer.command.manage.faq.EnableFAQ;
import ru.zaxar163.indexer.command.manage.faq.ListFAQ;
import ru.zaxar163.indexer.command.manage.faq.ReloadFAQ;
import ru.zaxar163.indexer.module.FaqManager;
import ru.zaxar163.indexer.module.FaqWorker;
import ru.zaxar163.indexer.module.SwearFilter;

public class Indexer {
	public static final AtomicReference<Indexer> instance = new AtomicReference<>(null);

	public static void main(final String[] args) throws Exception {
		instance.set(new Indexer());
		final BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
		while (true)
			switch (r.readLine()) {
			case "stop":
				System.exit(0);
				break;
			case "reloadFaq":
				instance.get().faqManager = instance.get().readFaqDataBase();
				break;
			}
	}

	public final DiscordApi client;
	public final CommandManager commandManager;

	public final Config config;
	public final Gson gson = new Gson();
	public final SwearFilter swearFilter;
	public final FaqWorker faqWorker;
	public FaqManager faqManager;

	private Indexer() throws Exception {
		config = readConfig();

		client = new DiscordApiBuilder().setToken(config.token).login().join();

		commandManager = new CommandManager(this);
		swearFilter = new SwearFilter(this);
		faqManager = readFaqDataBase();
		/*Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			final File confFile = new File("faq.json");
			try (JsonWriter writer = new JsonWriter(new FileWriter(confFile))) {
				gson.toJson(faqManager, FaqManager.class, writer);
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}));*/
		faqWorker = new FaqWorker(this);

		commandManager.registerCommand(new HelpCommand(commandManager));
		commandManager.registerCommand(new RemoveMsgCommand());
		commandManager.registerCommand(new RemoveMsgCommand1());
		commandManager.registerCommand(new RemoveMsgCommand2());
		commandManager.registerCommand(new SwearFilterCommand(swearFilter));
		commandManager.registerCommand(new ExecChannelCommand(commandManager));

		commandManager.registerCommand(new EnableFAQ(faqWorker));
		commandManager.registerCommand(new ReloadFAQ(faqWorker));
		commandManager.registerCommand(new ListFAQ(faqWorker));
	}

	private Config readConfig() throws IOException {
		final File confFile = new File("config.json");
		Config config = null;
		if (!confFile.exists()) {
			config = new Config();
			try (final JsonWriter writer = new JsonWriter(new FileWriter(confFile))) {
				writer.setIndent("  ");
				writer.setHtmlSafe(false);
				gson.toJson(config, Config.class, writer);
			}
			System.out.println("Created config.json");
			System.exit(0);
		} else
			config = gson.fromJson(
					Files.readAllLines(confFile.toPath()).stream().map(String::trim)
							.filter(s -> !s.startsWith("#") && !s.isEmpty()).reduce((a, b) -> a += b).orElse(""),
					Config.class);
		return config;
	}

	public FaqManager readFaqDataBase() throws IOException {
		final File confFile = new File("faq.json");
		if (!confFile.exists()) {
			final FaqManager manager = new FaqManager();
			try (JsonWriter writer = new JsonWriter(new FileWriter(confFile))) {
				gson.toJson(manager, FaqManager.class, writer);
			}
			return manager;
		} else
			try (JsonReader reader = new JsonReader(new FileReader(confFile))) {
				return gson.fromJson(reader, FaqManager.class);
			}
	}
}
