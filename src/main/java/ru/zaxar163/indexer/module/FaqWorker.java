package ru.zaxar163.indexer.module;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.ServerTextChannel;

public class FaqWorker {
	public final Map<String, String> faq = new ConcurrentHashMap<>();
	public final Set<Long> enabledChannels = Collections.newSetFromMap(new ConcurrentHashMap<>());
	public final FaqManager faqManager;
	public FaqWorker(DiscordApi api, FaqManager faqManager) {
		this.faqManager = faqManager;
		if (new File("channels_faq.lst").exists())
			try (BufferedReader readerChannels = new BufferedReader(
					new InputStreamReader(new FileInputStream("channels_cmd.lst"), StandardCharsets.UTF_8))) {
				String word;
				while ((word = readerChannels.readLine()) != null)
					enabledChannels.add(Long.parseLong(word));
			} catch (final Exception ex) {
				System.err.println("File 'channels_cmd.lst' parsing error");
				return;
			}
		if (new File("msgs.lst").exists())
			try (DataInputStream reader = new DataInputStream(new FileInputStream("msgs.lst"))) {
				while (reader.available() > 0)
					faq.put(reader.readUTF(), reader.readUTF());
			} catch (final Exception ex) {
				System.err.println("File 'msgs.lst' parsing error");
				return;
			}
		api.addMessageCreateListener(e -> {
			if (!active(e.getMessage().getServerTextChannel()))
				return;
			String str = e.getMessage().getContent();
			faq.forEach((k, v) -> {
				if (str.contains(k))
					e.getMessage().getServerTextChannel().ifPresent(a -> a.sendMessage(v));
			});
		});

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			try (PrintWriter readerChannels = new PrintWriter(
					new OutputStreamWriter(new FileOutputStream("channels_faq.lst"), StandardCharsets.UTF_8))) {
				enabledChannels.forEach(readerChannels::println);
			} catch (final Exception ex) {
				System.err.println(ex.toString());
			}
			try (DataOutputStream readerChannels = new DataOutputStream(new FileOutputStream("msgs.lst"))) {
				faq.entrySet().forEach(e -> {
					try {
						readerChannels.writeUTF(e.getKey());
						readerChannels.writeUTF(e.getValue());
					} catch (IOException e1) {
						System.err.println(e1.toString());
					}
				});
			} catch (final Exception ex) {
				System.err.println(ex.toString());
			}
		}, "Saving channels thread"));
	}

	private boolean active(Optional<ServerTextChannel> serverTextChannel) {
		if (!serverTextChannel.isPresent())
			return false;
		return enabledChannels.contains(serverTextChannel.get().getId());
	}

	public void attachChannelListener(final ServerTextChannel ch) {
		enabledChannels.add(ch.getId());
		ch.sendMessage("Проверка FAQ для этого канала теперь **включена**");
	}

	public void removeChannelListener(final ServerTextChannel ch) {
		enabledChannels.removeIf(e -> e.longValue() == ch.getId());
		ch.sendMessage("Проверка FAQ для этого канала теперь **отключена**");
	}

	public void work(final ServerTextChannel ch) {
		if (enabledChannels.contains(ch.getId()))
			removeChannelListener(ch);
		else
			attachChannelListener(ch);
	}
}
