package net.dirtydeeds.discordsoundboard.chat.admin;

import net.dirtydeeds.discordsoundboard.beans.Setting;
import net.dirtydeeds.discordsoundboard.chat.OwnerMultiArgumentChatCommandProcessor;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class SettingProcessor extends
        OwnerMultiArgumentChatCommandProcessor {

  public SettingProcessor(String prefix, SoundboardBot bot) {
    super(prefix, "Set Setting", bot);
  }

  protected void handleEvent(MessageReceivedEvent event, String message) {
    String[] args = getArguments();
    if (args.length < 1) {
      e(event, "Need a setting key (and possible setting value).");
      return;
    }
    String key = args[0];
    String value = (args.length > 1) ? args[1] : null;
    if (event.isFromType(ChannelType.PRIVATE)) {
      pm(event, "You did not send this command in a server.");
      return;
    }
    Setting s = bot.getDispatcher().getSetting(key, event.getGuild());
    if (s == null) {
      e(event, "Could not find setting for server with key `" + key + "`.");
      return;
    }
    if (value != null) {
      s.setValue(value);
      bot.getDispatcher().saveSetting(s);
    }
    pm(event, String.format("%s (**%d**) \u2014 `%s` : `%s`",
            event.getGuild().getName(), s.getGuildId(), s.getKey(),
            s.getValue()));
  }

  @Override
  public String getCommandHelpString() {
    return getPrefix() + " <key>[, <value>] (*) - set a setting for this server " +
            "by a key or see what the setting value is";
  }
}