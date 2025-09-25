package tooling.leyden.aotcache;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * This class represents a method inside the AOT Cache.
 */
public class MethodObject extends ReferencingElement {

	private ClassObject classObject;
	private String compilationLevel = "unknown";
	private String name;
	private String returnType;
	private Boolean constMethod = false;
	private List<String> parameters = new ArrayList<>();

	public String getType() {
		return (isConstMethod() ? "Const" : "" ) + "Method";
	}

	public ClassObject getClassObject() {
		return classObject;
	}

	public void setClassObject(ClassObject classObject) {
		this.classObject = classObject;
		addReference(classObject);
	}

	public void addParameter(Element parameter) {
		this.parameters.add(parameter.getKey());
		addReference(parameter);
	}

	//If Class is not found on the AOT Cache,
	//Maybe it is defined later?
	public void addParameter(String parameter) {
		this.parameters.add(parameter);
	}

	public String getCompilationLevel() {
		return compilationLevel;
	}

	public void setCompilationLevel(String compilationLevel) {
		this.compilationLevel = compilationLevel;
	}

	public Boolean isConstMethod() {
		return constMethod;
	}

	public void setConstMethod(Boolean constMethod) {
		this.constMethod = constMethod;
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
		StringBuilder sb = new StringBuilder(getReturnType() + " ");
		sb.append((getClassObject() != null) ? getClassObject().getKey() + "." + getName() : getName());
		sb.append("(");
		if (!parameters.isEmpty()) {
			sb.append(String.join(", ", parameters));
		}
		sb.append(")");
		return sb.toString();
	}

	@Override
	public String getDescription(String leftPadding) {
		StringBuilder sb = new StringBuilder(super.getDescription(leftPadding));
		sb.append('\n' + leftPadding + "Compilation level " + compilationLevel + ".");
		if (classObject != null) {
			sb.append('\n' + leftPadding + "Belongs to the class " +  getClassObject().getKey());
		};
		if (getReturnType() != null) {
			sb.append('\n' + leftPadding + "Returns " + getReturnType() + ".");
		}
		return sb.toString();
	}
}
