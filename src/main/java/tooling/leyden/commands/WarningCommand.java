package tooling.leyden.commands;

import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import tooling.leyden.aotcache.Element;
import tooling.leyden.aotcache.Information;
import tooling.leyden.aotcache.Warning;
import tooling.leyden.aotcache.WarningType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Command(name = "warning", mixinStandardHelpOptions = true,
		version = "1.0",
		description = {"Help detect and clarify warnings found. By default, it lists incidents detected on the logs."},
		subcommands = {CommandLine.HelpCommand.class})
class WarningCommand implements Runnable {

	@CommandLine.ParentCommand
	DefaultCommand parent;

	@CommandLine.Option(
			names = {"-t", "--type"},
			arity = "0..*",
			split=",",
			paramLabel = "<type>",
			description = "Restrict the command to this type of warnings",
			completionCandidates = Types.class)
	protected String[] types;

	public void run() {
		printWarnings(parent.getInformation().getWarnings());
	}

	private void printWarnings(Collection<Warning> wa) {
		if (types != null && types.length > 0) {
			wa = wa.parallelStream().filter(
					warning -> Arrays.stream(types).anyMatch(t -> t.equalsIgnoreCase(warning.getType().name())))
					.collect(Collectors.toSet());
		}

		wa.forEach(element -> element.getDescription().println(parent.getTerminal()));

		parent.getOut().println("Found " + wa.size() + " warnings.");
	}

	static class Types implements Iterable<String> {
		@Override
		public Iterator<String> iterator() {
			return Arrays.stream(WarningType.values()).map(warningType -> warningType.name()).iterator();
		}
	}
}
