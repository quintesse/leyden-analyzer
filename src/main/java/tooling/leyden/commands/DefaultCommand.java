package tooling.leyden.commands;

import org.jline.reader.LineReader;
import picocli.CommandLine;
import picocli.shell.jline3.PicocliCommands;
import tooling.leyden.aotcache.AOTCache;

import java.io.PrintWriter;

/**
 * Top-level command. All other commands should be subcommands of this.
 */
@CommandLine.Command(name = "", version = "1.0", description = {
		"Interactive shell to explore the contents of the AOT cache. Start by loading an AOT map file."}, footer
		= {"", "Press Ctrl-D to exit."},
		subcommands = {
				CleanCommand.class,
				CountCommand.class,
				DescribeCommand.class,
				ErrorCommand.class,
				InfoCommand.class,
				ListCommand.class,
				LoadFileCommand.class,
				PicocliCommands.ClearScreen.class,
				TreeCommand.class,
				CommandLine.HelpCommand.class})
public class DefaultCommand implements Runnable {
	private PrintWriter out;
	private AOTCache aotCache = new AOTCache();


	public DefaultCommand() {
	}

	public void setReader(LineReader reader) {
		out = reader.getTerminal().writer();
	}

	public void run() {
		out.println(new CommandLine(this).getUsageMessage());
	}

	public AOTCache getAotCache() {
		return aotCache;
	}

	public PrintWriter getOut() {
		return out;
	}
}
