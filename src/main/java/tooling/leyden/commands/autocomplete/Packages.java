package tooling.leyden.commands.autocomplete;

import tooling.leyden.aotcache.AOTCache;

import java.util.Iterator;


public class Packages implements Iterable<String> {
	@Override
	public Iterator<String> iterator() {
		return AOTCache.getMyself().getAllPackages().iterator();
	}
}
