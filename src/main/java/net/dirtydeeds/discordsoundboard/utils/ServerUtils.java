package net.dirtydeeds.discordsoundboard.utils;

import java.util.function.Consumer;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;

import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;

public class ServerUtils {

  public static void changePublicChannelTopic(Guild guild, String topic) {
    if (guild == null) return;
    guild.getDefaultChannel().getManager().setTopic(topic).queue();
  }

  public static void addVoiceChannel(Guild guild, String name, Consumer<GuildChannel> after) {
    if (guild == null) return;
    if (name == null || name.isEmpty()) {
      name = StringUtils.randomPhrase();
    }
    guild.createVoiceChannel(name).queue(after);
  }

}
