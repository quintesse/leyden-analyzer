package tooling.leyden.commands;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import tooling.leyden.aotcache.ClassObject;
import tooling.leyden.aotcache.MethodObject;

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
					"This may restrict the types of elements shown, regardless of what was passed as parameters."},
			defaultValue = "false",
			arity = "0..1")
	protected Boolean trained;

	public void run() {

		if (trained) {
			parameters.types = new String[] {"Class", "Method"};
		}

		var elements =
				parent.getInformation().getElements(parameters.getName(), parameters.packageName,
						parameters.excludePackageName, parameters.showArrays, parameters.useNotCached,
						parameters.types).stream();

		if (trained) {
			elements = elements.filter(e -> {
				if (e instanceof MethodObject method) {
					return method.getMethodCounters() != null;
				} else if (e instanceof ClassObject classObject) {
					return classObject.getKlassTrainingData() != null;
				}
				return false;
			});
		}

		final var counter = new AtomicInteger();
		elements = elements.peek(item -> counter.incrementAndGet());

		elements.forEach(element -> parent.getOut().println("  > " + element.toString()));
		parent.getOut().println("Found " + counter.get() + " elements.");
	}
}