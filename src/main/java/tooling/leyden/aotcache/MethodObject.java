package tooling.leyden.aotcache;

import java.util.LinkedList;
import java.util.List;

/**
 * This class represents a method inside the AOT Cache.
 */
public class MethodObject implements Element {

	private ClassObject classObject;

	private String compilationLevel = "unknown";

	private List<String> source = new LinkedList<>();

	private String name;
	private String returnType;

	public String getType() {
		return "Method";
	}

	public ClassObject getClassObject() {
		return classObject;
	}

	public void setClassObject(ClassObject classObject) {
		this.classObject = classObject;
	}

	public String getCompilationLevel() {
		return compilationLevel;
	}

	public void setCompilationLevel(String compilationLevel) {
		this.compilationLevel = compilationLevel;
	}

	public String getReturnType() {
		return returnType;
	}

	public void setReturnType(String returnType) {
		this.returnType = returnType;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getKey() {
		return (getClassObject() != null) ? getClassObject().getKey() + "." + getName() : getName();
	}

	public void addSource(String source) {
		this.source.add(source);
	}

	@Override
	public List<String> getSources() {
		return this.source;
	}

	@Override
	public String toString() {
		return getType() + " -> " + getKey();
	}

	public String getDescription() {
		StringBuilder sb = new StringBuilder("Method " + getName() + " [compilation level: " + compilationLevel +
				"]");
		if (classObject != null) {
			sb.append(" on class " +  getClassObject().getKey());
		};
		sb.append(" returning " + getReturnType() + ".");
		sb.append('\n');
		sb.append("This element comes from: \n");
		source.forEach( s -> sb.append("  > " + s + '\n'));
		return sb.toString();
	}
}
