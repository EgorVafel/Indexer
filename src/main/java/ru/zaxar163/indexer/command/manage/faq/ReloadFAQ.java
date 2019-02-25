package ru.zaxar163.indexer.command.manage.faq;

import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.permission.PermissionType;

import ru.zaxar163.indexer.Utils;
import ru.zaxar163.indexer.command.Command;
import ru.zaxar163.indexer.module.FaqWorker;

public class ReloadFAQ extends Command {
	private final FaqWorker w;

	public ReloadFAQ(FaqWorker w) {
		super("reloadfaq", "`!reloadfaq` - перезагружает списки FAQ.");
		this.w = w;
	}

	@Override
	public boolean canUse(final Message message) {
		return super.canUse(message)
				&& Utils.hasAnyPerm(message, PermissionType.ADMINISTRATOR, PermissionType.MANAGE_MESSAGES);
	}

	@Override
	public void onCommand(Message message, String[] args) throws Exception {
		w.i.faqManager = w.i.readFaqDataBase();
		message.getChannel().sendMessage("Успешно перезагружены листы FAQ.");
		message.delete();
	}
}