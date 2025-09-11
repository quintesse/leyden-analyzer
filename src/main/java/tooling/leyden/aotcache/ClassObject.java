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

	public String getDescription() {
		StringBuilder sb =
				new StringBuilder(getType() + " " + getKey());
		if (getAddress() != null) {
			sb.append(" on address " + getAddress());
		}
		sb.append(" with " + methods.size() + " methods.");
		sb.append('\n');
		sb.append("This element comes from: \n");
		getSources().forEach( s -> sb.append("  > " + s + '\n'));
		sb.append("Methods:");
		for (MethodObject method : this.methods) {
			sb.append('\n');
			sb.append(" [method] " + method.getName());
		}
		return sb.toString();
	}

}
