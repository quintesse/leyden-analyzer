package tooling.leyden.commands;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import tooling.leyden.aotcache.Element;

import java.util.stream.Stream;

@Command(name = "describe", mixinStandardHelpOptions = true,
		version = "1.0",
		description = { "Describe an object, showing all related info." },
		subcommands = { CommandLine.HelpCommand.class })
class DescribeObject implements Runnable {

	@CommandLine.ParentCommand
	DefaultCommand parent;

	@CommandLine.Parameters(description = {"The object name/identifier.", "If it is a class, use the full qualified " +
			"name."}, index="0")
	private String name;

	public void run() {
		Element e = parent.aotCache.getObject(name);
		if (e != null) {
			parent.out.println(e.getDescription());
		} else {
			parent.out.println("ERROR: Element not found. Try looking for it with ls.");
		}
	}
}