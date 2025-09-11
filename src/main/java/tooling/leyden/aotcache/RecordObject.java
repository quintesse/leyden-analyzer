package tooling.leyden.aotcache;

/**
 * This element represents a record inside the AOT Cache.
 * Records don't offer much information on the AOT map file.
 *
 * 169948:0x0000000802b57428: @@ RecordComponent   24
 * 169949-0x0000000802b57428:   0000000000000000 0000000000000000 0000000000160015                    ........................
 */
public class RecordObject extends Element {
	private String key;

	public String getType() {
		return "Record";
	}
	public String getKey() {
		return key;
	}

	public RecordObject() {
		key = String.valueOf(System.currentTimeMillis());
	}

	@Override
	public String toString() {
		return getType() + " -> [random generated key] " + getKey();
	}

}
