package tooling.leyden.commands.logparser;

import tooling.leyden.aotcache.Information;
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
	private final Information information;

	public AOTMapParser(LoadFileCommand loadFile) {
		this.loadFile = loadFile;
		this.information = loadFile.getParent().getInformation();
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
				element = processClass(identifier, thisSource);
			} else if (type.equalsIgnoreCase("Method")) {
				//Metadata Method
//					0x0000000800831250: @@ Method            88 void java.lang.management.MemoryUsage.<init>(javax.management.openmbean.CompositeData)
// 					0x000000080082ac80: @@ Method            88 char example.Class.example(long)
//				 	0x0000000800773ea0: @@ Method            88 boolean java.lang.Object.equals(java.lang.Object)
				element = processMethod(identifier, thisSource);
			} else if (type.equalsIgnoreCase("ConstMethod")) {
//					 0x0000000804990600: @@ ConstMethod       88 void jdk.internal.access.SharedSecrets.setJavaNetHttpCookieAccess(jdk.internal.access.JavaNetHttpCookieAccess)
				element = processConstMethod(identifier);
			} else if (type.equalsIgnoreCase("Symbol")) {
//					0x0000000801e3c000: @@ Symbol            40 [Ljdk/internal/vm/FillerElement;
//					0x0000000801e3c028: @@ Symbol            32 jdk/internal/event/Event
//					0x0000000801e3c048: @@ Symbol            24 jdk/jfr/Event
//					0x0000000801e3c060: @@ Symbol            8 [Z
				element = processReferencingElement(new ReferencingElement(), identifier, content);
			} else if (type.equalsIgnoreCase("MethodCounters")
//					0x0000000801e4c280: @@ MethodCounters    64
//					0x0000000801e4c280:   0000000800001800 0000000000000002 0000000801e4c228 0000000801e4c280   ................(...............
//					0x0000000801e4c2a0:   0000000000000000 000000fe00000000 00000000000007fe 0000000000000000   ................................
					|| type.equalsIgnoreCase("MethodData")) {
//					0x0000000801f54000: @@ MethodData        496 java.lang.Class sun.invoke.util.Wrapper.wrapperType(java.lang.Class)
//					0x0000000801f5fb68: @@ MethodData        296 void java.lang.Long.<init>(long)
//					0x0000000801f6b328: @@ MethodData        328 int jdk.internal.util.ArraysSupport.hashCode(int, short[], int, int)
//					0x0000000801f6f848: @@ MethodData        584 void java.util.ImmutableCollections$Set12.<init>(java.lang.Object, java.lang.Object)
				element = processMethodDataAndCounter(content, identifier, address, type);
			} else if (type.equalsIgnoreCase("ConstantPoolCache")) {
//					0x0000000800ec7408: @@ ConstantPoolCache 64 javax.naming.spi.ObjectFactory
				element = processConstantPoolCache(identifier, content, address);
				type = "ConstantPool";
			} else if (type.equalsIgnoreCase("ConstantPool")) {
				element = processConstantPool(identifier, content);
			} else if (type.equalsIgnoreCase("KlassTrainingData")) {
//					0x0000000801bc7e40: @@ KlassTrainingData 40 java.util.logging.LogManager
//					0x0000000801bc8968: @@ KlassTrainingData 40 java.lang.invoke.MethodHandleImpl$IntrinsicMethodHandle
//					0x0000000801bcb1a8: @@ KlassTrainingData 40 java.lang.classfile.AttributeMapper$AttributeStability
				element = processKlassTrainingData(new ReferencingElement(), identifier, address);
			} else if (type.equalsIgnoreCase("MethodTrainingData")) {
//					0x0000000801bc7e40: @@ KlassTrainingData 40 java.util.logging.LogManager
//					0x0000000801bc8968: @@ KlassTrainingData 40 java.lang.invoke.MethodHandleImpl$IntrinsicMethodHandle
//					0x0000000801bcb1a8: @@ KlassTrainingData 40 java.lang.classfile.AttributeMapper$AttributeStability
				element = processMethodTrainingData(new ReferencingElement(), identifier, address);
			} else if (type.equalsIgnoreCase("CompileTrainingData")) {
//					0x0000000801cd54b8: @@ CompileTrainingData 80 4 void java.lang.ref.Reference.reachabilityFence(java.lang.Object)
//					0x0000000801cd5508: @@ CompileTrainingData 80 3 void java.lang.ref.Reference.reachabilityFence(java.lang.Object)
//					0x0000000801cd5558: @@ CompileTrainingData 80 3 java.lang.AbstractStringBuilder java.lang.AbstractStringBuilder.append(java.lang.String)
				element = processCompileTrainingData(new ReferencingElement(), identifier, address);
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
			) {
				element = new BasicObject(identifier.isBlank() ? address : identifier);
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
				this.information.addAOTCacheElement(element, thisSource);
			}
		} catch (Exception e) {
			loadFile.getParent().getOut().println("ERROR: " + e.getMessage());
			loadFile.getParent().getOut().println("ERROR: " + content);
		}
	}

	private Element processMethodDataAndCounter(String content, String identifier, String address, String type) {
		ReferencingElement result = new ReferencingElement();
		if (!identifier.isBlank()) {
			result.setName(identifier);
			MethodObject method;
			var methods = this.information.getElements(identifier, null, null, true, false, "Method");
			if (!methods.isEmpty()) {
				method = (MethodObject) methods.getFirst();
			} else {
				method = new MethodObject(identifier, "Referenced by '" + content + "'", false, this.information);
			}
			result.addReference(method);

			if (type.equalsIgnoreCase("MethodData")) {
				method.setMethodData(result);
			} else {
				method.setMethodCounters(result);
			}
		} else {
			result.setName(address);
		}
		return result;
	}

	private Element processConstantPoolCache(String identifier, String content, String address) {
		//Look for an existing constant pool object
		//Usually we get the ConstantPoolCache before the ConstantPool
		//So this should not find anything
		ConstantPoolObject e = null;
		var cps = this.information.getElements(identifier, null, null, true, true, "ConstantPool");
		for (Element cp : cps) {
			e = (ConstantPoolObject) cp;
			break;
		}

		//If not found, create it
		if (e == null) {
			e = (ConstantPoolObject) processConstantPool(identifier, content);
			this.information.addExternalElement(e, "Referenced by a ConstantPoolCache.");
		}

		//And assign the ConstantPoolCache address to the element
		e.setConstantPoolCacheAddress(address);
		return e;
	}

	private Element processConstantPool(String identifier, String content) {
		var cps = this.information.getElements(identifier, null, null, true, true, "ConstantPool");
		for (Element cp : cps) {
			return processReferencingElement((ReferencingElement) cp, identifier, content);
		}
		return processReferencingElement(new ConstantPoolObject(), identifier, content);
	}


	private Element processReferencingElement(ReferencingElement e, String identifier, String content) {
		e.setName(identifier);
		fillReferencedElement(identifier, e, content);
		return e;
	}

	private Element processKlassTrainingData(ReferencingElement e, String identifier, String address) {
		//0x0000000801bb4950: @@ KlassTrainingData 40 java.nio.file.Files$AcceptAllFilter
		//0x0000000801bbd700: @@ KlassTrainingData 40 sun.util.calendar.ZoneInfo

		//Looking for the Class
		if (!identifier.isBlank()) {
			var classes = this.information.getElements(identifier, null, null, true, true, "Class");
			if (classes.isEmpty()) {
				if (!identifier.isBlank()) {
					ClassObject classObject = new ClassObject(identifier);
					e.getReferences().add(classObject);
					this.information.addExternalElement(classObject, "Referenced from a KlassTrainingData.");
				}
			} else {
				e.getReferences().add(classes.getFirst());
			}

			e.setName(identifier);
		} else {
			e.setName(address);
		}

		return e;
	}

	private Element processMethodTrainingData(ReferencingElement e, String identifier, String address) {
		//0x0000000801c4d7a8: @@ MethodTrainingData 96 void java.util.concurrent.atomic.AtomicLong.lazySet(long)

		//Looking for the Method
		if (!identifier.isBlank()) {
			var methods = this.information.getElements(identifier, null, null, true, true, "Method");
			MethodObject method;
			if (methods.isEmpty()) {
				String source = "Referenced from a MethodTrainingData: " + address;
				method = new MethodObject(identifier, source, true, this.information);
				this.information.addExternalElement(method, source);
			} else {
				method = (MethodObject) methods.getFirst();
			}
			e.getReferences().add(method);
			method.setMethodTrainingData(e);
			e.setName(identifier);
		} else {
			e.setName(address);
		}

		return e;
	}



	private Element processCompileTrainingData(ReferencingElement e, String content, String address) {
		// 0x0000000801a41200: @@ CompileTrainingData 80 3 org.apache.logging.log4j.spi.LoggerContext org.apache.logging.log4j.LogManager.getContext(boolean)

		content = content.trim();

		//Looking for the Method
		if (!content.isBlank()) {
			Integer level = Integer.valueOf(content.substring(0, 1));
			String identifier = content.substring(2);
			var methods = this.information.getElements(identifier, null, null, true, true, "Method");
			MethodObject method;
			if (methods.isEmpty()) {
				String source = "Referenced from a CompiledTrainingData: " + address;
				method = new MethodObject(identifier, source, true, this.information);
				this.information.addExternalElement(method, source);
			} else {
				method = (MethodObject) methods.getFirst();
			}
			e.getReferences().add(method);
			method.getCompileTrainingData().put(level, e);
			e.setName(content);
		} else {
			e.setName(address);
		}

		return e;
	}

	private Element processClass(String identifier, String thisSource) {
		// 0x000000080082d490: @@ Class             760 java.lang.StackFrameInfo
		// It could have been already loaded
		for (Element element : this.information.getElements(identifier, null, null, true, true, "Class")) {
			element.getSources().add(thisSource);
			return element;
		}
		ClassObject classObject = new ClassObject(identifier);

		if (identifier.contains("$$")) {
			//Lambda class, link to main outer class
			String parent = identifier.substring(0, identifier.indexOf("$$"));
			final var parentClass = this.information.getElements(parent, null, null, true, false, "Class");
			if (parentClass.isEmpty()) {
				ClassObject parentObject = new ClassObject(parent);
				information.addExternalElement(parentObject, "Referenced from a Class element in " + thisSource);
			} else {
				classObject.getReferences().add(parentClass.getFirst());
			}
		}

		return classObject;
	}

	private Element processMethod(String identifier, String thisSource) {
		// It could have been already loaded
		for (Element element : this.information.getElements(identifier, null, null, true, false, "Method")) {
			return element;
		}
		return new MethodObject(identifier, thisSource, false, this.information);
	}

	private Element processConstMethod(String identifier) {
		// It could have been already loaded
		for (Element element : this.information.getElements(identifier, null, null, true, false, "ConstMethod")) {
			return element;
		}

		BasicObject constMethod = new BasicObject(identifier);

		//Which Method do we belong to?
		boolean methodExists = false;
		for (Element element : this.information.getElements(identifier, null, null, true, false, "Method")) {
			((MethodObject) element).setConstMethod(constMethod);
			methodExists = true;
		}
		if (!methodExists) {
			MethodObject method = new MethodObject(identifier, "Referenced by ConstMethod in AOT Cache", true, this.information);
			method.setConstMethod(constMethod);
			this.information.addExternalElement(method, "Referenced by ConstMethod in AOT Cache");
		}

		return constMethod;
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
			for (Element e : this.information.getElements("java.lang.Class", new String[]{"java.lang"}, null, true, false,
					"Class")) {
				element.addReference(e);
			}

			//Now look for the class itself
			objectName = objectName.substring(16).trim();
		}

		//Now try to locate the class itself
		if (!objectName.isBlank()) {
			List<Element> elements = this.information.getElements(objectName, null, null, true, false, "Class");
			if (!elements.isEmpty()) {
				elements.forEach(e -> element.addReference(e));
			} else {
				//Maybe we are looking for a Symbol
				for (Element e : this.information.getElements(
						objectName.replaceAll("\\.", "/"), null, null, true, false,
						"Symbol")) {
					element.addReference(e);
				}
			}
		}
	}

	private boolean literalString(ReferencingElement element, String objectName) {
		if (objectName.trim().startsWith("java.lang.String ")) {
			for (Element e : this.information.getElements("java.lang.String", new String[]{"java.lang"}, null, true, false,
					"Class")) {
				element.addReference(e);
			}
			//Coming from an Object, we are looking to reference a Symbol
			if (objectName.length() > 18) {
				//avoid empty strings
				final var name = objectName.substring(18, objectName.length() - 1);
				if (!name.isBlank()) {
					for (Element e : this.information.getElements(
							name, null, null, true, false,
							"Symbol")) {
						element.addReference(e);
					}
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
			for (Element el : this.information.getClassesByName(objectName)) {
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
			for (Element e : this.information.getElements(objectName, null, null, true, false, "Method")) {
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
