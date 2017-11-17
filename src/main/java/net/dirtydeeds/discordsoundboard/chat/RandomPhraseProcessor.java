package net.dirtydeeds.discordsoundboard.chat;

import java.util.List;

import net.dirtydeeds.discordsoundboard.utils.*;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.utils.SimpleLog;

public class RandomPhraseProcessor extends SingleArgumentChatProcessor {

  public RandomPhraseProcessor(String prefix, SoundboardBot bot) {
    super(prefix, "Random Phrase(s)", bot);
  }

  private String randomPhrase() {
    return (String) RandomUtils.chooseOne(StringUtils.randomPhrase(),
                                          StringUtils.randomString(
                                            bot.getDispatcher().getPhrases()));
  }

  protected void handleEvent(MessageReceivedEvent event, String message) {
    int numTimesToPlay = (getArguments().length > 0) ?
                         Integer.valueOf(getArguments()[0]) : 1;
    MessageBuilder mb = new MessageBuilder();
    for (int i = 0; i < numTimesToPlay; ++i) {
      mb.append("`" + randomPhrase() + "`");
    }
    for (String s : mb.getStrings()) m(event, s);
  }

  @Override
  public String getCommandHelpString() {
    return getPrefix() + " [numPhrases] - randomize from user-submitted" +
           " phrases and the cache of game name words";
  }

}