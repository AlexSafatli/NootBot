package net.dirtydeeds.discordsoundboard.chat;

import java.io.File;
import java.util.List;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.entities.Message.Attachment;
import net.dv8tion.jda.events.message.guild.GuildMessageReceivedEvent;

public class SoundAttachmentProcessor implements ChatCommandProcessor {

	protected SoundboardBot soundPlayer;
	private static final int MAX_FILE_SIZE_IN_BYTES = 1000000; // 1 MB
	
	public SoundAttachmentProcessor(SoundboardBot soundPlayer) {
		this.soundPlayer = soundPlayer;
	}
	
	public void process(GuildMessageReceivedEvent event) {
		if (isApplicableCommand(event)) {
			List<Attachment> attachments = event.getMessage().getAttachments();
			for (Attachment attachment : attachments) {
				String name = attachment.getFileName();
				String extension = name.substring(name.indexOf(".") + 1);
				if (extension.equals("wav") || extension.equals("mp3")) {
					if (attachment.getSize() < MAX_FILE_SIZE_IN_BYTES) {
						attachment.download(new File(soundPlayer.getSoundsPath().toString(), name));
						event.getChannel().sendMessage("Downloaded file `" + name + "` and added to list of sounds " + event.getAuthor().getAsMention() + ".");
						event.getMessage().deleteMessage();
					} else {
						event.getChannel().sendMessage("File `" + name + "` is too large to add to library.");
					}
				}
			}
		}
	}

	public boolean isApplicableCommand(GuildMessageReceivedEvent event) {
		String author = event.getAuthor().getUsername();
		return (!event.getMessage().getAttachments().isEmpty() && 
				author.equals(soundPlayer.getOwner()));
	}

}
