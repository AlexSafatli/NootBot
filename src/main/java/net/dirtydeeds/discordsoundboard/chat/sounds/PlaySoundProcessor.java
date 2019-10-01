package net.dirtydeeds.discordsoundboard.chat.sounds;

import net.dirtydeeds.discordsoundboard.chat.SingleArgumentChatCommandProcessor;
import net.dirtydeeds.discordsoundboard.moderation.ModerationRules;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.utils.StringUtils;
import net.dirtydeeds.discordsoundboard.utils.Strings;
import net.dirtydeeds.discordsoundboard.utils.StyledEmbedMessage;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.utils.SimpleLog;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class PlaySoundProcessor extends SingleArgumentChatCommandProcessor {

  public static final SimpleLog LOG = SimpleLog.getLog("Sound");

  public PlaySoundProcessor(String prefix, SoundboardBot bot) {
    super(prefix, "Play Sound", bot);
  }

  private void sendBadSoundMessage(MessageReceivedEvent event, String name,
                                   String suggestion, User user) {
    StyledEmbedMessage msg = buildStyledEmbedMessage(event);
    msg.setTitle("Sound Not Found");
    msg.addDescription("Sound `" + name + "` not found. " + suggestion + Strings.SEPARATOR + user.getAsMention());
    msg.addContent("Search", "You can use `.search` with a keyword.", true);
    embed(event, msg.isWarning(true));
  }

  private List<String> getNumberedSet(String name) {
    List<String> numberedSet = new LinkedList<>();
    String query = name;
    while (query.length() > 0) {
      char c = query.charAt(query.length()-1);
      if (Character.isDigit(c)) {
        query = query.substring(0, query.length()-2);
      } else {
        break;
      }
    }
    if (query.length() == 0 || bot.getSoundMap().get(query) == null) {
      return numberedSet;
    }
    numberedSet.add(query);

    int i = 2;
    boolean found = true;
    while (found) {
      if (bot.getSoundMap().get(query + i) != null) {
        numberedSet.add(query + i);
      } else {
        found = false;
      }
    }
    return numberedSet;
  }

  private void play(MessageReceivedEvent event, String name) {
    try {
      bot.playFileForChatCommand(name, event);
    } catch (Exception e) {
      e(event, "Could not play sound => " + e.getMessage());
      LOG.warn("Did not play sound.");
    }
  }

  @Override
  public void process(MessageReceivedEvent event) {
    String message = event.getMessage().getContent().toLowerCase();
    if (StringUtils.containsOnly(message, '?')) return;
    super.process(event);
  }

  protected void handleEvent(MessageReceivedEvent event, String message) {
    User user = event.getAuthor();
    String name = message.substring(1);
    ModerationRules r = bot.getRulesForGuild(event.getGuild());
    boolean playNumberedSets = r != null && r.canPlayNumberedSets();
    if (!bot.isAllowedToPlaySound(user)) {
      pm(event, "You're not allowed to do that.");
      LOG.info(
              String.format("%s isn't allowed to play sounds.", user.getName()));
    } else if (StringUtils.containsAny(name, '?')) {
      return;
    } else if (bot.getSoundMap().get(name) == null) {
      LOG.info("Sound was not found.");
      String suggestion = "Check your spelling.",
              possibleName = bot.getClosestMatchingSoundName(name);
      if (name.equals("help")) {
        suggestion = "Were you trying to access the `.help` command?";
      } else if (possibleName != null) {
        suggestion = String.format("Did you mean `%s`?", possibleName);
      } else {
        // Do a naive search to see if something contains this name. Stop early.
        for (String s : bot.getSoundMap().keySet()) {
          if (s.contains(name)) {
            suggestion = String.format("Did you mean `%s`?", s);
            break;
          }
        }
      }
      LOG.info("Suggestion: " + suggestion);
      sendBadSoundMessage(event, name, suggestion, user);
    } else if (playNumberedSets && getNumberedSet(name).size() > 0) {
      play(event, StringUtils.randomString(getNumberedSet(name)));
    } else play(event, name);
  }

  @Override
  public String getCommandHelpString() {
    String help = getPrefix() + "soundfile - play a sound by name";
    Set<String> soundFileNames = bot.getSoundMap().keySet();
    if (!soundFileNames.isEmpty()) {
      help += " - e.g., " +
              getPrefix() + StringUtils.randomString(soundFileNames);
    }
    return help;
  }
}
