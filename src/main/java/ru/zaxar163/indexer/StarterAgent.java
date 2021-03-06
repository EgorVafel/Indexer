package ru.zaxar163.indexer;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.jar.JarFile;

public final class StarterAgent {

	private static final class StarterVisitor extends SimpleFileVisitor<Path> {
		private final Instrumentation inst;

		private StarterVisitor(final Instrumentation inst) {
			this.inst = inst;
		}

		@Override
		public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
			if (file.toFile().getName().endsWith(".jar"))
				inst.appendToSystemClassLoaderSearch(new JarFile(file.toFile()));
			return super.visitFile(file, attrs);
		}
	}

	public static Instrumentation inst;
	private static boolean isStarted = false;

	public static boolean isAgentStarted() {
		return StarterAgent.isStarted;
	}

	public static void premain(final String agentArgument, final Instrumentation inst) {
		StarterAgent.inst = inst;
		StarterAgent.isStarted = true;
		try {
			Files.walkFileTree(Paths.get("libraries"), Collections.singleton(FileVisitOption.FOLLOW_LINKS),
					Integer.MAX_VALUE, new StarterVisitor(inst));
		} catch (final IOException e) {
			e.printStackTrace(System.err);
		}
	}
}
