package net.dirtydeeds.discordsoundboard.chat;

import net.dirtydeeds.discordsoundboard.Version;
import net.dirtydeeds.discordsoundboard.beans.SoundFile;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.utils.StringUtils;
import net.dirtydeeds.discordsoundboard.utils.StyledEmbedMessage;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class StatsProcessor extends AbstractChatCommandProcessor {

  public StatsProcessor(String prefix, SoundboardBot bot) {
    super(prefix, String.format("Hello, My Name is %s", bot.getBotName()), bot);
  }

  private SoundFile mostPlayedSound() {
    List<SoundFile> files = bot.getDispatcher().getSoundFilesOrderedByNumberOfPlays();
    return getSoundFile(files);
  }

  private SoundFile getSoundFile(List<SoundFile> files) {
    int i = 0;
    while (files != null && !files.isEmpty() && i < files.size()) {
      SoundFile f = files.get(i++);
      if (bot.getSoundMap().get(f.getSoundFileId()) != null) return f;
    }
    return null;
  }

  private SoundFile longestSound() {
    List<SoundFile> files = bot.getDispatcher().getSoundFilesOrderedByDuration();
    return getSoundFile(files);
  }

  private StyledEmbedMessage statsMessage(MessageReceivedEvent event) {
    StyledEmbedMessage msg = buildStyledEmbedMessage(event);
    String desc = String.format("I'm a bot that makes sounds and " +
            "occasionally does something that looks intelligent. " +
            "Or stupid. %s.", StringUtils.randomPhrase());
    msg.addDescription(desc);
    int numberOfSounds = bot.getSoundMap().size();
    int numberOfServers = bot.getGuilds().size();
    msg.addContent("Number of Sounds", "" + numberOfSounds, true);
    msg.addContent("Number of Categories", "" +
            bot.getDispatcher().getNumberOfCategories(), true);
    if (numberOfSounds > 0) {
      SoundFile mostPlayed = mostPlayedSound(), longest = longestSound();
      if (mostPlayed != null)
        msg.addContent("Most Played", "`" + mostPlayed.getSoundFileId() +
                        "` with **" + mostPlayed.getNumberOfPlays() + "** plays",
                true);
      if (longest != null)
        msg.addContent("Longest Sound", "`" + longest.getSoundFileId() +
                "` at **" + longest.getDuration() + "s**", true);
      msg.addContent("Size of Sound Library",
              bot.getDispatcher().sizeOfLibrary(), true);
    }
    msg.addContent("Bot Uptime", bot.getUptimeAsString(), true);
    if (numberOfServers > 1) {
      msg.addContent("Number of Servers", "" + numberOfServers, true);
    }
    msg.addContent("Version", Version.getVersionCode(), true);
    msg.addContent("Developer", Version.getAuthor(bot), true);
    return msg;
  }

  protected void handleEvent(MessageReceivedEvent event, String message) {
    embed(event, statsMessage(event));
  }

  @Override
  public String getCommandHelpString() {
    return getPrefix() + " - print stats related to the bot";
  }
}