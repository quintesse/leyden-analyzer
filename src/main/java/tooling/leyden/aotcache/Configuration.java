package tooling.leyden.aotcache;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Configuration {

	private Map<String, String> configuration = new ConcurrentHashMap<>();

	public void addValue(String key, String value) {
		if (configuration.containsKey(key) && !configuration.get(key).equals(value)) {
			System.out.println("Rewriting value for '" + key + "' previously it was '" + configuration.get(key) + "'.");
		}
		configuration.put(key.trim(), value.trim());
	}

	public String getValue(String key) {
		return configuration.getOrDefault(key, "unknown");
	}

	public Set<String> getKeys(){
		return configuration.keySet();
	}

	public void clear() {
		configuration.clear();
	}
}
