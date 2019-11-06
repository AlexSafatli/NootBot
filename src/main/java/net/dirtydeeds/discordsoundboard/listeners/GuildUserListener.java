package net.dirtydeeds.discordsoundboard.listeners;

import net.dirtydeeds.discordsoundboard.moderation.ModerationRules;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.utils.StyledEmbedMessage;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.internal.utils.SimpleLogger;

import java.util.Map;

public class GuildUserListener extends AbstractListener {

  public static final SimpleLogger LOG = SimpleLogger.getLog("GuildUsers");

  private Map<Guild, ModerationRules> modRules;

  public GuildUserListener(SoundboardBot bot, Map<Guild, ModerationRules> rules) {
    this.bot = bot;
    this.modRules = rules;
  }

  public void onGuildMemberJoin(GuildMemberJoinEvent event) {
    Member member = event.getMember();
    verifyRole(member);
  }

  public void onGuildMemberLeave(GuildMemberLeaveEvent event) {
    Guild guild = event.getGuild();
    TextChannel channel = bot.getBotChannel(guild);
    mentionMember(channel, event.getMember(),
            event.getMember().getEffectiveName() + " just left the server.",
            "Good riddance.");
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

  private void mentionMember(TextChannel channel, Member member, String title, String desc) {
    if (channel != null) {
      StyledEmbedMessage em = StyledEmbedMessage.forMember(bot, member, title, desc);
      embed(channel, em);
    }
  }

}
