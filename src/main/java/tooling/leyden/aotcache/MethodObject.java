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

	public MethodObject() {}

	public MethodObject(String identifier, String thisSource, AOTCache aotCache) {
		String qualifiedName = identifier.substring(identifier.indexOf(" ") + 1, identifier.indexOf("("));
		this.setName(qualifiedName.substring(qualifiedName.lastIndexOf(".") + 1));
		this.fillReturnClass(identifier, aotCache);
		this.fillClass(thisSource, qualifiedName, aotCache);
		this.procesParameters(identifier, aotCache);
	}

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

	private void procesParameters(final String identifier, final AOTCache aotCache) {
		//Get parameter classes to add as references
//88 void java.util.Hashtable.reconstitutionPut(java.util.Hashtable$Entry[], java.lang.Object, java.lang.Object)
		String parameters[] = identifier.substring(identifier.indexOf("(") + 1, identifier.indexOf(")"))
				.split(", ");
		for (String parameter : parameters) {
			if (!parameter.isBlank()) {
				var classes = aotCache.getElements(parameter, null, null, true, "Class").stream().toList();
				classes.forEach(this::addParameter);
				if (classes.isEmpty()) {
					this.addParameter(parameter);
					//Maybe it was an array:
					if (parameter.endsWith("[]")) {
						parameter = parameter.substring(0, parameter.length() - 2);
						classes = aotCache.getElements(parameter, null, null, true, "Class").stream().toList();
						classes.forEach(this::addParameter);
					}
				}
			}
		}
	}

	private void fillClass(String thisSource, String qualifiedName, AOTCache aotCache) {
		String className = qualifiedName.substring(0, qualifiedName.lastIndexOf("."));
		List<Element> objects = aotCache.getElements(className, null, null, true, "Class");
		if (objects.isEmpty()) {
			ClassObject classObject = new ClassObject(className);
			aotCache.addElement(classObject, "Referenced from a Method element in " + thisSource);
		} else {
			Element element = objects.getFirst();
			if (element instanceof ClassObject classObject) {
				//Should always be a class object, but...
				classObject.addMethod(this);
			}
		}
	}

	private void fillReturnClass(String identifier, AOTCache aotCache) {
		if (identifier.indexOf(" ") > 0) {
			this.setReturnType(identifier.substring(0, identifier.indexOf(" ")));
			aotCache
					.getElements(this.getReturnType(), null, null, true, "Class")
					.forEach(this::addReference);
		}
	}
}
