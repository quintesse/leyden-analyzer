package tooling.leyden.commands;

import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import tooling.leyden.aotcache.ClassObject;
import tooling.leyden.aotcache.Element;
import tooling.leyden.aotcache.MethodObject;
import tooling.leyden.aotcache.Warning;
import tooling.leyden.aotcache.WarningType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Command(name = "warning", mixinStandardHelpOptions = true,
		version = "1.0",
		description = {"Help detect and clarify warnings found. By default, it lists incidents detected on the logs."},
		subcommands = {CommandLine.HelpCommand.class})
class WarningCommand implements Runnable {

	@CommandLine.ParentCommand
	DefaultCommand parent;

	@CommandLine.Option(
			names = {"-t", "--type"},
			arity = "0..*",
			split = ",",
			paramLabel = "<type>",
			description = "Restrict the command to this type of warnings",
			completionCandidates = Types.class)
	protected String[] types;

	// Packages that are usually not part of the user application
	// so we skip them on our auto checks
	private String[] excludedPackages = new String[]{"java", "jdk", "sun", "com.sun"};

	public void run() {
		printWarnings();
	}

	private void printWarnings() {
		Set<Warning> wa = new HashSet<>();
		wa.addAll(parent.getInformation().getWarnings());
		wa.addAll(parent.getInformation().getAutoWarnings());
		if (types != null && types.length > 0) {
			wa = wa.parallelStream().filter(
							warning -> Arrays.stream(types).anyMatch(t -> t.equalsIgnoreCase(warning.getType().name())))
					.collect(Collectors.toSet());
		}

		wa.forEach(element -> element.getDescription().println(parent.getTerminal()));

		parent.getOut().println("Found " + wa.size() + " warnings.");
	}

	@Command(mixinStandardHelpOptions = true,
			version = "1.0",
			subcommands = {CommandLine.HelpCommand.class},
			description = {"Mark a warning as safe, clearing it from the list of warnings.",
					"You can't recover it afterwards without doing a new check or loading the log file."})
	public void rm(@CommandLine.Parameters(paramLabel = "<id>",
			description = "warning to clear") String id) {
		parent.getInformation().getAutoWarnings().removeIf(w -> w.getId().equalsIgnoreCase(id));
		parent.getInformation().getWarnings().removeIf(w -> w.getId().equalsIgnoreCase(id));
	}


	@Command(mixinStandardHelpOptions = true,
			version = "1.0",
			subcommands = {CommandLine.HelpCommand.class},
			description = {"Automatically try to detect inconsistencies and potential defects in your run.",
					"The auto-detected issues may or may not be problematic.",
					"It is up to the developer to decide that."
			})
	public void check() {

		parent.getOut().println("Trying to detect problems...");

		List<Warning> warnings = parent.getInformation().getAutoWarnings();
		warnings.clear();

		warnings.addAll(getTopTenPackagesNotCached());
		warnings.addAll(getTopTenPackagesUsedAndNotTrained());

		printWarnings();
		parent.getOut().println("The auto-detected issues may or may not be problematic.");
		parent.getOut().println("It is up to the developer to decide that.");
	}

	// Get top ten packages with most methods called but not trained
	private List<Warning> getTopTenPackagesUsedAndNotTrained() {
		var result = new ArrayList<Warning>();
		var packages = new HashMap<String, Integer>();

		parent.getInformation().getElements(null, null, excludedPackages, false, false, "Method")
				.map(MethodObject.class::cast)
				.filter(e -> e.getMethodCounters() != null)
				.filter(e -> e.getMethodTrainingData() == null || e.getCompileTrainingData().isEmpty())
				.forEach(method -> {
					final var classObject = method.getClassObject();
					addToPackageList(classObject, packages);
				});

		String warningString = " methods that were called during training run but lack proper training (don't have " +
				"TrainingData objects associated to them).";
		getTopTenPackages(packages, warningString, result);

		return result;
	}

	// Get top ten packages with most classes not cached
	private List<Warning> getTopTenPackagesNotCached() {
		var result = new ArrayList<Warning>();
		var packages = new HashMap<String, Integer>();

		for (Element e : parent.getInformation().getExternalElements().values()) {
			if (e instanceof ClassObject classObject) {
				if (Arrays.stream(excludedPackages).noneMatch(p -> classObject.getPackageName().startsWith(p))) {
					addToPackageList(classObject, packages);
				}
			}
		}

		getTopTenPackages(packages, " classes loaded and not cached.", result);

		return result;
	}

	private static void getTopTenPackages(HashMap<String, Integer> packages, String warningString, ArrayList<Warning> result) {
		packages.entrySet().stream().sorted((e1, e2) -> e2.getValue() - e1.getValue()).limit(10)
				.forEach(entry -> {
					AttributedStringBuilder sb = new AttributedStringBuilder();
					sb.append("Package '");
					sb.style(AttributedStyle.DEFAULT.bold().foreground(AttributedStyle.CYAN));
					sb.append(entry.getKey());
					sb.style(AttributedStyle.DEFAULT);
					sb.append("' contains ");
					sb.style(AttributedStyle.DEFAULT.bold().foreground(AttributedStyle.RED));
					sb.append(entry.getValue().toString());
					sb.style(AttributedStyle.DEFAULT);
					sb.append(warningString);
					result.add(new Warning(null, sb.toAttributedString(), WarningType.Training));
				});
	}

	private static void addToPackageList(ClassObject classObject, HashMap<String, Integer> packages) {
		//Cut package name to just three levels (dots)
		//because we are looking for base packages
		String packageName = "";
		int i = 1;
		for (String s : classObject.getPackageName().split("\\.")) {
			if (packageName.isBlank()) {
				packageName += s;
			} else {
				packageName += "." + s;
			}
			if (i++ > 2) {
				break;
			}
		}
		packages.putIfAbsent(packageName, 0);
		packages.compute(packageName, (key, v) -> {
			v++;
			return v;
		});
	}

	static class Types implements Iterable<String> {
		@Override
		public Iterator<String> iterator() {
			return Arrays.stream(WarningType.values()).map(warningType -> warningType.name()).iterator();
		}
	}
}
