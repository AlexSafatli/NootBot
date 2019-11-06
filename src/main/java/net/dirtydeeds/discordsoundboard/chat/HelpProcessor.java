package net.dirtydeeds.discordsoundboard.chat;

import java.util.List;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.utils.MessageBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.internal.utils.SimpleLogger;

public class HelpProcessor extends AbstractChatCommandProcessor {

  public static final SimpleLogger LOG = SimpleLogger.getLog("Help");

  private List<ChatCommandProcessor> processors;

  public HelpProcessor(SoundboardBot bot, List<ChatCommandProcessor> processors) {
    super("", "Commands", bot);
    this.processors = processors;
  }

  protected void handleEvent(MessageReceivedEvent event, String message) {
    if (processors == null || processors.isEmpty()) return;

    boolean canRunAuthenticated =
            event.getAuthor().getName().equalsIgnoreCase(bot.getOwner());
    MessageBuilder mb = new MessageBuilder(true);

    mb.append("Type any of these commands in a channel or in a PM to me:\n\n");
    for (ChatCommandProcessor processor : processors) {
      if (processor.canBeRunByAnyone()
              || (!processor.canBeRunByAnyone()
              && processor.canBeRunBy(event.getAuthor(), event.getGuild()))) {
        // Print command help for this processor.
        if (!processor.canBeRunByAnyone() && !canRunAuthenticated)
          canRunAuthenticated = true;
        String cmdHelp = processor.getCommandHelpString();
        if (!cmdHelp.isEmpty())
          mb.append(cmdHelp + "\n");
      }
    }

    if (canRunAuthenticated) {
      mb.append("\nAn * symbol means this is a command only you "
              + "(and possibly others) can run.");
    }

    // Send all buffered data.
    for (String s : mb) {
      if (canRunAuthenticated) pm(event, s);
      else m(event, s);
    }
    LOG.info("Responded to help command from " + event.getAuthor().getName());
  }

  @Override
  public String getCommandHelpString() {
    return "";
  }
}