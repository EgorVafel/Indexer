package ru.zaxar163.indexer.command.standard;

import java.util.Map;

import org.javacord.api.entity.message.Message;

import ru.zaxar163.indexer.command.Command;
import ru.zaxar163.indexer.command.CommandManager;

public class HelpCommand extends Command {
	private final CommandManager commands;

	public HelpCommand(CommandManager commands) {
		super("help", "`!help` - список команд\n" + "`!help <команда>` - информация о команде");
		this.commands = commands;
	}

	@Override
	public void onCommand(Message message, String[] args) throws Exception {
		if (args.length != 0) {
			Command command = commands.getCommand(args[0]);
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
		for (Map.Entry<String, Command> entry : commands.registered.entrySet()) {
			if (!entry.getValue().canUse(message))
				continue;
			if (!first)
				msg += ", ";
			else
				first = false;
			msg += entry.getKey();
		}
		msg += "```";
		msg += "Напишите `!help <команда>` чтобы посмотреть описание команды";
		message.getChannel().sendMessage(msg);
	}
}
