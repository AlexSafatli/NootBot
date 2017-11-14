package net.dirtydeeds.discordsoundboard.utils;

import java.util.List;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.entities.MessageHistory;
import net.dv8tion.jda.core.entities.Message;

public class ChatUtils {

  public static final int NUM_MESSAGES_TO_GO_BACK = 1000;
  public static final int MAX_NUM_MESSAGES_TO_GO_BACK = 99;

  public static void clearBotMessagesInChannel(SoundboardBot bot,
      TextChannel channel) {
    MessageHistory history = new MessageHistory(channel);
    for (int i = 0; i < NUM_MESSAGES_TO_GO_BACK;
         i += MAX_NUM_MESSAGES_TO_GO_BACK) {
      RestAction<List<Message>> a =
        history.retrievePast(MAX_NUM_MESSAGES_TO_GO_BACK);
      a.queue(msgs -> {
        for (Message msg : msgs) {
          if (msg.getAuthor().equals(bot.getAPI().getSelfUser())) {
            try {
              msg.delete().queue();
            } catch (Exception e) {
              continue;
            }
          }
        }
      });
    }
  }

  public static void cacheMessagesInChannel(SoundboardBot bot,
      TextChannel channel) {
    MessageHistory history = new MessageHistory(channel);
    for (int i = 0; i < NUM_MESSAGES_TO_GO_BACK;
         i += MAX_NUM_MESSAGES_TO_GO_BACK) {
      RestAction<List<Message>> a =
        history.retrievePast(MAX_NUM_MESSAGES_TO_GO_BACK);
      a.queue(msgs -> {
        for (Message msg : msgs) {
          if (!msg.getAuthor().equals(bot.getAPI().getSelfUser()))
            StringUtils.cacheWords(msg.getContent());
        }
      });
    }
  }

  public static TextChannel getDiscussionChannel(SoundboardBot bot,
      Guild guild) {
    TextChannel channel = guild.getDefaultChannel();
    if (channel == null) {
      for (TextChannel c : guild.getTextChannels()) {
        if (bot.hasPermissionInChannel(c, Permission.MESSAGE_WRITE)) {
          channel = c; break;
        }
      }
    }
    return channel;
  }

}
