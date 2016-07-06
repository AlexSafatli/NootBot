package net.dirtydeeds.discordsoundboard.reddit;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import net.dean.jraw.RedditClient;
import net.dean.jraw.http.NetworkException;
import net.dean.jraw.http.UserAgent;
import net.dean.jraw.http.oauth.Credentials;
import net.dean.jraw.http.oauth.OAuthData;
import net.dean.jraw.http.oauth.OAuthException;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.Subreddit;
import net.dean.jraw.paginators.SubredditPaginator;
import net.dirtydeeds.discordsoundboard.Version;

public class Reddit {

	private RedditClient reddit;
	private UserAgent useragent;
	private Credentials creds;
	private Properties appProperties;
	
	public Reddit() {
		useragent = UserAgent.of(
				"desktop", "net.dirtydeeds.discordsoundboard",
				"v" + Version.VERSION, "Asaph");
		reddit = new RedditClient(useragent);
		loadProperties();
		String username = appProperties.getProperty("reddit_username");
		String password = appProperties.getProperty("reddit_password");
		String clientId = appProperties.getProperty("reddit_clientid");
		String clientSecret = appProperties.getProperty("reddit_clientsecret");
		creds = Credentials.script(username, password, clientId, clientSecret);
		try {
			OAuthData auth = reddit.getOAuthHelper().easyAuth(creds);
			reddit.authenticate(auth);
		} catch (NetworkException | OAuthException e) {
			e.printStackTrace();
		}
	}
	
    private void loadProperties() {
        appProperties = new Properties();
        InputStream stream = null;
        try {
            stream = new FileInputStream(System.getProperty("user.dir") + "/app.properties");
            appProperties.load(stream);
            stream.close();
            return;
        } catch (FileNotFoundException e) {
        	e.printStackTrace();
        } catch (IOException e) { e.printStackTrace(); }
        if (stream == null) {
            try {
                stream = this.getClass().getResourceAsStream("/app.properties");
                appProperties.load(stream);
                stream.close();
            } catch (IOException e) { e.printStackTrace(); }
        }
    }
    
	public boolean isSubreddit(String subreddit) {
		try {
			reddit.getSubreddit(subreddit);
			return true;
		} catch (IllegalArgumentException e) {
			return false;
		}
	}
	
	public List<String> search(String query) {
		try {
			return reddit.searchSubreddits(query, true);
		} catch (NetworkException e) {
			return new LinkedList<>();
		}
	}
	
	public Subreddit getSubreddit(String subreddit) {
		try {
			return reddit.getSubreddit(subreddit);
		} catch (NetworkException | IllegalArgumentException e) {
			return null;
		}
	}
	
	public List<Submission> getSubredditTop(String subreddit) {
		LinkedList<Submission> submissions = new LinkedList<>();
		try {
			SubredditPaginator sr = new SubredditPaginator(reddit, subreddit);
			for (Submission s : sr.next()) {
				submissions.add(s);
			}
		} catch (Exception e) {
			;
		}
		return submissions;
	}
	
}
