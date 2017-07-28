package net.dirtydeeds.discordsoundboard.async;

import java.util.function.Consumer;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.service.SoundboardDispatcher;

public class LambdaJob implements SoundboardJob {

	private Consumer<SoundboardBot> lambda;

	public LambdaJob(Consumer<SoundboardBot> lambda) {
		this.lambda = lambda;
	}

	public boolean isApplicable(SoundboardDispatcher dispatcher) {
		return true;
	}

	public void run(SoundboardDispatcher dispatcher) {
		for (SoundboardBot bot : dispatcher.getBots()) lambda.accept(bot);
	}
}