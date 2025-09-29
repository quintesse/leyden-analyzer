package tooling.leyden.commands.logparser;

import tooling.leyden.aotcache.AOTCache;
import tooling.leyden.aotcache.ClassObject;
import tooling.leyden.aotcache.ConstantPoolObject;
import tooling.leyden.aotcache.Element;
import tooling.leyden.aotcache.MethodObject;
import tooling.leyden.aotcache.BasicObject;
import tooling.leyden.aotcache.ReferencingElement;
import tooling.leyden.commands.LoadFileCommand;

import java.util.List;
import java.util.function.Consumer;

/**
 * This class is capable of parsing the AOT logs that generate the AOT cache map.
 */
public class AOTMapParser implements Consumer<String> {

	private final LoadFileCommand loadFile;
	private final AOTCache aotCache;

	public AOTMapParser(LoadFileCommand loadFile) {
		this.loadFile = loadFile;
		this.aotCache = loadFile.getParent().getAotCache();
	}


	@Override
	public void accept(String content) {
		if (content.indexOf(": @@") != 18)
			return;

		try {
			final var thisSource = "AOT Map";
			String[] contentParts = content.split(" +");

			final var address = contentParts[0].substring(0, contentParts[0].length() - 1);
			var type = contentParts[2];
			Integer size;
			try {
				size = Integer.valueOf(contentParts[3]);
			} catch (NumberFormatException e) {
				size = -1;
			}
			final var identifier =
					content.substring(content.indexOf(" " + contentParts[3]) + 1 + contentParts[3].length()).trim();

			Element element = null;

			if (type.equalsIgnoreCase("Class")) {
				// Metadata Klass
//					0x0000000800868d58: @@ Class             520 java.lang.constant.ClassDesc
//					0x0000000800869078: @@ Class             512 [Ljava.lang.constant.ClassDesc;
				element = processClass(identifier);
			} else if (type.equalsIgnoreCase("Method")) {
				//Metadata Method
//					0x0000000800831250: @@ Method            88 void java.lang.management.MemoryUsage.<init>(javax.management.openmbean.CompositeData)
//					0x0000000800831250:   0000000800001710 0000000802c9dc38 0000000000000000 0000000000000000   ........8.................
				element = processMethod(identifier, thisSource);
			} else if (type.equalsIgnoreCase("ConstMethod")) {
//					 0x0000000804990600: @@ ConstMethod       88 void jdk.internal.access.SharedSecrets.setJavaNetHttpCookieAccess(jdk.internal.access.JavaNetHttpCookieAccess)
				element = processMethod(identifier, thisSource);
				((MethodObject) element).setConstMethod(true);
			} else if (type.equalsIgnoreCase("Symbol")) {
//					0x0000000801e3c000: @@ Symbol            40 [Ljdk/internal/vm/FillerElement;
//					0x0000000801e3c028: @@ Symbol            32 jdk/internal/event/Event
//					0x0000000801e3c048: @@ Symbol            24 jdk/jfr/Event
//					0x0000000801e3c060: @@ Symbol            8 [Z
				element = processReferencingElement(new ReferencingElement(), identifier, content);
			} else if (type.equalsIgnoreCase("ConstantPoolCache")) {
//					0x0000000800ec7408: @@ ConstantPoolCache 64 javax.naming.spi.ObjectFactory
				element = processReferencingElement(new ConstantPoolObject(true), identifier, content);
			} else if (type.equalsIgnoreCase("ConstantPool")) {
				element = processReferencingElement(new ConstantPoolObject(false), identifier, content);
			} else if (type.startsWith("TypeArray")
//					0x0000000800001d80: @@ TypeArrayU1       600
//					0x000000080074cc50: @@ TypeArrayOther    800
					|| type.equalsIgnoreCase("AdapterFingerPrint")
//					0x000000080074cc20: @@ AdapterFingerPrint 8
//					0x000000080074cc20:   bbbeaaaa00000001                                                      ........
//					0x000000080074cc28: @@ AdapterFingerPrint 16
//					0x000000080074cc28:   bbbeaaaa00000002 000000000000aaaa
					|| type.equalsIgnoreCase("AdapterHandlerEntry")
//					0x00000008008431b0: @@ AdapterHandlerEntry 48
//					0x00000008008431b0:   0000000800019868 00007f276e002e60 00007f276e002ee5 00007f276e002ec4   h.......`..n'......n'......n'...
//					0x00000008008431d0:   00007f276e002f20 0000000000000001
					|| type.equals("RecordComponent")
//					 0x00000008029329e8: @@ RecordComponent   24
					|| type.equalsIgnoreCase("Annotations")
//					0x0000000802bf50f0: @@ Annotations       32
//					0x0000000802bf50f0:   0000000802b719a0 0000000000000000 0000000000000000 0000000000000000   ................................
					|| type.equalsIgnoreCase("MethodCounters")
//					0x0000000801e4c280: @@ MethodCounters    64
//					0x0000000801e4c280:   0000000800001800 0000000000000002 0000000801e4c228 0000000801e4c280   ................(...............
//					0x0000000801e4c2a0:   0000000000000000 000000fe00000000 00000000000007fe 0000000000000000   ................................
					|| type.equalsIgnoreCase("MethodData")
//					0x0000000801e44448: @@ MethodData        584
//					0x0000000801e44448:   0000000800001788 0000000800773428 000000b800000248 0000000000000000   ........(4w.....H...............
					|| type.endsWith("TrainingData")
//					0x0000000801d5e050: @@ MethodTrainingData 96
//					0x0000000801d5e050:   0000000800001bd0 0000000000000000 0000000801d5e028 0000000000000000   ................(...............
//					0x0000000800a45f58: @@ CompileTrainingData 80
			) {
				element = new BasicObject(address);
			} else if (type.equalsIgnoreCase("Misc")) {
//					0x00000008049a8410: @@ Misc data 1985520 bytes
//					0x00000008049a8410:   0000000000000005 0000000801e563d0 0000000801e56600 0000000801e56420   .........c.......f...... d......
//					0x00000008049a8430:   0000000801e543a8 0000000801e548a8 0000000000000005 0000000801e58dc0   .C.......H................
				type = "Misc-data";
				size = Integer.valueOf(contentParts[4]);
				element = new BasicObject(address);
			} else if (type.equalsIgnoreCase("Object")) {
				//Instances of classes:
//				0x00000000fff69c68: @@ Object (0xfff69c68) [B length: 45
//				0x00000000fff63458: @@ Object (0xfff63458) java.lang.String$CaseInsensitiveComparator
				//2 Special cases: they a literal representation (String and Class instances)
				// and therefore they can be named in the code of some class
				// and be linked into the heap via a constant pool cache.
//				0x00000000ffe94558: @@ Object (0xffe94558) java.lang.String "sun.util.locale.BaseLocale"
				//java.lang.Class instances (they have been pre-created by <clinit> method:
//				0x00000000ffef4720: @@ Object (0xffef4720) java.lang.Class Lsun/util/locale/BaseLocale$1;
				var id = "";
				for (int i = 4; i < contentParts.length; i++) {
					id = id + " " + contentParts[i];
				}
				element = processReferencingElement(new ReferencingElement(), id.trim(), content);
				((ReferencingElement) element).setName(contentParts[3] + " " + id.trim());
			} else {
				loadFile.getParent().getOut().println("Unidentified: " + type);
				loadFile.getParent().getOut().println(content);
				element = new BasicObject(address);
			}
			if (element != null) {
				element.setAddress(address);
				element.setType(type);
				element.setSize(size);
				this.aotCache.addElement(element, thisSource);
			}
		} catch (Exception e) {
			loadFile.getParent().getOut().println("ERROR: " + e.getMessage());
			loadFile.getParent().getOut().println("ERROR: " + content);
		}
	}

