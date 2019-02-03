package ru.zaxar163.indexer.command.manage;

import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.permission.PermissionType;

import ru.zaxar163.indexer.Indexer;
import ru.zaxar163.indexer.Utils;
import ru.zaxar163.indexer.command.Command;
public class ExecChannelCommand extends Command {

	private final Indexer indexer;

	public ExecChannelCommand(final Indexer indexer) {
		super("execable", "`!execable` - включает/выключает исполнение команд в канале");
		this.indexer = indexer;
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
		message.getMentionedChannels().stream()
				.filter(Utils.channelMatches(args[0])).forEach(indexer.commandManager::work);
		message.delete();
	}

}
