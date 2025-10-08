package tooling.leyden.aotcache;

import java.util.ArrayList;
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

	public MethodObject() {
	}

	public MethodObject(String identifier, String thisSource, Boolean useNotCached, Information information) {
		String qualifiedName = identifier.substring(identifier.indexOf(" ") + 1);
		if (qualifiedName.contains("(")) {
			qualifiedName = qualifiedName.substring(0, qualifiedName.indexOf("("));
		}
		String className;
		if (qualifiedName.contains("$$")) {
			this.setName(qualifiedName.substring(qualifiedName.indexOf("$$") + 2));
			className = qualifiedName.substring(0, qualifiedName.indexOf("$$"));
		} else {
			this.setName(qualifiedName.substring(qualifiedName.lastIndexOf(".") + 1));
			className = qualifiedName.substring(0, qualifiedName.lastIndexOf("."));
		}
		this.fillReturnClass(identifier, information);
		this.fillClass(thisSource, className, information, useNotCached);
		this.procesParameters(identifier, information);
	}

	public String getType() {
		return (isConstMethod() ? "Const" : "") + "Method";
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
		return returnType == null ? "void" : returnType;
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
			sb.append('\n' + leftPadding + "Belongs to the class " + getClassObject().getKey());
		}
		;
		if (getReturnType() != null) {
			sb.append('\n' + leftPadding + "Returns " + getReturnType() + ".");
		}
		return sb.toString();
	}

	private void procesParameters(final String identifier, final Information information) {
		if (!identifier.contains("(") || !identifier.contains(")")) {
			return;
		}
		//Get parameter classes to add as references
//88 void java.util.Hashtable.reconstitutionPut(java.util.Hashtable$Entry[], java.lang.Object, java.lang.Object)
		String parameters[] = identifier.substring(identifier.indexOf("(") + 1, identifier.indexOf(")"))
				.split(", ");
		for (String parameter : parameters) {
			if (!parameter.isBlank()) {
				var classes = information.getElements(parameter, null, null, true, true, "Class").stream().toList();
				classes.forEach(this::addParameter);
				if (classes.isEmpty()) {
					this.addParameter(parameter);
					//Maybe it was an array:
					if (parameter.endsWith("[]")) {
						parameter = parameter.substring(0, parameter.length() - 2);
						classes = information.getElements(parameter, null, null, true, true, "Class").stream().toList();
						classes.forEach(this::addParameter);
					}
				}
			}
		}
	}

	private void fillClass(String thisSource, String className, Information information, Boolean useNotCached) {
		List<Element> objects = information.getElements(className, null, null, true, useNotCached, "Class");
		if (objects.isEmpty()) {
			ClassObject classObject = new ClassObject(className);
			classObject.addMethod(this);
			information.addExternalElement(classObject, "Referenced from a Method element in " + thisSource);
		} else {
			Element element = objects.getFirst();
			if (element instanceof ClassObject classObject) {
				//Should always be a class object, but...
				classObject.addMethod(this);
			}
		}
	}

	private void fillReturnClass(String identifier, Information information) {
		if (identifier.indexOf(" ") > 0) {
			this.setReturnType(identifier.substring(0, identifier.indexOf(" ")));
			information
					.getElements(this.getReturnType(), null, null, true, true, "Class")
					.forEach(this::addReference);
		}
	}
}
