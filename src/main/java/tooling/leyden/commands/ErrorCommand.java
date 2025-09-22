package tooling.leyden.commands;

import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "error", mixinStandardHelpOptions = true,
		version = "1.0",
		description = {"Help detect and clarify errors found. By default, it lists errors detected on the logs."},
		subcommands = {CommandLine.HelpCommand.class})
class ErrorCommand implements Runnable {

	@CommandLine.ParentCommand
	DefaultCommand parent;

	public void run() {
		var errors = parent.getAotCache().getErrors();
		errors.forEach(element -> parent.getOut().println("  > " + element.toString()));
		parent.getOut().println("Found " + errors.size() + " errors.");

	}
}