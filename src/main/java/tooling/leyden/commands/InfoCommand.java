package tooling.leyden.commands;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import tooling.leyden.aotcache.ClassObject;
import tooling.leyden.aotcache.Configuration;
import tooling.leyden.aotcache.Element;
import tooling.leyden.commands.autocomplete.InfoCommandTypes;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

@Command(name = "info", mixinStandardHelpOptions = true,
		version = "1.0",
		description = {"Show information and statistics on the AOT Cache based on the information we have loaded."},
		subcommands = {CommandLine.HelpCommand.class})
class InfoCommand implements Runnable {

	@CommandLine.ParentCommand
	DefaultCommand parent;

	@CommandLine.Option(names = {"--type", "-t"},
			arity = "0..*",
			description = "What type of statistics we want to show. By default, configuration.",
			defaultValue = "configuration",
			completionCandidates = InfoCommandTypes.class)
	private String[] whatToShow;

	public void run() {
		if (shouldShow("configuration"))
			print("Configuration", parent.getAotCache().getConfiguration());
		if (shouldShow("statistics"))
			print("Statistics", parent.getAotCache().getStatistics());
		if (shouldShow("allocation"))
			print("Allocation", parent.getAotCache().getAllocation());
		if (shouldShow("count"))
			countElements();
	}

	private boolean shouldShow(String s) {
		return this.whatToShow.length == 0
				|| Arrays.stream(this.whatToShow).anyMatch(wts -> wts.equals(s));
	}

	private void print(String title, Configuration configuration) {
		parent.getOut().println('\n' + title + ": ");
		parent.getOut().println("  _________");
		configuration.getKeys().stream().sorted()
				.forEachOrdered(key ->
						parent.getOut().println("  | " + key + " -> " +
								configuration.getValue(key)));
		parent.getOut().println("  |________");
	}

	private void countElements() {
		Stream<Element> elements;
		elements = parent.getAotCache().getAll().parallelStream();

		final var counts = new HashMap<String, AtomicInteger>();

		Map<String, Integer> packages = new ConcurrentHashMap<>();

		elements.forEach(item -> {
			counts.putIfAbsent(item.getType(), new AtomicInteger());
			var count = counts.get(item.getType());
			count.incrementAndGet();
			if (item instanceof ClassObject classObject) {
				var pn = classObject.getPackageName();
				if (!packages.containsKey(pn)) {
					packages.put(pn, 0);
				}
				packages.computeIfPresent(pn, (key, value) -> value + 1);
			}
		});

		counts.forEach((type, count) ->
				parent.getOut().println("There are " + count.get() + " elements of type " + type + "."));

		parent.getOut().println("These are the top packages with more classes:");
		packages.entrySet().stream()
				.sorted((o1, o2) -> o2.getValue() - o1.getValue())
				.limit(20)
				.forEachOrdered(entry ->
						parent.getOut().println(" - " + entry.getKey() + "(" + entry.getValue() + ")"));
	}
}