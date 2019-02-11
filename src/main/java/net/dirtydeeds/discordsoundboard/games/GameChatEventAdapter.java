package net.dirtydeeds.discordsoundboard.games;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
//import net.dv8tion.jda.JDA;
import net.dv8tion.jda.core.entities.MessageChannel;
//import net.dv8tion.jda.core.entities.Message;
//import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class GameChatEventAdapter {

  //private SoundboardBot bot;
  //private JDA api;

  public GameChatEventAdapter(SoundboardBot bot) {
    //this.bot = bot;
    //this.api = bot.getAPI();
  }

  public void process(MessageChannel channel, String message, GameContext context) {
    //Message msg = new GameChatMessage(channel, message, context);
    //MessageReceivedEvent event = new GameChatMessageReceivedEvent(api, 0, msg);
    //bot.getChatListener().onMessageReceived(event);
  }

}
