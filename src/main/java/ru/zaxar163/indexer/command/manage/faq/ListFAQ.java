package ru.zaxar163.indexer.command.manage.faq;

import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.permission.PermissionType;

import ru.zaxar163.indexer.Utils;
import ru.zaxar163.indexer.command.Command;
import ru.zaxar163.indexer.module.FaqWorker;

public class ListFAQ extends Command {

	private final FaqWorker w;

	public ListFAQ(FaqWorker w) {
		super("listfaq", "`!listfaq` - выводит все FAQ.");
		this.w = w;
	}

	@Override
	public boolean canUse(final Message message) {
		return super.canUse(message)
				&& Utils.hasAnyPerm(message, PermissionType.ADMINISTRATOR, PermissionType.MANAGE_MESSAGES);
	}

	@Override
	public void onCommand(Message message, String[] args) throws Exception {
		if (!message.getUserAuthor().isPresent()) {
			message.delete();
			return;
		}
		message.getServerTextChannel().ifPresent(e -> {
			w.i.faqManager.problems.forEach((a, b) -> e.sendMessage(w.solveList(b, message)));
		});
		message.delete();
	}
}