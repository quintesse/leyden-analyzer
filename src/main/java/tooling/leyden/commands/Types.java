package tooling.leyden.commands;

import java.util.Arrays;
import java.util.Iterator;

/**
 * 🐱class Types
 */
class Types implements Iterable<String> {
	@Override
	public Iterator<String> iterator() {
		return Arrays.asList(
				"class", "method", "symbol", "constantPool",
				"adapterFingerPrint", "adapterHandlerEntry", "annotations",
				"error"
		).iterator();
	}
}
