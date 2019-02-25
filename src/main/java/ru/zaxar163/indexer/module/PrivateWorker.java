package ru.zaxar163.indexer.module;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.PrivateChannel;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

public class PrivateWorker {
	public final Map<Server, List<MessageBuilder>> srvInfo;
	public PrivateWorker(DiscordApi client) {
		srvInfo = new ConcurrentHashMap<>();
		client.addServerMemberJoinListener(e -> {
			srvInfo.computeIfAbsent(e.getServer(), a -> {
				final List<MessageBuilder> msgs = new ArrayList<>(1);
				a.getTextChannelsByName("important").forEach(f -> {
					f.getMessages(10).join().stream().map(m -> new MessageBuilder().setContent(m.getContent())).forEach(msgs::add);
				});
				a.getTextChannelsByName("rules").forEach(f -> {
					f.getMessages(10).join().stream().map(m -> new MessageBuilder().setContent(m.getContent())).forEach(msgs::add);
				});
				return msgs;
			}).forEach(m -> sendMessage(e.getUser(), m));
		});
	}
	
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
