package tooling.leyden.aotcache;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;


/**
 * This element represents a class inside the AOT Cache.
 */
public class ClassObject implements Element {

	private String name;
	private String packageName;
	private String address;
	private List<String> source = new LinkedList<>();

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

	@Override
	public List<String> getSources() {
		return source;
	}

	public void addSource(String source) {
		this.source.add(source);
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
				new StringBuilder("Class " + packageName + "." + name);
		if (getAddress() != null) {
			sb.append(" on address " + address);
		}
		sb.append(" with " + methods.size() + " methods.");
		sb.append('\n');
		sb.append("This element comes from: \n");
		source.forEach( s -> sb.append("  > " + s + '\n'));
		sb.append("Methods:");
		for (MethodObject method : this.methods) {
			sb.append('\n');
			sb.append(" [method] " + method.getName());
		}
		return sb.toString();
	}

}
