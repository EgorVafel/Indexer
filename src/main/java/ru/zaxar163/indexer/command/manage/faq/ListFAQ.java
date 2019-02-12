package ru.zaxar163.indexer.command.manage.faq;

import org.javacord.api.entity.message.Message;

import ru.zaxar163.indexer.command.Command;
import ru.zaxar163.indexer.module.FaqWorker;

public class ListFAQ extends Command {

	private final FaqWorker w;

	public ListFAQ(FaqWorker w) {
		super("listfaq", "`!listfaq` - выводит все FAQ.");
		this.w = w;
	}

	@Override
	public void onCommand(Message message, String[] args) throws Exception {
		message.getServerTextChannel().ifPresent(e -> {
			w.faqManager.problems.forEach((a, b) -> {
				e.sendMessage(new StringBuilder().append("Проблема: ").append(a).append('\n').append("Решение: ").append(FaqWorker.solve(b)).toString());
			});
		});
		message.delete();
	}
}