package ru.zaxar163.indexer;

import java.util.Optional;
import java.util.function.Predicate;

import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.user.User;

public class RoleManager {
	public static Predicate<? super ServerTextChannel> channelMatches(final String string) {
		return e -> e.getName().equals(string);
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
}
