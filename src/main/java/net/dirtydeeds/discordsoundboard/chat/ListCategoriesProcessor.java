package net.dirtydeeds.discordsoundboard.chat;

import java.util.List;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.events.message.MessageReceivedEvent;
import net.dv8tion.jda.utils.SimpleLog;

public class ListCategoriesProcessor extends AbstractChatCommandProcessor {

	public static final SimpleLog LOG = SimpleLog.getLog("ListCategoriesProcessor");
	
	public ListCategoriesProcessor(String prefix, SoundboardBot soundPlayer) {
		super(prefix, soundPlayer);
	}

	protected void handleEvent(MessageReceivedEvent event, String message) {
        List<String> categories = bot.getSoundCategories();
        if (categories.size() > 0) {
        	StringBuilder sb = new StringBuilder();
        	int i = 0;
        	for (String category : categories) {
        		sb.append("**" + category + "** ");
        		++i;
        		if (i != categories.size()) sb.append(" / ");
        	}
        	event.getChannel().sendMessageAsync("Here is a list of categories:\n\n" + 
        			sb.toString(), null);
            LOG.info("Listed categories for user " + event.getAuthor().getUsername());
        } else {
        	event.getChannel().sendMessageAsync("There are no categories found.", null);
        }
	}
	
	@Override
	public String getCommandHelpString() {
		return "`" + getPrefix() + "` - lists all sound file categories";
	}
	
}
