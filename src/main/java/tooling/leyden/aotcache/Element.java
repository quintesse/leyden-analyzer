package tooling.leyden.aotcache;


import java.util.LinkedList;
import java.util.List;

/** Elements that can be found on the Information.**/
public abstract class Element {

	private String type;

	private String whereDoesItComeFrom;

	/**
	 * Do we know why this element was stored in the cache?
	 *
	 * @return reason why it was stored
	 */
	public String getWhereDoesItComeFrom() {
		return whereDoesItComeFrom;
	}

	public void setWhereDoesItComeFrom(String whereDoesItComeFrom) {
		this.whereDoesItComeFrom = whereDoesItComeFrom;
	}

	/**
	 * Is this a class, a method,...?
	 *
	 * @return The type of element
	 */
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	/**
	 * When describing an element, this is the String we are going to use.
	 *
	 * @return A complete description of this element.
	 */
	public String getDescription(String leftPadding) {
		StringBuilder sb =
				new StringBuilder(leftPadding + getType() + " " + getKey());
		if (getAddress() != null) {
			sb.append(" on address " + address);
		}
		sb.append(" with size " + getSize() + ".");
		sb.append('\n' + leftPadding + "This information comes from: ");
		getSources().forEach( s -> sb.append('\n' + leftPadding + "  > " + s));
		return sb.toString();
	}

	/**
	 * Size that is written on the description of the object. On the following example, 600:
	 * 0x0000000800001d80: @@ TypeArrayU1       600
	 */
	private Integer size = null;

	public Integer getSize() {
		return size;
	}

	public void setSize(Integer size) {
		this.size = size;
	}

	/**
	 * Used to search for this element. For example, on classes this would be the full qualified name of the class.
	 *
	 * @return The key that identifies the element
	 */
	public abstract String getKey();


	private List<String> source = new LinkedList<>();
	public void addSource(String source) {
		if (!this.source.contains(source)) {
			this.source.add(source);
		}
	}

	/**
	 * Used to understand why this element is added to the cache. There may be more than one source of information
	 * for this element.
	 *
	 * @return Where this element comes from
	 */
	public List<String> getSources() {
		return this.source;
	}


	/**
	 * Address where an element can be found
	 */
	private String address;
	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	@Override
	public String toString() {
		return getType() + " -> " + getKey();
	}

	@Override
	public final boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof Element element))
			return false;

		return getType().equals(element.getType()) && getKey().equals(element.getKey());
	}

	@Override
	public int hashCode() {
		return getType().hashCode();
	}
}
