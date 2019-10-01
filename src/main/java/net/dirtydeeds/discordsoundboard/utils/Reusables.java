package net.dirtydeeds.discordsoundboard.utils;

import net.dirtydeeds.discordsoundboard.moderation.ModerationRules;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.service.SoundboardDispatcher;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.managers.ChannelManagerUpdatable;

public class Reusables {

  public static void setRandomGame(SoundboardBot b) {
    Game currentGame = b.getAPI().getPresence().getGame();
    String newName = getRandomGameName(b.getDispatcher());
    while (currentGame != null && newName.equals(currentGame.getName())) {
      newName = getRandomGameName(b.getDispatcher());
    }
    b.getAPI().getPresence().setGame(Game.of(newName));
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
        ChannelManagerUpdatable mngr = publicChannel.getManagerUpdatable();
        Message msg = (Message)RandomUtils.chooseOne(rules.getSayings().toArray());
        mngr.getTopicField().setValue(msg.getStrippedContent());
        mngr.update().queue();
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
