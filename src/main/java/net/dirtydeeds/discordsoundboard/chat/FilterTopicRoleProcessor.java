package net.dirtydeeds.discordsoundboard.chat;

import net.dirtydeeds.discordsoundboard.moderation.ModerationRules;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.core.entities.Role;

import java.util.List;
import java.util.regex.Pattern;

public class FilterTopicRoleProcessor extends FilterChatProcessor {
  public FilterTopicRoleProcessor(Pattern regexp, String cname, SoundboardBot bot) {
    super(regexp, cname, bot);
  }

  // TODO
}