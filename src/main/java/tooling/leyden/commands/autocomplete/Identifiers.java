package tooling.leyden.commands.autocomplete;

import tooling.leyden.aotcache.AOTCache;

import java.util.Iterator;


public class Identifiers implements Iterable<String> {
	@Override
	public Iterator<String> iterator() {
		return AOTCache.getMyself().getAll().stream().map(element -> element.getKey()).limit(50).iterator();
	}
}
