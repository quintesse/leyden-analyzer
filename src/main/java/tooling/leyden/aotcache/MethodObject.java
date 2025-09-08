package tooling.leyden.aotcache;

import java.util.LinkedList;
import java.util.List;

public class MethodObject implements Element {

	private ClassObject classObject;

	private String compilationLevel = "unknown";

	private String name;
	private String returnType;
	private List<String> parameters = new LinkedList<>();

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

	public List<String> getParameters() {
		return parameters;
	}

	public void addParameter(String parameter) {
		this.parameters.add(parameter);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getKey() {
		return getClassObject().getKey() + "." + getName();
	}

	@Override
	public String toString() {
		return getType() + " -> " + getKey();
	}

	public String getDescription() {
		StringBuilder sb = new StringBuilder("Method " + getName() + " [compilation level: " + compilationLevel +
				"]" + " on class " +  getClassObject().getKey() + " returning " + getReturnType() + " with " + parameters.size() + " parameters.");
		for (String parameter : parameters) {
			sb.append('\n');
			sb.append(" [parameter] " + parameter);
		}
		return sb.toString();
	}
}
