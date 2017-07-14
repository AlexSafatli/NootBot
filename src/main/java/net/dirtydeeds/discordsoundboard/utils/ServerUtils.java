package net.dirtydeeds.discordsoundboard.utils;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;

import net.dv8tion.jda.core.entities.Icon;
import net.dv8tion.jda.core.entities.Guild;

public class ServerUtils {

  public static void changePublicChannelTopic(Guild guild, String topic) {
    if (guild == null) return;
    guild.getPublicChannel().getManager().setTopic(topic).queue();
  }

}
