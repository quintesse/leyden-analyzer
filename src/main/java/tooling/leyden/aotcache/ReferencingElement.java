package tooling.leyden.aotcache;


import java.util.LinkedList;
import java.util.List;

/**
 * Elements that refer to other types of elements. For example: An element in the ConstantPool may be of certain
 * class, which is defined and loaded on the AOTCache independently.
 **/
public abstract class ReferencingElement implements Element {

	private String address;
	private List<String> source = new LinkedList<>();
	private Element reference = null;

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}


	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getKey() {
		return name;
	}
	@Override
	public List<String> getSources() {
		return source;
	}

	public void addSource(String source) {
		this.source.add(source);
	}

	public Element getReference() {
		return this.reference;
	}

	public void setReference(Element reference) {
		this.reference = reference;
	}

	@Override
	public String toString() {
		return getType() + " -> " + getKey();
	}


	public String getDescription() {
		StringBuilder sb =
				new StringBuilder(getType() + " with identifier " + getKey());

		if (getAddress() != null) {
			sb.append(" on address " + getAddress());
		}
		if (getReference() != null) {
			sb.append(" referencing " + getReference());
		}
		sb.append(".");
		sb.append('\n');
		sb.append("This element comes from: \n");
		getSources().forEach(s -> sb.append("  > " + s + '\n'));
		return sb.toString();
	}
}
