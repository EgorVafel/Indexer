package net.xtrafrancyz.degustator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;

import net.xtrafrancyz.degustator.command.CommandManager;
import net.xtrafrancyz.degustator.command.manage.MassBanCommand;
import net.xtrafrancyz.degustator.command.manage.SwearFilterCommand;
import net.xtrafrancyz.degustator.command.standard.HelpCommand;
import net.xtrafrancyz.degustator.command.standard.JokeCommand;
import net.xtrafrancyz.degustator.module.SwearFilter;
import net.xtrafrancyz.degustator.mysql.MysqlPool;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.ActivityType;
import sx.blah.discord.handle.obj.StatusType;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.RateLimitException;

public class Degustator {
	private static Degustator instance;

	public static Degustator instance() {
		return instance;
	}

	public static void main(String[] args) throws Exception {
		new Degustator();
	}

	public final Gson gson = new Gson();
	public Config config;

	public final IDiscordClient client;
	public final MysqlPool mysql;
	private final CommandManager commandManager;

	public final SwearFilter swearFilter;

	private Degustator() throws Exception {
		instance = this;
		readConfig();
		Scheduler.init(2);

		client = new ClientBuilder().withToken(config.token).build();
		mysql = new MysqlPool(this);

		commandManager = new CommandManager(this);
		commandManager.registerCommand(new HelpCommand(commandManager));
		// commandManager.registerCommand(new InfoCommand());
		commandManager.registerCommand(new JokeCommand());
		commandManager.registerCommand(new SwearFilterCommand());
		commandManager.registerCommand(new MassBanCommand());
		swearFilter = new SwearFilter(this);

		client.login();
		client.getDispatcher().registerListener(this);
	}

	@EventSubscriber
	public void onMessage(MessageReceivedEvent event) {
		if (event.getMessage().getContent().startsWith("!"))
			commandManager.process(event.getMessage());
	}

	@EventSubscriber
	public void onReady(ReadyEvent event) throws RateLimitException, DiscordException {
		client.changePresence(StatusType.ONLINE, ActivityType.WATCHING, "прон | !help");
	}

	private void readConfig() throws IOException {
		File confFile = new File("config.json");
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
	}
}
