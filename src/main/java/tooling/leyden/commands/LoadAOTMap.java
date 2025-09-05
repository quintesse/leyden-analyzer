package tooling.leyden.commands;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import tooling.leyden.aotcache.ClassObject;
import tooling.leyden.aotcache.Element;
import tooling.leyden.aotcache.MethodObject;

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
class LoadAOTMap implements Runnable {

	@CommandLine.ParentCommand
	DefaultCommand parent;

	public void run() {
	}

	private void load(Path path) {
		parent.out.println("Adding " + path + " to our analysis.");

		Consumer<String> consumer = new AOTMapParser();

		try (Scanner scanner = new Scanner(Files.newInputStream(path),StandardCharsets.UTF_8)) {
			while (scanner.hasNextLine()) {
				consumer.accept(scanner.nextLine());
			}
		} catch (IOException e) {
			parent.out.println("ERROR: Couldn't load " + path.getFileName());
			parent.out.println("ERROR: " + e.getMessage());
		}
	}

	@Command(mixinStandardHelpOptions = true, subcommands = {
			CommandLine.HelpCommand.class }, description = "Load an AOT Map cache generated with " +
			"-Xlog:aot+map=trace:file=aot.map:none:filesize=0 ")
	public void aotCache(
			@CommandLine.Parameters(arity = "1..*", paramLabel = "<file>",
					description = "file to load") Path[] files) {
		if (files != null) {
			for (Path file : files) {
				load(file);
			}
		}
	}
	@Command(mixinStandardHelpOptions = true, subcommands = {
			CommandLine.HelpCommand.class }, description = "Load a generic log (not working yet).")
	public void log(
			@CommandLine.Parameters(arity = "1..*", paramLabel = "<file>",
					description = "file to load") Path[] files) {
		if (files != null) {
			for (Path file : files) {
				load(file);
			}
		}
	}


	private class AOTMapParser implements Consumer<String> {

		@Override
		public void accept(String content) {
			// 0x0000000800001d80: @@ TypeArrayU1       600
			// 0x000000080082d490: @@ Class             760 java.lang.StackFrameInfo
			// 0x000000080082ac80: @@ Method            88 char example.Class.example(long)
			if (content.indexOf(": @@") == 18) {
				String address = content.substring(0, content.indexOf(":"));
				final var typeStart = content.indexOf("@@") + 2;
				try {
					String type = content.substring(typeStart, typeStart + 18).trim();
					final var identifier = content.substring(typeStart + 22).trim();
					if (type.equalsIgnoreCase("Class")) {
						ClassObject classObject = new ClassObject();
						classObject.setName(identifier.substring(identifier.lastIndexOf(".") + 1));
						classObject.setPackageName(identifier.substring(0, identifier.lastIndexOf(".")));
						classObject.setAddress(address);
						parent.aotCache.addElement(classObject);
					} else if (type.equalsIgnoreCase("Method")) {
						MethodObject methodObject = new MethodObject();
						String qualifiedName = identifier.substring(identifier.indexOf(" ") + 1, identifier.indexOf("("));
						methodObject.setName(qualifiedName.substring(qualifiedName.lastIndexOf(".") + 1));
						methodObject.setReturnType(identifier.substring(0, identifier.indexOf(" ")));
						String className = qualifiedName.substring(0, qualifiedName.lastIndexOf("."));
						Element object = parent.aotCache.getObject(className);
						if (object == null) {
							ClassObject classObject = new ClassObject();
							classObject.setName(className.substring(className.lastIndexOf(".") + 1));
							classObject.setPackageName(className.substring(0, className.lastIndexOf(".")));
							parent.aotCache.addElement(classObject);
						} else if (object instanceof ClassObject classObject) {
							classObject.addMethod(methodObject);
						} else {
							parent.out.println("ERROR: " + methodObject + " couldn't be assigned to its class.");
						}
						parent.aotCache.addElement(methodObject);
					}
				} catch (Exception e) {
					parent.out.println("ERROR: " + e.getMessage());
					parent.out.println(content);
				}
			}
		}
	}

}