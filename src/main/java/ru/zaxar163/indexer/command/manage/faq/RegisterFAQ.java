package ru.zaxar163.indexer.command.manage.faq;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
		if (args.length < 1)
			throw new IllegalArgumentException("Illegal args");
		
	}
	
	public static void main(String[] args) {
		String str = "\"fgkorkgR$43\" \"fr3rogk\"";
		System.out.println(str.substring(1));
		int start = 0;
		boolean nStarted = false;
		for (int i = 0; i < str.length(); i++) {
			if (str.charAt(i) == '"')
				if (!nStarted) { 
					start = i+1;
					nStarted = true;
				} else if (i != 0) {
					System.out.println(str.substring(start, i));
					nStarted = false;
				}
		}
	}
}