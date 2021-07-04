package net.dirtydeeds.discordsoundboard.chat.sounds;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import net.dirtydeeds.discordsoundboard.beans.SoundFile;
import net.dirtydeeds.discordsoundboard.chat.AbstractChatCommandProcessor;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.utils.MessageBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;

public class ListNewSoundsProcessor extends AbstractChatCommandProcessor {

  private static final int MIN_NUMBER_OF_HOURS = 168; // 7 days
  private static final int MAX_NUMBER_OF_HOURS = 1152; // 48 days
  private static final int NUM_HOURS_FOR_DAY_TRANSFORM = 72; // 3 days
  private static final int DAYS = 24;

  public ListNewSoundsProcessor(String prefix, SoundboardBot bot, CommandListUpdateAction commands) {
    super(prefix, "new", "Newest Sounds", bot);
    commands.addCommands(new CommandData("new", "List all recently added sound files."));
  }

  private Map<String, List<SoundFile>> getCategoryMappings(
          Collection<SoundFile> newSounds) {
    Map<String, List<SoundFile>> categoryFiles =
            new TreeMap<String, List<SoundFile>>();
    for (SoundFile file : newSounds) {
      String category = (file.getCategory().equalsIgnoreCase("sounds")) ?
              "Uncategorized" : file.getCategory();
      if (categoryFiles.get(category) == null) {
        categoryFiles.put(category, new LinkedList<>());
      }
      categoryFiles.get(category).add(file);
    }
    return categoryFiles;
  }

  private List<String> getMessagesForCategory(String category,
                                              Collection<SoundFile> soundFiles) {
    MessageBuilder b = new MessageBuilder();
    b.append("**" + category + "** (" + soundFiles.size() + ") \u2014 ");
    for (SoundFile file : soundFiles) {
      String filename = file.getSoundFile().getName();
      String name = filename.substring(0, filename.indexOf("."));
      b.append("`?" + name + "` ");
    }
    return b.getStrings();
  }

  private Collection<SoundFile> getNewSounds(Collection<SoundFile> soundFiles,
                                             int numHours) {
    List<SoundFile> newSounds = new LinkedList<>();
    for (SoundFile file : soundFiles) {
      Date lastModified = file.getLastModified();
      if (lastModified == null) {
        continue;
      }
      if (!lastModified.after(new Date(System.currentTimeMillis() -
              numHours * 60 * 60 * 1000)))
        continue;
      newSounds.add(file);
    }
    return newSounds;
  }

  private void listNewSounds(Collection<SoundFile> newSounds,
                             MessageReceivedEvent event, int numHours) {
    MessageBuilder b = new MessageBuilder();
    String timeType = "hours";
    int numTime = numHours;
    if (numHours > NUM_HOURS_FOR_DAY_TRANSFORM) {
      numTime /= DAYS;
      timeType = "days";
    }
    b.append("The **newest sound files** added (in the last " + numTime +
            " " + timeType + ") were:\n\n");
    Map<String, List<SoundFile>> catMap = getCategoryMappings(newSounds);
    for (String category : catMap.keySet()) {
      for (String msg : getMessagesForCategory(
              category, catMap.get(category))) {
        b.append(msg);
      }
    }
    for (String msg : b.getStrings()) m(event, msg);
  }

  private void listNewSounds(Collection<SoundFile> newSounds,
                             SlashCommandEvent event, int numHours) {
    MessageBuilder b = new MessageBuilder();
    String timeType = "hours";
    int numTime = numHours;
    if (numHours > NUM_HOURS_FOR_DAY_TRANSFORM) {
      numTime /= DAYS;
      timeType = "days";
    }
    b.append("The **newest sound files** added (in the last " + numTime +
            " " + timeType + ") were:\n\n");
    Map<String, List<SoundFile>> catMap = getCategoryMappings(newSounds);
    for (String category : catMap.keySet()) {
      for (String msg : getMessagesForCategory(
              category, catMap.get(category))) {
        b.append(msg);
      }
    }
    for (String msg : b.getStrings()) m(event, msg);
  }

  protected void handleEvent(MessageReceivedEvent event, String message) {
    Map<String, SoundFile> soundFiles = bot.getSoundMap();
    if (soundFiles.isEmpty()) {
      e(event, "There are **no sound files** stored yet.");
      return;
    }
    String timeType = "hours";
    int numHours = MIN_NUMBER_OF_HOURS;
    Collection<SoundFile> newSounds = null;
    while (numHours <= MAX_NUMBER_OF_HOURS) {
      newSounds = getNewSounds(soundFiles.values(), numHours);
      if (newSounds != null && !newSounds.isEmpty()) {
        listNewSounds(newSounds, event, numHours);
        return;
      }
      numHours += 48; // Add 2 days.
    }
    if (newSounds == null || newSounds.isEmpty()) {
      int numTime = numHours;
      if (numHours > NUM_HOURS_FOR_DAY_TRANSFORM) {
        numTime /= DAYS;
        timeType = "days";
      }
      w(event, "There were no **new sounds** found from the last " +
              numTime + " " + timeType + ".");
    } else {
      listNewSounds(newSounds, event, numHours);
    }
  }

  protected void handleEvent(SlashCommandEvent event) {
    Map<String, SoundFile> soundFiles = bot.getSoundMap();
    if (soundFiles.isEmpty()) {
      e(event, "There are **no sound files** stored yet.");
      return;
    }
    String timeType = "hours";
    int numHours = MIN_NUMBER_OF_HOURS;
    Collection<SoundFile> newSounds = null;
    while (numHours <= MAX_NUMBER_OF_HOURS) {
      newSounds = getNewSounds(soundFiles.values(), numHours);
      if (newSounds != null && !newSounds.isEmpty()) {
        listNewSounds(newSounds, event, numHours);
        return;
      }
      numHours += 48; // Add 2 days.
    }
    if (newSounds == null || newSounds.isEmpty()) {
      int numTime = numHours;
      if (numHours > NUM_HOURS_FOR_DAY_TRANSFORM) {
        numTime /= DAYS;
        timeType = "days";
      }
      w(event, "There were no **new sounds** found from the last " +
              numTime + " " + timeType + ".");
    } else {
      listNewSounds(newSounds, event, numHours);
    }
  }

  @Override
  public String getCommandHelpString() {
    return getPrefix() + " - list the newest sound files at least " +
            MIN_NUMBER_OF_HOURS / DAYS + " days old";
  }
}