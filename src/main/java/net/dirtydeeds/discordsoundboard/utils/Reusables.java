package net.dirtydeeds.discordsoundboard.utils;

import java.awt.Color;

import net.dirtydeeds.discordsoundboard.beans.Setting;
import net.dirtydeeds.discordsoundboard.moderation.ChannelMessageCrawler;
import net.dirtydeeds.discordsoundboard.moderation.ModerationRules;
import net.dv8tion.jda.core.entities.Game;

import net.dirtydeeds.discordsoundboard.Icons;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.service.SoundboardDispatcher;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.managers.ChannelManagerUpdatable;

public class Reusables {

  private static final String DONATION_TITLE = "Heya!";
  private static final String DONATION_MSG = "I am hosted on a personally owned computer that is always on and consuming power. Not cheap! If you wanna help out, you can.";
  private static final String DONATION_BUG_TITLE = "Found a bug!";
  private static final String DONATION_BUG = "You can report any bugs you find using the `.bug` command.";
  private static final String DONATION_DONATE_TITLE = "Donate?";
  private static final String DONATION_DONATE = "You can do this through patreon <https://www.patreon.com/asaph>";

  private static final Color DONATION_COLOR = new Color(255, 153, 255);

  public static void sendDonationMessage(SoundboardBot b) {
    StyledEmbedMessage msg = new StyledEmbedMessage(DONATION_TITLE, b);
    msg.setColor(DONATION_COLOR);
    msg.addDescription(DONATION_MSG);
    msg.addContent(DONATION_BUG_TITLE, DONATION_BUG, false);
    msg.addContent(DONATION_DONATE_TITLE, DONATION_DONATE, false);
    b.sendMessageToAllGuilds(msg.getMessage());
  }

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
