package tooling.leyden.commands;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import tooling.leyden.aotcache.Element;
import tooling.leyden.commands.autocomplete.Packages;
import tooling.leyden.commands.autocomplete.Types;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

@Command(name = "ls", mixinStandardHelpOptions = true,
		version = "1.0",
		description = {"List what is on the cache. By default, it lists everything on the cache."},
		subcommands = {CommandLine.HelpCommand.class})
class ListCommand implements Runnable {

	@CommandLine.ParentCommand
	DefaultCommand parent;

	@CommandLine.Option(names = {"-pn", "--packageName"},
			description = "Restrict the listing to this package.",
			defaultValue = "",
			arity = "1..1",
			paramLabel = "<packageName>",
			completionCandidates = Packages.class)
	private String packageName;

	@CommandLine.Option(
			names = {"-t", "--type"},
			arity = "0..*",
			paramLabel = "<type>",
			description = "Restrict the listing to this type of element",
			completionCandidates = Types.class)
	private String[] types;

	public void run() {
		var elements = parent.getAotCache().getByPackage(packageName, types).stream();

		final var counter = new AtomicInteger();
		elements = elements.peek(item -> counter.incrementAndGet());

		elements.forEach(element -> parent.getOut().println("  > " + element.toString()));
		parent.getOut().println("Found " + counter.get() + " elements.");
	}
}