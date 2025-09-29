package tooling.leyden.commands;

import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import tooling.leyden.commands.logparser.AOTMapParser;
import tooling.leyden.commands.logparser.LogParser;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;
import java.util.function.Consumer;

/**
 * Commands to load information about the AOT Cache into memory. This can be for example in the form of logs.
 */
@Command(name = "load", mixinStandardHelpOptions = true,
		version = "1.0",
		description = {"Load a file to extract information."},
		subcommands = {CommandLine.HelpCommand.class})
public class LoadFileCommand implements Runnable {

	@CommandLine.ParentCommand
	DefaultCommand parent;

	@CommandLine.Option(names = {"--background"},
			description = {"Run this load in the background.",
					"This allows you to continue working while the file gets parsed."},
			defaultValue = "false",
			arity = "0..1",
			scope = CommandLine.ScopeType.INHERIT)
	protected Boolean background= false;

	public void run() {
	}

	private void load(Consumer<String> consumer, Path... files) {

		if (files != null) {
			for (Path file : files) {
				if (background) {
					new Thread(() -> load(file, consumer)).start();
				} else {
					load(file, consumer);
				}
			}
		}
	}

	private void load(Path path, Consumer<String> consumer) {
		long time = System.currentTimeMillis();
		parent.getOut().println("Adding " + path.toAbsolutePath().getFileName()
				+ (background ? " in background " : "")
				+ "to our analysis...");

		long megabytes = Math.round((double) path.toFile().length() / 1024 / 1024);
		if (megabytes > 100) {
			new AttributedString("This is a big file. The size of this file is "
					+ megabytes + " MB. This may take a while.",
					AttributedStyle.DEFAULT.foreground(AttributedStyle.RED))
					.println(parent.getTerminal());
			if (!background) {
				new AttributedString("Consider using the `--background` option to load this file.",
						AttributedStyle.DEFAULT.bold().foreground(AttributedStyle.RED))
						.println(parent.getTerminal());
			}
		}
		if (background) {
			new AttributedString("A message will be displayed when the loading finishes.",
					AttributedStyle.DEFAULT.bold().foreground(AttributedStyle.BLUE))
					.println(parent.getTerminal());
		}
		parent.getOut().flush();

		try (Scanner scanner = new Scanner(Files.newInputStream(path), StandardCharsets.UTF_8)) {
			while (scanner.hasNextLine()) {
				consumer.accept(scanner.nextLine());
			}
		} catch (Exception e) {
			parent.getOut().println("ERROR: Loading " + path.getFileName());
			parent.getOut().println("ERROR: " + e.getMessage());
		}

		AttributedString attributedString = new AttributedString("File " + path.toAbsolutePath().getFileName()
				+ " added in " + (System.currentTimeMillis() - time) + "ms.",
				AttributedStyle.DEFAULT.bold().foreground(AttributedStyle.GREEN));
		attributedString.println(parent.getTerminal());
		parent.getOut().flush();
	}

	@Command(mixinStandardHelpOptions = true, version = "1.0", subcommands = {
			CommandLine.HelpCommand.class}, description = "Load an AOT Map cache generated with " +
			"-Xlog:aot+map=trace:file=aot.map:none:filesize=0")
	public void aotCache(
			@CommandLine.Parameters(arity = "1..*", paramLabel = "<file>",
					description = "file to load") Path[] files) {
		load(new AOTMapParser(this), files);
	}

	@Command(mixinStandardHelpOptions = true, version = "1.0", subcommands = {
			CommandLine.HelpCommand.class}, description = "Load a log generated with " +
			"-Xlog:class+load,aot*=warning:file=aot.log:level,tags")
	public void log(
			@CommandLine.Parameters(arity = "1..*", paramLabel = "<file>",
					description = "file to load") Path[] files) {
		load(new LogParser(this), files);
	}

	public DefaultCommand getParent() {
		return parent;
	}

	public void setParent(DefaultCommand parent) {
		this.parent = parent;
	}
}