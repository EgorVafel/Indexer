package ru.zaxar163.indexer.command.manage;

import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.permission.PermissionType;

import ru.zaxar163.indexer.Utils;
import ru.zaxar163.indexer.command.Command;
import ru.zaxar163.indexer.command.CommandManager;

public class ExecChannelCommand extends Command {

	private final CommandManager commandManager;

	public ExecChannelCommand(final CommandManager commandManager) {
		super("execable", "`!execable` - включает/выключает исполнение команд в канале");
		this.commandManager = commandManager;
	}

	@Override
	public boolean canUse(final Message message) {
		return super.canUse(message)
				&& Utils.hasAnyPerm(message, PermissionType.ADMINISTRATOR, PermissionType.MANAGE_CHANNELS);
	}

	@Override
	public void onCommand(final Message message, final String[] args) throws Exception {
		if (args.length < 1)
			throw new IllegalArgumentException("Illegal args");
		message.getMentionedChannels().stream().forEach(commandManager::work);
		message.delete();
	}

}
