package ru.zaxar163.indexer.command.manage;

import ru.zaxar163.indexer.command.Command;
import sx.blah.discord.Discord4J;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

/**
 * @author xtrafrancyz
 */
public class MassBanCommand extends Command {
	public MassBanCommand() {
		super("massban", "`!massban` - массовый бан людей на сервере");
	}

	@Override
	public boolean canUse(IMessage message) {
		return message.getGuild().getOwnerLongID() == message.getAuthor().getLongID();
	}

	@Override
	public void onCommand(IMessage message, String[] args) throws Exception {
		if (args.length == 0) {
			message.getChannel().sendMessage("Укажите начало ника");
			return;
		}
		String start = String.join(" ", args);
		if (start.length() < 4) {
			message.getChannel()
					.sendMessage("Длина начала ника должна быть хотя бы 4 символа, во избежание ошибочных банов");
			return;
		}
		int bans = 0;
		for (IUser user : message.getGuild().getUsers())
			if (user.getName().startsWith(start)) {
				Discord4J.LOGGER.info("ban " + user.getName());
				message.getGuild().banUser(user.getLongID(), 1);
				bans++;
			}
		message.getChannel().sendMessage("Забанено юзеров: " + bans);
	}
}
