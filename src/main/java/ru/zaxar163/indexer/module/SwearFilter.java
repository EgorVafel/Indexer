package ru.zaxar163.indexer.module;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import ru.zaxar163.indexer.Indexer;
import sx.blah.discord.Discord4J;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageUpdateEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;

public class SwearFilter {
	private static final OpenOption[] WRITE_OPTIONS = { StandardOpenOption.CREATE, StandardOpenOption.WRITE,
			StandardOpenOption.TRUNCATE_EXISTING };

	public static void main(String... args) throws Throwable {
		try (BufferedWriter out = Files.newBufferedWriter(Paths.get("out_swear.txt"), StandardCharsets.UTF_8,
				WRITE_OPTIONS)) {
			List<String> lines = Files.readAllLines(Paths.get("in_swear.txt"), StandardCharsets.UTF_8).stream()
					.filter(e -> !e.isEmpty()).map(e -> e.replace('|', '\n')).collect(Collectors.toList());
			for (String line : lines)
				out.append(line);
			out.flush();
		}
	}

	private static String normalizeWord(String str) {
		if (str.isEmpty())
			return "";
		char[] chars = str.toCharArray();
		int len = chars.length;
		int st = 0;
		while (st < len && !Character.isAlphabetic(chars[st]))
			st++;
		while (st < len && !Character.isAlphabetic(chars[len - 1]))
			len--;
		str = st > 0 || len < chars.length ? str.substring(st, len) : str;
		return str.toLowerCase().replace('a', 'а').replace('e', 'е').replace('э', 'е').replace('ё', 'е')
				.replace('y', 'у').replace('p', 'р').replace('x', 'х').replace('o', 'о').replace('c', 'с')
				.replace('s', 'с');
	}

	private final Indexer indexer;
	private final Set<Long> enabledChannels;

	private final Set<String> badWords;

	private boolean enabled = true;

	public SwearFilter(Indexer indexer) {
		this.indexer = indexer;
		enabledChannels = new HashSet<>();
		badWords = new HashSet<>();

		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(new FileInputStream("badwords.txt"), StandardCharsets.UTF_8))) {
			String word;
			while ((word = reader.readLine()) != null) {
				word = normalizeWord(word.trim());
				if (!word.isEmpty())
					badWords.add(word);
			}
		} catch (Exception ex) {
			enabled = false;
			Discord4J.LOGGER.info("SwearFilter disabled. File 'badwords.txt' not found");
			return;
		}

		if (new File("channels.lst").exists())
			try (BufferedReader readerChannels = new BufferedReader(
					new InputStreamReader(new FileInputStream("channels.lst"), StandardCharsets.UTF_8))) {
				String word;
				while ((word = readerChannels.readLine()) != null)
					enabledChannels.add(Long.parseLong(word));
			} catch (Exception ex) {
				enabled = false;
				Discord4J.LOGGER.info("SwearFilter disabled. File 'channels.lst' not found");
				return;
			}

		indexer.client.getDispatcher().registerListeners(this);
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			try (PrintWriter readerChannels = new PrintWriter(
					new OutputStreamWriter(new FileOutputStream("channels.lst"), StandardCharsets.UTF_8))) {
				enabledChannels.forEach(readerChannels::println);
			} catch (Exception ex) {
				Discord4J.LOGGER.error(ex.toString());
			}
		}, "Saving channels thread"));
	}

	private void checkMessage(IMessage message) {
		if (hasSwear(message.getContent()))
			try {
				message.delete();
					/*IMessage rs = message.getChannel()
							.sendMessage("**" + message.getAuthor().getDisplayName(message.getGuild())
									+ "**, пожалуйста, следите за словами.");
					RequestWorker.schedule(rs::delete, 5, TimeUnit.SECONDS);*/
				indexer.client.getOrCreatePMChannel(message.getAuthor()).sendMessage("**" + message.getAuthor().mention()
								+ "**, пожалуйста, следите за словами.");
			} catch (Exception ignored) {
			}
	}
	
	public void disableFor(IChannel channel) {
		if (!enabled || !isActive(channel))
			return;
		enabledChannels.remove(channel.getLongID());
	}

	public void enableFor(IChannel channel) {
		if (!enabled || isActive(channel))
			return;
		enabledChannels.add(channel.getLongID());
	}

	private boolean hasSwear(String message) {
		for (String word : message.split(" "))
			if (badWords.contains(normalizeWord(word)))
				return true;
		return false;
	}

	public boolean isActive(IChannel channel) {
		return enabledChannels.contains(channel.getLongID());
	}

	public boolean isEnabled() {
		return enabled;
	}

	public boolean isFilterable(MessageEvent event) {
		return isActive(event.getChannel()) && event.getAuthor().hasRole(indexer.commandManager.developer)
				&& !event.getAuthor().equals(indexer.client.getOurUser());
	}

	@EventSubscriber
	public void onMessage(MessageReceivedEvent event) {
		if (isFilterable(event))
			checkMessage(event.getMessage());
	}

	@EventSubscriber
	public void onMessageEdit(MessageUpdateEvent event) {
		if (isFilterable(event))
			checkMessage(event.getNewMessage());
	}
}
