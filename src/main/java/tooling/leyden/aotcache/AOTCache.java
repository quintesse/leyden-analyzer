package tooling.leyden.aotcache;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AOTCache {
	private Map<String, Element> elements = new HashMap<>();

	public void addElement(Element e) {
		elements.put(e.getKey(), e);
	}

	public void clear() {
		elements.clear();
	}

	public Element getObject(String name) {
		return elements.get(name);
	}

	public Set<Element> getClassesByPackage(String packageName) {
		Set<Element> result = new HashSet<>();

		elements.forEach((String key, Element e) -> {
			if (key.startsWith(packageName)) {
				result.add(e);
			}
		});

		return result;
	}

	public Map<String, Element> getAll() {
		return elements;
	}
}
