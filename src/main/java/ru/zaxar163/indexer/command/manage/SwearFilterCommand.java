package ru.zaxar163.indexer.command.manage;

import ru.zaxar163.indexer.Indexer;
import ru.zaxar163.indexer.command.Command;
import ru.zaxar163.indexer.module.SwearFilter;
import sx.blah.discord.handle.obj.IMessage;

/**
 * @author xtrafrancyz
 */
public class SwearFilterCommand extends Command {
	public SwearFilterCommand() {
		super("swearfilter", "`!swearfilter` - включает/выключает фильтр мата в канале");
	}

	@Override
	public boolean canUse(IMessage message) {
		return message.getAuthor().hasRole(Indexer.instance().commandManager.developer);
	}

	@Override
	public void onCommand(IMessage message, String[] args) throws Exception {
		SwearFilter swearFilter = Indexer.instance().swearFilter;
		if (!swearFilter.isEnabled()) {
			message.getChannel().sendMessage("Фильтр мата не настроен.");
			message.delete();
			return;
		}
		if (swearFilter.isActive(message.getChannel())) {
			swearFilter.disableFor(message.getChannel());
			message.getChannel().sendMessage("Фильтр мата для этого канала теперь **отключен**");
			message.delete();
		} else {
			swearFilter.enableFor(message.getChannel());
			message.getChannel().sendMessage("Фильтр мата для этого канала теперь **включен**");
			message.delete();
		}
	}
}
