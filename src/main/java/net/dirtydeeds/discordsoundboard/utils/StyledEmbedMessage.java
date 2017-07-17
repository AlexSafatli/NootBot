package net.dirtydeeds.discordsoundboard.utils;

import java.awt.Color;
import java.util.Date;
import java.util.Random;

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

	private EmbedBuilder builder;
	private String footer;
	private String footerIconUrl;

	private static final String NOOT_BOT_FOOTER_TEXT = Version.NAME + " " + Version.VERSION;
	private static final String NOOT_BOT_DEFAULT_TOP = " ";
	private static final String NOOT_BOT_DEFAULT_ERR = "I fwubbed it.";
	private static final Color  NOOT_BOT_EMBED_COLOR = new Color(87, 70, 158);
	private static final Color  NOOT_BOT_ERROR_COLOR = new Color(179, 0,   0);
	private static final Color  NOOT_BOT_WARN_COLOR  = new Color(255, 217, 0);
	
	public StyledEmbedMessage() {
		this.footerIconUrl = Icons.ELLIPSIS;
		builder = new EmbedBuilder();
		builder.setColor(NOOT_BOT_EMBED_COLOR);
	}
	
	public StyledEmbedMessage(String title) {
		this();
		this.footer = NOOT_BOT_FOOTER_TEXT + " \u2014 " + Version.AUTHOR;
		builder.setTitle(title);
		builder.setAuthor(NOOT_BOT_DEFAULT_TOP, null, null);
		updateFooter();
	}
	
	public StyledEmbedMessage(String title, SoundboardBot bot) {
		this(title);
		String numSounds = bot.getSoundMap().size() + " sounds";
		this.footer = NOOT_BOT_FOOTER_TEXT + " \u2014 " + Version.AUTHOR + 
			" \u2014 " + numSounds;
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

	public void setFooterIcon(String url) {
		footerIconUrl = url;
		updateFooter();
	}

	public void addFooterText(String text) {
		footer += " \u2014 " + text;
		updateFooter();
	}

	public StyledEmbedMessage isWarning(boolean warning) {
		if (warning) {
			builder.setColor(NOOT_BOT_WARN_COLOR);
			builder.setAuthor(NOOT_BOT_DEFAULT_ERR, null, Icons.WARNING);
		}
		return this;
	} 

	public StyledEmbedMessage isError(boolean error) {
		if (error) {
			builder.setColor(NOOT_BOT_ERROR_COLOR);
			builder.setAuthor(NOOT_BOT_DEFAULT_ERR, null, Icons.TIMES);
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
	
	public static StyledEmbedMessage forSoundFile(SoundboardBot bot, SoundFile file, String title, String description) {
		StyledEmbedMessage msg = new StyledEmbedMessage(title, bot);
		msg.addDescription(description);
		if (!file.getCategory().equals("sounds")) msg.addContent("Category", file.getCategory(), true);
		msg.addContent("Name", "`" + file.getSoundFileId() + "`", true);
		msg.addContent("Duration", file.getDuration() + " seconds", true);
		msg.addContent("Played", file.getNumberOfPlays() + " times", true);
		if (file.getNumberOfReports() > 0) {
			msg.addContent("Reports", file.getNumberOfReports() + " times", true);
		}
		if (file.getSoundFile() != null) {
			String stamp = StringUtils.dayTimeStamp(file.getLastModified());
			if (!stamp.isEmpty()) msg.addContent("Added", stamp, true);
		}
		return msg;
	}

	public static StyledEmbedMessage forUser(SoundboardBot bot, User user, String title, String description) {
		StyledEmbedMessage msg = new StyledEmbedMessage(title, bot);
		msg.addDescription(description);
		msg.addFooterText("Requested by " + user.getName());
		msg.setFooterIcon(user.getEffectiveAvatarUrl());
	}
	
}

