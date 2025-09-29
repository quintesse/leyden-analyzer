package tooling.leyden.commands;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import tooling.leyden.aotcache.ClassObject;
import tooling.leyden.aotcache.Configuration;
import tooling.leyden.aotcache.Element;
import tooling.leyden.commands.autocomplete.InfoCommandTypes;
import tooling.leyden.commands.autocomplete.Packages;
import tooling.leyden.commands.autocomplete.Types;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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
		Stream<Element> elements = parent.getAotCache().getElements(parameters.name, parameters.packageName,
				parameters.excludePackageName, parameters.showArrays, parameters.types).stream();
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