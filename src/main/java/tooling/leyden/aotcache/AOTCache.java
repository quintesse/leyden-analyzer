package tooling.leyden.aotcache;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
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
		List<Element> result = new LinkedList<>();
		if (packageName == null) {
			packageName = "";
		}

		for (Map.Entry<Key, Element> entry : elements.entrySet()) {
			Key key = entry.getKey();
			Element e = entry.getValue();
			if (key.identifier().startsWith(packageName)
					&& (type == null || key.type().equalsIgnoreCase(type))) {
				result.add(e);
			}
		}

		return result;
	}

	public List<Element> getObjects(String packageName, String type) {
		List<Element> result = new LinkedList<>();
		if (packageName == null) {
			packageName = "";
		}

		for (Map.Entry<Key, Element> entry : elements.entrySet()) {
			Key key = entry.getKey();
			Element e = entry.getValue();
			if (key.identifier().equalsIgnoreCase(packageName)
					&& (type == null || type.isBlank() || key.type().equalsIgnoreCase(type))) {
				result.add(e);
			}
		}

		return result;
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
