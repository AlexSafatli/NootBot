package net.dirtydeeds.discordsoundboard.chat.sounds;

import net.dirtydeeds.discordsoundboard.chat.SingleArgumentChatCommandProcessor;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.utils.MessageBuilder;
import net.dirtydeeds.discordsoundboard.utils.StringUtils;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.internal.utils.SimpleLogger;

import java.util.LinkedList;
import java.util.List;

public class SearchProcessor extends SingleArgumentChatCommandProcessor {

  public static final SimpleLogger LOG = SimpleLogger.getLog("Search");

  public SearchProcessor(String prefix, SoundboardBot bot) {
    super(prefix, "Search Sounds", bot);
  }

  protected void handleEvent(MessageReceivedEvent event, String message) {
    User user = event.getAuthor();
    String query = getArgument();
    if (query == null) {
      w(event, "I need something to search for. *Baka*.");
      return;
    }

    List<String> possibilities = new LinkedList<>();

    if (StringUtils.containsAny(query, '?')) {
      w(event, "Sounds cannot contain `?` characters.");
      return;
    } else if (query.length() <= 1) {
      w(event, "`" + query + "` is too short. *Baka*.");
      return;
    }

    // Leverage trie first.
    String possibleName = bot.getClosestMatchingSoundName(query);
    if (possibleName != null) {
      LOG.info("Closest matching sound name from trie: " + possibleName);
      possibilities.add(possibleName);
    }

    // Naive iteration through all sound names.
    for (String name : bot.getSoundMap().keySet()) {
      if (name.contains(query) && !name.equals(possibleName))
        possibilities.add(name);
    }

    // Display results.
    MessageBuilder mb = new MessageBuilder();
    mb.append("Found **" + possibilities.size() +
            "** possible sounds for query `" + query + "` \u2014 " +
            user.getAsMention() + ".\n\n");
    LOG.info("Found " + possibilities.size() + " possible sounds.");
    if (!possibilities.isEmpty()) {
      for (String possibility : possibilities)
        mb.append("`?" + possibility + "` ");
      for (String m : mb)
        m(event, m);
    } else {
      w(event, "Could not find any sounds matching `" + query + "` \u2014 " +
              user.getAsMention() + ".");
    }
  }

  @Override
  public String getCommandHelpString() {
    return getPrefix() + " <keyword> - search for sounds by keyword";
  }
}
