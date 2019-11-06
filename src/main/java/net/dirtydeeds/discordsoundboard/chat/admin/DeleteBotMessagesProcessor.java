package net.dirtydeeds.discordsoundboard.chat.admin;

import net.dirtydeeds.discordsoundboard.chat.SingleArgumentChatCommandProcessor;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dirtydeeds.discordsoundboard.utils.ChatUtils;
import net.dv8tion.jda.api.entities.TextChannel;

public class DeleteBotMessagesProcessor extends
        SingleArgumentChatCommandProcessor {

  public DeleteBotMessagesProcessor(String prefix, SoundboardBot bot) {
    super(prefix, "Delete Messages", bot);
  }

  protected void handleEvent(MessageReceivedEvent event, String message) {
    ChatUtils.clearBotMessagesInChannel(bot, (TextChannel) event.getChannel());
  }

  @Override
  public String getCommandHelpString() {
    return getPrefix() + " - clear all bot messages in channel";
  }
}