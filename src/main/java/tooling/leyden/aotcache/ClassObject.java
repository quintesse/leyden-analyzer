package tooling.leyden.aotcache;

import java.util.HashSet;
import java.util.Set;


/**
 * This element represents a class inside the AOT Cache.
 */
public class ClassObject extends ReferencingElement {

	private String name;
	private String packageName;
	private Set<MethodObject> methods = new HashSet<>();
	private String arrayPrefix = "";

	public ClassObject(String identifier) {
		this.setName(identifier.substring(identifier.lastIndexOf(".") + 1));
		if (identifier.indexOf(".") > 0) {
			this.setPackageName(identifier.substring(0, identifier.lastIndexOf(".")));
		}
	}

	public String getType() {
		return "Class";
	}
	public String getKey() {
		return getPackageName() + "." + getName();
	}

	public String getName() {
		return name;
	}

	public String getPackageName() {
		return arrayPrefix + packageName;
	}

	public Set<MethodObject> getMethods() {
		return methods;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setPackageName(String packageName) {
		while (packageName.startsWith("[")) {
			if (packageName.startsWith("[L")) {
				arrayPrefix += "[L";
				packageName = packageName.substring(2);
			} else {
				arrayPrefix += "[";
				packageName = packageName.substring(1);
			}
		}
		this.packageName = packageName;
	}

	public void addMethod(MethodObject method) {
		this.methods.add(method);
		method.setClassObject(this);
	}

	public Boolean isArray() {
		return !this.arrayPrefix.isBlank();
	}

	@Override
	public String getDescription(String leftPadding) {
		StringBuilder sb =
				new StringBuilder(super.getDescription(leftPadding));
		if (!this.getMethods().isEmpty()) {
			sb.append('\n' + leftPadding + "This class has the following methods:");
			sb.append('\n' + leftPadding + "   ______");
			var methodPadding = leftPadding + "   | ";
			for (MethodObject method : this.getMethods()) {
				sb.append('\n' + methodPadding + method.getKey());
			}
			sb.append('\n' + methodPadding + "______");
		}
		return sb.toString();
	}

}
