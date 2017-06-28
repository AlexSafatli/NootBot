package net.dirtydeeds.discordsoundboard.chat;

import net.dirtydeeds.discordsoundboard.org.Category;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.utils.SimpleLog;

public class ListCategoriesProcessor extends AbstractChatCommandProcessor {

	public static final SimpleLog LOG = SimpleLog.getLog("ListCategoriesProcessor");
	
	public ListCategoriesProcessor(String prefix, SoundboardBot soundPlayer) {
		super(prefix, "Categories", soundPlayer);
	}

	protected void handleEvent(MessageReceivedEvent event, String message) {
        Category root = bot.getDispatcher().getCategoryTree();
        if (root.getChildren().size() > 0) {
        	m(event, "Here is a list of categories and subcategories:\n\n" + 
        			listCategories(root));
            LOG.info("Listed categories for user " + event.getAuthor().getName());
        } else {
        	e(event, "There were no categories found.");
        }
	}
	
	private String listCategories(Category root) {
		StringBuilder sb = new StringBuilder();
		for (Category category : root.getChildren()) {
			sb.append("**" + category.getName() + "** / ");
			if (!category.getChildren().isEmpty())
				sb.append(listCategories(category));
		}
		return sb.toString();
	}
	
	@Override
	public String getCommandHelpString() {
		return "`" + getPrefix() + "` - list all sound categories";
	}
	
}
