package tooling.leyden.commands;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import tooling.leyden.aotcache.Element;
import tooling.leyden.aotcache.MethodObject;
import tooling.leyden.aotcache.ReferencingElement;
import tooling.leyden.commands.autocomplete.Types;

import java.util.ArrayList;
import java.util.List;

@Command(name = "tree", mixinStandardHelpOptions = true,
		version = "1.0",
		description = {"Shows all dependencies of an object."},
		subcommands = {CommandLine.HelpCommand.class})
class TreeCommand implements Runnable {

	@CommandLine.ParentCommand
	DefaultCommand parent;

	@CommandLine.Parameters(
			description = {"The object name/identifier. If it is a class, use the full qualified name."})
	private String name;

	@CommandLine.Option(names = {"--type", "-t"},
			description = "Restrict the listing to this type of element. By default, it shows classes.",
			defaultValue = "Class",
			completionCandidates = Types.class)
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
				parent.getOut().println("+-- [" + e.getType() + "] " + e.getKey());
				printReferrals(e, "|   ");
			});
		} else {
			parent.getOut().println("ERROR: Element not found. Try looking for it with ls.");
		}
	}

	private void printReferrals(Element up, String leftPadding) {
		var referring = getElementsReferencingThisOne(up);
		boolean isFirst = true;
		for (Element refer : referring) {
			if (isFirst) {
				parent.getOut().println(leftPadding + "+-- " + " [" + refer.getType() + "] " + refer.getKey());
			} else {
				parent.getOut().println(leftPadding + "├── " + " [" + refer.getType() + "] " + refer.getKey());
			}
			printReferrals(refer, leftPadding + "|   ");
			isFirst = false;
		}
	}

	public List<Element> getElementsReferencingThisOne(Element element) {
		var referenced = new ArrayList<Element>();

		referenced.addAll(parent.getAotCache().getAll().parallelStream()
				.filter(e -> (e instanceof ReferencingElement))
				.filter(e -> ((ReferencingElement) e).getReferences().contains(element))
				.toList());

		referenced.addAll(parent.getAotCache().getByPackage("", "Method")
				.parallelStream().filter(
						e -> element.equals(((MethodObject) e).getClassObject())).toList());

		return referenced;
	}


}