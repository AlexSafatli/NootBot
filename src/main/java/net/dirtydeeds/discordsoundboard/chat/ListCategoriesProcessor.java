package net.dirtydeeds.discordsoundboard.chat;

import java.util.LinkedList;

import net.dirtydeeds.discordsoundboard.org.Category;
import net.dirtydeeds.discordsoundboard.utils.StringUtils;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.utils.SimpleLog;

public class ListCategoriesProcessor extends AbstractChatCommandProcessor {
	
	public ListCategoriesProcessor(String prefix, SoundboardBot soundPlayer) {
		super(prefix, "Categories", soundPlayer);
	}

	protected void handleEvent(MessageReceivedEvent event, String message) {
        Category root = bot.getDispatcher().getCategoryTree();
        if (root.getChildren().size() > 0) {
        	m(event, "Here is a list of categories and subcategories:\n\n" + 
        			listCategories(root));
        } else {
        	e(event, "There were no categories found.");
        }
	}

  private LinkedList<Category> getCategories(Category root) {
    LinkedList<Category> categories = new LinkedList<>();
    for (Category category : root.getChildren()) {
      categories.add(category);
      if (!category.getChildren().isEmpty())
        categories.addAll(getCategories(category));
    }
    return categories;
  }
	
	private String listCategories(Category root) {
		LinkedList<Category> categories = getCategories(root);
		return StringUtils.listToString(categories);
	}
	
	@Override
	public String getCommandHelpString() {
		return "`" + getPrefix() + "` - list all sound categories";
	}
	
}
