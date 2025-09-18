package tooling.leyden.commands;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import tooling.leyden.aotcache.Element;
import tooling.leyden.aotcache.ReferencingElement;
import tooling.leyden.commands.autocomplete.Types;

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
			elements.forEach(e -> {
				var leftPadding = "|  ";
				parent.getOut().println("-----");
				parent.getOut().println(e.getDescription(leftPadding));
				var referring = getElementsReferencingThisOne(e);
				if (!referring.isEmpty()) {
					parent.getOut().println(leftPadding + "There are other elements of the cache that link " +
							"to this element: ");
					parent.getOut().println(leftPadding + "  _____");
					referring.forEach(refer -> parent.getOut().println(refer.getDescription(leftPadding + "  | ")));
					parent.getOut().println(leftPadding + "  _____");
				}
				parent.getOut().println("-----");
			});
		} else {
			parent.getOut().println("ERROR: Element not found. Try looking for it with ls.");
		}
	}

	public List<Element> getElementsReferencingThisOne(Element element) {
		return parent.getAotCache().getAll().parallelStream()
				.filter(e -> (e instanceof ReferencingElement))
				.filter(e -> ((ReferencingElement) e).getReference() != null)
				.filter(e -> ((ReferencingElement) e).getReference().equals(element))
				.toList();
	}


}