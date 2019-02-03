package ru.zaxar163.indexer.command.manage;

import java.util.Iterator;
import java.util.stream.Stream;

import org.javacord.api.entity.message.Message;

import ru.zaxar163.indexer.Indexer;
import ru.zaxar163.indexer.RoleManager;
import ru.zaxar163.indexer.command.Command;

public class RemoveMsgCommand extends Command {

	private final Indexer indexer;

	public RemoveMsgCommand(final Indexer indexer) {
		super("removemsg", "!removemsg - удаляет сообщения в диапазоне");
		this.indexer = indexer;
	}

	@Override
	public boolean canUse(final Message message) {
		return RoleManager.hasRole(indexer.roler.middleDeveloper, message.getUserAuthor());
	}

	@Override
	public void onCommand(final Message message, final String[] args) throws Exception {
		final Stream<Message> msgs = message.getServerTextChannel().get().getMessagesAsStream();
		if (args.length < 1)
			throw new IllegalArgumentException("Invalid args.");
		final int cnt = Integer.parseInt(args[0]);
		final Iterator<Message> it = msgs.iterator();
		if (!it.hasNext())
			return;
		for (int i = 0; i < cnt && it.hasNext(); i++)
			it.next().delete();
		msgs.close();
	}
}
