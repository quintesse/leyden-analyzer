package tooling.leyden.commands;

import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import tooling.leyden.aotcache.AOTCache;
import tooling.leyden.aotcache.Element;
import tooling.leyden.aotcache.ReferencingElement;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

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

	@CommandLine.Option(names = {"-max"},
			description = {"Maximum number of elements to display. By default, 100. If using -1, it shows all " +
					"elements. Note that on some cases this may mean showing thousands of elements."},
			defaultValue = "100",
			arity = "0..*",
			paramLabel = "<N>")
	protected Integer max;

	public void run() {
		List<Element> elements = parent.getAotCache().getElements(parameters.getName(), parameters.packageName,
				parameters.excludePackageName,
				parameters.showArrays, parameters.types);

		if (!elements.isEmpty()) {
			elements.forEach(e -> {
				parent.getOut().println("+── [" + e.getType() + "] " + e.getKey());
				printReferrals(e, "  ", new ArrayList<>(List.of(e)), 0);
			});
		} else {
			parent.getOut().println("ERROR: Element not found. Try looking for it with ls.");
		}
	}

	private void printReferrals(Element root, String leftPadding, List<Element> travelled, Integer level) {
		if (level > this.level || (max > 0 &&  travelled.size() > max))
			return;
		level++;
		var referring = getElementsReferencingThisOne(root);
		boolean isFirst = true;
		for (Element refer : referring) {
			var style = AttributedStyle.DEFAULT.bold();
			if (travelled.contains(refer)) {
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

			if (!travelled.contains(refer)) {
				printReferrals(refer, leftPadding + "  ", travelled, level);
			}
			isFirst = false;

			travelled.add(refer);
			if (max > 0 && travelled.size() > max) {
				break;
			}
		}
	}

	private List<Element> getElementsReferencingThisOne(Element element) {
		var referenced = new ArrayList<Element>();

		if (element instanceof ReferencingElement re) {
			referenced.addAll(filter(re.getReferences().parallelStream()));
		}
		referenced.addAll(filter(parent.getAotCache().getAll().parallelStream()
				.filter(e -> (e instanceof ReferencingElement))
				.filter(e -> ((ReferencingElement) e).getReferences().contains(element))));

		return referenced;
	}

	//Delegate on AOTCache for filtering
	private List<Element> filter(Stream<Element> elements) {
		return AOTCache.filterByParams(parameters.packageName, parameters.excludePackageName, parameters.showArrays,
				parameters.types, elements);
	}

}