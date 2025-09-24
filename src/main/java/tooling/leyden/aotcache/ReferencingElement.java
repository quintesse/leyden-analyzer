package tooling.leyden.aotcache;


import java.util.LinkedList;
import java.util.List;

/**
 * Elements that refer to other types of elements. For example: An element in the ConstantPool may be of certain
 * class, which is defined and loaded on the AOTCache independently.
 **/
public class ReferencingElement extends Element {
	private List<Element> references = new LinkedList<>();
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

	public List<Element> getReferences() {
		return this.references;
	}

	public void addReference(Element reference) {
		if (!this.references.contains(reference)) {
			this.references.add(reference);
		}
	}

	@Override
	public String getDescription(String leftPadding) {
		StringBuilder sb =
				new StringBuilder(super.getDescription(leftPadding));

		if (!this.getReferences().isEmpty()) {
			sb.append('\n' + leftPadding + "This element refers to :");
			for (Element e : getReferences()) {
				sb.append('\n' + leftPadding + " > " + e);
			}
		} else {
			sb.append('\n' + leftPadding + "Couldn't determine which elements does this link/refer to.");
		}

		return sb.toString();
	}
}
