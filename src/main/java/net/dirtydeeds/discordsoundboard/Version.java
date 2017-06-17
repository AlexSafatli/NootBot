package net.dirtydeeds.discordsoundboard;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.core.entities.User;

public class Version {

	public static final String NAME    = "NootBot";
	public static final String VERSION = "2.3.7";
	public static final String AUTHOR  = "Asaph";
	
	public static String getAuthor(SoundboardBot bot) {
		String author = AUTHOR;
		User user = bot.getUserByName(author);
		if (user != null) author = user.getAsMention();
		return author;
	}
	
}