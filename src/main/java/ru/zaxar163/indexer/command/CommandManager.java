package ru.zaxar163.indexer.command;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.permission.Role;

import ru.zaxar163.indexer.Indexer;

public class CommandManager {
	public final Indexer app;
	public Map<String, Command> registered;
	public Map<String, Command> alises;
	public Role developer = null;

	public CommandManager(Indexer app) {
		this.app = app;
		registered = new HashMap<>();
		alises = new HashMap<>();
	}

	public Command getCommand(String cmd) {
		Command command = registered.get(cmd);
		if (command != null)
			return command;
		return alises.get(cmd);
	}

	public void process(Message message) {
		try {
			if (!message.getChannel().asServerTextChannel().isPresent()) return;
			if (developer == null)
				developer = message.getChannel().asServerTextChannel().get().getServer().getRoles().stream().filter(e -> e.getName().equals("Developer"))
						.findFirst().get();
			String text = message.getContent().substring(1);
			String[] args = text.split(" ");
			Command command = getCommand(args[0].toLowerCase());
			if (command == null || !command.canUse(message))
				return;
			command.onCommand(message, Arrays.copyOfRange(args, 1, args.length));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void registerCommand(Command command) {
		registered.put(command.command, command);
		for (String cmd : command.aliases)
			alises.put(cmd, command);
	}
}
