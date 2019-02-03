package ru.zaxar163.indexer;

import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.user.User;

public class Utils {
	public static final OpenOption[] WRITE_OPTIONS = { StandardOpenOption.CREATE, StandardOpenOption.WRITE,
			StandardOpenOption.TRUNCATE_EXISTING };

	public static Predicate<? super ServerTextChannel> channelMatches(final String string) {
		return e -> e.getName().equals(string);
	}

	public static void filterSrvList(final DiscordApi client, final Set<Long> enabledChannels) {
		final List<Long> applied = new ArrayList<>();
		client.getServerTextChannels().stream().mapToLong(e -> e.getId()).forEach(e -> {
			if (enabledChannels.contains(Long.valueOf(e)))
				applied.add(e);
		});
		enabledChannels.removeIf(e -> !applied.contains(e));
	}

	public static boolean hasAnyPerm(final Message auth, final PermissionType... r) {
		if (!auth.getUserAuthor().isPresent())
			return false;
		return auth.getServerTextChannel().get().hasAnyPermission(auth.getUserAuthor().get(), r);
	}

	public static boolean hasPerm(final Message auth, final PermissionType... r) {
		if (!auth.getUserAuthor().isPresent())
			return false;
		return auth.getServerTextChannel().get().hasPermissions(auth.getUserAuthor().get(), r);
	}

	public static boolean hasRole(final Role r, final Optional<User> auth) {
		if (!auth.isPresent())
			return false;
		return r.getUsers().parallelStream().anyMatch(auth.get()::equals);
	}

	public static void main(final String... args) throws Throwable {
		try (BufferedWriter out = Files.newBufferedWriter(Paths.get("out_swear.txt"), StandardCharsets.UTF_8,
				WRITE_OPTIONS)) {
			final List<String> lines = Files.readAllLines(Paths.get("in_swear.txt"), StandardCharsets.UTF_8).stream()
					.filter(e -> !e.isEmpty()).map(e -> e.replace('|', '\n')).collect(Collectors.toList());
			for (final String line : lines)
				out.append(line);
			out.flush();
		}
	}
}
