package tooling.leyden.aotcache;

/**
 * This class represents a method inside the AOT Cache.
 */
public class MethodObject extends ReferencingElement {

	private ClassObject classObject;
	private String compilationLevel = "unknown";
	private String name;
	private String returnType;
	private Boolean constMethod = false;

	public String getType() {
		return "Method";
	}

	public ClassObject getClassObject() {
		return classObject;
	}

	public void setClassObject(ClassObject classObject) {
		this.classObject = classObject;
		addReference(classObject);
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
		return (getClassObject() != null) ? getClassObject().getKey() + "." + getName() : getName();
	}

	@Override
	public String getDescription(String leftPadding) {
		StringBuilder sb = new StringBuilder(super.getDescription(leftPadding));
		if(isConstMethod()) {
			sb.append('\n' + leftPadding + "This is a ConstMethod.");
		}
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
