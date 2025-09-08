package tooling.leyden.aotcache;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AOTCache {
	private Map<String, Element> elements = new HashMap<>();
	private Set<Error> errors = new HashSet<>();

	public void addElement(Element e) {
		elements.put(e.getKey(), e);
	}

	public void addError(Element element, String reason) {
		this.errors.add(new Error(element, reason));
	}

	public void clear() {
		elements.clear();
	}

	public Element getObject(String name) {
		return elements.get(name);
	}

	public Set<Element> getByPackage(String packageName) {
		Set<Element> result = new HashSet<>();

		elements.forEach((String key, Element e) -> {
			if (key.startsWith(packageName)) {
				result.add(e);
			}
		});

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

	public Map<String, Element> getAll() {
		return elements;
	}
}
