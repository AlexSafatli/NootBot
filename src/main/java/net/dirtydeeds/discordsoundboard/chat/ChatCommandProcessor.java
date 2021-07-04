package net.dirtydeeds.discordsoundboard.chat;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;

public interface ChatCommandProcessor {

  void process(MessageReceivedEvent event);
  void processAsSlashCommand(SlashCommandEvent event);
  boolean isApplicableCommand(MessageReceivedEvent event);
  boolean isApplicableCommand(SlashCommandEvent event);
  boolean canBeRunByAnyone();
  boolean canBeRunBy(User user, Guild guild);
  boolean canBeRunAsSlashCommand();
  String getTitle();
  String getCommandHelpString();

}