	private Element processReferencingElement(ReferencingElement e, String identifier, String content) {
		e.setName(identifier);
		fillReferencedElement(identifier, e, content);
		return e;
	}

	private Element processMethod(final String identifier, final String thisSource) {
		// 0x000000080082ac80: @@ Method            88 char example.Class.example(long)
		// 0x0000000800773ea0: @@ Method            88 boolean java.lang.Object.equals(java.lang.Object)
		MethodObject methodObject = new MethodObject();
		String qualifiedName = identifier.substring(identifier.indexOf(" ") + 1, identifier.indexOf("("));
		methodObject.setName(qualifiedName.substring(qualifiedName.lastIndexOf(".") + 1));

		methodObject.setReturnType(identifier.substring(0, identifier.indexOf(" ")));
		this.aotCache
				.getElements(methodObject.getReturnType(), null, null, true, "Class")
				.forEach(e -> methodObject.addReference(e));

		String className = qualifiedName.substring(0, qualifiedName.lastIndexOf("."));
		List<Element> objects = this.aotCache.getElements(className, null, null, true, "Class");
		if (objects.isEmpty()) {
			ClassObject classObject = new ClassObject();
			classObject.setName(className.substring(className.lastIndexOf(".") + 1));
			classObject.setPackageName(className.substring(0, className.lastIndexOf(".")));
			this.aotCache.addElement(classObject, "Referenced from a Method element in " + thisSource);
		} else {
			Element element = objects.get(0);
			if (element instanceof ClassObject classObject) {
				//Should always be a class object, but...
				classObject.addMethod(methodObject);
			}
		}

		//Get parameter classes to add as references
//88 void java.util.Hashtable.reconstitutionPut(java.util.Hashtable$Entry[], java.lang.Object, java.lang.Object)
		String parameters[] = identifier.substring(identifier.indexOf("(") + 1, identifier.indexOf(")"))
				.split(", ");
		for (String parameter : parameters) {
			if (!parameter.isBlank()) {
				var classes = this.aotCache.getElements(parameter, null, null, true, "Class").stream().toList();
				classes.forEach(e -> methodObject.addParameter(e));
				if (classes.isEmpty()) {
					methodObject.addParameter(parameter);
					//Maybe it was an array:
					if (parameter.endsWith("[]")) {
						parameter = parameter.substring(0, parameter.length() - 2);
						classes = this.aotCache.getElements(parameter, null, null, true, "Class").stream().toList();
						classes.forEach(e -> methodObject.addParameter(e));
					}
				}
			}
		}

		return methodObject;
	}

