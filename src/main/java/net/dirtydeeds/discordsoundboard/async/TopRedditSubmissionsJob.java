package net.dirtydeeds.discordsoundboard.async;

import java.util.LinkedList;
import java.util.List;

import net.dean.jraw.models.Submission;
import net.dean.jraw.models.Subreddit;
import net.dirtydeeds.discordsoundboard.reddit.Reddit;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.service.SoundboardDispatcher;
import net.dv8tion.jda.entities.Game;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.utils.SimpleLog;

public class TopRedditSubmissionsJob extends AbstractAsyncJob {

	private static final String SUBREDDIT = "games";
	public static final SimpleLog LOG = SimpleLog.getLog("TopRedditJob");
	private final String msgIntro;
	private List<String> pastPosts;
	
	public TopRedditSubmissionsJob() {
		msgIntro = String.format("I found this on `/r/%s` while browsing **reddit**.\n", SUBREDDIT);
		pastPosts = new LinkedList<>();
	}
	
	public void handle(SoundboardDispatcher dispatcher) {
		Reddit r = new Reddit();
		List<String> newPosts = new LinkedList<>();
		LOG.info("Getting posts from subreddit " + SUBREDDIT);
		List<Submission> gamePosts = r.getSubredditTop(SUBREDDIT);
		LOG.info("Found " + gamePosts.size() + " posts.");
		for (SoundboardBot bot : dispatcher.getBots()) {
			guildloop: for (Guild guild : bot.getGuilds()) {
				// See if relevant to anyone online.
				for (User user : guild.getUsers()) {
					Game g = user.getCurrentGame();
					if (g != null) {
						for (Submission post : gamePosts) {
							Subreddit sr = r.getSubreddit(post.getSubredditName());
							if (sr == null) {
								LOG.warn("Could not resolve subreddit " + post.getSubredditName());
								continue;
							}
							if (post.getTitle().contains(g.getName()) || sr.getPublicDescription().contains(g.getName())) {
								if (!pastPosts.contains(post.getUrl())) {
									LOG.info("Post " + post.getId() + " seems relevant to " + user.getUsername());
									guild.getPublicChannel().sendMessageAsync(msgIntro + post.getUrl(), null);
									continue guildloop;
								}
								if (!newPosts.contains(post.getUrl())) newPosts.add(post.getUrl());
							}
						}
					}
				}
			}
		}
		pastPosts = newPosts;
	}

}
