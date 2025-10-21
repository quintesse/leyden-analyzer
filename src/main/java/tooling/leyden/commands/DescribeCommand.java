package tooling.leyden.commands;

import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
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
		var elements = parent.getInformation().getElements(parameters.getName(), parameters.packageName,
				parameters.excludePackageName, parameters.showArrays, parameters.useNotCached, parameters.types).toList();

		AttributedStringBuilder sb = new AttributedStringBuilder();
		if (!elements.isEmpty()) {
			elements.forEach(e -> {
				var leftPadding = "|  ";
				sb.append("-----");
				sb.append(AttributedString.NEWLINE);
				sb.append(e.getDescription(leftPadding));
				sb.append(AttributedString.NEWLINE);
				if(verbose) {
					if (e instanceof ReferencingElement re) {
						if (!re.getReferences().isEmpty()) {
							sb.append(leftPadding + "Elements referenced from this element: ");
							sb.append(AttributedString.NEWLINE);
							sb.append(leftPadding + "  _____");
							sb.append(AttributedString.NEWLINE);
							re.getReferences().forEach(refer -> sb.append(leftPadding + "  | " + refer.toString() +
											"\n"));
							sb.append(leftPadding + "  _____");
							sb.append(AttributedString.NEWLINE);
						} else {
							sb.append(leftPadding + "There are no elements referenced from this element.");
							sb.append(AttributedString.NEWLINE);
						}
					}

					var referring = getElementsReferencingThisOne(e);
					if (!referring.isEmpty()) {
						sb.append(leftPadding + "Elements that refer to this element: ");
						sb.append(AttributedString.NEWLINE);
						sb.append(leftPadding + "  _____");
						sb.append(AttributedString.NEWLINE);
						referring.forEach(refer -> sb.append(leftPadding + "  | " + refer.toString() + "\n"));
						sb.append(leftPadding + "  _____");
						sb.append(AttributedString.NEWLINE);
					} else {
						sb.append(leftPadding + "There are no other elements of the cache that refer " +
								"to this element.");
						sb.append(AttributedString.NEWLINE);
					}
				}
				sb.append("-----");
				sb.append(AttributedString.NEWLINE);
			});
		} else {
			sb.style(AttributedStyle.DEFAULT.bold().foreground(AttributedStyle.RED));
			sb.append("ERROR: Element not found. Try looking for it with ls.");
		}
		sb.toAttributedString().println(parent.getTerminal());
	}

	public List<Element> getElementsReferencingThisOne(Element element) {
		return parent.getInformation().getAll().parallelStream()
				.filter(e -> (e instanceof ReferencingElement))
				.filter(e -> ((ReferencingElement) e).getReferences().contains(element))
				.toList();
	}


}