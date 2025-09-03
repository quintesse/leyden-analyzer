package tooling.leyden.commands;

import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.nio.file.Path;
import java.util.List;

@Command(name = "load", mixinStandardHelpOptions = true,
		version = "1.0",
		description = { "Load an AOT file." },
		subcommands = { CommandLine.HelpCommand.class })
class LoadAOTMap implements Runnable {

	@CommandLine.Parameters
	private List<Path> files;

	@CommandLine.ParentCommand
	DefaultCommand parent;

	public void run() {
		if (files != null) {
			files.forEach(path -> load(path));
		}
	}

	private void load(Path path) {
		parent.out.println("Adding " + path + " to our analysis.");
	}

}