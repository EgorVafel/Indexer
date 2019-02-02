package ru.zaxar163.indexer.command.basic;

import org.javacord.api.entity.message.Message;

import ru.zaxar163.indexer.Indexer;
import ru.zaxar163.indexer.command.Command;

public class SrvOwnersManageCmd extends Command {
	private final Indexer i;

	public SrvOwnersManageCmd(Indexer i) {
		super("!srv-admin", "!srv-admin - управляет ролью владелец сервера на этом сервере.");
		this.i = i;
	}

	@Override
	public void onCommand(Message message, String[] args) throws Exception {
	}

	@Override
	public boolean canUse(Message ms) {
		return super.canUse(ms) && ms.getUserAuthor().filter(u -> i.roler.active.getUsers().contains(u) || i.roler.serverOwner.getUsers().contains(u) || i.roler.developer.getUsers().contains(u)).isPresent();
	}
}
