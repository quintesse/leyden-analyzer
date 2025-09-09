package tooling.leyden.commands;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import tooling.leyden.commands.logparser.AOTMapParser;
import tooling.leyden.commands.logparser.LogParser;

import java.io.IOException;
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
		description = { "Load a file to extract information." },
		subcommands = { CommandLine.HelpCommand.class })
public class LoadFile implements Runnable {

	@CommandLine.ParentCommand
	DefaultCommand parent;

	public void run() {
	}

	private void load(Consumer<String> consumer, Path... files) {
		if (files != null) {
			for (Path file : files) {
				load(file, consumer);
			}
		}
	}

	private void load(Path path, Consumer<String> consumer) {
		parent.getOut().println("Adding " + path.toAbsolutePath() + " to our analysis.");

		try (Scanner scanner = new Scanner(Files.newInputStream(path),StandardCharsets.UTF_8)) {
			while (scanner.hasNextLine()) {
				consumer.accept(scanner.nextLine());
			}
		} catch (IOException e) {
			parent.getOut().println("ERROR: Couldn't load " + path.getFileName());
			parent.getOut().println("ERROR: " + e.getMessage());
		}
		parent.getOut().println("Now the AOTCache contains " + parent.getAotCache().getAll().size() + " elements and "
				+ parent.getAotCache().getErrors().size() + " errors.");
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
			"-Xlog:class+load,aot*=warning:file=aot.log:tags")
	public void log(
			@CommandLine.Parameters(arity = "1..*", paramLabel = "<file>",
					description = "file to load") Path[] files) {
		load(new LogParser(this), files);
	}

    public DefaultCommand getParent() {
		return parent;
	}

}