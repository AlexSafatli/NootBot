package net.dirtydeeds.discordsoundboard.chat.sounds;

import com.google.common.io.Files;
import net.dirtydeeds.discordsoundboard.beans.SoundFile;
import net.dirtydeeds.discordsoundboard.beans.User;
import net.dirtydeeds.discordsoundboard.chat.AuthenticatedMultiArgumentChatCommandProcessor;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.internal.utils.JDALogger;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class RenameSoundProcessor extends
        AuthenticatedMultiArgumentChatCommandProcessor {

  public RenameSoundProcessor(String prefix, SoundboardBot bot) {
    super(prefix, "Rename Sound", bot);
  }

  protected void handleEvent(MessageReceivedEvent event, String message) {
    int numArgs = getArguments().length;
    if (numArgs != 2) {
      pm(event, "You need to provide an old and new name. For example: `" +
              getPrefix() + " holdthedoor, door`.");
      return;
    }
    String oldName = getArguments()[0], newName = getArguments()[1];
    if (bot.getSoundMap().get(oldName) == null) {
      pm(event, "That sound was not found.");
      return;
    }
    deleteOriginalMessage(event);
    try {
      SoundFile old = bot.getSoundMap().get(oldName);
      File oldFile = old.getSoundFile();
      Path source = Paths.get(oldFile.getPath());
      JDALogger.getLog("Rename").info("Identified path of file: " + source);
      int extIndex = oldFile.getName().lastIndexOf(".");
      String ext = (extIndex != -1) ?
              oldFile.getName().substring(extIndex) : "";
      JDALogger.getLog("Rename").info("Identified extension of file: " + ext);
      File newFile = source.resolveSibling(newName + ext).toFile();
      JDALogger.getLog("Rename").info("Moving file to: " + newFile.getPath());
      Files.move(oldFile, newFile);
      bot.getDispatcher().updateFileList();
      SoundFile sound = bot.getDispatcher().getSoundFileByName(newName);
      if (sound != null) {
        List<User> usersWithEntrance =
                bot.getDispatcher().getUsersWithEntrance(oldName);
        for (User user : usersWithEntrance) {
          user.setEntrance(newName);
          bot.getDispatcher().saveUser(user);
        }
        sound.setNumberOfPlays(old.getNumberOfPlays());
        sound.setNumberOfReports(old.getNumberOfReports());
        sound.setExcludedFromRandom(old.isExcludedFromRandom());
        bot.getDispatcher().saveSound(sound);
      }
      if (old.getNumberOfPlays() > 0) {
        m(event, "Sound `" + oldName + "` has been renamed to `" + newName +
                "`.");
      } else {
        pm(event, String.format("Renamed `%s` -> `%s`", oldName, newName));
      }
    } catch (Exception e) {
      JDALogger.getLog("Rename").error("While renaming a file: " + e.toString() + " => " +
              e.getMessage());
      e.printStackTrace();
      bot.getDispatcher().updateFileList();
      pm(event, String.format("Sound '%s' could not be renamed to '%s'",
              oldName, newName) + "\n\u2014\n => " + e.getMessage());
    }
  }

  @Override
  public String getCommandHelpString() {
    return getPrefix() + " <soundfile>, <newname> - rename a sound";
  }
}