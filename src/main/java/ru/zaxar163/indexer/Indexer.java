package ru.zaxar163.indexer;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.gson.stream.JsonReader;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;

import com.google.gson.Gson;
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
import ru.zaxar163.indexer.command.manage.faq.RegisterFAQ;
import ru.zaxar163.indexer.command.manage.faq.RemoveFAQ;
import ru.zaxar163.indexer.module.FaqManager;
import ru.zaxar163.indexer.module.FaqWorker;
import ru.zaxar163.indexer.module.SwearFilter;

public class Indexer {
	public static final List<Indexer> instances = Collections.synchronizedList(new ArrayList<>(1));

	public static void main(final String[] args) throws Exception {
		instances.add(new Indexer());
		final BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
		while (true)
			if (r.readLine().contains("stop"))
				System.exit(0);
	}

	public final DiscordApi client;
	public final CommandManager commandManager;

	public final Config config;
	public final Gson gson = new Gson();
	public final SwearFilter swearFilter;
	public final FaqWorker faqWorker;
	public final FaqManager faqManager;

	private Indexer() throws Exception {
		config = readConfig();

		client = new DiscordApiBuilder().setToken(config.token).login().join();

		commandManager = new CommandManager(this);
		swearFilter = new SwearFilter(this);
		faqManager = readFaqDataBase();
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			final File confFile = new File("faq.json");
			try(JsonWriter writer = new JsonWriter(new FileWriter(confFile)))
			{
				gson.toJson(faqManager,FaqManager.class, writer);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}));
		faqWorker = new FaqWorker(client, faqManager);

		commandManager.registerCommand(new HelpCommand(commandManager));
		commandManager.registerCommand(new RemoveMsgCommand());
		commandManager.registerCommand(new RemoveMsgCommand1());
		commandManager.registerCommand(new RemoveMsgCommand2());
		commandManager.registerCommand(new SwearFilterCommand(swearFilter));
		commandManager.registerCommand(new ExecChannelCommand(commandManager));

		commandManager.registerCommand(new EnableFAQ(faqWorker));
		commandManager.registerCommand(new RegisterFAQ(faqWorker));
		commandManager.registerCommand(new RemoveFAQ(faqWorker));
		commandManager.registerCommand(new ListFAQ(faqWorker));
	}
	private FaqManager readFaqDataBase() throws IOException
	{
		final File confFile = new File("faq.json");
		if(!confFile.exists())
		{
			FaqManager manager = new FaqManager();
			try(JsonWriter writer = new JsonWriter(new FileWriter(confFile)))
			{
				gson.toJson(manager,FaqManager.class, writer);
			}
			return manager;
		}
		else
		{
			try(JsonReader reader = new JsonReader(new FileReader(confFile)))
			{
				return gson.fromJson(reader, FaqManager.class);
			}
		}
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
