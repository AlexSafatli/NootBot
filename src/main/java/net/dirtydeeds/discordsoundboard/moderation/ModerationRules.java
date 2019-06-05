package net.dirtydeeds.discordsoundboard.moderation;

import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.requests.RestAction;

import java.util.LinkedList;
import java.util.List;

public class ModerationRules {

  private Guild guild;
  private final boolean permitted;
  private Role defaultRole;
  private Role suspendedRole;
  private ChannelMessageCrawler sayingsCrawler;

  public ModerationRules(Guild guild, boolean permitted) {
    this.guild = guild;
    this.permitted = permitted;
  }

  public boolean isPermitted() {
    return permitted;
  }

  public Role getDefaultRole() {
    return defaultRole;
  }

  public void setDefaultRole(Role defaultRole) {
    this.defaultRole = defaultRole;
  }

  public Role getSuspendedRole() {
    return suspendedRole;
  }

  public void setSuspendedRole(Role suspendedRole) {
    this.suspendedRole = suspendedRole;
  }

  public void setSayingsChannel(TextChannel sayingsChannel) {
    if (sayingsChannel == null) return;
    sayingsCrawler = new ChannelMessageCrawler(sayingsChannel);
    sayingsCrawler.crawl();
  }

  public List<Message> getSayings() {
    return sayingsCrawler.getMessages();
  }

  public List<Message> updateSayings() {
    sayingsCrawler.crawl();
    return getSayings();
  }

  public RestAction<Void> giveDefaultRole(Member member) {
    if (permitted && getDefaultRole() != null) {
      return RolesModerator.assertMemberOnlyHasRole(member, defaultRole);
    }
    return null;
  }

  public RestAction<Void> giveSuspendedRole(Member member) {
    if (permitted && getSuspendedRole() != null) {
      return RolesModerator.assertMemberOnlyHasRole(member, suspendedRole);
    }
    return null;
  }

  public List<Role> getTopicRoles() {
    List<Role> roles = new LinkedList<>();
    for (Role role : guild.getRoles()) {
      if (RolesModerator.isTopicRole(role)) roles.add(role);
    }
    return roles;
  }

  public TextChannel getTopicForTopicRole(Role role) {
    return RolesModerator.getTopicForRole(role);
  }

  public RestAction<Void> giveTopicRole(Member member, Role role) {
    if (permitted && getTopicRoles().contains(role)) {
      return RolesModerator.assertMemberHasRole(member, role);
    }
    return null;
  }

  public Role newTopicRole(String name, String channelName) {
    if (permitted) {
      return RolesModerator.newTopicRole(guild, name, channelName);
    }
    return null;
  }

}
