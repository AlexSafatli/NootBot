package net.dirtydeeds.discordsoundboard.chat;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import net.dirtydeeds.discordsoundboard.beans.SoundFile;
import net.dirtydeeds.discordsoundboard.org.Category;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.utils.MessageBuilder;
import net.dirtydeeds.discordsoundboard.utils.Strings;
import net.dirtydeeds.discordsoundboard.utils.StyledEmbedMessage;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.utils.SimpleLog;

public class ListSoundsProcessor extends SingleArgumentChatCommandProcessor {

  private static final int BIG_NUMBER_OF_SOUNDS = 1000;
  public static final SimpleLog LOG = SimpleLog.getLog("ListSounds");

  public ListSoundsProcessor(String prefix, SoundboardBot soundPlayer) {
    super(prefix, "List Sounds", soundPlayer);
  }

  private Map<String, List<SoundFile>> getCategoryMappings() {
    Map<String, List<SoundFile>> categoryFiles =
      new TreeMap<String, List<SoundFile>>();
    for (SoundFile file : bot.getSoundMap().values()) {
      String category = (file.getCategory().equalsIgnoreCase("sounds")) ?
                        "Uncategorized" : file.getCategory();
      if (categoryFiles.get(category) == null) {
        categoryFiles.put(category, new LinkedList<SoundFile>());
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

  protected void handleEvent(MessageReceivedEvent event, String message) {
    String cat = getArgument();
    Map<String, SoundFile> soundFiles = bot.getSoundMap();
    // List the sound files.
    Map<String, List<SoundFile>> categoryFiles = getCategoryMappings();
    if (soundFiles.size() > 0) {
      if (cat == null) {
        if (soundFiles.size() > BIG_NUMBER_OF_SOUNDS) {
          w(event, "**" + soundFiles.size() + " files are stored** " +
            "\u2014 that's a lot of files! Listing them will *flood this" +
            " channel*. List sounds by category using `" + getPrefix() +
            " <category>` instead.");
          return;
        }
        m(event, "**" + soundFiles.size() + " files are stored**. " +
          "They are organized in **" +
          bot.getDispatcher().getNumberOfCategories() +
          "** categories \u2014 type any of these to play them.");
        // List everything uncategorized.
        if (categoryFiles.get("Uncategorized") != null &&
            categoryFiles.get("Uncategorized").size() > 0) {
          for (String s : getStringsForCategory(
                 "Uncategorized", categoryFiles.get("Uncategorized"))) {
            m(event, s);
          }
        }
        // Traverse category tree.
        for (Category category :
             bot.getDispatcher().getCategoryTree().getChildren()) {
          listByCategory(category, null, categoryFiles, event);
        }
      } else {
        if (bot.isASoundCategory(cat)) {
          for (Category category : bot.getDispatcher().getCategories()) {
            if (category.getName().equalsIgnoreCase(cat)) {
              LOG.info("Listing sounds for category " + category.getName() +
                       " in " + event.getGuild());
              listByCategory(category, null, categoryFiles, event);
              return;
            }
          }
        } else {
          e(event, formatString(Strings.NOT_FOUND, cat));
          LOG.info(event.getAuthor() + " requested a list for category " +
                   cat + " but it wasn't found.");
        }
      }
    } else e(event, "There are **no sounds** that can be played! Add some.");
  }

  private void listByCategory(Category category, Category parent,
                              Map<String, List<SoundFile>> categoryFiles,
                              MessageReceivedEvent event) {
    List<SoundFile> sounds = categoryFiles.get(category.getName());
    List<String> strings = getStringsForCategory(category.getName(), sounds);
    String title = category.getName();
    if (sounds != null) title += " (**" + sounds.size() + "**)";
    if (parent != null) title += " \u2014 subcategory of " + parent.getName();
    int i = 0;
    for (String s : strings) {
      int k = i + 1;
      StyledEmbedMessage em = new StyledEmbedMessage((i == 0) ? title : title +
          " (" + k + ")", bot);
      em.addDescription(s);
      embed(event, em);
      ++i;
    }
    if (!category.getChildren().isEmpty()) {
      for (Category child : category.getChildren())
        listByCategory(child, category, categoryFiles, event);
    }
  }

  @Override
  public String getCommandHelpString() {
    return getPrefix() + " [category] - list sounds (or those for a category)";
  }

}
