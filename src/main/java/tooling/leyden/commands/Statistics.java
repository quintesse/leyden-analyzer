package tooling.leyden.commands;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import tooling.leyden.aotcache.BasicObject;
import tooling.leyden.aotcache.ClassObject;
import tooling.leyden.aotcache.ConstantPoolObject;
import tooling.leyden.aotcache.Element;
import tooling.leyden.aotcache.MethodObject;
import tooling.leyden.aotcache.ReferencingElement;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

@Command(name = "stats", mixinStandardHelpOptions = true,
		version = "1.0",
		description = {"Show statistics on the AOT Cache based on the information we have loaded."},
		subcommands = {CommandLine.HelpCommand.class})
class Statistics implements Runnable {

	@CommandLine.ParentCommand
	DefaultCommand parent;

	public void run() {
		Stream<Element> elements;
		elements = parent.getAotCache().getAll().parallelStream();

		final var constantPool = new AtomicInteger();
		final var constantPoolCache = new AtomicInteger();
		final var classes = new AtomicInteger();
		final var symbols = new AtomicInteger();
		final var adapterFingerPrint = new AtomicInteger();
		final var adapterHandlerEntry = new AtomicInteger();
		final var annotations = new AtomicInteger();

		Map<String, Integer> packages = new ConcurrentHashMap<>();

		elements.forEach(item -> {
			if (item instanceof ReferencingElement re) {
				if (item instanceof ConstantPoolObject cp) {
					constantPool.incrementAndGet();
					if (cp.getCache()) {
						constantPoolCache.incrementAndGet();
					}
				} else if (re.getType().equalsIgnoreCase("Symbol")) {
					symbols.incrementAndGet();
				}
			} else if (item instanceof BasicObject bo) {
				if (bo.getType().equalsIgnoreCase("AdapterFingerPrint")) {
					adapterFingerPrint.incrementAndGet();
				} else if (bo.getType().equalsIgnoreCase("AdapterHandlerEntry")) {
					adapterHandlerEntry.incrementAndGet();
				} else if (bo.getType().equalsIgnoreCase("Annotations")) {
					annotations.incrementAndGet();
				}
			} else if (item instanceof ClassObject classObject) {
				classes.incrementAndGet();
				var pn = classObject.getPackageName();
				if (!packages.containsKey(pn)) {
					packages.put(pn, 0);
				}
				packages.computeIfPresent(pn, (key, value) -> value + 1);
			}
		});

		parent.getOut().println("There are " + constantPool.get()
				+ " elements in the ConstantPool. Of those, "
				+ constantPoolCache.get() + " are in the ConstantPoolCache.");
		parent.getOut().println("There are " + symbols.get()
				+ " symbols.");
		parent.getOut().println("There are " + adapterFingerPrint.get()
				+ " AdapterFingerPrints.");
		parent.getOut().println("There are " + adapterHandlerEntry.get()
				+ " AdapterHandlerEntry.");
		parent.getOut().println("There are " + annotations.get()
				+ " Annotations.");
		parent.getOut().println("There are " + classes.get() + " Classes.");
		parent.getOut().println("These are the top packages with more classes:");
		packages.entrySet().stream()
				.sorted((o1, o2) -> o2.getValue() - o1.getValue())
				.limit(20)
				.forEachOrdered(entry ->
						parent.getOut().println(" - " + entry.getKey() + "(" + entry.getValue() + ")"));
	}
}