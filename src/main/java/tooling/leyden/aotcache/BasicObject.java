package tooling.leyden.aotcache;

import java.util.Random;
import java.util.random.RandomGenerator;

/**
 * This element represents a basic object like a record, annotation,... inside the AOT Cache.
 * They don't offer much information on the AOT map file.
 *
 */
 public class BasicObject extends Element {
	private String key;
	private Random random = new Random();

	@Override
	public String getKey() {
		return key;
	}

	public BasicObject() {
		key = String.valueOf(random.nextInt());
	}

	public BasicObject(String key) {
		this.key = key;
	}
}