	private Element processClass(String identifier) {
		// 0x000000080082d490: @@ Class             760 java.lang.StackFrameInfo
		List<Element> elements = this.aotCache.getElements(identifier, null, null, true, "Class");
		// It could have been already loaded
		ClassObject classObject = null;
		for (Element element : elements) {
			if (element instanceof ClassObject object) {
				classObject = object;
				break;
			}
		}
		if (classObject == null) {
			classObject = new ClassObject();
			classObject.setName(identifier.substring(identifier.lastIndexOf(".") + 1));
			if (identifier.indexOf(".") > 0) {
				classObject.setPackageName(identifier.substring(0, identifier.lastIndexOf(".")));
			}
		}

		return classObject;
	}

	private void fillReferencedElement(final String identifier, final ReferencingElement element,
									   final String content) {
		//In case we are referencing some class already loaded
		// Replace / for . because some elements use / to point to a class
		var objectName = identifier.replaceAll("/", ".");

		if (methodSignature(element, content, objectName)) {
			return;
		}

		if (listOfElements(element, content, objectName)) {
			return;
		}

		if (isMethod(element, objectName)) {
			return;
		}

		if (javaFileName(element, objectName)) {
			return;
		}

		if (literalString(element, objectName)) {
			return;
		}

		if (objectName.trim().startsWith("java.lang.Class ")) {
			//Coming from an Object, we are looking to reference the java.lang.Class
			//and the class that that java.lang.Class refers itself
			for (Element e : this.aotCache.getElements("java.lang.Class", new String[]{"java.lang"}, null, true,
					"Class")) {
				element.addReference(e);
			}

			//Now look for the class itself
			objectName = objectName.substring(16).trim();
		}

		//Now try to locate the class itself
		List<Element> elements = this.aotCache.getElements(objectName, null, null, true, "Class");
		if (!elements.isEmpty()) {
			elements.forEach(e -> element.addReference(e));
		} else {
			//Maybe we are looking for a Symbol
			for (Element e : this.aotCache.getElements(
					objectName.replaceAll("\\.", "/"), null, null, true,
					"Symbol")) {
				element.addReference(e);
			}
		}
	}

