package ru.zaxar163.indexer.module;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;

public class FaqManager {
    public class FaqProblem
    {
        public ArrayList<FaqPattern> patterns = new ArrayList<>();
        public ArrayList<String> solutions = new ArrayList<>();
        public String name;
        public String template;
        public String description;
    }
    public class FaqPattern
    {
        public String str;
        public FaqPatternType pattern;

        public FaqPattern(String str, FaqPatternType pattern) {
            this.str = str;
            this.pattern = pattern;
        }
    }
    public enum FaqPatternType
    {
        COMPARE
    }
    public TreeMap<String, FaqProblem> problems = new TreeMap<>();
    public HashMap<String, String> templates = new HashMap<>();
    public void addProblem(String name)
    {
        FaqProblem problem = new FaqProblem();
        problem.name = name;
        problems.put(name,problem);
    }
    public void addPatternToProblem(String problemName, String str, FaqPatternType type)
    {
        FaqProblem problem = problems.get(problemName);
        problem.patterns.add(new FaqPattern(str,type));
    }
    public void addSolutionToProblem(String problemName, String solution)
    {
        FaqProblem problem = problems.get(problemName);
        problem.solutions.add(solution);
    }
    public void setDescription(String problemName, String description)
    {
        FaqProblem problem = problems.get(problemName);
        problem.description = description;
    }
    public void setTemplate(String problemName, String template)
    {
        FaqProblem problem = problems.get(problemName);
        problem.template = template;
    }
    public void removeProblem(String problemName)
    {
        problems.remove(problemName);
    }
    public void removePatternIntoProblem(String problemName, int index)
    {
        FaqProblem problem = problems.get(problemName);
        problem.patterns.remove(index);
    }
    public void removeSolutionIntoProblem(String problemName, int index)
    {
        FaqProblem problem = problems.get(problemName);
        problem.solutions.remove(index);
    }
    public boolean isProblem(String problemName)
    {
        return problems.containsKey(problemName);
    }
}
