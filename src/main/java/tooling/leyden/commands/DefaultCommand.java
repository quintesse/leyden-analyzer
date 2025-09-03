package tooling.leyden.commands;

import org.jline.reader.LineReader;
import picocli.CommandLine;
import picocli.shell.jline3.PicocliCommands;

import java.io.PrintWriter;

/**
 * Top-level command that just prints help.
 */
@CommandLine.Command(name = "", description = {
		"Interactive shell to explore the contents of the AOT cache. Start by loading an AOT map file."}, footer
		= {"", "Press Ctrl-D to exit."},
		subcommands = {
				ListObjects.class,
				LoadAOTMap.class,
				PicocliCommands.ClearScreen.class,
				CommandLine.HelpCommand.class})
public class DefaultCommand implements Runnable {
	PrintWriter out;

	public DefaultCommand() {
	}

	public void setReader(LineReader reader) {
		out = reader.getTerminal().writer();
	}

	public void run() {
		out.println(new CommandLine(this).getUsageMessage());
	}
}
