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
			w.faq.forEach((k, v) -> {
				e.sendMessage("Ошибка: " + k + "\nРешение: " + v);
			});
		});
		message.delete();
	}
}