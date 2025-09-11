package tooling.leyden.aotcache;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AOTCache {
	private Map<Key, Element> elements = new HashMap<>();
	private Set<Error> errors = new HashSet<>();

	public void addElement(Element e, String source) {
		e.addSource(source);
		elements.put(new Key(e.getKey(), e.getType()), e);
	}

	public void addError(Element element, String reason, Boolean load) {
		this.errors.add(new Error(element, reason, load));
	}

	public void clear() {
		elements.clear();
	}

	public List<Element> getByPackage(String packageName, String type) {
		var result = elements.entrySet().parallelStream();

		if (packageName != null && !packageName.isBlank()) {
				result =
						result.filter(keyElementEntry -> keyElementEntry.getKey().identifier().startsWith(packageName));
		}
		if (type != null && !type.isBlank()) {
			result = result.filter(keyElementEntry -> keyElementEntry.getKey().type().equalsIgnoreCase(type));
		}

		return result.map(keyElementEntry -> keyElementEntry.getValue()).toList();
	}

	public List<Element> getObjects(String objectName, String type) {
		if (objectName == null || objectName.isBlank()) {
			return Collections.emptyList();
		}

		var result = elements.entrySet().parallelStream()
				.filter(keyElementEntry -> keyElementEntry.getKey().identifier().equalsIgnoreCase(objectName));

		if (type != null && !type.isBlank()) {
			result = result.filter(keyElementEntry -> keyElementEntry.getKey().type().equalsIgnoreCase(type));
		}

		return result.map(keyElementEntry -> keyElementEntry.getValue()).toList();
	}

	public Set<Error> getErrors() {
		return errors;
	}

	public Error getError(String identifier) {
		var error =
				errors.stream().filter(e -> identifier.equalsIgnoreCase(e.getIdentifier())).findFirst();
		return error.orElse(null);
	}

	public Collection<Element> getAll() {
		return elements.values();
	}

	record Key(String identifier, String type) {
	}
}
