package ru.zaxar163.indexer.command.manage.faq;

import org.javacord.api.entity.message.Message;

import ru.zaxar163.indexer.command.Command;
import ru.zaxar163.indexer.module.FaqWorker;

public class EnableFAQ extends Command {

	private final FaqWorker w;

	public EnableFAQ(FaqWorker w) {
		super("faq", "`!faq` - включает/выключает проверку сообщений на частый вопрос в канале");
		this.w = w;
	}

	@Override
	public void onCommand(Message message, String[] args) throws Exception {
		if (args.length < 1)
			throw new IllegalArgumentException("Illegal args");
		message.getMentionedChannels().stream().forEach(w::work);
		message.delete();
	}
}
