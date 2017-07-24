package net.dirtydeeds.discordsoundboard.chat;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.core.entities.Invite;
import net.dv8tion.jda.core.requests.restaction.InviteAction;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class TemporaryInviteToGuildProcessor extends AuthenticatedSingleArgumentChatCommandProcessor {
  
  private static final int MAX_AGE = 3600;
  private static final int MAX_USES = 10;

  public TemporaryInviteToGuildProcessor(String prefix, SoundboardBot bot) {
    super(prefix, "Temporary Invite Link to Server", bot);
  }

  protected void handleEvent(MessageReceivedEvent event, String message) {
    if (event.getGuild() == null) {
      pm(event, "You need to be in a server!");
      return;
    }
    if (bot.hasPermissionInChannel(event.getGuild().getPublicChannel(), Permission.CREATE_INSTANT_INVITE)) {
      InviteAction invite = event.getGuild().getPublicChannel().createInvite();
      invite.setMaxAge(MAX_AGE).setMaxUses(MAX_USES).setTemporary(true).queue((Invite i)-> {
        pm(event, i.getURL());
      });
    } else {
      e(event, "I need permission to create instant invites!");
    }
  }

  @Override
  public String getCommandHelpString() {
    return getPrefix() + " (*) - generates a temporary invite link to this server";
  }
  
}