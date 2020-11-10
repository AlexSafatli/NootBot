package net.dirtydeeds.discordsoundboard.chat.sounds;

import net.dirtydeeds.discordsoundboard.async.DeleteMessageJob;
import net.dirtydeeds.discordsoundboard.beans.SoundFile;
import net.dirtydeeds.discordsoundboard.chat.MultiArgumentChatCommandProcessor;
import net.dirtydeeds.discordsoundboard.chat.SingleArgumentChatCommandProcessor;
import net.dirtydeeds.discordsoundboard.org.Category;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.utils.MessageBuilder;
import net.dirtydeeds.discordsoundboard.utils.Strings;
import net.dirtydeeds.discordsoundboard.utils.StyledEmbedMessage;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.internal.utils.JDALogger;

import java.util.LinkedList;
import java.util.List;

public class PlayMultipleRandomProcessor extends MultiArgumentChatCommandProcessor {

  public PlayMultipleRandomProcessor(String prefix, SoundboardBot soundPlayer) {
    super(prefix, "Multiple Random Sounds", soundPlayer);
  }

  protected void handleEvent(MessageReceivedEvent event, String message) {
    String[] args = getArguments();
    if (args.length < 1) {
      w(event, "I need a number of sounds to play.");
      return;
    }
    int num = Integer.parseInt(args[0]);
    if (num <= 0) {
      w(event, "I need a positive number of sounds to play (not " + num + ").");
      return;
    }
    String category = (args.length > 1) ? args[1] : null,
            desc = "No category was provided.",
            title = "Played Multiple Random Sounds";
    if (!bot.isAllowedToPlaySound(event.getAuthor())) {
      pm(event, "You're not allowed to do that.");
      return;
    }

    LinkedList<String> filesPlayed = new LinkedList<>();
    MessageBuilder mb = new MessageBuilder(1024);

    try {
      if (category != null) {
        if (bot.isASoundCategory(category)) {
          Category cat = bot.getSoundCategory(category);
          desc = "The **" + cat + "** category was provided.";
          title = "Played Multiple Random " + cat + " Sounds";
          for (int i = 1; i <= num; ++i) {
            String sound = bot.playRandomFileForCategory(event.getAuthor(), category);
            filesPlayed.add(sound);
            SoundFile f = bot.getDispatcher().getSoundFileByName(sound);
            if (f != null)
              mb.append("`" + sound + "` (**" + f.getNumberOfPlays() + "**)");
            else
              mb.append("`" + sound + "`");
            if (i == num && num > 1) mb.append(", and ");
            else if (i < num - 1) mb.append(", ");
          }
        } else {
          w(event, String.format("Category `%s` was not found.", category));
        }
      } else {
        for (int i = 1; i <= num; ++i) {
          String sound = bot.playRandomFile(event.getAuthor());
          filesPlayed.add(sound);
          SoundFile f = bot.getDispatcher().getSoundFileByName(sound);
          if (f != null)
            mb.append("`" + sound + "` (**" + f.getNumberOfPlays() + "**)");
          else
            mb.append("`" + sound + "`");
          if (i == num && num > 1) mb.append(", and ");
          else if (i < num - 1) mb.append(", ");
        }
      }
      if (filesPlayed.size() > 0 &&
          bot.getUsersVoiceChannel(event.getAuthor()) != null) {
        JDALogger.getLog("Sound").info("Played multiple sounds in server " +
                 event.getGuild().getName());
        List<Message> messages = makeMessages(
                title,
                "Queued sound" + (num > 1 ? "s " : " ") +
                        Strings.SEPARATOR + desc + Strings.SEPARATOR +
                        event.getAuthor().getAsMention(),
                event.getAuthor(), mb);
        TextChannel c = bot.getBotChannel(event.getGuild());
        for (Message msg : messages) {
          if (c != null) {
            RestAction<Message> m = c.sendMessage(msg);
            m.queue((Message s) -> bot.getDispatcher().getAsyncService().runJob(
                    new DeleteMessageJob(s, 500)));
          }
        }
      }
    } catch (Exception e) {
      e(event, e.toString());
    }
  }

  private List<Message> makeMessages(String title, String desc,
                                     User user,
                                     MessageBuilder mb) {
    List<Message> messages = new LinkedList<>();
    if (mb != null) {
      for (String str : mb) {
        boolean paginatedMessage = messages.size() >= 1;
        String titleSuffix =
                paginatedMessage ? " (" + (messages.size() + 1) + ")" : "";
        StyledEmbedMessage msg =
                StyledEmbedMessage.forUser(
                        bot, user, title + titleSuffix,
                        paginatedMessage ? "" : desc);
        msg.addContent("Sounds Queued", str, false);
        messages.add(msg.getMessage());
      }
    }
    return messages;
  }

  @Override
  public String getCommandHelpString() {
    return getPrefix() + " [category] - play a random sound (from " +
           "category if specified)";
  }
}