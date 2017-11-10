package net.dirtydeeds.discordsoundboard.chat;

import java.util.List;

import net.dirtydeeds.discordsoundboard.utils.MessageBuilder;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.utils.SimpleLog;

public class ListPhrasesProcessor extends AbstractChatCommandProcessor {

  public ListPhrasesProcessor(String prefix, SoundboardBot bot) {
    super(prefix, "Phrases", bot);
  }

  protected void handleEvent(MessageReceivedEvent event, String message) {
    for (String msg : listPhrases()) {
      m(event, msg);
    }
  }

  private List<String> listPhrases() {
    List<String> phrases = bot.getDispatcher().getPhrases();
    MessageBuilder mb = new MessageBuilder();
    mb.append("Here is a list of phrases:\n\n");
    for (String phrase : phrases) {
      mb.append("`" + phrase + "`\n");
    }
    return mb.getStrings();
  }

  @Override
  public String getCommandHelpString() {
    return getPrefix() + " - list all user-submitted phrases";
  }

}
