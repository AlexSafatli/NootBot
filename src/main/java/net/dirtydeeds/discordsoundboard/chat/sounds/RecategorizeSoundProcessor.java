package net.dirtydeeds.discordsoundboard.chat.sounds;

import com.google.common.io.Files;
import net.dirtydeeds.discordsoundboard.chat.AuthenticatedMultiArgumentChatCommandProcessor;
import net.dirtydeeds.discordsoundboard.org.Category;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.internal.utils.JDALogger;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class RecategorizeSoundProcessor extends
        AuthenticatedMultiArgumentChatCommandProcessor {

  public RecategorizeSoundProcessor(String prefix, SoundboardBot bot) {
    super(prefix, "Recategorize", bot);
  }

  protected void handleEvent(MessageReceivedEvent event, String message) {
    int numArgs = getArguments().length;
    if (numArgs != 2) {
      pm(event, "You need to provide a sound name and category to move it " +
              "to. For example: `" + getPrefix() + " holdthedoor, Games`.");
      return;
    }
    String name = getArguments()[0], cat = getArgumentsCased(event)[1];
    if (bot.getSoundMap().get(name) == null) {
      pm(event, "That sound was not found.");
      return;
    } else if (!bot.isASoundCategory(cat)) {
      pm(event, "The category `" + cat + "` does not exist. *Will make it as " +
              "a new primary category!*");
      Path newCat = bot.getSoundsPath().resolve(cat);
      JDALogger.getLog("Recat").info("Creating directory: " + newCat);
      newCat.toFile().mkdir();
      bot.getDispatcher().updateFileList();
    }
    try {
      boolean success = false;
      File file = bot.getSoundMap().get(name).getSoundFile();
      Path source = Paths.get(file.getPath());
      JDALogger.getLog("Recat").info("Identified path of file: " + source);
      int extIndex = file.getName().lastIndexOf(".");
      String ext = (extIndex != -1) ? file.getName().substring(extIndex) : "";
      JDALogger.getLog("Recat").info("Identified extension of file: " + ext);
      for (Category category : bot.getDispatcher().getCategories()) {
        if (category.getName().equalsIgnoreCase(cat)) {
          File destination =
                  category.getFolderPath().resolve(name + ext).toFile();
          JDALogger.getLog("Recat").info("Moving file to: " + destination.getPath());
          Files.move(file, destination);
          success = true;
          pm(event, String.format("Moved '%s' from '%s' -> '%s'", file.getName(),
                  bot.getSoundMap().get(name).getCategory(),
                  cat));
        }
      }
      if (!success)
        pm(event, String.format("Could not move '%s'", name));
    } catch (Exception e) {
      e.printStackTrace();
      JDALogger.getLog("Recat").error("While renaming a file: " + e.toString() + " => " +
              e.getMessage());
      pm(event, String.format("Could not move '%s'", name));
    }
    bot.getDispatcher().updateFileList();
  }

  @Override
  public String getCommandHelpString() {
    return getPrefix() + " <soundfile>, <category> - change the category " +
            "for a sound";
  }
}