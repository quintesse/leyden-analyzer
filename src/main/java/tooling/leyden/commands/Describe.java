package tooling.leyden.commands;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import tooling.leyden.aotcache.Element;

import java.util.List;

@Command(name = "describe", mixinStandardHelpOptions = true,
		version = "1.0",
		description = {"Describe an object, showing all related info."},
		subcommands = {CommandLine.HelpCommand.class})
class Describe implements Runnable {

	@CommandLine.ParentCommand
	DefaultCommand parent;

	@CommandLine.Parameters(description = {"The object name/identifier. If it is a class, use the full qualified name" +
			"."})
	private String name;

	@CommandLine.Option(names = {"--type", "-t"}, description = "Restrict the listing to this " +
			"type of element", defaultValue = "", completionCandidates = Types.class)
	private String type;

	public void run() {
		List<Element> elements;
		if (type == null || type.isBlank()) {
			elements = parent.getAotCache().getObjects(name);
		} else {
			elements = parent.getAotCache().getObjects(name, type);
		}
		if (!elements.isEmpty()) {
			elements.forEach(e -> parent.getOut().println(e.getDescription()));
		} else {
			parent.getOut().println("ERROR: Element not found. Try looking for it with ls.");
		}
	}
}