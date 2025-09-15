package tooling.leyden.aotcache;

/**
 * This element represents a basic object like a record, annotation,... inside the AOT Cache.
 * They don't offer much information on the AOT map file.
 *
 * 169948:0x0000000802b57428: @@ RecordComponent   24
 * 169949-0x0000000802b57428:   0000000000000000 0000000000000000 0000000000160015                    ........................
 *
 * 0x0000000802be8620: @@ Annotations       32
 * 0x0000000802be8620:   0000000000000000 0000000802be8640 0000000000000000 0000000000000000   ........@.......................
 *
 */
 public class BasicObject extends Element {
	private String key;

	public String getKey() {
		return key;
	}

	public BasicObject() {
		key = String.valueOf(System.currentTimeMillis());
	}

	@Override
	public String toString() {
		return getType() + " -> [random generated key] " + getKey();
	}

}
