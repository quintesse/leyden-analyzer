package tooling.leyden.aotcache;

import java.util.HashSet;
import java.util.Set;


/**
 * This element represents a class inside the AOT Cache.
 */
public class ClassObject extends Element {

	private String name;
	private String packageName;
	private Set<MethodObject> methods = new HashSet<>();

	public String getType() {
		return "Class";
	}
	public String getKey() {
		return packageName + "." + name;
	}

	public String getName() {
		return name;
	}

	public String getPackageName() {
		return packageName;
	}

	public Set<MethodObject> getMethods() {
		return methods;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public void addMethod(MethodObject method) {
		this.methods.add(method);
		method.setClassObject(this);
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
