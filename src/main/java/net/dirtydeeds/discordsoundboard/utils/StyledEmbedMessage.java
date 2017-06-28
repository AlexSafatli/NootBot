package net.dirtydeeds.discordsoundboard.utils;

import java.awt.Color;
import java.util.Date;
import java.util.Random;

import net.dirtydeeds.discordsoundboard.Version;
import net.dirtydeeds.discordsoundboard.beans.SoundFile;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Message;

public class StyledEmbedMessage {

	private EmbedBuilder builder;
	private static final Color NOOT_BOT_EMBED_COLOR = Color.getHSBColor(252.7f, 0.565f, 0.622f);
	private static final Color NOOT_BOT_ERROR_COLOR = Color.getHSBColor(360.0f, 1.000f, 0.500f);
	private static final Color NOOT_BOT_WARN_COLOR  = Color.getHSBColor(44.00f, 1.000f, 0.500f);
	
	public StyledEmbedMessage() {
		builder = new EmbedBuilder();
		builder.setColor(NOOT_BOT_EMBED_COLOR);
	}
	
	public StyledEmbedMessage(String title) {
		this();
		builder.setTitle(title);
		builder.setFooter(Version.NAME + " " + Version.VERSION + " \u2014 " + Version.AUTHOR, null);
	}
	
	public StyledEmbedMessage(String title, SoundboardBot bot) {
		this(title);
		builder.setFooter(Version.NAME + " " + Version.VERSION + " \u2014 " + Version.getAuthor(bot), null);
	}
	
	public void addDescription(String desc) {
		builder.setDescription(desc);
	}
	
	public void setImage(String url) {
		builder.setImage(url);
	}
	
	public void setThumbnail(String url) {
		builder.setThumbnail(url);
	}

	public StyledEmbedMessage isWarning(boolean warning) {
		if (warning) builder.setColor(NOOT_BOT_WARN_COLOR);
		return this;
	} 

	public StyledEmbedMessage isError(boolean error) {
		if (error) builder.setColor(NOOT_BOT_ERROR_COLOR);
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
	
	public static StyledEmbedMessage forSoundFile(SoundFile file, String title, String description) {
		StyledEmbedMessage msg = new StyledEmbedMessage(title);
		msg.addDescription(description);
		if (!file.getCategory().equals("sounds")) msg.addContent("Category", file.getCategory(), true);
		msg.addContent("Name", "`" + file.getSoundFileId() + "`", true);
		msg.addContent("Duration", file.getDuration() + " seconds", true);
		msg.addContent("Played", file.getNumberOfPlays() + " times", true);
		if (file.getSoundFile() != null) {
			Date modified = new Date(file.getSoundFile().lastModified());
			String stamp = StringUtils.dayTimeStamp(modified);
			if (!stamp.isEmpty()) msg.addContent("Added", stamp, true);
		}
		return msg;
	}
	
}

