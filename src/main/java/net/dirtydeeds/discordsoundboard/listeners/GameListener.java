package net.dirtydeeds.discordsoundboard.listeners;

import net.dirtydeeds.discordsoundboard.Thumbnails;

import net.dirtydeeds.discordsoundboard.games.*;
import net.dirtydeeds.discordsoundboard.utils.StringUtils;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Game.GameType;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.user.UserGameUpdateEvent;
import net.dv8tion.jda.core.utils.SimpleLog;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class GameListener extends AbstractListener {

  public static final SimpleLog LOG = SimpleLog.getLog("Game");

  private List<GameUpdateProcessor> processors;
  private static final List<String> MONITORED_GAMES = Arrays.asList(
        new String[] {
          "League of Legends", "PUBG",
          "Divinity Original Sin 2", "Destiny 2", "Dead by Daylight"
        }
      );
  private static final String[] THUMBNAIL_URLS = new String[] {
    Thumbnails.LEAGUE, Thumbnails.PUBG, Thumbnails.DOS2,
    Thumbnails.DESTINY2, Thumbnails.DBD
  };

  public GameListener(SoundboardBot bot) {
    this.bot = bot;
    this.processors = new LinkedList<>();
    initializeProcessors();
  }

  private void initializeProcessors() {
    for (int i = 0; i < MONITORED_GAMES.size(); ++i) {
      String game = MONITORED_GAMES.get(i), url = THUMBNAIL_URLS[i];
      LOG.info("Initializing game launch processor for " + game);
      if (url != null && url.length() > 0) {
        processors.add(new SpecificGameStartProcessor(bot, game, url));
      } else {
        processors.add(new SpecificGameStartProcessor(bot, game));
      }
    }
  }

  private void logGameChange(String name, Guild guild, Game previousGame,
                             Game currentGame) {
    String guildName = (guild != null) ? guild.getName() : null;
    if (currentGame == null && previousGame != null) {
      if (previousGame.getType().equals(GameType.STREAMING)) return;
      LOG.info(name + " stopped playing " + previousGame.getName() +
               " in server " + guildName + ".");
    } else if (previousGame == null) {
      if (currentGame.getType().equals(GameType.STREAMING)) return;
      LOG.info(name + " started playing " + currentGame.getName() +
               " in server " + guildName + ".");
    } else {
      if (currentGame.getType().equals(GameType.STREAMING) ||
          previousGame.getType().equals(GameType.STREAMING)) return;
      LOG.info(name + " changed to " + currentGame.getName() + " from " +
               previousGame.getName() + " in server " + guildName + ".");
    }
  }

  private void cacheGameName(Game game) {
    if (game == null || game.getType().equals(GameType.STREAMING)) return;
    StringUtils.cacheWords(game.getName());
  }

  public void onUserGameUpdate(UserGameUpdateEvent event) {
    User user = event.getUser();
    if (user.isBot()) return; // Ignore bots.
    Guild guild = event.getGuild();
    Member member = guild.getMemberById(user.getId());

    String name = user.getName();
    Game previousGame = event.getPreviousGame(),
         currentGame = member.getGame();
    logGameChange(name, guild, previousGame, currentGame);
    cacheGameName(currentGame);

    for (GameUpdateProcessor processor : processors) {
      if (processor.isApplicableUpdateEvent(event, user)) {
        processor.process(event);
        LOG.info("Processed game update event with " + processor);
        if (processor.isMutuallyExclusive()) {
          LOG.info("That processor cannot be run with others. Stopping.");
          return;
        }
      }
    }
  }
}
