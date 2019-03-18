package net.dirtydeeds.discordsoundboard.chat.admin;

import net.dirtydeeds.discordsoundboard.chat.OwnerSingleArgumentChatCommandProcessor;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.utils.Reusables;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class ServerDonationMessageProcessor extends
        OwnerSingleArgumentChatCommandProcessor {

  public ServerDonationMessageProcessor(String prefix, SoundboardBot bot) {
    super(prefix, "Donation Message", bot);
  }

  protected void handleEvent(MessageReceivedEvent event, String message) {
    Reusables.sendDonationMessage(bot);
  }

  @Override
  public String getCommandHelpString() {
    return getPrefix() + " (*) - broadcast a donation message";
  }
}