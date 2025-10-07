package tooling.leyden.commands.autocomplete;

import tooling.leyden.aotcache.Information;

import java.util.Iterator;


public class Packages implements Iterable<String> {
	@Override
	public Iterator<String> iterator() {
		return Information.getMyself().getAllPackages().iterator();
	}
}
