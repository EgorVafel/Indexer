package ru.zaxar163.indexer.command.manage;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.user.User;

import ru.zaxar163.indexer.Indexer;
import ru.zaxar163.indexer.RoleManager;
import ru.zaxar163.indexer.command.Command;

public class RemoveMsgCommand2 extends Command {
	public RemoveMsgCommand2(final Indexer indexer) {
		super("removemsgnamed", "!removemsgnamed - удаляет сообщения пользователя, лимит задан параметрами");
		super.aliases.add("rmn");
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
		final Iterator<Message> it = msgs.iterator();
		if (!it.hasNext())
			return;
		int i = 0;
		List<User> users = message.getMentionedUsers();
		while (it.hasNext() && i < cnt) {
			Message i1 = it.next();
			if (i1.getUserAuthor().filter(users::contains).isPresent()) {
				i1.delete();
				i++;
			}
		}
		msgs.close();
	}
}
