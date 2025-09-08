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

@Command(name = "load", mixinStandardHelpOptions = true,
		version = "1.0",
		description = { "Load a file to extract information." },
		subcommands = { CommandLine.HelpCommand.class })
public class LoadFile implements Runnable {

	@CommandLine.ParentCommand
	DefaultCommand parent;

	public void run() {
	}

	private void load(Path path, Consumer<String> consumer) {
		parent.out.println("Adding " + path + " to our analysis.");

		try (Scanner scanner = new Scanner(Files.newInputStream(path),StandardCharsets.UTF_8)) {
			while (scanner.hasNextLine()) {
				consumer.accept(scanner.nextLine());
			}
		} catch (IOException e) {
			parent.out.println("ERROR: Couldn't load " + path.getFileName());
			parent.out.println("ERROR: " + e.getMessage());
		}
		parent.out.println("Now the AOTCache contains " + parent.aotCache.getAll().size() + " elements.");
	}

	@Command(mixinStandardHelpOptions = true, subcommands = {
			CommandLine.HelpCommand.class }, description = "Load an AOT Map cache generated with " +
			"-Xlog:aot+map=trace:file=aot.map:none:filesize=0 ")
	public void aotCache(
			@CommandLine.Parameters(arity = "1..*", paramLabel = "<file>",
					description = "file to load") Path[] files) {
		if (files != null) {
			for (Path file : files) {
				load(file, new AOTMapParser(this));
			}
		}
	}
	@Command(mixinStandardHelpOptions = true, subcommands = {
			CommandLine.HelpCommand.class }, description = "Load a log generated with -Xlog:class+load:file=aot" +
			".log:tags.")
	public void log(
			@CommandLine.Parameters(arity = "1..*", paramLabel = "<file>",
					description = "file to load") Path[] files) {
		if (files != null) {
			for (Path file : files) {
				load(file, new LogParser(this));
			}
		}
	}
    public DefaultCommand getParent() {
		return parent;
	}

}