package tooling.leyden.aotcache;

/**
 * This class represents a method inside the AOT Cache.
 */
public class MethodObject extends Element {

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
	public String toString() {
		return getType() + " -> " + getKey();
	}

	public String getDescription() {
		StringBuilder sb = new StringBuilder("Method " + getName());
		if(isConstMethod()) {
			sb.append(" [CONST]");
		}
		sb.append(" [compilation level: " + compilationLevel + "]");
		if (classObject != null) {
			sb.append(" on class " +  getClassObject().getKey());
		};
		sb.append(" returning " + getReturnType() + ".");
		sb.append('\n');
		sb.append("This element comes from: \n");
		getSources().forEach( s -> sb.append("  > " + s + '\n'));
		return sb.toString();
	}
}
