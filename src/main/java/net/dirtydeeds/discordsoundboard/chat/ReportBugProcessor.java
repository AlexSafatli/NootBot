package net.dirtydeeds.discordsoundboard.chat;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class ReportBugProcessor extends SingleArgumentChatCommandProcessor {

  public ReportBugProcessor(String prefix, SoundboardBot bot) {
    super(prefix, "Ah!", bot);
  }

  protected void handleEvent(MessageReceivedEvent event, String message) {
    pm(event, "Got it!");
    bot.sendMessageToUser(event.getMessage().getContent().trim() + " \u2014 " + event.getAuthor().getName() + " \u2014 " + event.getGuild(), bot.getOwner());
  }

  @Override
  public String getCommandHelpString() {
    return getPrefix() + " - report a bug";
  }

}