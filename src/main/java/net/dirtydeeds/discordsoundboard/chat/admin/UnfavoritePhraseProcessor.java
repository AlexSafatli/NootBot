package net.dirtydeeds.discordsoundboard.chat.admin;

import net.dirtydeeds.discordsoundboard.chat.AuthenticatedSingleArgumentChatCommandProcessor;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.utils.StringUtils;
import net.dirtydeeds.discordsoundboard.utils.Strings;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class UnfavoritePhraseProcessor extends
        AuthenticatedSingleArgumentChatCommandProcessor {

  public UnfavoritePhraseProcessor(String prefix, SoundboardBot bot) {
    super(prefix, "Remove Phrase", bot);
  }

  protected void handleEvent(MessageReceivedEvent event, String message) {
    String m = event.getMessage().getContent(),
           phrase = m.substring(getPrefix().length() + 1).trim();
    if (bot.getDispatcher().removePhrase(phrase)) {
      m(event, "Removed phrase `" + phrase + "` from list of phrases!");
    } else {
      pm(event, "Didn't have phrase `" + phrase + "` in list of phrases!");
    }
  }

  @Override
  public String getCommandHelpString() {
    return getPrefix() + " <phrase> (*) - let the bot know about a phrase " +
           " you dislike";
  }
}