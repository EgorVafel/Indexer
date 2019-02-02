package ru.zaxar163.indexer.command;

import java.util.ArrayList;
import java.util.List;

import org.javacord.api.entity.message.Message;

public abstract class Command {
	public final String command;
	public final String help;
	public final List<String> aliases = new ArrayList<>();

	public Command(final String command) {
		this(command, null);
	}

	public Command(final String command, final String help) {
		this.command = command;
		this.help = help;
	}

	public boolean canUse(final Message message) {
		if (!message.getChannel().asServerChannel().isPresent())
			return false;
		return true;
	}

	public abstract void onCommand(Message message, String[] args) throws Exception;
}
