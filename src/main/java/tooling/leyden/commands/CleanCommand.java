package tooling.leyden.commands;

import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "clean", mixinStandardHelpOptions = true,
		version = "1.0",
		description = { "Empties the information loaded." },
		subcommands = { CommandLine.HelpCommand.class })
class CleanCommand implements Runnable {

	@CommandLine.ParentCommand
	DefaultCommand parent;

	public void run() {
		parent.getInformation().clear();
		parent.getOut().println("Cleaned the elements. Load again files to start a new analysis.");
	}
}