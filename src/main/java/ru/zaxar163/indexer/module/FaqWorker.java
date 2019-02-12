package ru.zaxar163.indexer.module;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.ServerTextChannel;

import ru.zaxar163.indexer.module.FaqManager.FaqProblem;

public class FaqWorker {
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
		api.addMessageCreateListener(e -> {
			if (!active(e.getMessage().getServerTextChannel()))
				return;
			if (e.getMessage().getAuthor().isYourself()) return;
			FaqProblem problem = faqManager.findProblem(e.getMessage().getContent());
			if (problem == null) return;
			StringBuilder sb = solve(problem);
			e.getMessage().getUserAuthor().ifPresent(u -> {
				sb.append(u.getMentionTag());
			});
			e.getMessage().getChannel().sendMessage(sb.toString());
		});
	}

	public static StringBuilder solve(FaqProblem problem) {
		StringBuilder sb = new StringBuilder();
		problem.solutions.forEach(s -> {
			sb.append(s);
			sb.append('\n');
		});
		return sb;
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
