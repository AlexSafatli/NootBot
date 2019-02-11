package net.dirtydeeds.discordsoundboard.utils;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.VoiceChannel;

public class VoiceUtils {

  public static long numUsersInVoiceChannels(Guild guild) {
    // Does not count bots or users in voice channels.
    long numUsers = 0;
    for (VoiceChannel channel : guild.getVoiceChannels()) {
      if (guild.getAfkChannel() != null && channel.getId().equals(guild.getAfkChannel().getId())) continue;
      for (Member m : channel.getMembers()) {
        if (!m.getUser().isBot()) ++numUsers;
      }
    }
    return numUsers;
  }

}
