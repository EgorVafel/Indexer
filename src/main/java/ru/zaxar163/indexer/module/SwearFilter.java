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

import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.listener.message.MessageCreateListener;
import org.javacord.api.listener.message.MessageEditListener;

import ru.zaxar163.indexer.Indexer;

public class SwearFilter {
	private static final OpenOption[] WRITE_OPTIONS = { StandardOpenOption.CREATE, StandardOpenOption.WRITE,
			StandardOpenOption.TRUNCATE_EXISTING };

	public static void main(final String... args) throws Throwable {
		try (BufferedWriter out = Files.newBufferedWriter(Paths.get("out_swear.txt"), StandardCharsets.UTF_8,
				SwearFilter.WRITE_OPTIONS)) {
			final List<String> lines = Files.readAllLines(Paths.get("in_swear.txt"), StandardCharsets.UTF_8).stream()
					.filter(e -> !e.isEmpty()).map(e -> e.replace('|', '\n')).collect(Collectors.toList());
			for (final String line : lines)
				out.append(line);
			out.flush();
		}
	}

	private static String normalizeWord(String str) {
		if (str.isEmpty())
			return "";
		final char[] chars = str.toCharArray();
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

	private final Set<Long> enabledChannels;

	private final Set<String> badWords;

	private boolean enabled = true;

	public final MessageCreateListener listenerC;
	public final MessageEditListener listenerE;

	public SwearFilter(final Indexer indexer) {
		enabledChannels = new HashSet<>();
		badWords = new HashSet<>();
		listenerC = event -> {
			checkMessage(event.getMessage());
		};
		
		listenerE = event -> {
			if (event.getMessage().isPresent())
				checkMessage(event.getMessage().get());
		};
		
		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(new FileInputStream("badwords.txt"), StandardCharsets.UTF_8))) {
			String word;
			while ((word = reader.readLine()) != null) {
				word = SwearFilter.normalizeWord(word.trim());
				if (!word.isEmpty())
					badWords.add(word);
			}
		} catch (final Exception ex) {
			enabled = false;
			System.err.println("SwearFilter disabled. File 'badwords.txt' not found");
			return;
		}

		if (new File("channels.lst").exists())
			try (BufferedReader readerChannels = new BufferedReader(
					new InputStreamReader(new FileInputStream("channels.lst"), StandardCharsets.UTF_8))) {
				String word;
				while ((word = readerChannels.readLine()) != null) 
					indexer.client.getChannelById(Long.parseLong(word)).ifPresent(t -> t.asTextChannel().ifPresent(this::enableFor));
					} catch (final Exception ex) {
				enabled = false;
				System.err.println("SwearFilter disabled. File 'channels.lst' not found");
				return;
			}

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			try (PrintWriter readerChannels = new PrintWriter(
					new OutputStreamWriter(new FileOutputStream("channels.lst"), StandardCharsets.UTF_8))) {
				enabledChannels.forEach(readerChannels::println);
			} catch (final Exception ex) {
				System.err.println(ex.toString());
			}
		}, "Saving channels thread"));
	}

	private void checkMessage(final Message message) {
		if (message.getAuthor().isYourself())
			return;
		if (hasSwear(message.getContent()))
			message.delete();
	}

	public void disableFor(final TextChannel channel) {
		if (!enabled || !isActive(channel))
			return;
		channel.removeListener(MessageCreateListener.class, listenerC);
		channel.removeListener(MessageEditListener.class, listenerE);
		enabledChannels.remove(channel.getId());
	}

	public void enableFor(final TextChannel channel) {
		if (!enabled || isActive(channel))
			return;
		channel.addMessageCreateListener(listenerC);
		channel.addMessageEditListener(listenerE);
		enabledChannels.add(channel.getId());
	}

	private boolean hasSwear(final String message) {
		for (final String word : message.split(" "))
			if (badWords.contains(SwearFilter.normalizeWord(word)))
				return true;
		return false;
	}

	public boolean isActive(final TextChannel channel) {
		return enabledChannels.contains(channel.getId());
	}

	public boolean isEnabled() {
		return enabled;
	}
}
