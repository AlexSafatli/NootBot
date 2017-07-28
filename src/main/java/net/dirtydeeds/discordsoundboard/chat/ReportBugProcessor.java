package net.dirtydeeds.discordsoundboard.chat;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class ReportBugProcessor extends SingleArgumentChatCommandProcessor {

  public ReportBugProcessor(String prefix, SoundboardBot bot) {
    super(prefix, "Ah!", bot);
  }

  protected void handleEvent(MessageReceivedEvent event,
                             String message) {
    pm(event, "Got it!");
    String guild = (event.getGuild() != null) ?
                   event.getGuild().getName() : "via PM";
    bot.sendMessageToUser("`" + event.getMessage().getContent().trim() + "`\n" +
                          event.getAuthor().getName() + " \u2014 " +
                          guild, bot.getOwner());
  }

  @Override
  public String getCommandHelpString() {
    return getPrefix() + " - report a bug";
  }
}