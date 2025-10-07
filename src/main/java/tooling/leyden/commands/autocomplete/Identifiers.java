package tooling.leyden.commands.autocomplete;

import tooling.leyden.aotcache.Information;

import java.util.Iterator;


public class Identifiers implements Iterable<String> {
	@Override
	public Iterator<String> iterator() {
		return Information.getMyself().getAll().stream().map(element -> element.getKey()).limit(50).iterator();
	}
}
