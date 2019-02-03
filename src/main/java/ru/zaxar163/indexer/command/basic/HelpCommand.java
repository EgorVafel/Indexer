package ru.zaxar163.indexer.command.basic;

import java.util.Map;

import org.javacord.api.entity.message.Message;

import ru.zaxar163.indexer.command.Command;
import ru.zaxar163.indexer.command.CommandManager;
import ru.zaxar163.indexer.module.PrivateWorker;

public class HelpCommand extends Command {
	private final CommandManager commands;

	public HelpCommand(final CommandManager commands) {
		super("help", "`!help` - список команд\n" + "`!help <команда>` - информация о команде");
		super.aliases.add("h");
		this.commands = commands;
	}

	@Override
	public void onCommand(final Message message, final String[] args) throws Exception {
		if (args.length != 0) {
			final Command command = commands.getCommand(args[0]);
			if (command == null) {
				PrivateWorker.sendMessage(message.getUserAuthor().get(), "Нет такой команды.");
				message.delete();
				return;
			}
			String msg;
			if (command.help != null)
				msg = command.help;
			else
				msg = "У команды !" + command.command + " нет описания :frowning2:";
			PrivateWorker.sendMessage(message.getUserAuthor().get(), msg);
			message.delete();
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
		msg += "Напишите `!help <команда>` на сервере " + message.getServer().get().getName() + " чтобы посмотреть описание команды";
		message.delete();
		PrivateWorker.sendMessage(message.getUserAuthor().get(), msg);
	}
}
