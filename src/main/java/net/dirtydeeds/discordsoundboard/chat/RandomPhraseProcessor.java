package net.dirtydeeds.discordsoundboard.chat;

import java.util.List;

import net.dirtydeeds.discordsoundboard.utils.*;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.utils.SimpleLog;

public class RandomPhraseProcessor extends AbstractChatCommandProcessor {

  public RandomPhraseProcessor(String prefix, SoundboardBot bot) {
    super(prefix, "Random Phrase", bot);
  }

  protected void handleEvent(MessageReceivedEvent event, String message) {
    m(event, (String) RandomUtils.chooseOne(StringUtils.randomPhrase(),
                                            StringUtils.randomString(
                                                bot.getDispatcher(
                                                ).getPhrases())));
  }

  @Override
  public String getCommandHelpString() {
    return getPrefix() + " - randomize from user-submitted phrases and " +
           "the cache of game name words";
  }

}
