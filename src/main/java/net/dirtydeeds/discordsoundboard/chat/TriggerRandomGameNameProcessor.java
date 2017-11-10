package net.dirtydeeds.discordsoundboard.chat;

import java.util.List;

import net.dirtydeeds.discordsoundboard.utils.*;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.utils.SimpleLog;

public class TriggerRandomGameNameProcessor extends
  OwnerSingleArgumentChatCommandProcessor {

  public TriggerRandomGameNameProcessor(String prefix, SoundboardBot bot) {
    super(prefix, "Random Game Name", bot);
  }

  protected void handleEvent(MessageReceivedEvent event, String message) {
    Reusables.setRandomGame(bot);
  }

  @Override
  public String getCommandHelpString() {
    return getPrefix() + " (*) - trigger a random game name change";
  }

}
