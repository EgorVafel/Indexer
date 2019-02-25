package ru.zaxar163.indexer.command.manage;

import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.permission.PermissionType;

import ru.zaxar163.indexer.Utils;
import ru.zaxar163.indexer.command.Command;
import ru.zaxar163.indexer.module.PrivateWorker;

public class UpdatedRulesCommand extends Command {

	private final PrivateWorker privateWorker;

	public UpdatedRulesCommand(final PrivateWorker privateWorker) {
		super("rulesc", "`!rulesc` - очищает кеш правил.");
		this.privateWorker = privateWorker;
	}

	@Override
	public boolean canUse(final Message message) {
		return super.canUse(message)
				&& Utils.hasAnyPerm(message, PermissionType.ADMINISTRATOR, PermissionType.MANAGE_CHANNELS);
	}

	@Override
	public void onCommand(final Message message, final String[] args) throws Exception {
		privateWorker.srvInfo.clear();
		message.getChannel().sendMessage("Успешно очищен кеш правил.");
		message.delete();
	}

}
