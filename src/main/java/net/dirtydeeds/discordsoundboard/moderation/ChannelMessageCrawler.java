package net.dirtydeeds.discordsoundboard.moderation;

import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.LinkedList;
import java.util.List;

public class ChannelMessageCrawler {

  private TextChannel channel;
  private List<Message> messages;

  public ChannelMessageCrawler(TextChannel channel) {
    this.channel = channel;
    this.messages = new LinkedList<>();
  }

  public void crawl() {
    messages.clear();
    for (Message m : channel.getIterableHistory().cache(false)) {
      messages.add(m);
    }
  }

  public List<Message> getMessages() {
    return this.messages;
  }

  public List<String> getMessageContent() {
    List<String> strings = new LinkedList<>();
    for (Message m : messages) {
      strings.add(m.getContent());
    }
    return strings;
  }

}
