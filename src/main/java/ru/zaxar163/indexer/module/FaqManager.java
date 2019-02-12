package ru.zaxar163.indexer.module;

import java.util.*;

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
    public class FaqTemplate
    {
        public String main;
        public String solutions;
    }
    public enum FaqPatternType
    {
        CONTAINS, LOW_CONTAINS, NO_SEPARATOR
    }
    public TreeMap<String, FaqProblem> problems = new TreeMap<>();
    public HashMap<String, FaqTemplate> templates = new HashMap<>();
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
    public void addTemplate(String templateName, FaqTemplate template)
    {
        templates.put(templateName,template);
    }
    public void removeTemplate(String templateName)
    {
        templates.remove(templateName);
    }
    public String compileTemplate(FaqTemplate template, FaqProblem problem, String username)
    {
        StringBuilder builder = new StringBuilder();
        for(String solution : problem.solutions)
        {
            String appendStr = template.solutions.replace("%_SOLUTION_%", solution);
            builder.append(appendStr);
        }
        return template.main.replace("%USERNAME%", username).replace("%NAME%",problem.name).replace("%DESCRIPTION%", problem.description).replace("%SOLUTIONS%", builder.toString());
    }
    public FaqProblem findProblem(String message)
    {
        for(Map.Entry<String,FaqProblem> p : problems.entrySet())
        {
            FaqProblem problem = p.getValue();
            for(FaqPattern pattern : problem.patterns)
            {
                switch(pattern.pattern)
                {

                    case CONTAINS:
                        if(message.contains(pattern.str)) return problem;
                        break;
                    case LOW_CONTAINS:
                        if(message.toLowerCase().contains(pattern.str.toLowerCase())) return problem;
                        break;
                    case NO_SEPARATOR:
                        if(message.toLowerCase().replaceAll("\\W","").contains(pattern.str.toLowerCase())) return problem;
                        break;
                }
            }
        }
        return null;
    }
}
