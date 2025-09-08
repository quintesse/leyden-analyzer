package tooling.leyden.aotcache;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

public class ClassObject implements Element {

	private String name;
	private String packageName;
	private String address;

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

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
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
	public String toString() {
		return getType() + " -> " + packageName + "." + name;
	}

	public String getDescription() {
		StringBuilder sb =
				new StringBuilder(packageName + "." + name + " on address " + address + " with " + methods.size() + " methods.");
		for (MethodObject method : this.methods) {
			sb.append('\n');
			sb.append(" [method] " + method.getName());
		}
		return sb.toString();
	}

}
