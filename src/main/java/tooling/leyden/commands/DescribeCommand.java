package tooling.leyden.commands;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import tooling.leyden.aotcache.Element;
import tooling.leyden.aotcache.ReferencingElement;
import tooling.leyden.commands.autocomplete.Packages;

import java.sql.Ref;
import java.util.List;

@Command(name = "describe", mixinStandardHelpOptions = true,
		version = "1.0",
		description = {"Describe an object, showing all related info."},
		subcommands = {CommandLine.HelpCommand.class})
class DescribeCommand implements Runnable {

	@CommandLine.ParentCommand
	DefaultCommand parent;

	@CommandLine.Option(names = {"-v", "--verbose"},
			description = {"Show the extended information, like the full list of related elements."},
			arity = "0..*",
			defaultValue = "false",
			paramLabel = "<verbose>")
	protected Boolean verbose;

	@CommandLine.Mixin
	private CommonParameters parameters;

	public void run() {
		List<Element> elements = parent.getInformation().getElements(parameters.getName(), parameters.packageName,
				parameters.excludePackageName, parameters.showArrays, parameters.useNotCached, parameters.types);

		if (!elements.isEmpty()) {
			elements.forEach(e -> {
				var leftPadding = "|  ";
				parent.getOut().println("-----");
				parent.getOut().println(e.getDescription(leftPadding));
				if(verbose) {
					if (e instanceof ReferencingElement re) {
						if (!re.getReferences().isEmpty()) {
							parent.getOut().println(leftPadding + "Elements referenced from this element: ");
							parent.getOut().println(leftPadding + "  _____");
							re.getReferences().forEach(refer -> parent.getOut().println(leftPadding + "  | " + refer.toString()));
							parent.getOut().println(leftPadding + "  _____");
						} else {
							parent.getOut().println(leftPadding + "There are no elements referenced from this element.");
						}
					}

					var referring = getElementsReferencingThisOne(e);
					if (!referring.isEmpty()) {
						parent.getOut().println(leftPadding + "Elements that refer to this element: ");
						parent.getOut().println(leftPadding + "  _____");
						referring.forEach(refer -> parent.getOut().println(leftPadding + "  | " + refer.toString()));
						parent.getOut().println(leftPadding + "  _____");
					} else {
						parent.getOut().println(leftPadding + "There are no other elements of the cache that refer " +
								"to this element.");
					}
				}
				parent.getOut().println("-----");
			});
		} else {
			parent.getOut().println("ERROR: Element not found. Try looking for it with ls.");
		}
	}

	public List<Element> getElementsReferencingThisOne(Element element) {
		return parent.getInformation().getAll().parallelStream()
				.filter(e -> (e instanceof ReferencingElement))
				.filter(e -> ((ReferencingElement) e).getReferences().contains(element))
				.toList();
	}


}