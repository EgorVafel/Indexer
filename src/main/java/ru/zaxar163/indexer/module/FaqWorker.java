package ru.zaxar163.indexer.module;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.message.Message;

import ru.zaxar163.indexer.Indexer;
import ru.zaxar163.indexer.module.FaqManager.FaqProblem;

public class FaqWorker {
	public final Set<Long> enabledChannels = Collections.newSetFromMap(new ConcurrentHashMap<>());
	public static final Random random = new Random();
	public final Indexer i;

	public FaqWorker(Indexer i) {
		this.i = i;
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
		i.client.addMessageCreateListener(e -> {
			if (!active(e.getMessage().getServerTextChannel()))
				return;
			if (e.getMessage().getAuthor().isYourself())
				return;
			final FaqProblem problem = this.i.faqManager.findProblem(e.getMessage().getContent());
			if (problem == null)
				return;
			final String sb = solve(problem, e.getMessage());
			e.getMessage().getChannel().sendMessage(sb.toString());
		});
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

	public String solve(FaqProblem problem, Message username) {
		FaqManager.FaqTemplate template = i.faqManager.templates.get(problem.template);
		return solve(problem,username,template.altTemplates == null ? template : i.faqManager.templates.get(template.altTemplates[ random.nextInt(template.altTemplates.length) ]));
	}

	public String solveList(FaqProblem problem, Message username) {
		FaqManager.FaqTemplate template = i.faqManager.templates.get(problem.template);
		return solve(problem,username,template.listTemplate == null ? template : i.faqManager.templates.get(template.listTemplate));
	}

	public String solve(FaqProblem problem, Message username, String template) {
		return solve(problem,username,i.faqManager.templates.get(template));
	}

	public String solve(FaqProblem problem, Message username, FaqManager.FaqTemplate template) {
		return i.faqManager.compileTemplate(template, problem,
				username.getUserAuthor().isPresent() ? username.getUserAuthor().get().getMentionTag() : "", username);
	}

	public void work(final ServerTextChannel ch) {
		if (enabledChannels.contains(ch.getId()))
			removeChannelListener(ch);
		else
			attachChannelListener(ch);
	}
}
