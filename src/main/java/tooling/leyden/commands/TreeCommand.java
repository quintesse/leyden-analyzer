package tooling.leyden.commands;

import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import tooling.leyden.aotcache.Element;
import tooling.leyden.aotcache.ReferencingElement;

import java.util.ArrayList;
import java.util.List;

@Command(name = "tree", mixinStandardHelpOptions = true,
		version = "1.0",
		description = {"Shows a tree with elements that are linked to the root.",
				"This means, elements that refer to/use the root element., ",
				"Blue italic elements have already been shown and will not be expanded."},
		subcommands = {CommandLine.HelpCommand.class})
class TreeCommand implements Runnable {

	@CommandLine.ParentCommand
	DefaultCommand parent;

	@CommandLine.Mixin
	private CommonParameters parameters;


	@CommandLine.Option(names = {"-l", "--level"},
			description = {"Maximum number of tree levels to display."},
			defaultValue = "3",
			arity = "0..*",
			paramLabel = "<N>")
	protected Integer level;

	public void run() {
		List<Element> elements = parent.getAotCache().getElements(parameters.name, parameters.packageName,
				parameters.showArrays, parameters.types);

		if (!elements.isEmpty()) {
			elements.forEach(e -> {
				parent.getOut().println("+── [" + e.getType() + "] " + e.getKey());
				printReferrals(e, "  ", new ArrayList<>(List.of(e.getKey())), 0);
			});
		} else {
			parent.getOut().println("ERROR: Element not found. Try looking for it with ls.");
		}
	}

	private void printReferrals(Element root, String leftPadding, List<String> travelled, Integer level) {
		if (level > this.level)
			return;
		level++;
		var referring = getElementsReferencingThisOne(root);
		boolean isFirst = true;
		for (Element refer : referring) {
			var style = AttributedStyle.DEFAULT.bold();
			if (travelled.contains(refer.getKey())) {
				style = AttributedStyle.DEFAULT.italic().foreground(AttributedStyle.BLUE);
			}

			if (isFirst) {
				parent.getOut().println(leftPadding.substring(0, leftPadding.length() - 1) + '\\');
				AttributedString attributedString = new AttributedString(
						leftPadding + "+── " + " [" + refer.getType() + "] " + refer.getKey(),
						style);
				attributedString.println(parent.getTerminal());
				parent.getTerminal().flush();
			} else {
				AttributedString attributedString = new AttributedString(
						leftPadding + "├── " + " [" + refer.getType() + "] " + refer.getKey(),
						style);
				attributedString.println(parent.getTerminal());
				parent.getTerminal().flush();
			}

			if (!travelled.contains(refer.getKey())) {
				printReferrals(refer, leftPadding + "  ", travelled, level);
			}
			isFirst = false;

			travelled.add(refer.getKey());
		}
	}

	private List<Element> getElementsReferencingThisOne(Element element) {
		var referenced = new ArrayList<Element>();

		if (element instanceof ReferencingElement re) {
			referenced.addAll(re.getReferences());
		}
		referenced.addAll(parent.getAotCache().getAll().parallelStream()
				.filter(e -> (e instanceof ReferencingElement))
				.filter(e -> ((ReferencingElement) e).getReferences().contains(element))
				.toList());

		return referenced;
	}


}