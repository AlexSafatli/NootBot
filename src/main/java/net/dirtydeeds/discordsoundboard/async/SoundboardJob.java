package net.dirtydeeds.discordsoundboard.async;

import net.dirtydeeds.discordsoundboard.service.SoundboardDispatcher;

public interface SoundboardJob {

  boolean isApplicable(SoundboardDispatcher dispatcher);
  void run(SoundboardDispatcher dispatcher);

}