package net.dirtydeeds.discordsoundboard.async;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.dean.jraw.models.Submission;
import net.dean.jraw.models.Subreddit;
import net.dirtydeeds.discordsoundboard.reddit.Reddit;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.service.SoundboardDispatcher;
import net.dirtydeeds.discordsoundboard.utils.StringUtils;
import net.dv8tion.jda.entities.Game;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.utils.SimpleLog;

public class TopRedditSubmissionsJob extends AbstractAsyncJob {

	public static final SimpleLog LOG = SimpleLog.getLog("TopRedditJob");
	private static final String SUBREDDIT = "games";
	private static final long HIGH_UPVOTES = 5000;
	private static final int MIN_USERS_RELEVANT_TO_PER_GUILD = 2;
	private static final int TITLE_TRUNCATE_LENGTH = 128;
	private final String msgIntro;
	private List<String> pastPosts;
	
	public TopRedditSubmissionsJob() {
		NUMBER_HOURS_BETWEEN = 2; // Check every two hours.
		msgIntro = String.format("I found this on `/r/%s` so it must be good!\n", SUBREDDIT);
		pastPosts = new LinkedList<>();
	}
	
	public void handle(SoundboardDispatcher dispatcher) {
		Reddit r = new Reddit();
		List<String> newPosts = new LinkedList<>();
		LOG.info("Getting posts from subreddit /r/" + SUBREDDIT);
		List<Submission> gamePosts = r.getSubredditTop(SUBREDDIT);
		for (SoundboardBot bot : dispatcher.getBots()) {
			for (Guild guild : bot.getGuilds()) {
				// See if relevant to anyone online. Want at least certain #/people.
				Map<String, Integer> gameMap = new HashMap<>();
				Map<String, Submission> postMap = new HashMap<>();
				for (User user : guild.getUsers()) {
					Game g = user.getCurrentGame();
					if (g != null) {
						if (gameMap.get(g.getName()) == null) gameMap.put(g.getName(), 0);
						for (Submission post : gamePosts) {
							Subreddit sr = r.getSubreddit(post.getSubredditName());
							if (sr == null) {
								LOG.warn("Could not resolve subreddit " + post.getSubredditName());
								continue;
							}
							if (post.getScore() >= HIGH_UPVOTES &&
									(post.getTitle().contains(g.getName()) || sr.getPublicDescription().contains(g.getName()))) {
								if (!newPosts.contains(post.getId()))
									newPosts.add(post.getId());
								if (!pastPosts.contains(post.getId())) {
									LOG.info("Post from /r/" + post.getSubredditName() + " seems relevant to " + user.getUsername());
									gameMap.put(g.getName(), gameMap.get(g.getName()) + 1);
									postMap.put(g.getName(), post);
								}
							}
						}
					}
				}
				for (String game : gameMap.keySet()) {
					int numUsers = gameMap.get(game);
					if (numUsers >= MIN_USERS_RELEVANT_TO_PER_GUILD) {
						Submission post = postMap.get(game);
						guild.getPublicChannel().sendMessageAsync(msgIntro + "This is related to game **" + game + "**\n*" + 
								StringUtils.truncate(post.getTitle(), TITLE_TRUNCATE_LENGTH) + "* (" + post.getScore() + 
								")\n" + post.getUrl(), null);
					}
				}
			}
		}
		pastPosts = newPosts;
	}

}
