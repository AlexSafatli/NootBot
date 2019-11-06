package net.dirtydeeds.discordsoundboard.utils;

import java.util.function.Consumer;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;

import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Channel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.core.managers.GuildController;

public class ServerUtils {

  public static void changePublicChannelTopic(Guild guild, String topic) {
    if (guild == null) return;
    guild.getDefaultChannel().getManager().setTopic(topic).queue();
  }

  public static void addVoiceChannel(Guild guild, String name, Consumer<Channel> after) {
    if (guild == null) return;
    if (name == null || name.isEmpty()) {
      name = StringUtils.randomPhrase();
    }
    GuildController ctrl = guild.getController();
    ctrl.createVoiceChannel(name).queue(after);
  }

}
