package tooling.leyden.aotcache;

import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class represents a method inside the AOT Cache.
 */
public class MethodObject extends ReferencingElement {

	private ClassObject classObject;
	private BasicObject constMethod;
	private Element methodData;
	private Element methodCounters;
	private Element methodTrainingData;
	private Map<Integer, Element> compileTrainingData = new HashMap<>();

	private String returnType;
	private List<String> parameters = new ArrayList<>();

	public MethodObject() {
		this.setType("Method");
	}

	public MethodObject(String identifier, String thisSource, Boolean useNotCached, Information information) {
		this.setType("Method");
		String qualifiedName = identifier.substring(identifier.indexOf(" ") + 1);
		if (qualifiedName.contains("(")) {
			qualifiedName = qualifiedName.substring(0, qualifiedName.indexOf("("));
		}
		this.setName(qualifiedName.substring(qualifiedName.lastIndexOf(".") + 1));
		String className = qualifiedName.substring(0, qualifiedName.lastIndexOf("."));
		this.fillReturnClass(identifier, information);
		this.fillClass(thisSource, className, information, useNotCached);
		this.procesParameters(identifier, information);
	}

	public ClassObject getClassObject() {
		return classObject;
	}

	public void setClassObject(ClassObject classObject) {
		this.classObject = classObject;
		addReference(classObject);
	}

	public BasicObject getConstMethod() {
		return constMethod;
	}

	public void setConstMethod(BasicObject constMethod) {
		this.constMethod = constMethod;
	}

	public Element getMethodData() {
		return methodData;
	}

	public void setMethodData(Element methodData) {
		this.methodData = methodData;
	}

	public Element getMethodCounters() {
		return methodCounters;
	}

	public void setMethodCounters(Element methodCounters) {
		this.methodCounters = methodCounters;
	}

	public Element getMethodTrainingData() {
		return methodTrainingData;
	}

	public void setMethodTrainingData(Element methodTrainingData) {
		this.methodTrainingData = methodTrainingData;
	}

	public Map<Integer, Element> getCompileTrainingData() {
		return compileTrainingData;
	}

	public void addCompileTrainingData(Integer level, Element compileTrainingData) {
		this.compileTrainingData.put(level, compileTrainingData);
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

	public String getReturnType() {
		return returnType == null ? "void" : returnType;
	}

	public void setReturnType(String returnType) {
		this.returnType = returnType;
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
	public AttributedString getDescription(String leftPadding) {
		AttributedStringBuilder sb = new AttributedStringBuilder();
		sb.append(super.getDescription(leftPadding));
		sb.append(AttributedString.NEWLINE);
		sb.append(leftPadding + "Belongs to the class ");
		sb.style(AttributedStyle.DEFAULT.bold().foreground(AttributedStyle.CYAN));
		sb.append(getClassObject().getKey());
		sb.style(AttributedStyle.DEFAULT);

		if (this.methodCounters != null) {
			sb.append(AttributedString.NEWLINE);
			sb.append(leftPadding + "It has a ");
			sb.style(AttributedStyle.DEFAULT.bold().foreground(AttributedStyle.GREEN));
			sb.append("MethodCounters");
			sb.style(AttributedStyle.DEFAULT);
			sb.append(" associated to it, which means it was called at least once during training run.");
			sb.style(AttributedStyle.DEFAULT);
		} else {
			sb.append(AttributedString.NEWLINE);
			sb.append(leftPadding);
			sb.style(AttributedStyle.DEFAULT.bold());
			sb.append("This method doesn't seem to have been called during training run.");
			sb.style(AttributedStyle.DEFAULT);
		}

		if (!this.compileTrainingData.isEmpty()) {
			sb.append(AttributedString.NEWLINE);
			sb.append(leftPadding + "It has ");
			sb.style(AttributedStyle.DEFAULT.bold().foreground(AttributedStyle.GREEN));
			sb.append("CompileTrainingData");
			sb.style(AttributedStyle.DEFAULT);
			sb.append(" associated to it on level:");
			sb.style(AttributedStyle.DEFAULT.bold());
			for (Integer level : this.compileTrainingData.keySet()) {
				sb.append(" " + level);
			}
			sb.style(AttributedStyle.DEFAULT);
		} else {
			sb.append(AttributedString.NEWLINE);
			sb.append(leftPadding + "It has no ");
			sb.style(AttributedStyle.DEFAULT.bold().foreground(AttributedStyle.RED));
			sb.append("CompileTrainingData");
			sb.style(AttributedStyle.DEFAULT);
			sb.append(" associated to it.");
		}

		if (this.methodData != null) {
			sb.append(AttributedString.NEWLINE);
			sb.append(leftPadding + "It has a ");
			sb.style(AttributedStyle.DEFAULT.bold().foreground(AttributedStyle.GREEN));
			sb.append("MethodData");
			sb.style(AttributedStyle.DEFAULT);
			sb.append(" associated to it.");
		}

		if (this.methodTrainingData != null) {
			sb.append(AttributedString.NEWLINE);
			sb.append(leftPadding + "It has a ");
			sb.style(AttributedStyle.DEFAULT.bold().foreground(AttributedStyle.GREEN));
			sb.append("MethodTrainingData");
			sb.style(AttributedStyle.DEFAULT);
			sb.append(" associated to it.");
		}
		return sb.toAttributedString();
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
				var classes = information.getElements(parameter, null, null, true, true, "Class").toList();
				classes.forEach(this::addParameter);
				if (classes.isEmpty()) {
					this.addParameter(parameter);
					//Maybe it was an array:
					if (parameter.endsWith("[]")) {
						parameter = parameter.substring(0, parameter.length() - 2);
						information
								.getElements(parameter, null, null, true, true, "Class")
								.forEachOrdered(this::addReference);
					}
				}
			}
		}
	}

	private void fillClass(String thisSource, String className, Information information, Boolean useNotCached) {
		List<Element> objects = information.getElements(className, null, null, true, useNotCached, "Class").toList();
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
