package tooling.leyden.commands;

import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.util.concurrent.atomic.AtomicInteger;

@Command(name = "ls", mixinStandardHelpOptions = true,
		version = "1.0",
		description = {"List what is on the cache. By default, it lists everything on the cache."},
		subcommands = {CommandLine.HelpCommand.class})
class ListCommand implements Runnable {

	@CommandLine.ParentCommand
	DefaultCommand parent;

	@CommandLine.Mixin
	private CommonParameters parameters;

	public void run() {
		var elements =
				parent.getAotCache().getElements(parameters.getName(), parameters.packageName,
						parameters.excludePackageName, parameters.showArrays,
						parameters.types).stream();

		final var counter = new AtomicInteger();
		elements = elements.peek(item -> counter.incrementAndGet());

		elements.forEach(element -> parent.getOut().println("  > " + element.toString()));
		parent.getOut().println("Found " + counter.get() + " elements.");
	}
}