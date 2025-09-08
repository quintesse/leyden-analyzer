package tooling.leyden.commands;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import tooling.leyden.aotcache.Element;

@Command(name = "clean", mixinStandardHelpOptions = true,
		version = "1.0",
		description = { "Empties the information loaded." },
		subcommands = { CommandLine.HelpCommand.class })
class CleanAOTCache implements Runnable {

	@CommandLine.ParentCommand
	DefaultCommand parent;

	public void run() {
		parent.aotCache.clear();
		parent.out.println("Cleaned the memory. Load again files to start.");
	}
}