package ru.zaxar163.indexer.command.manage.faq;

import java.util.ArrayList;

import org.javacord.api.entity.message.Message;

import ru.zaxar163.indexer.command.Command;
import ru.zaxar163.indexer.module.FaqWorker;

public class RegisterFAQ extends Command {
	private final FaqWorker w;

	public RegisterFAQ(FaqWorker w) {
		super("regfaq", "`!regfaq \"<ошибка>\" \"<решение>\"` - регистрирует ошибку в FAQ.");
		this.w = w;
	}

	@Override
	public void onCommand(Message message, String[] args) throws Exception {
		int start = 0;
		boolean nStarted = false;
		String str = message.getContent();
		ArrayList<String> strs = new ArrayList<>();
		for (int i = 0; i < str.length(); i++)
			if (str.charAt(i) == '"')
				if (!nStarted) {
					start = i + 1;
					nStarted = true;
				} else if (i != 0) {
					strs.add(str.substring(start, i));
					nStarted = false;
				}
		if (strs.size() > 1)
			w.faq.put(strs.get(0), strs.get(1));
	}
}