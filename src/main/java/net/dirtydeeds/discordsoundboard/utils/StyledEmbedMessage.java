package net.dirtydeeds.discordsoundboard.utils;

import java.awt.Color;
import java.util.Date;
import java.util.Random;
import java.util.List;
import java.util.Arrays;

import net.dirtydeeds.discordsoundboard.Icons;
import net.dirtydeeds.discordsoundboard.Version;
import net.dirtydeeds.discordsoundboard.beans.SoundFile;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.Icon;

public class StyledEmbedMessage {

	public static final String FOR_USER_FOOTER_PREFIX = " ";

	private EmbedBuilder builder;
	private String footer;
	private String footerIconUrl;
	private String errorTitle;

	private static final List<String> ERROR_STRINGS = Arrays.asList(new String[] {
	      "Fwubbed it.", "Excuse me, I stuttered.", "Nani?", "Derp"
	    });

	private static final String FOOTER_TEXT = Version.NAME + " " +
	    Version.VERSION + " by " + Version.AUTHOR;
	private static final String DEFAULT_TOP = " ";
	private static final Color  EMBED_COLOR = new Color(87, 70, 158);
	private static final Color  ERROR_COLOR = new Color(179, 0,   0);
	private static final Color  WARN_COLOR  = new Color(255, 217, 0);

	public StyledEmbedMessage() {
		this.footerIconUrl = Icons.ELLIPSIS;
		this.errorTitle = StringUtils.randomString(ERROR_STRINGS);
		builder = new EmbedBuilder();
		builder.setColor(EMBED_COLOR);
	}

	public StyledEmbedMessage(String title) {
		this();
		this.footer = FOOTER_TEXT;
		builder.setTitle(title);
		builder.setAuthor(DEFAULT_TOP, null, null);
		updateFooter();
	}

	public StyledEmbedMessage(String title, SoundboardBot bot) {
		this(title);
		String numSounds = bot.getSoundMap().size() + " sounds";
		this.footer = FOOTER_TEXT + Strings.SEPARATOR + numSounds;
		updateFooter();
	}

	private void updateFooter() {
		builder.setFooter(footer, footerIconUrl);
	}

	public void setTitle(String title) {
		builder.setTitle(title);
	}

	public void addDescription(String desc) {
		builder.setDescription(desc);
	}

	public void setColor(Color color) {
		builder.setColor(color);
	}

	public void setImage(String url) {
		builder.setImage(url);
	}

	public void setThumbnail(String url) {
		builder.setThumbnail(url);
	}

	public void setFooter(String footer) {
		this.footer = footer;
		updateFooter();
	}

	public void setFooterIcon(String url) {
		footerIconUrl = url;
		updateFooter();
	}

	public void addFooterText(String text) {
		footer += Strings.SEPARATOR + text;
		updateFooter();
	}

	public StyledEmbedMessage isWarning(boolean warning) {
		if (warning) {
			builder.setColor(WARN_COLOR);
			builder.setAuthor(DEFAULT_TOP, null, null);
		}
		return this;
	}

	public StyledEmbedMessage isError(boolean error) {
		if (error) {
			builder.setColor(ERROR_COLOR);
			builder.setAuthor(errorTitle, null, Icons.TIMES);
		}
		return this;
	}

	public void addContent(String name, String value, boolean inline) {
		builder.addField(name, value, inline);
	}

	public Message getMessage() {
		MessageBuilder mb = new MessageBuilder();
		mb.setEmbed(builder.build());
		return (Message) mb.build();
	}

	public static StyledEmbedMessage forSoundFile(SoundboardBot bot,
	    SoundFile file, String title, String description) {
		StyledEmbedMessage msg = new StyledEmbedMessage(title, bot);
		msg.addDescription(description);
		msg.addContent("Category", (!file.getCategory().equals("sounds")) ?
		               file.getCategory() : "\u2014", true);
		msg.addContent("Name", "`" + file.getSoundFileId() + "`", true);
		msg.addContent("Duration", file.getDuration() + " seconds", true);
		msg.addContent("Played", file.getNumberOfPlays() + " times", true);
		if (file.getNumberOfReports() > 0) {
			msg.addContent("Reported", file.getNumberOfReports() + " times", true);
		}
		if (file.getSoundFile() != null) {
			String stamp = StringUtils.dayTimeStamp(file.getLastModified());
			if (!stamp.isEmpty()) msg.addContent("Added", stamp, false);
		}
		List<net.dirtydeeds.discordsoundboard.beans.User> usersWithEntrance =
		  bot.getDispatcher().getUsersWithEntrance(file.getSoundFileId());
		if (!usersWithEntrance.isEmpty()) {
			msg.addContent("Entrance For",
			               StringUtils.listToString(usersWithEntrance), false);
		}
		return msg;
	}

	public static StyledEmbedMessage forUser(SoundboardBot bot, User user,
	    String title, String description) {
		StyledEmbedMessage msg = new StyledEmbedMessage(title, bot);
		msg.addDescription(description);
		//msg.addFooterText(FOR_USER_FOOTER_PREFIX + user.getName());
		msg.setFooterIcon(user.getEffectiveAvatarUrl());
		return msg;
	}

}

