package net.dirtydeeds.discordsoundboard.games;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.utils.StyledEmbedMessage;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.user.UserActivityUpdateEvent;
import net.dv8tion.jda.api.requests.RestAction;

import java.util.function.Consumer;

public abstract class AbstractGameUpdateProcessor implements GameUpdateProcessor {

  private static final String ERROR_TITLE = "Derp!";

  protected SoundboardBot bot;

  public AbstractGameUpdateProcessor(SoundboardBot bot) {
    this.bot = bot;
  }

  protected abstract void handleEvent(UserActivityUpdateEvent event, User user);

  public abstract boolean isApplicableUpdateEvent(UserActivityUpdateEvent event, User user);

  public void process(UserActivityUpdateEvent event) {
    handleEvent(event, event.getUser());
  }

  protected void embed(TextChannel channel, StyledEmbedMessage embed) {
    if (bot.hasPermissionInChannel(channel, Permission.MESSAGE_WRITE)) {
      channel.sendMessage(embed.getMessage()).queue();
    }
  }

  protected void embed(TextChannel channel, StyledEmbedMessage embed, Consumer<Message> m) {
    if (bot.hasPermissionInChannel(channel, Permission.MESSAGE_WRITE)) {
      RestAction<Message> ra = channel.sendMessage(embed.getMessage());
      ra.queue(m);
    }
  }

  protected void error(UserActivityUpdateEvent event, Exception e) {
    Guild guild = event.getGuild();
    VoiceChannel botChannel = bot.getConnectedChannel(guild);
    Game game = guild.getMemberById(event.getUser().getId()).getGame();
    StyledEmbedMessage msg = new StyledEmbedMessage(ERROR_TITLE, bot).isError(true);
    msg.addDescription(e.toString());
    msg.addContent("Connected Channel", botChannel.getName(), true);
    msg.addContent("Triggering User", event.getUser().getName(), true);
    msg.addContent("Game", game.getName(), true);
    msg.addContent("Processor", this.toString(), true);
    embed(bot.getBotChannel(guild), msg);
    e.printStackTrace();
  }

  public String toString() {
    return this.getClass().getSimpleName();
  }

}