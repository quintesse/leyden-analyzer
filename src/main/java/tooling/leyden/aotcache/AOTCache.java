package tooling.leyden.aotcache;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class AOTCache {
	private Map<Key, Element> elements = new ConcurrentHashMap<>();
	private Set<Error> errors = new HashSet<>();
	private Configuration configuration = new Configuration();
	private Configuration statistics = new Configuration();
	private Configuration allocation = new Configuration();
	private static AOTCache myself;

	public static AOTCache getMyself() {
		return myself;
	}

	public AOTCache() {
		myself = this;
	}

	public void addElement(Element e, String source) {
		e.addSource(source);
		elements.put(new Key(e.getKey(), e.getType()), e);
	}

	public void addError(Element element, String reason, Boolean load) {
		this.errors.add(new Error(element, reason, load));
	}

	public void clear() {
		elements.clear();
		errors.clear();
		statistics.clear();
		allocation.clear();
		configuration.clear();
	}

	public List<Element> getElements(String key, String packageName, Boolean addArrays, String... type) {

		var result = elements.entrySet().parallelStream();

		if (key != null && !key.isBlank()) {
			result = result.filter(keyElementEntry -> keyElementEntry.getKey().identifier().equalsIgnoreCase(key));
		}

		if (packageName != null && !packageName.isBlank()) {
			result = result.filter(keyElementEntry -> {
				if (keyElementEntry.getValue() instanceof ClassObject classObject) {
					return classObject.getPackageName().startsWith(packageName);
				}
				if (keyElementEntry.getValue() instanceof MethodObject methodObject) {
					return methodObject.getClassObject().getPackageName().startsWith(packageName);
				}
				if (keyElementEntry.getValue().getType().equals("Object")
						|| keyElementEntry.getValue().getType().startsWith("ConstantPool")) {
					return keyElementEntry.getValue().getKey().substring(0).startsWith(packageName);
				}
				return true;
			});
		}

		if (type != null && type.length > 0) {
			result = result.filter(keyElementEntry ->
					Arrays.stream(type).anyMatch(t -> t.equalsIgnoreCase(keyElementEntry.getKey().type()))
			);
		}

		if (!addArrays) {
			result = result.filter(keyElementEntry -> {
				if (keyElementEntry.getValue() instanceof ClassObject classObject) {
					return !classObject.isArray();
				}
				return true;
			});
		}

		return result.map(keyElementEntry -> keyElementEntry.getValue()).toList();
	}

	public Set<Error> getErrors() {
		return errors;
	}

	public Collection<Element> getAll() {
		return elements.values();
	}

	public Configuration getConfiguration() {
		return configuration;
	}

	public Configuration getStatistics() {
		return statistics;
	}

	public Configuration getAllocation() {
		return allocation;
	}

	public List<String> getAllTypes() {
		return this.elements.keySet()
				.parallelStream().map(key -> key.type).distinct().toList();
	}

	public List<String> getAllPackages() {
		return this.elements.entrySet()
				.parallelStream()
				.filter((entry) -> entry.getValue() instanceof ClassObject)
				.map(entry -> ((ClassObject) entry.getValue()).getPackageName())
				.distinct()
				.toList();
	}

	record Key(String identifier, String type) {
	}
}
