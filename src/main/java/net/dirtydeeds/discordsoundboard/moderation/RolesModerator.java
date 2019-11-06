package net.dirtydeeds.discordsoundboard.moderation;

import net.dirtydeeds.discordsoundboard.utils.StringUtils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.RoleAction;

import java.util.LinkedList;
import java.util.List;

class RolesModerator {

  static RestAction<Void> assertMemberHasRole(Member member, Role role) {
    if (!member.getRoles().contains(role)) {
      Guild g = member.getGuild();
      return g.addRoleToMember(member, role);
    }
    return null;
  }

  static RestAction<Void> assertMemberOnlyHasRole(Member member, Role role) {
    Guild g = member.getGuild();
    g.modifyMemberRoles(member, new LinkedList<>(), member.getRoles()).complete();
    return g.addRoleToMember(member, role);
  }

  static Role newTopicRole(Guild guild, String name, String channelName) {
    for (Role role : guild.getRoles()) {
      if (role.getName().equals(name)) return null;
    }
    RoleAction action = guild.createRole();
    Role r = action.setColor(StringUtils.toColor(
            name)).setHoisted(false).setMentionable(false).setName(name
    ).complete();
    guild.createTextChannel(channelName).addPermissionOverride(
            guild.getPublicRole(), 0,
            Permission.ALL_TEXT_PERMISSIONS).addPermissionOverride(
                    r, Permission.ALL_TEXT_PERMISSIONS, 0).queue();
    return r;
  }

  static TextChannel getTopicForRole(Role role) {
    Guild guild = role.getGuild();
    List<Role> otherRoles = guild.getRoles();
    otherRoles.remove(role);
    for (TextChannel channel : guild.getTextChannels()) {
      boolean canViewAlone = true;
      if (role.hasPermission(channel, Permission.MESSAGE_READ, Permission.MESSAGE_WRITE)) {
        for (Role otherRole : otherRoles) {
          if (!otherRole.hasPermission(Permission.ADMINISTRATOR) && otherRole.hasPermission(channel, Permission.MESSAGE_READ, Permission.MESSAGE_WRITE))
            canViewAlone = false;
        }
        if (canViewAlone) return channel;
      }
    }
    return null;
  }

  static boolean isTopicRole(Role role) {
    return getTopicForRole(role) != null;
  }

}
