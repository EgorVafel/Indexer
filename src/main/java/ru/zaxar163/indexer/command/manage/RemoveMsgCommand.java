package ru.zaxar163.indexer.command.manage;

import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.permission.PermissionType;

import ru.zaxar163.indexer.Utils;
import ru.zaxar163.indexer.command.Command;

public class RemoveMsgCommand extends Command {

	public RemoveMsgCommand() {
		super("rm", "`!rm <количество>` - удаляет сообщения в диапазоне, максимум 100");
	}

	@Override
	public boolean canUse(final Message message) {
		return super.canUse(message)
				&& Utils.hasAnyPerm(message, PermissionType.ADMINISTRATOR, PermissionType.MANAGE_MESSAGES);
	}

	@Override
	public void onCommand(final Message message, final String[] args) throws Exception {
		if (args.length < 1)
			throw new IllegalArgumentException("Invalid args.");
		final int cnt = Integer.parseInt(args[0]);
		if (cnt < 1 || cnt > 100)
			throw new IllegalArgumentException("Invalid args.");
		final ServerTextChannel ctx = message.getServerTextChannel().get();
		ctx.deleteMessages(ctx.getMessages(cnt).join());
	}
}
