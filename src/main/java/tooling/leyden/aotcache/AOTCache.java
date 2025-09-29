package tooling.leyden.aotcache;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

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

	public List<Element> getElements(String key, String[] packageName, String[] excludePackageName,
									 Boolean addArrays, String... type) {

		var result = elements.entrySet().parallelStream();

		if (key != null && !key.isBlank()) {
			result = result.filter(keyElementEntry -> keyElementEntry.getKey().identifier().equalsIgnoreCase(key));
		}

		return filterByParams(packageName, excludePackageName, addArrays, type,
				result.map(keyElementEntry -> keyElementEntry.getValue()));
	}

	public static List<Element> filterByParams(String[] packageName,
																 String[] excludePackageName,
																 Boolean addArrays,
																 String[] type,
																 Stream<Element> result) {
		if (packageName != null && packageName.length > 0) {
			result = result.filter(e -> {
				if (e instanceof ClassObject classObject) {
					return Arrays.stream(packageName).anyMatch(p -> classObject.getPackageName().startsWith(p));
				}
				if (e instanceof MethodObject methodObject) {
					if (methodObject.getClassObject() != null) {
						return Arrays.stream(packageName).anyMatch(p ->
								methodObject.getClassObject().getPackageName().startsWith(p));
					}
					return Arrays.stream(packageName).anyMatch(p -> methodObject.getName().startsWith(p));
				}
				if (e.getType().equals("Object")
						|| e.getType().startsWith("ConstantPool")) {

					return Arrays.stream(packageName)
							.anyMatch(p -> e.getKey().startsWith(p));
				}
				return false;
			});
		}

		if (excludePackageName != null && excludePackageName.length > 0) {
			result = result.filter(e -> {
				if (e instanceof ClassObject classObject) {
					return Arrays.stream(excludePackageName).noneMatch(p -> classObject.getPackageName().startsWith(p));
				}
				if (e instanceof MethodObject methodObject) {
					if (methodObject.getClassObject() != null) {
						return Arrays.stream(excludePackageName).noneMatch(p ->
								methodObject.getClassObject().getPackageName().startsWith(p));
					}
					return Arrays.stream(excludePackageName).noneMatch(p -> methodObject.getName().startsWith(p));
				}
				if (e.getType().equals("Object") || e.getType().startsWith("ConstantPool")) {

					return Arrays.stream(excludePackageName).noneMatch(p -> e.getKey().startsWith(p));
				}
				return false;
			});
		}

		if (type != null && type.length > 0) {
			result = result.filter(e -> Arrays.stream(type).anyMatch(t -> t.equalsIgnoreCase(e.getType()))
			);
		}

		if (!addArrays) {
			result = result.filter(e -> {
				if (e instanceof ClassObject classObject) {
					return !classObject.isArray();
				}
				return true;
			});
		}
		return result.toList();
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
