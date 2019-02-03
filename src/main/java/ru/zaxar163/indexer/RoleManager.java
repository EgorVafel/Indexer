package ru.zaxar163.indexer;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

public class RoleManager {
	public static Predicate<? super ServerTextChannel> channelMatches(final String string) {
		return e -> e.getName().equals(string);
	}

	public static boolean hasRole(final Role r, final Optional<User> auth) {
		if (!auth.isPresent())
			return false;
		return r.getUsers().parallelStream().anyMatch(auth.get()::equals);
	}

	public List<Role> roles;

	public Server gravitLauncher;
	public final Map<Server, List<Role>> rolesMap;
	public Role maintainer;
	public Role developer;

	public Role middleDeveloper;
	public Role juniorDeveloper;
	public Role serverOwner;

	public Role betaTester;

	public Role active;

	public Role fft;

	public ServerTextChannel github;

	private final DiscordApi client;

	public RoleManager(final DiscordApi client) {
		this.client = client;

		rolesMap = new ConcurrentHashMap<>();
		roles = null;
		gravitLauncher = null;
		rehash();
	}

	public void rehash() {
		client.getServers().stream().forEach(e -> rolesMap.put(e, e.getRoles()));
		gravitLauncher = client.getServers().stream().filter(e -> e.getName().toLowerCase().contains("gravitlauncher"))
				.findFirst().get();
		roles = gravitLauncher.getRoles();
		maintainer = roles.stream().filter(e -> "Maintainer".equals(e.getName())).findFirst().get();
		developer = roles.stream().filter(e -> "Developer".equals(e.getName())).findFirst().get();
		middleDeveloper = roles.stream().filter(e -> "Middle Developer".equals(e.getName())).findFirst().get();
		juniorDeveloper = roles.stream().filter(e -> "Junior Developer".equals(e.getName())).findFirst().get();
		serverOwner = roles.stream().filter(e -> e.getName().contains("Владелец сервера")).findFirst().get();
		betaTester = roles.stream().filter(e -> e.getName().contains("Бета-тестер")).findFirst().get();
		active = roles.stream().filter(e -> e.getName().contains("Активный участник")).findFirst().get();
		fft = roles.stream().filter(e -> "FFTeam".equals(e.getName())).findFirst().get();
		github = gravitLauncher.getChannelsByName("github").stream().findFirst().get().asServerTextChannel().get();
	}
}
