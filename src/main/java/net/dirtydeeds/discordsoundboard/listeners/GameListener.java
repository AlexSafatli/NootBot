package net.dirtydeeds.discordsoundboard.listeners;

import net.dirtydeeds.discordsoundboard.games.GameUpdateProcessor;
import net.dirtydeeds.discordsoundboard.games.GenericGameStartProcessor;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.utils.StringUtils;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Activity.ActivityType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.user.UserActivityStartEvent;
import net.dv8tion.jda.internal.utils.JDALogger;

import java.util.LinkedList;
import java.util.List;

public class GameListener extends AbstractListener {

  private List<GameUpdateProcessor> processors;

  public GameListener(SoundboardBot bot) {
    this.bot = bot;
    initializeProcessors();
  }

  private void initializeProcessors() {
    processors = new LinkedList<>();
    processors.add(new GenericGameStartProcessor(bot));
  }

  private void cacheGameName(Activity game) {
    if (game == null || game.getType().equals(ActivityType.STREAMING) ||
            game.getName().equals("Spotify")) return;
    StringUtils.cacheWords(game.getName());
  }

  public void onUserActivityStart(UserActivityStartEvent event) {
    if (event.getUser().isBot()) return; // Ignore bots.

    User user = event.getUser();
    Guild guild = event.getGuild();
    Member member = guild.getMemberById(user.getId());

    String name = user.getName();
    List<Activity> activities = member.getActivities();
    for (Activity g : activities) {
      cacheGameName(g);
    }

    for (GameUpdateProcessor processor : processors) {
      if (processor.isApplicableUpdateEvent(event, user)) {
        processor.process(event);
        JDALogger.getLog("Game").info("Processed game update event with " + processor);
        if (processor.isMutuallyExclusive()) {
          JDALogger.getLog("Game").info("That processor cannot be run with others. Stopping.");
          return;
        }
      }
    }
  }
}
