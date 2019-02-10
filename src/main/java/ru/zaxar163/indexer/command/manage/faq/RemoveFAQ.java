package ru.zaxar163.indexer.command.manage.faq;

import org.javacord.api.entity.message.Message;

import ru.zaxar163.indexer.command.Command;
import ru.zaxar163.indexer.module.FaqWorker;

public class RemoveFAQ extends Command {

	private final FaqWorker w;
	
	public RemoveFAQ(FaqWorker w) {
		super("remfaq", "`!remfaq <проблема>` - удаляет проблему из FAQ");
		this.w = w;
	}

	@Override
	public void onCommand(Message message, String[] args) throws Exception {
		if (args.length < 1)
			throw new IllegalArgumentException("Illegal args");
		w.faq.entrySet().removeIf(e -> {
			return message.getContent().contains(e.getKey());
		});
		message.delete();
	}
}