package net.dirtydeeds.discordsoundboard.utils;

import net.dirtydeeds.discordsoundboard.moderation.ModerationRules;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.service.SoundboardDispatcher;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.managers.ChannelManager;

public class Reusables {

  public static void setRandomGame(SoundboardBot b) {
    Activity currentGame = b.getAPI().getPresence().getActivity();
    String newName = getRandomGameName(b.getDispatcher());
    while (currentGame != null && newName.equals(currentGame.getName())) {
      newName = getRandomGameName(b.getDispatcher());
    }
    b.getAPI().getPresence().setActivity(Activity.playing(newName));
  }

  private static String getRandomGameName(SoundboardDispatcher dispatcher) {
    return (String) RandomUtils.chooseOne(StringUtils.randomPhrase(),
                                          StringUtils.randomString(
                                            dispatcher.getPhrases()));
  }

   public static void setRandomTopicForPublicChannels(SoundboardBot b) {
     for (Guild guild : b.getGuilds()) {
       TextChannel publicChannel = b.getBotChannel(guild);
       ModerationRules rules = b.getRulesForGuild(guild);
       if (publicChannel != null && rules.isPermitted()) {
         ChannelManager mngr = publicChannel.getManager();
         Message msg = (Message)RandomUtils.chooseOne(
                 rules.getSayings().toArray());
         String saying = msg.getContentStripped();
         mngr.setTopic(saying).queue();
       }
     }
   }

  public static void updateSayingsCache(SoundboardBot b) {
    for (Guild guild : b.getGuilds()) {
      ModerationRules rules = b.getRulesForGuild(guild);
      if (rules.isPermitted()) {
        rules.updateSayings();
      }
    }
  }

}
