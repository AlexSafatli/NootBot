package net.dirtydeeds.discordsoundboard.listeners;

import net.dirtydeeds.discordsoundboard.moderation.ModerationRules;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.utils.StyledEmbedMessage;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.utils.SimpleLog;

import java.util.Map;

public class GuildUserListener extends AbstractListener {

  public static final SimpleLog LOG = SimpleLog.getLog("GuildUsers");

  private Map<Guild, ModerationRules> modRules;

  public GuildUserListener(SoundboardBot bot, Map<Guild, ModerationRules> rules) {
    this.bot = bot;
    this.modRules = rules;
  }

  public void onGuildMemberJoin(GuildMemberJoinEvent event) {
    Guild guild = event.getGuild();
    Member member = event.getMember();
    verifyRole(member);
  }

  public void onGuildMemberLeave(GuildMemberLeaveEvent event) {
      Guild guild = event.getGuild();
      TextChannel channel = bot.getBotChannel(guild);
      if (channel != null) {
        StyledEmbedMessage em = new StyledEmbedMessage(
                event.getMember().getEffectiveName() + " just left the server.", bot);
        em.addDescription("Say farewell!");
        embed(channel, em);
      }
  }

  private void verifyRole(Member member) {
    ModerationRules rules = modRules.get(member.getGuild());
    if (rules != null) {
      RestAction<Void> assign = rules.giveDefaultRole(member);
      if (assign != null) {
        assign.queue(
                x -> bot.sendMessageToUser(
                        "Added " + member.getEffectiveName() + " to role " +
                                rules.getDefaultRole().getName(), bot.getOwner()));
      }
    }
  }

}
