package tooling.leyden.commands;

import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import tooling.leyden.aotcache.Information;
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
		if (parameters.types == null) {
			parameters.types = new String[]
					{"Class", "Method", "CompileTrainingData", "KlassTrainingData",
							"MethodCounters", "MethodData", "MethodTrainingData"};
		}

		List<Element> elements = parent.getInformation().getElements(parameters.getName(), parameters.packageName,
				parameters.excludePackageName,
				parameters.showArrays, parameters.useNotCached, parameters.types);

		if (!elements.isEmpty()) {
			elements.forEach(e -> {
				(new AttributedString("+ ")).print(parent.getTerminal());
				e.toAttributedString().println(parent.getTerminal());
				printReferrals(e, "  ", new ArrayList<>(List.of(e)), 0);
			});
		} else {
			(new AttributedString("ERROR: Element not found. Try looking for it with ls.",
					AttributedStyle.DEFAULT.foreground(AttributedStyle.RED).bold())).println(parent.getTerminal());
		}
	}

	private void printReferrals(Element root, String leftPadding, List<Element> travelled, Integer level) {
		if (level > this.level || (max > 0 &&  travelled.size() > max))
			return;
		level++;

		boolean isFirst = true;
		for (Element refer : getElementsReferencingThisOne(root)) {
			AttributedStringBuilder asb = new AttributedStringBuilder();

			if (isFirst) {
				asb.append(leftPadding.substring(0, leftPadding.length() - 1) + '\\');
				asb.append(AttributedString.NEWLINE);
			} else {
				asb.append(leftPadding + '|');
				asb.append(AttributedString.NEWLINE);
			}

			if (travelled.contains(refer)) {
				asb.style(AttributedStyle.DEFAULT.bold().italic().foreground(AttributedStyle.BLUE));
				asb.append(leftPadding + "- ");
			} else {
				asb.append(leftPadding + "+ ");
			}

			asb.append(refer.toAttributedString());
			asb.toAttributedString().println(parent.getTerminal());

			if (!travelled.contains(refer)) {
				printReferrals(refer, leftPadding + "  ", travelled, level);
			}
			isFirst = false;

			travelled.add(refer);
			if (max > 0 && travelled.size() > max) {
				break;
			}
		}
		parent.getTerminal().flush();
	}

	private List<Element> getElementsReferencingThisOne(Element element) {
		var referenced = new ArrayList<Element>();

		if (element instanceof ReferencingElement re) {
			referenced.addAll(filter(re.getReferences().parallelStream()));
		}
		referenced.addAll(filter(parent.getInformation().getAll().parallelStream()
				.filter(e -> (e instanceof ReferencingElement))
				.filter(e -> ((ReferencingElement) e).getReferences().contains(element))));

		return referenced;
	}

	//Delegate on Information for filtering
	private List<Element> filter(Stream<Element> elements) {
		return Information.filterByParams(parameters.packageName, parameters.excludePackageName, parameters.showArrays,
				parameters.types, elements);
	}

}