package ru.zaxar163.indexer.mysql;

public class Column {
	final String name;

	Column(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
