package net.dirtydeeds.discordsoundboard.chat.users;

import net.dirtydeeds.discordsoundboard.beans.User;
import net.dirtydeeds.discordsoundboard.chat.SingleArgumentChatCommandProcessor;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.utils.StringUtils;
import net.dirtydeeds.discordsoundboard.utils.Strings;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class AlternateHandleProcessor extends
        SingleArgumentChatCommandProcessor {

  public AlternateHandleProcessor(String prefix, SoundboardBot bot) {
    super(prefix, "Alternate Handles", bot);
  }

  protected void handleEvent(MessageReceivedEvent event, String message) {
    String m = event.getMessage().getContent(),
            handle = m.substring(getPrefix().length() + 1).trim();
    User u = bot.getUser(event.getAuthor());
    u.addAlternateHandle(handle);
    bot.getDispatcher().saveUser(u);
    pm(event, "Updated your handles to include `" + handle +
            "`!\nHandles registered for you now are: " +
            StringUtils.listToString(u.getAlternateHandles()));
  }

  @Override
  public String getCommandHelpString() {
    return getPrefix() + " <handle> - let the bot know about another handle " +
            "you go by on the internet; used for some *optional* functionality";
  }
}