package net.dirtydeeds.discordsoundboard.async;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.dean.jraw.models.Submission;
import net.dirtydeeds.discordsoundboard.reddit.Reddit;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.service.SoundboardDispatcher;
import net.dirtydeeds.discordsoundboard.utils.StringUtils;
import net.dv8tion.jda.entities.Game;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.utils.SimpleLog;

public class RelatedRedditSubmissionsJob extends AbstractAsyncJob {

	public static final SimpleLog LOG = SimpleLog.getLog("RelatedRedditJob");
	private static final long HIGH_UPVOTES = 4750;
	private static final int TITLE_TRUNCATE_LENGTH = 128;
	private List<String> pastPosts;
	
	public RelatedRedditSubmissionsJob() {
		NUMBER_HOURS_BETWEEN = 1; // Check every hour.
		pastPosts = new LinkedList<>();
	}
	
	public void handle(SoundboardDispatcher dispatcher) {
		Reddit r = new Reddit();
		List<String> newPosts = new LinkedList<>();
		for (SoundboardBot bot : dispatcher.getBots()) {
			for (Guild guild : bot.getGuilds()) {
				// See people playing games and check to see if their game has any 
				// highly rated posts for a subreddit.
				Map<String, List<User>> games = new HashMap<>();
				Map<String, Submission> posts = new HashMap<>();
				userloop: for (User user : guild.getUsers()) {
					Game g = user.getCurrentGame();
					String game = (g != null) ? g.getName() : null;
					if (g != null && guild.getVoiceStatusOfUser(user).inVoiceChannel()) {
						LOG.info("User " + user.getUsername() + " is in voice channel and playing " + user.getCurrentGame());
						List<String> possibleSubreddits = r.search(game.replace(" ","").replaceAll("\\d","").trim().toLowerCase());
						if (!possibleSubreddits.isEmpty()) {
							String subreddit = possibleSubreddits.get(0);
							List<Submission> gamePosts = r.getSubredditTop(subreddit);
							LOG.info("Looking through posts in subreddit /r/" + subreddit);
							for (Submission post : gamePosts) {
								if (post.getScore() > HIGH_UPVOTES) {
									if (!newPosts.contains(post.getFullName())) {
										newPosts.add(post.getFullName());
									}
									if (!pastPosts.contains(post.getFullName())) {
										if (games.get(game) == null) {
											games.put(game, new LinkedList<User>());
											posts.put(game, post);
										}
										games.get(game).add(user);
										LOG.info("Post with score " + post.getScore() + " relevant to " + 
												user.getUsername());
										continue userloop;
									}
								}
							}
						}
					}
				}
				for (String g : games.keySet()) {
					// Go through all of the games and group the users together.
					Submission post = posts.get(g);
					String msgToSend = "I found this on `/r/" + post.getSubredditName() + "`\n*" + 
							StringUtils.truncate(post.getTitle(), TITLE_TRUNCATE_LENGTH) + 
							"* (" + post.getScore() + ") ";
					for (User user : games.get(g)) {
						msgToSend += user.getAsMention() + " ";
					}
					msgToSend += "\n" + post.getUrl();
					guild.getPublicChannel().sendMessageAsync(msgToSend, null);
				}
			}
		}
		pastPosts = newPosts;
	}

}
