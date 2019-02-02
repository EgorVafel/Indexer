package ru.zaxar163.indexer.command.standard;

import java.util.Map;

import org.javacord.api.entity.message.Message;

import ru.zaxar163.indexer.command.Command;
import ru.zaxar163.indexer.command.CommandManager;
import ru.zaxar163.indexer.module.PrivateWorker;

public class HelpCommand extends Command {
	private final CommandManager commands;
	private final PrivateWorker privateWorker;

	public HelpCommand(final CommandManager commands) {
		super("help", "`!help` - список команд\n" + "`!help <команда>` - информация о команде");
		this.commands = commands;
		this.privateWorker = commands.app.privateWorker;
	}

	@Override
	public void onCommand(final Message message, final String[] args) throws Exception {
		if (args.length != 0) {
			final Command command = commands.getCommand(args[0]);
			if (command == null) {
				message.getChannel().sendMessage("Нет такой команды.");
				return;
			}
			String msg;
			if (command.help != null)
				msg = command.help;
			else
				msg = "У команды !" + command.command + " нет описания :frowning2:";
			message.getChannel().sendMessage(msg);
			return;
		}
		String msg = "Доступные для вас команды: ```";
		boolean first = true;
		for (final Map.Entry<String, Command> entry : commands.registered.entrySet()) {
			if (!entry.getValue().canUse(message))
				continue;
			if (!first)
				msg += ", ";
			else
				first = false;
			msg += entry.getKey();
		}
		msg += "```";
		msg += "Напишите `!help <команда>` на сервере GravitLauncher чтобы посмотреть описание команды";
		message.delete();
		privateWorker.sendMessage(message.getUserAuthor().get(), msg);
	}
}
