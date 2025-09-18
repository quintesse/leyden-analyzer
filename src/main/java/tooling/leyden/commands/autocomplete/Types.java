package tooling.leyden.commands.autocomplete;

import java.util.Arrays;
import java.util.Iterator;


public class Types implements Iterable<String> {
	@Override
	public Iterator<String> iterator() {
		return Arrays.asList(
				"class", "method", "symbol", "constantPool",
				"adapterFingerPrint", "adapterHandlerEntry", "annotations",
				"error"
		).iterator();
	}
}
