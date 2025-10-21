package tooling.leyden.commands;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import tooling.leyden.aotcache.ClassObject;
import tooling.leyden.aotcache.Element;
import tooling.leyden.aotcache.MethodObject;

import java.util.Arrays;
import java.util.Comparator;
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

	@CommandLine.Option(names = {"--trained"},
			description = {"Only displays elements with training information.",
					"This may restrict the types of elements shown, along with what was passed as parameters."},
			defaultValue = "false",
			arity = "0..1")
	protected Boolean trained;

	public void run() {
		var elements =
				parent.getInformation().getElements(parameters.getName(), parameters.packageName,
						parameters.excludePackageName, parameters.showArrays, parameters.useNotCached,
						parameters.types);

		if (trained) {
			elements = elements.filter(e -> e.isTraineable() && e.isTrained());
		}

		final var counter = new AtomicInteger();
		elements = elements.sorted(Comparator.comparing(Element::getKey).thenComparing(Element::getType));
		elements = elements.peek(item -> counter.incrementAndGet());

		elements.forEach(element -> element.toAttributedString().println(parent.getTerminal()));
		parent.getOut().println("Found " + counter.get() + " elements.");
	}
}