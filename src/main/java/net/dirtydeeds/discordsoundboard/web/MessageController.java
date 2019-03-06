package net.dirtydeeds.discordsoundboard.web;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.service.SoundboardDispatcher;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedList;
import java.util.List;

@RestController
public class MessageController {

  @Autowired
  SoundboardDispatcher dispatcher;

  @RequestMapping(value = "/bots")
  public ResponseEntity<Object> getBots() {
    List<String> names = new LinkedList<>();
    for (SoundboardBot bot : dispatcher.getBots()) {
      names.add(bot.getBotName());
    }
    return new ResponseEntity<>(names, HttpStatus.OK);
  }

  @RequestMapping(value = "/bots/{id}/servers")
  public ResponseEntity<Object> getServers(@PathVariable("id") String id) {
    SoundboardBot bot = dispatcher.getBots().get(Integer.parseInt(id));
    return new ResponseEntity<>(bot.getGuilds(), HttpStatus.OK);
  }

  @RequestMapping(value = "/bots/{id}/servers/{sid}/channels")
  public ResponseEntity<Object> getTextChannels(@PathVariable("id") String id, @PathVariable("sid") String sid) {
    SoundboardBot bot = dispatcher.getBots().get(Integer.parseInt(id));
    Guild guild = bot.getGuilds().get(Integer.parseInt(sid));
    return new ResponseEntity<>(guild.getTextChannels(), HttpStatus.OK);
  }

  @RequestMapping(value = "/bots/{id}/servers/{sid}/channels/{cid}/messages", method = RequestMethod.POST)
  public ResponseEntity<Object> sendMessageToChannel(@PathVariable("id") String id, @PathVariable("sid") String sid, @PathVariable("cid") String cid, @RequestBody String msg) {
    SoundboardBot bot = dispatcher.getBots().get(Integer.parseInt(id));
    Guild guild = bot.getGuilds().get(Integer.parseInt(sid));
    TextChannel c = guild.getTextChannels().get(Integer.parseInt(cid));
    c.sendMessage(msg).queue();
    return new ResponseEntity<>("Message sent", HttpStatus.OK);
  }

}
