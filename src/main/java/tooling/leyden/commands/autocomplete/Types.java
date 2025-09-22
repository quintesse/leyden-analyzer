package tooling.leyden.commands.autocomplete;

import tooling.leyden.aotcache.AOTCache;

import java.util.Arrays;
import java.util.Iterator;


public class Types implements Iterable<String> {
	@Override
	public Iterator<String> iterator() {
		return AOTCache.getMyself().getAllTypes().iterator();
	}
}
