package tooling.leyden.commands;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import tooling.leyden.aotcache.Element;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

@Command(name = "count", mixinStandardHelpOptions = true,
		version = "1.0",
		description = {"Count what elements are on the AOT Cache based on the information we have loaded."},
		subcommands = {CommandLine.HelpCommand.class})
class CountCommand implements Runnable {

	@CommandLine.ParentCommand
	DefaultCommand parent;

	@CommandLine.Mixin
	private CommonParameters parameters;

	public void run() {
		Stream<Element> elements = parent.getInformation().getElements(parameters.getName(), parameters.packageName,
				parameters.excludePackageName, parameters.showArrays, parameters.useNotCached, parameters.types);
		final var counts = new HashMap<String, AtomicInteger>();

		elements.forEach(item -> {
			counts.putIfAbsent(item.getType(), new AtomicInteger());
			counts.get(counts.get(item.getType()).incrementAndGet());
		});

		counts.entrySet().
				stream().
				sorted(Map.Entry.comparingByKey())
				.forEach(entry ->
						parent.getOut().
								println("There are " + entry.getValue().get()
										+ " elements of type " + entry.getKey() + "."));
	}
}