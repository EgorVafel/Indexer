package ru.zaxar163.indexer.module;

import java.util.concurrent.ExecutionException;

import org.javacord.api.entity.channel.PrivateChannel;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.user.User;

public class PrivateWorker {
	public final PrivateChannel getOrCreateForUser(User u) {
		if (u.getPrivateChannel().isPresent()) return u.getPrivateChannel().get();
		try {
			return u.openPrivateChannel().join();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public final void sendMessage(User u, String msg) {
		getOrCreateForUser(u).sendMessage(msg);
	}
	
	public final void sendMessage(User u, MessageBuilder msg) {
		getOrCreateForUser(u);
		msg.send(u);
	}
}
