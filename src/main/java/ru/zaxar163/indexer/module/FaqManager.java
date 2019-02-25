package ru.zaxar163.indexer.module;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.message.Message;

public class FaqManager {
	public static class FaqPattern {
		public String str;
		public FaqPatternType pattern;

		public FaqPattern(String str, FaqPatternType pattern) {
			this.str = str;
			this.pattern = pattern;
		}
	}

	public enum FaqPatternType {
		CONTAINS, LOW_CONTAINS, NO_SEPARATOR
	}

	public static class FaqProblem {
		public ArrayList<FaqPattern> patterns = new ArrayList<>();
		public ArrayList<String> solutions = new ArrayList<>();
		public String name;
		public String template;
		public String description;
	}

	public static class FaqTemplate {
		public String main;
		public String solutions;
		public String[] altTemplates;
		public String listTemplate; //May be null
	}

	public SortedMap<String, FaqProblem> problems = Collections.synchronizedSortedMap(new TreeMap<>());
	public Map<String, FaqTemplate> templates = new ConcurrentHashMap<>();

	public void addPatternToProblem(String problemName, String str, FaqPatternType type) {
		final FaqProblem problem = problems.get(problemName);
		problem.patterns.add(new FaqPattern(str, type));
	}

	public void addProblem(String name) {
		final FaqProblem problem = new FaqProblem();
		problem.name = name;
		problems.put(name, problem);
	}

	public void addSolutionToProblem(String problemName, String solution) {
		final FaqProblem problem = problems.get(problemName);
		problem.solutions.add(solution);
	}

	public void addTemplate(String templateName, FaqTemplate template) {
		templates.put(templateName, template);
	}

	public String compileTemplate(FaqTemplate template, FaqProblem problem, String username, Message msg) {
		final StringBuilder builder = new StringBuilder();
		int index = 1;
		for (final String solution : problem.solutions) {
			final String appendStr = template.solutions.replace("%_SOLUTION_%", solution).replace("%_INDEX_%", String.valueOf(index));
			builder.append(appendStr);
			index++;
		}
		return FAQChannel(template.main, msg).replace("%USERNAME%", username).replace("%NAME%", problem.name)
				.replace("%DESCRIPTION%", problem.description).replace("%SOLUTIONS%", builder.toString());
	}

	private String FAQChannel(String main, Message msg) {
		final AtomicReference<ServerTextChannel> ref = new AtomicReference<>(null);
		msg.getServer().ifPresent(e -> {
			e.getTextChannels().stream().filter(v -> e.getName().equalsIgnoreCase("faq")).findFirst()
					.ifPresent(ref::set);
		});
		return ref.get() == null ? main : main.replace("%FAQ_CHANNEL_MENTION%", ref.get().getMentionTag());
	}

	public FaqProblem findProblem(String message) {
		for (final Map.Entry<String, FaqProblem> p : problems.entrySet()) {
			final FaqProblem problem = p.getValue();
			for (final FaqPattern pattern : problem.patterns)
				switch (pattern.pattern) {
				case CONTAINS:
					if (message.contains(pattern.str))
						return problem;
					break;
				case LOW_CONTAINS:
					if (message.toLowerCase().contains(pattern.str.toLowerCase()))
						return problem;
					break;
				case NO_SEPARATOR:
					if (message.toLowerCase().replaceAll("\\W", "").contains(pattern.str.toLowerCase()))
						return problem;
					break;
				}
		}
		return null;
	}

	public boolean isProblem(String problemName) {
		return problems.containsKey(problemName);
	}

	public void removePatternIntoProblem(String problemName, int index) {
		final FaqProblem problem = problems.get(problemName);
		problem.patterns.remove(index);
	}

	public void removeProblem(String problemName) {
		problems.remove(problemName);
	}

	public void removeSolutionIntoProblem(String problemName, int index) {
		final FaqProblem problem = problems.get(problemName);
		problem.solutions.remove(index);
	}

	public void removeTemplate(String templateName) {
		templates.remove(templateName);
	}

	public void setDescription(String problemName, String description) {
		final FaqProblem problem = problems.get(problemName);
		problem.description = description;
	}

	public void setTemplate(String problemName, String template) {
		final FaqProblem problem = problems.get(problemName);
		problem.template = template;
	}
}
