package net.dirtydeeds.discordsoundboard.chat;

import java.io.Reader;
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
      Reader r = new InputStreamReader(p.getInputStream());
      BufferedReader stdin = new BufferedReader(r);
      while ((s = stdin.readLine()) != null) output += s + "\n";
      p.waitFor();
      p.destroy();
      stdin.close();
    } catch (Exception e) {
      return e.toString();
    }
    return output;
  }

  protected void handleEvent(MessageReceivedEvent event, String message) {
    String cmd = getArgument();
    if (cmd != null) {
      String out = run(cmd);
      if (!out.isEmpty()) m(event, "```" + run(cmd) + "```");
      else pm(event, "Ran command and received no output.");
    }
  }

  @Override
  public String getCommandHelpString() {
    return getPrefix() + " <cmd> (*) - run a command on the system";
  }
}