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
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

import ru.zaxar163.indexer.Indexer;

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

	private final Set<Long> enabledChannels;

	private final Set<String> badWords;

	private boolean enabled = true;

	public SwearFilter(Indexer indexer) {
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
			System.err.println("SwearFilter disabled. File 'badwords.txt' not found");
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
				System.err.println("SwearFilter disabled. File 'channels.lst' not found");
				return;
			}

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			try (PrintWriter readerChannels = new PrintWriter(
					new OutputStreamWriter(new FileOutputStream("channels.lst"), StandardCharsets.UTF_8))) {
				enabledChannels.forEach(readerChannels::println);
			} catch (Exception ex) {
				System.err.println(ex.toString());
			}
		}, "Saving channels thread"));
		
		indexer.client.addMessageCreateListener(event -> {
			if (isActive(event.getChannel()) && canCheck(event.getMessage().getUserAuthor(), event.getMessage().getServer()))
				checkMessage(event.getMessage());
		});
		indexer.client.addMessageEditListener(event -> {
			if (isActive(event.getChannel()) && event.getMessage().isPresent())
				if (canCheck(event.getMessage().get().getUserAuthor(), event.getMessage().get().getServer()))
					checkMessage(event.getMessage().get());
		});
	}

	private boolean canCheck(Optional<User> userAuthor, Optional<Server> srv) {
		if (userAuthor.isPresent() && srv.isPresent()) {
			User u = userAuthor.get();
			if (srv.get().hasPermission(u, PermissionType.MANAGE_CHANNELS)) return false;
		}
		return true;
	}

	private void checkMessage(Message message) {
		if (message.getAuthor().isYourself()) return;
		if (hasSwear(message.getContent()))
			message.delete()/*.complete(null)*/;
	}
	
	public void disableFor(Channel channel) {
		if (!enabled || !isActive(channel))
			return;
		enabledChannels.remove(channel.getId());
	}

	public void enableFor(Channel channel) {
		if (!enabled || isActive(channel))
			return;
		enabledChannels.add(channel.getId());
	}

	private boolean hasSwear(String message) {
		for (String word : message.split(" "))
			if (badWords.contains(normalizeWord(word)))
				return true;
		return false;
	}

	public boolean isActive(Channel channel) {
		return enabledChannels.contains(channel.getId());
	}

	public boolean isEnabled() {
		return enabled;
	}
}
