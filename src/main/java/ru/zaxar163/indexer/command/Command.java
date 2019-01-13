package ru.zaxar163.indexer.command;

import java.util.ArrayList;
import java.util.List;

import org.javacord.api.entity.message.Message;

public abstract class Command {
	public final String command;
	public final String help;
	public final List<String> aliases = new ArrayList<>();

	public Command(String command) {
		this(command, null);
	}

	public Command(String command, String help) {
		this.command = command;
		this.help = help;
	}

	public boolean canUse(Message message) {
		if (!message.getChannel().asServerChannel().isPresent()) return false;
		String name = message.getChannel().asServerChannel().get().getName().toLowerCase();
		return name.contains("offtop") || name.contains("fft") || name.contains("bot");
	}

	public abstract void onCommand(Message message, String[] args) throws Exception;
}
