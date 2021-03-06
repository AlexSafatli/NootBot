package net.dirtydeeds.discordsoundboard.chat.sounds;

import java.util.LinkedList;

import net.dirtydeeds.discordsoundboard.chat.AbstractChatCommandProcessor;
import net.dirtydeeds.discordsoundboard.org.Category;
import net.dirtydeeds.discordsoundboard.utils.StringUtils;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.dv8tion.jda.internal.utils.JDALogger;

public class ListCategoriesProcessor extends AbstractChatCommandProcessor {

  public ListCategoriesProcessor(String prefix, SoundboardBot bot, CommandListUpdateAction commands) {
    super(prefix, "categories", "Categories", bot);
    commands.addCommands(new CommandData("categories", "List all sound categories."));
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

  protected void handleEvent(SlashCommandEvent event) {
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
    return StringUtils.listToString(getCategories(root));
  }

  @Override
  public String getCommandHelpString() {
    return getPrefix() + " - list all sound categories";
  }

}
