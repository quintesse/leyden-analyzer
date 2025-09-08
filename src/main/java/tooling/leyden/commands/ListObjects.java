package tooling.leyden.commands;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import tooling.leyden.aotcache.Element;
import tooling.leyden.aotcache.Error;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

@Command(name = "ls", mixinStandardHelpOptions = true,
		version = "1.0",
		description = { "List what is on the cache. By default, it lists everything on the cache." },
		subcommands = { CommandLine.HelpCommand.class })
class ListObjects implements Runnable {

	@CommandLine.ParentCommand
	DefaultCommand parent;

	public void run() {
		classes("");
	}

	@Command(mixinStandardHelpOptions = true, subcommands = {
			CommandLine.HelpCommand.class }, description = "Lists everything on the cache.")
	public void all() {
		listElements(null, null);
	}
	@Command(mixinStandardHelpOptions = true, subcommands = {
			CommandLine.HelpCommand.class }, description = "Lists classes on the cache.")
	public void classes(@CommandLine.Option(names = "--packageName", description = "Restrict the listing to this " +
			"package", defaultValue = "") String packageName) {
		listElements(packageName, "Class");
	}

	@Command(mixinStandardHelpOptions = true, subcommands = {
			CommandLine.HelpCommand.class }, description = "Lists methods on the cache.")
	public void methods(@CommandLine.Option(names = "--packageName", description = "Restrict the listing to this " +
			"package", defaultValue = "") String packageName) {
		listElements(packageName, "Method");
	}


	@Command(mixinStandardHelpOptions = true, subcommands = {
			CommandLine.HelpCommand.class }, description = "Lists errors on creating or loading the AOTCache.")
	public void errors(@CommandLine.Option(names = "--packageName", description = "Restrict the listing to this " +
			"package", defaultValue = "") String packageName) {
		Stream<Error> elements;
		elements = parent.aotCache.getErrors().stream();
		if (packageName != null && !packageName.isBlank()) {
			elements = elements.filter( error -> error.getIdentifier().startsWith(packageName));
		}

		final var counter = new AtomicInteger();
		elements = elements.peek(item -> counter.incrementAndGet());

		elements.forEach(element -> parent.out.println("  > " + element.toString()));
		parent.out.println("Found " + counter.get() + " errors.");
	}

	private void listElements(String packageName, String type) {
		Stream<Element> elements;
		if (packageName == null || packageName.isBlank()) {
			elements = parent.aotCache.getAll().values().stream();
		} else {
			elements = parent.aotCache.getByPackage(packageName).stream();
		}
		if (type != null && !type.isBlank()) {
			elements = elements.filter(element -> element.getType().equals(type));
		}
		final var counter = new AtomicInteger();
		elements = elements.peek(item -> counter.incrementAndGet());

		elements.forEach(element -> parent.out.println("  > " + element.toString()));
		parent.out.println("Found " + counter.get() + " elements.");
	}
}