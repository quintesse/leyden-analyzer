package tooling.leyden.commands.autocomplete;

import java.util.Arrays;
import java.util.Iterator;


public class InfoCommandTypes implements Iterable<String> {
	@Override
	public Iterator<String> iterator() {
		return Arrays.asList(
				"count", "configuration", "statistics", "allocation"
		).iterator();
	}
}