	private boolean literalString(ReferencingElement element, String objectName) {
		if (objectName.trim().startsWith("java.lang.String ")) {
			for (Element e : this.aotCache.getElements("java.lang.String", new String[]{"java.lang"}, null, true,
					"Class")) {
				element.addReference(e);
			}
			//Coming from an Object, we are looking to reference a Symbol
			if (objectName.length() > 18) {
				//avoid empty strings
				for (Element e : this.aotCache.getElements(
						objectName.substring(18, objectName.length() - 1), null, null, true,
						"Symbol")) {
					element.addReference(e);
				}
			}
			return true;
		}
		return false;
	}

	private boolean javaFileName(ReferencingElement element, String objectName) {
		if (objectName.trim().endsWith(".java")) {
			//Some Symbols have the .java filename of a class:
			objectName = objectName.substring(0, objectName.length() - 5);

			//We need to find the class without package:
			//This is heavy that's why we have a special function for it
			for (Element el : this.aotCache.getClassesByName(objectName)) {
				element.addReference(el);
			}
			return true;
		}
		return false;
	}

	private boolean isMethod(ReferencingElement element, String objectName) {
		//if it refers to a method, let's search for it
		if (objectName.indexOf("$$") > 0) {
			objectName = objectName.replace("$$", ".");
			for (Element e : this.aotCache.getElements(objectName, null, null, true, "Method", "ConstMethod")) {
				element.addReference(e);
			}
			return true;
		}
		return false;
	}

	private boolean listOfElements(ReferencingElement element, String content, String objectName) {
		//Sometimes they are a list of concatenated classes/elements
		// Ljava/lang/Object;Ljava/util/function/Supplier<Ljdk/internal/util/ReferencedKeySet<Lsun/util/locale/BaseLocale;>;>;
		if (objectName.contains(";")
				//Sometimes it is a lonely class which we don't process on this code block:
				// Lsun/util/locale/BaseLocale$1;
				&& (objectName.indexOf(";") != objectName.lastIndexOf(";")
				|| !objectName.endsWith(";"))) {

			if (objectName.contains("<")) {
				//Get generics out
				fillReferencedElement(objectName.substring(0, objectName.indexOf("<")), element, content);

				//Now process the generics inside the first <>
				//FIXME: This should be done with regexp probably.
				// But the damn nested <> are breaking my attempts.
				String generics = "";
				String tmp = objectName.substring(objectName.indexOf("<") + 1);
				int nested = 1;
				while (nested > 0) {
					if (tmp.indexOf("<") > 0
							&& tmp.indexOf("<") < tmp.indexOf(">")) {
						generics = generics + tmp.substring(0, tmp.indexOf("<") + 1);
						tmp = tmp.substring(tmp.indexOf("<") + 1);
						nested++;
					} else if (tmp.indexOf(">") > 0) {
						generics = generics + tmp.substring(0, tmp.indexOf(">") + (nested > 1 ? 1 : 0));
						tmp = tmp.substring(tmp.indexOf(">") + 1);
						nested--;
					} else {
						generics = generics + tmp;
						tmp = "";
						nested = 0;
					}
				}

				fillReferencedElement(generics, element, content);
				if (!(tmp.isBlank() || tmp.equals(";"))) {
					fillReferencedElement(tmp, element, content);
				}
			} else {
				for (String className : objectName.split(";")) {
					fillReferencedElement(className + ";", element, content);
				}
			}
			return true;
		}
		return false;
	}

	private boolean methodSignature(ReferencingElement element, String content, String objectName) {
		if (objectName.startsWith("(")
				&& objectName.indexOf(")") > 0
				&& (objectName.endsWith(";") || objectName.endsWith("V"))) {
			//We are probably looking at some method signature
			//(Ljava.lang.String;Ljava.lang.String;)Lsun.util.locale.BaseLocale;
			//()Lsun.util.locale.BaseLocale;
			//(Lsun.util.locale.BaseLocale;)V
			//Let's try to separate each class and process them independently
			String[] parameters = objectName.substring(1, objectName.indexOf(")")).split(";");
			for (String parameter : parameters) {
				if (parameter != null && !parameter.isBlank()) {
					fillReferencedElement(parameter, element, content);
				}
			}
			String returns = objectName.substring(objectName.indexOf(")") + 1);
			if (!returns.equals("V")) {
				fillReferencedElement(returns.substring(0, returns.length() - 1), element, content);
			}
			//And stop processing this
			return true;
		}
		return false;
	}
}
