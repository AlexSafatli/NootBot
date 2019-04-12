package net.dirtydeeds.discordsoundboard.moderation;

import net.dirtydeeds.discordsoundboard.utils.StringUtils;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.managers.GuildController;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.restaction.RoleAction;

import java.util.LinkedList;
import java.util.List;

class RolesModerator {

  static RestAction<Void> assertMemberHasRole(Member member, Role role) {
    if (!member.getRoles().contains(role)) {
      GuildController ctrl = member.getGuild().getController();
      return ctrl.addSingleRoleToMember(member, role);
    }
    return null;
  }

  static RestAction<Void> assertMemberOnlyHasRole(Member member, Role role) {
    GuildController ctrl = member.getGuild().getController();
    ctrl.modifyMemberRoles(member, new LinkedList<>(), member.getRoles()).complete();
    return ctrl.addSingleRoleToMember(member, role);
  }

  static Role newTopicRole(Guild guild, String name, String channelName) {
    for (Role role : guild.getRoles()) {
      if (role.getName().equals(name)) return null;
    }
    GuildController ctrl = guild.getController();
    RoleAction action = ctrl.createRole();
    Role r = action.setColor(StringUtils.toColor(
            name)).setHoisted(false).setMentionable(false).setName(name
    ).complete();
    ctrl.createTextChannel(channelName).addPermissionOverride(
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
