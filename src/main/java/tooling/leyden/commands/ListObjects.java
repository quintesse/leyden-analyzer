package tooling.leyden.commands;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import tooling.leyden.aotcache.Element;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

@Command(name = "ls", mixinStandardHelpOptions = true,
		version = "1.0",
		description = { "List what is on the cache. By default, it lists everything on the cache." },
		subcommands = { CommandLine.HelpCommand.class })
class ListObjects implements Runnable {

	@CommandLine.ParentCommand
	DefaultCommand parent;

	@CommandLine.Option(names = {"-pn", "--packageName"},
			description = "Restrict the listing to this package.",
			defaultValue = "")
	private String packageName;

	@CommandLine.Parameters(arity = "0..*",
			paramLabel = "<type>",
			description = "Restrict the listing to this type of element",
			completionCandidates = Types.class)
	private String[] types;

	@Command(mixinStandardHelpOptions = true,
			subcommands = { CommandLine.HelpCommand.class },
			description = "Lists everything on the cache.")
	public void run() {
		Stream<Element> elements;
		elements = parent.getAotCache().getByPackage(packageName, types).stream();

		final var counter = new AtomicInteger();
		elements = elements.peek(item -> counter.incrementAndGet());

		elements.forEach(element -> parent.getOut().println("  > " + element.toString()));
		parent.getOut().println("Found " + counter.get() + " elements.");

		if (types != null && Arrays.stream(types).anyMatch(t -> t.equalsIgnoreCase("error"))) {
			var errors = parent.getAotCache().getErrors();
			errors.forEach(element -> parent.getOut().println("  > " + element.toString()));
			parent.getOut().println("Found " + errors.size() + " errors.");
		}
	}
}