package ru.zaxar163.indexer.command;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.message.Message;

import ru.zaxar163.indexer.Indexer;
import ru.zaxar163.indexer.Utils;

public class CommandManager {
	public static final Class<ServerTextChannel> stc = ServerTextChannel.class;

	public final Indexer app;

	public Map<String, Command> registered;
	public Map<String, Command> alises;
	public final Set<Long> enabledChannels;

	public CommandManager(final Indexer app) {
		this.app = app;
		registered = new HashMap<>();
		alises = new HashMap<>();
		enabledChannels = Collections.newSetFromMap(new ConcurrentHashMap<>());
		app.client.addMessageCreateListener(ev -> {
			if (enabledChannels.contains(Long.valueOf(ev.getChannel().getId()))
					&& ev.getMessage().getUserAuthor().isPresent()
					&& ev.getMessage().getContent().startsWith(this.app.config.messageToken))
				process(ev.getMessage());
		});
		if (new File("channels_cmd.lst").exists())
			try (BufferedReader readerChannels = new BufferedReader(
					new InputStreamReader(new FileInputStream("channels_cmd.lst"), StandardCharsets.UTF_8))) {
				String word;
				while ((word = readerChannels.readLine()) != null)
					enabledChannels.add(Long.parseLong(word));
			} catch (final Exception ex) {
				System.err.println("File 'channels_cmd.lst' parsing error");
				return;
			}
		app.client.getServers().stream().forEach(s -> {
			if (!s.getChannels().stream().mapToLong(e -> e.getId())
					.anyMatch(e -> enabledChannels.contains(Long.valueOf(e))))
				s.getChannels().stream().filter(CommandManager.stc::isInstance).map(CommandManager.stc::cast)
						.forEach(this::attachChannelListener);
		});
		app.client.addServerJoinListener(e -> {
			e.getServer().getChannels().stream().filter(CommandManager.stc::isInstance).map(CommandManager.stc::cast)
					.forEach(this::attachChannelListener);
		});
		Utils.filterSrvList(app.client, enabledChannels);
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			try (PrintWriter readerChannels = new PrintWriter(
					new OutputStreamWriter(new FileOutputStream("channels_cmd.lst"), StandardCharsets.UTF_8))) {
				enabledChannels.forEach(readerChannels::println);
			} catch (final Exception ex) {
				System.err.println(ex.toString());
			}
		}, "Saving channels thread"));
	}

	public void attachChannelListener(final ServerTextChannel ch) {
		enabledChannels.add(ch.getId());
		ch.sendMessage("Исполнение команд для этого канала теперь **включено**");
	}

	public Command getCommand(final String cmd) {
		final Command command = registered.get(cmd);
		if (command != null)
			return command;
		return alises.get(cmd);
	}

	public void process(final Message message) {
		try {
			if (!message.getChannel().asServerTextChannel().isPresent())
				return;
			final String text = message.getContent().substring(1);
			final String[] args = text.split(" ");
			final Command command = getCommand(args[0].toLowerCase(Locale.ENGLISH));
			if (command == null || !command.canUse(message))
				return;
			command.onCommand(message, Arrays.copyOfRange(args, 1, args.length));
		} catch (final Exception ex) {
			ex.printStackTrace();
		}
	}

	public void registerCommand(final Command command) {
		registered.put(command.command, command);
		for (final String cmd : command.aliases)
			alises.put(cmd, command);
	}

	public void removeChannelListener(final ServerTextChannel ch) {
		enabledChannels.removeIf(e -> e.longValue() == ch.getId());
		ch.sendMessage("Исполнение команд для этого канала теперь **отключено**");
	}

	public void work(final ServerTextChannel ch) {
		if (enabledChannels.contains(ch.getId()))
			removeChannelListener(ch);
		else
			attachChannelListener(ch);
	}
}
