package net.dirtydeeds.discordsoundboard.chat.sounds;

import net.dirtydeeds.discordsoundboard.beans.SoundFile;
import net.dirtydeeds.discordsoundboard.chat.SingleArgumentChatCommandProcessor;
import net.dirtydeeds.discordsoundboard.org.Category;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.utils.MessageBuilder;
import net.dirtydeeds.discordsoundboard.utils.StringUtils;
import net.dirtydeeds.discordsoundboard.utils.StyledEmbedMessage;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.internal.utils.JDALogger;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ListSoundsProcessor extends SingleArgumentChatCommandProcessor {

  private static final int BIG_NUMBER_OF_SOUNDS = 1750;
  private static final String UNCATEGORIZED = "Uncategorized";

  private Map<String, SoundFile> soundFiles;

  public ListSoundsProcessor(String prefix, SoundboardBot soundPlayer) {
    super(prefix, "Sounds", soundPlayer);
  }

  private Map<String, List<SoundFile>> getCategoryMappings() {
    Map<String, List<SoundFile>> categoryFiles = new TreeMap<>();
    for (SoundFile file : bot.getSoundMap().values()) {
      String category = (file.getCategory().equalsIgnoreCase("sounds")) ?
                        UNCATEGORIZED : file.getCategory();
      if (categoryFiles.get(category) == null) {
        categoryFiles.put(category, new LinkedList<>());
      }
      categoryFiles.get(category).add(file);
    }
    return categoryFiles;
  }

  private List<String> getStringsForCategory(String category,
      List<SoundFile> soundFiles) {
    MessageBuilder b = new MessageBuilder();
    if (soundFiles != null && !soundFiles.isEmpty()) {
      for (SoundFile file : soundFiles) {
        String filename = file.getSoundFile().getName();
        String name = filename.substring(0, filename.indexOf("."));
        b.append("`?" + name + "` ");
      }
    }
    return b.getStrings();
  }

  private void listByCategory(Category category, Category parent,
                              Map<String, List<SoundFile>> categoryFiles,
                              MessageReceivedEvent event) {
    String title = category.getName();
    List<SoundFile> sounds = categoryFiles.get(title);
    List<String> strings = getStringsForCategory(title, sounds);

    if (sounds != null) title += " (**" + sounds.size() + "**)";
    if (parent != null) title += " \u2014 subcategory of " + parent.getName();
    int i = 0;

    for (String s : strings) {
      StyledEmbedMessage em = new StyledEmbedMessage(
              (i == 0) ? title : title + " [" + (i + 1) + "]", bot);
      Color color = StringUtils.toColor(category.getName());
      em.setColor(color);
      em.addDescription(s);
      embed(event, em);
      ++i;
    }
    if (!category.getChildren().isEmpty()) {
      for (Category child : category.getChildren())
        listByCategory(child, category, categoryFiles, event);
    }
  }

  private void sendFullListHeader(MessageReceivedEvent event) {
    m(event, "**" + soundFiles.size() + " files (" +
            bot.getDispatcher().sizeOfLibrary() +
            ") are stored**. They are organized in **" +
            bot.getDispatcher().getNumberOfCategories() +
            "** categories \u2014 type any of these to play them.");
  }

  private void sendTooManySoundsWarning(MessageReceivedEvent event) {
    w(event, "**" + soundFiles.size() + " files are stored** (" +
            bot.getDispatcher().sizeOfLibrary() + ") " +
            "\u2014 that's so much! Listing them will *flood this" +
            " channel*. List sounds by category using `" + getPrefix() +
            " <category>` instead. To list categories, use `.categories`.");
  }

  protected void handleEvent(MessageReceivedEvent event, String message) {
    String cat = getArgument();
    soundFiles = bot.getSoundMap();

    // List the sound files.
    if (soundFiles.size() > 0) {

      Map<String, List<SoundFile>> categoryFiles = getCategoryMappings();
      if (cat == null) {
        // No category provided; list all sounds.

        if (soundFiles.size() > BIG_NUMBER_OF_SOUNDS &&
            !event.isFromType(ChannelType.PRIVATE)) {
          sendTooManySoundsWarning(event);
          return;
        }
        sendFullListHeader(event);

        // List everything uncategorized.
        if (categoryFiles.get(UNCATEGORIZED) != null &&
            categoryFiles.get(UNCATEGORIZED).size() > 0) {
          for (String s : getStringsForCategory(UNCATEGORIZED,
                  categoryFiles.get(UNCATEGORIZED))) {
            m(event, s);
          }
        }
        // Traverse category tree.
        for (Category category :
             bot.getDispatcher().getCategoryTree().getChildren()) {
          listByCategory(category, null, categoryFiles, event);
        }

      } else {
        // Category provided.
        if (bot.isASoundCategory(cat)) {
          for (Category category : bot.getDispatcher().getCategories()) {
            if (category.getName().equalsIgnoreCase(cat)) {
              JDALogger.getLog("List").info("Listing sounds for category " + category.getName() +
                       " in " + event.getGuild());
              listByCategory(category, null, categoryFiles, event);
              return;
            }
          }
        } else {
          e(event, "Not found.");
          JDALogger.getLog("List").info(event.getAuthor() + 
            " requested a list for category " + cat + " but it wasn't found.");
        }
      }

    } else e(event, "There are **no sounds**! Add some :rage:.");
  }

  @Override
  public String getCommandHelpString() {
    return getPrefix() + " [category] - list sounds (or those for a category)";
  }

}
