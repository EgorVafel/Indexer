package ru.zaxar163.indexer.command;

import java.util.ArrayList;
import java.util.List;

import sx.blah.discord.handle.obj.IMessage;

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

	public boolean canUse(IMessage message) {
		String name = message.getChannel().getName().toLowerCase();
		return name.contains("offtop") || name.contains("fft") || name.contains("bot");
	}

	public abstract void onCommand(IMessage message, String[] args) throws Exception;
}
