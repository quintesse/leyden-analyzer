package tooling.leyden.aotcache;


/**
 * Elements that refer to other types of elements. For example: An element in the ConstantPool may be of certain
 * class, which is defined and loaded on the AOTCache independently.
 **/
public abstract class ReferencingElement extends Element {
	private Element reference = null;
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

	public Element getReference() {
		return this.reference;
	}

	public void setReference(Element reference) {
		this.reference = reference;
	}
}
