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
		description = {"Shows a tree with elements that are linked to the root. This means, elements that refer to/use the root element. "},
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

	@CommandLine.Option(names = {"--reversed", "-rev"},
			description = "Build the reversed tree: see which elements are linked from the root.",
			defaultValue = "false",
			completionCandidates = Types.class)
	private Boolean reversed;

	public void run() {
		List<Element> elements;
		if (type == null || type.isBlank()) {
			elements = parent.getAotCache().getObjects(name);
		} else {
			elements = parent.getAotCache().getObjects(name, type);
		}

		if (!elements.isEmpty()) {
			elements.forEach(e -> {
				parent.getOut().println("+── [" + e.getType() + "] " + e.getKey());
				printReferrals(e, "  ");
			});
		} else {
			parent.getOut().println("ERROR: Element not found. Try looking for it with ls.");
		}
	}

	private void printReferrals(Element root, String leftPadding) {
		var referring = getElementsReferencingThisOne(root);
		boolean isFirst = true;
		for (Element refer : referring) {
			if (isFirst) {
				parent.getOut().println(leftPadding.substring(0, leftPadding.length() - 1) + '\\');
				parent.getOut().println(leftPadding + "+── " + " [" + refer.getType() + "] " + refer.getKey());
			} else {
				parent.getOut().println(leftPadding + "├── " + " [" + refer.getType() + "] " + refer.getKey());
			}
			printReferrals(refer, leftPadding + "  ");
			isFirst = false;
		}
	}

	public List<Element> getElementsReferencingThisOne(Element element) {
		var referenced = new ArrayList<Element>();

		if (reversed) {
			if (element instanceof ReferencingElement re) {
				referenced.addAll(re.getReferences());
			}
		}
		else {
			referenced.addAll(parent.getAotCache().getAll().parallelStream()
					.filter(e -> (e instanceof ReferencingElement))
					.filter(e -> ((ReferencingElement) e).getReferences().contains(element))
					.toList());

			referenced.addAll(parent.getAotCache().getByPackage("", "Method")
					.parallelStream().filter(
							e -> element.equals(((MethodObject) e).getClassObject())).toList());
		}
		return referenced;
	}


}