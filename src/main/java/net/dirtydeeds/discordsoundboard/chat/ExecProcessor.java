package net.dirtydeeds.discordsoundboard.chat;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class ExecProcessor extends OwnerSingleArgumentChatCommandProcessor {

  public ExecProcessor(String prefix, SoundboardBot bot) {
    super(prefix, "Execute Command", bot);
  }

  private String run(String cmd) {
    String s, output = "";
    Process p;
    try {
      p = Runtime.getRuntime().exec(cmd);
      BufferedReader br = new BufferedReader(
        new InputStreamReader(p.getInputStream()));
      while ((s = br.readLine()) != null) output += s + "\n";
      p.waitFor();
      p.destroy();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return output;
  }

  protected void handleEvent(MessageReceivedEvent event, String message) {
    String cmd = getArgument();
    if (cmd != null) {
      m(event, "```" + run(cmd) + "```");
    }
  }

  @Override
  public String getCommandHelpString() {
    return super.getCommandHelpString() + " \u2014 run a command on the system";
  }

}
