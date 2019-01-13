package ru.zaxar163.indexer.command.manage;

import org.javacord.api.entity.message.Message;

import ru.zaxar163.indexer.Indexer;
import ru.zaxar163.indexer.command.Command;
import ru.zaxar163.indexer.module.SwearFilter;

/**
 * @author xtrafrancyz
 */
public class SwearFilterCommand extends Command {
	public SwearFilterCommand() {
		super("swearfilter", "`!swearfilter` - включает/выключает фильтр мата в канале");
	}

	@Override
	public boolean canUse(Message message) {
		if (message.getUserAuthor().isPresent()) return message.getChannel().asServerTextChannel().get().getServer().getRoles(message.getUserAuthor().get()).contains(Indexer.instance().commandManager.developer)	;
		return false;
	}

	@Override
	public void onCommand(Message message, String[] args) throws Exception {
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
