package tooling.leyden.commands;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import tooling.leyden.QuarkusPicocliLineApp;
import tooling.leyden.commands.logparser.AOTMapParser;
import tooling.leyden.commands.logparser.LogParser;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Commands to load information about the AOT Cache into memory. This can be for example in the form of logs.
 */
@Command(name = "load", mixinStandardHelpOptions = true,
		version = "1.0",
		description = { "Load a file to extract information." },
		subcommands = { CommandLine.HelpCommand.class })
public class LoadFileCommand implements Runnable {

	@CommandLine.ParentCommand
	DefaultCommand parent;

	public void run() {
	}

	private void load(Consumer<String> consumer, Path... files) {

		if (files != null) {
			for (Path file : files) {
				load(file, consumer);
			}
			QuarkusPicocliLineApp.updateStatus();
		}
	}

	private void load(Path path, Consumer<String> consumer) {
		long time = System.currentTimeMillis();
		parent.getOut().println("Adding " + path.toAbsolutePath().getFileName()
				+ " to our analysis... this may take a while...");
		parent.getOut().flush();

		try (Scanner scanner = new Scanner(Files.newInputStream(path),StandardCharsets.UTF_8)) {
			while (scanner.hasNextLine()) {
				consumer.accept(scanner.nextLine());
			}
		} catch (Exception e) {
			parent.getOut().println("ERROR: Loading " + path.getFileName());
			parent.getOut().println("ERROR: " + e.getMessage());
		}

		parent.getOut().println("File " + path.toAbsolutePath().getFileName()
				+ " added in " + (System.currentTimeMillis() - time) + "ms.");

	}

	@Command(mixinStandardHelpOptions = true, version = "1.0", subcommands = {
			CommandLine.HelpCommand.class }, description = "Load an AOT Map cache generated with " +
			"-Xlog:aot+map=trace:file=aot.map:none:filesize=0")
	public void aotCache(
			@CommandLine.Parameters(arity = "1..*", paramLabel = "<file>",
					description = "file to load") Path[] files) {
		load(new AOTMapParser(this), files);
	}

	@Command(mixinStandardHelpOptions = true, version = "1.0", subcommands = {
			CommandLine.HelpCommand.class }, description = "Load a log generated with " +
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