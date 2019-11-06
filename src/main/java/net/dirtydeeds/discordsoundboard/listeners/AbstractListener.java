package net.dirtydeeds.discordsoundboard.listeners;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.utils.StyledEmbedMessage;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.function.Consumer;

public abstract class AbstractListener extends ListenerAdapter {

  protected SoundboardBot bot;

  protected void embed(TextChannel channel, StyledEmbedMessage embed) {
    if (bot.hasPermissionInChannel(channel, Permission.MESSAGE_WRITE)) {
      channel.sendMessage(embed.getMessage()).queue();
    }
  }

  protected void embed(TextChannel channel, StyledEmbedMessage embed, Consumer<Message> m) {
    if (bot.hasPermissionInChannel(channel, Permission.MESSAGE_WRITE)) {
      channel.sendMessage(embed.getMessage()).queue(m);
    }
  }

}