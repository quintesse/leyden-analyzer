package tooling.leyden.aotcache;

import java.util.LinkedList;
import java.util.List;


/**
 * This element represents a record inside the AOT Cache.
 * Records don't offer much information on the AOT map file.
 *
 * 169948:0x0000000802b57428: @@ RecordComponent   24
 * 169949-0x0000000802b57428:   0000000000000000 0000000000000000 0000000000160015                    ........................
 */
public class RecordObject implements Element {

	private String address;
	private String key;
	private List<String> source = new LinkedList<>();

	public String getType() {
		return "Record";
	}
	public String getKey() {
		return key;
	}

	public RecordObject() {
		key = String.valueOf(System.currentTimeMillis());
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	@Override
	public List<String> getSources() {
		return source;
	}

	public void addSource(String source) {
		this.source.add(source);
	}

	@Override
	public String toString() {
		return getType() + " -> [random generated key] " + getKey();
	}

	public String getDescription() {
		StringBuilder sb =
				new StringBuilder("Record");
		if (getAddress() != null) {
			sb.append(" on address " + address);
		}
		sb.append(".");
		sb.append('\n');
		sb.append("This element comes from: \n");
		source.forEach( s -> sb.append("  > " + s + '\n'));
		return sb.toString();
	}

}
