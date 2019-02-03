package ru.zaxar163.indexer.module;

import org.javacord.api.entity.channel.PrivateChannel;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.user.User;

public class PrivateWorker {
	public static PrivateChannel getOrCreateForUser(final User u) {
		if (u.getPrivateChannel().isPresent())
			return u.getPrivateChannel().get();
		try {
			return u.openPrivateChannel().join();
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void sendMessage(final User u, final MessageBuilder msg) {
		getOrCreateForUser(u);
		msg.send(u);
	}

	public static void sendMessage(final User u, final String msg) {
		getOrCreateForUser(u).sendMessage(msg);
	}
}
