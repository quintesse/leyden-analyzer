package tooling.leyden.commands;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import tooling.leyden.aotcache.Configuration;
import tooling.leyden.commands.autocomplete.InfoCommandTypes;

import java.util.Arrays;

@Command(name = "info", mixinStandardHelpOptions = true,
		version = "1.0",
		description = {"Show information and statistics on the AOT Cache based on the information we have loaded."},
		subcommands = {CommandLine.HelpCommand.class})
class InfoCommand implements Runnable {

	@CommandLine.ParentCommand
	DefaultCommand parent;

	@CommandLine.Option(names = {"--type", "-t"},
			arity = "0..*",
			description = "What type of statistics we want to show. By default, configuration.",
			defaultValue = "configuration",
			completionCandidates = InfoCommandTypes.class)
	private String[] whatToShow;

	public void run() {
		if (shouldShow("configuration"))
			print("Configuration", parent.getInformation().getConfiguration());
		if (shouldShow("statistics"))
			print("Statistics", parent.getInformation().getStatistics());
		if (shouldShow("allocation"))
			print("Allocation", parent.getInformation().getAllocation());
	}

	private boolean shouldShow(String s) {
		return this.whatToShow.length == 0
				|| Arrays.stream(this.whatToShow).anyMatch(wts -> wts.equals(s));
	}

	private void print(String title, Configuration configuration) {
		parent.getOut().println('\n' + title + ": ");
		parent.getOut().println("  _________");
		configuration.getKeys().stream().sorted()
				.forEachOrdered(key ->
						parent.getOut().println("  | " + key + " -> " +
								configuration.getValue(key)));
		parent.getOut().println("  |________");
	}
}