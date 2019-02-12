package ru.zaxar163.indexer.command.manage.faq;

import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.permission.PermissionType;

import ru.zaxar163.indexer.Utils;
import ru.zaxar163.indexer.command.Command;
import ru.zaxar163.indexer.module.FaqWorker;

public class EnableFAQ extends Command {

	private final FaqWorker w;

	public EnableFAQ(FaqWorker w) {
		super("faq", "`!faq` - включает/выключает проверку сообщений на частый вопрос в канале");
		this.w = w;
	}

	@Override
	public boolean canUse(final Message message) {
		return super.canUse(message)
				&& Utils.hasAnyPerm(message, PermissionType.ADMINISTRATOR, PermissionType.MANAGE_MESSAGES);
	}

	@Override
	public void onCommand(Message message, String[] args) throws Exception {
		if (args.length < 1)
			throw new IllegalArgumentException("Illegal args");
		message.getMentionedChannels().stream().forEach(w::work);
		message.delete();
	}
}
