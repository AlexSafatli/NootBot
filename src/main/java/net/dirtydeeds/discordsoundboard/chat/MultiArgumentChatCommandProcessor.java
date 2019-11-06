package net.dirtydeeds.discordsoundboard.chat;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public abstract class MultiArgumentChatCommandProcessor extends
        AbstractChatCommandProcessor {

  private String[] args = {};

  public MultiArgumentChatCommandProcessor(String prefix, String title,
                                           SoundboardBot bot) {
    super(prefix, title, bot);
  }

  protected abstract void handleEvent(MessageReceivedEvent event,
                                      String message);

  public void process(MessageReceivedEvent event) {
    String message = event.getMessage().getContentRaw().toLowerCase();
    if (!message.endsWith(getPrefix())) {
      // Get arguments. Comma-delimited.
      String noPrefix = message.substring(getPrefix().length() + 1);
      args = noPrefix.split(",\\s?");
    }
    super.process(event);
    args = new String[0]; // Clear arguments.
  }

  public String[] getArguments() {
    return this.args;
  }

  public String[] getArgumentsCased(MessageReceivedEvent event) {
    String[] argsCased = null;
    String message = event.getMessage().getContentRaw();
    if (!message.endsWith(getPrefix())) {
      // Get arguments. Comma-delimited.
      String noPrefix = message.substring(getPrefix().length() + 1);
      argsCased = noPrefix.split(",\\s?");
    }
    return argsCased;
  }

  @Override
  public String getCommandHelpString() {
    return getPrefix() + " [argument1], [argument2], ...";
  }
}