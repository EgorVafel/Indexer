package ru.zaxar163.indexer.command.manage;

import java.util.Iterator;
import java.util.stream.Stream;

import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.permission.PermissionType;

import ru.zaxar163.indexer.Indexer;
import ru.zaxar163.indexer.RoleManager;
import ru.zaxar163.indexer.command.Command;

public class RemoveMsgCommand1 extends Command {

	public RemoveMsgCommand1(final Indexer indexer) {
		super("removemsg", "!removemsg - удаляет сообщения в диапазоне");
		super.aliases.add("rmd");
	}

	@Override
	public boolean canUse(final Message message) {
		return super.canUse(message) && RoleManager.hasAnyPerm(message, PermissionType.ADMINISTRATOR, PermissionType.MANAGE_MESSAGES);
	}

	@Override
	public void onCommand(final Message message, final String[] args) throws Exception {
		final Stream<Message> msgs = message.getServerTextChannel().get().getMessagesAsStream();
		if (args.length < 1)
			throw new IllegalArgumentException("Invalid args.");
		final int cnt = Integer.parseInt(args[0]);
		if (cnt < 1) throw new IllegalArgumentException("Invalid args.");
		final Iterator<Message> it = msgs.iterator();
		if (!it.hasNext())
			return;
		for (int i = 0; i <= cnt && it.hasNext(); i++)
			it.next().delete();
		msgs.close();
	}
}
