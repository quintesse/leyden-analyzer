package tooling.leyden.commands;

import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "ls", mixinStandardHelpOptions = true,
		version = "1.0",
		description = { "List what is on the cache. By default, it lists classes." },
		subcommands = { CommandLine.HelpCommand.class })
class ListObjects implements Runnable {

	@CommandLine.ParentCommand
	DefaultCommand parent;

	public void run() {
		classes();
	}

	@Command(mixinStandardHelpOptions = true, subcommands = {
			CommandLine.HelpCommand.class }, description = "Lists everything on the cache.")
	public void all() {
		classes();
	}
	@Command(mixinStandardHelpOptions = true, subcommands = {
			CommandLine.HelpCommand.class }, description = "Lists classes on the cache.")
	public void classes() {
		parent.out.println("Listing all classes");
	}
}