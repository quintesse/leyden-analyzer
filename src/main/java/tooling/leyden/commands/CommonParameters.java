package tooling.leyden.commands;

import picocli.CommandLine;
import tooling.leyden.commands.autocomplete.Identifiers;
import tooling.leyden.commands.autocomplete.Packages;
import tooling.leyden.commands.autocomplete.Types;

@CommandLine.Command(synopsisHeading      = "%nUsage:%n%n",
		descriptionHeading   = "%nDescription:%n%n",
		parameterListHeading = "%nParameters:%n%n",
		optionListHeading    = "%nOptions:%n%n",
		commandListHeading   = "%nCommands:%n%n")
public class CommonParameters {

	@CommandLine.Option(names = {"-pn", "--packageName"},
			description = {"Restrict the command to elements inside this package. ",
					"Note that some elements don't belong to any particular package"},
			arity = "0..*",
			split=",",
			paramLabel = "<packageName>",
			completionCandidates = Packages.class)
	protected String[] packageName;

	@CommandLine.Option(names = {"-epn", "--excludePackageName"},
			description = {"Exclude the elements inside this package. ",
					"Note that some elements don't belong to any particular package."},
			arity = "0..*",
			split=",",
			paramLabel = "<exclude>",
			completionCandidates = Packages.class)
	protected String[] excludePackageName;

	@CommandLine.Option(names = {"--identifier", "-i"},
			description ={"The object identifier. If it is a class, use the full qualified name."},
			defaultValue = "",
			arity = "0..1",
			paramLabel = "<id>",
			completionCandidates = Identifiers.class)
	protected String name;

	@CommandLine.Option(names = {"--useArrays"},
			description = "Use array classes if true. True by default.",
			defaultValue = "true",
			arity = "0..1")
	protected Boolean showArrays;

	@CommandLine.Option(
			names = {"-t", "--type"},
			arity = "0..*",
			split=",",
			paramLabel = "<type>",
			description = "Restrict the command to this type of element",
			completionCandidates = Types.class)
	protected String[] types;
}
