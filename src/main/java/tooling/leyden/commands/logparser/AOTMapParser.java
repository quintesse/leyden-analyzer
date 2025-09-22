package tooling.leyden.commands.logparser;

import tooling.leyden.aotcache.AOTCache;
import tooling.leyden.aotcache.ClassObject;
import tooling.leyden.aotcache.ConstantPoolObject;
import tooling.leyden.aotcache.Element;
import tooling.leyden.aotcache.MethodObject;
import tooling.leyden.aotcache.BasicObject;
import tooling.leyden.aotcache.ReferencingElement;
import tooling.leyden.commands.LoadFile;

import java.util.List;
import java.util.function.Consumer;

/**
 * This class is capable of parsing the AOT logs that generate the AOT cache map.
 */
public class AOTMapParser implements Consumer<String> {

	private final LoadFile loadFile;
	private final AOTCache aotCache;

	public AOTMapParser(LoadFile loadFile) {
		this.loadFile = loadFile;
		this.aotCache = loadFile.getParent().getAotCache();
	}


	@Override
	public void accept(String content) {
		if (content.indexOf(": @@") != 18)
			return;

		if (!this.aotCache.getAll().isEmpty() && this.aotCache.getAll().size() % 10000 == 0) {
			this.loadFile.getParent().getOut().println("... processed " + this.aotCache.getAll().size() + " " +
					"elements from the AOT cache ...");
			this.loadFile.getParent().getOut().flush();
		}

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
//					0x0000000800868d58: @@ Class             520 java.lang.constant.ClassDesc
//					0x0000000800869078: @@ Class             512 [Ljava.lang.constant.ClassDesc;
				element = processClass(identifier);
			} else if (type.equalsIgnoreCase("Method")) {
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
				element = processReferencingElement(new ReferencingElement(), identifier);
			} else if (type.equalsIgnoreCase("ConstantPoolCache")) {
//					0x0000000800ec7408: @@ ConstantPoolCache 64 javax.naming.spi.ObjectFactory
				element = processReferencingElement(new ConstantPoolObject(), identifier);
				((ConstantPoolObject) element).setCache(true);
			} else if (type.equalsIgnoreCase("ConstantPool")) {
				element = processReferencingElement(new ConstantPoolObject(), identifier);
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
				element = new BasicObject();
			} else if (type.equalsIgnoreCase("Misc")) {
//					0x00000008049a8410: @@ Misc data 1985520 bytes
//					0x00000008049a8410:   0000000000000005 0000000801e563d0 0000000801e56600 0000000801e56420   .........c.......f...... d......
//					0x00000008049a8430:   0000000801e543a8 0000000801e548a8 0000000000000005 0000000801e58dc0   .C.......H................
				type = "Misc-data";
				size = Integer.valueOf(contentParts[4]);
				element = new BasicObject();
			} else {
				loadFile.getParent().getOut().println("Unidentified: " + type);
				loadFile.getParent().getOut().println(content);
				element = new BasicObject();
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

	private Element processReferencingElement(ReferencingElement e, String identifier) {
		e.setName(identifier);
		fillReferencedElement(identifier, e);
		return e;
	}

	private Element processMethod(String identifier, String thisSource) {
		// 0x000000080082ac80: @@ Method            88 char example.Class.example(long)
		// 0x0000000800773ea0: @@ Method            88 boolean java.lang.Object.equals(java.lang.Object)
		MethodObject methodObject = new MethodObject();
		String qualifiedName = identifier.substring(identifier.indexOf(" ") + 1, identifier.indexOf("("));
		methodObject.setName(qualifiedName.substring(qualifiedName.lastIndexOf(".") + 1));
		methodObject.setReturnType(identifier.substring(0, identifier.indexOf(" ")));
		String className = qualifiedName.substring(0, qualifiedName.lastIndexOf("."));
		List<Element> objects = this.aotCache.getObjects(className, "Class");
		if (objects.isEmpty()) {
			ClassObject classObject = new ClassObject();
			classObject.setName(className.substring(className.lastIndexOf(".") + 1));
			classObject.setPackageName(className.substring(0, className.lastIndexOf(".")));
			this.aotCache.addElement(classObject, thisSource);
		} else {
			Element element = objects.get(0);
			if (element instanceof ClassObject classObject) {
				//Should always be a class object, but...
				classObject.addMethod(methodObject);
			}
		}
		return methodObject;
	}

	private Element processClass(String identifier) {
		// 0x000000080082d490: @@ Class             760 java.lang.StackFrameInfo
		List<Element> elements = this.aotCache.getObjects(identifier, "Class");
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

	private void fillReferencedElement(String identifier, ReferencingElement element) {
		//In case we are referencing some class already loaded
		// Replace / for . because some elements use /
		List<Element> elements = this.aotCache.getObjects(identifier.replaceAll("/", "."), "Class");
		if (!elements.isEmpty()) {
			element.setReference(elements.get(0));
		}

		if (elements.size() > 1) {
			loadFile.getParent().getOut().println("ERROR: Review the code to assign referenced elements. " +
					"We have more than one possible match.");
			loadFile.getParent().getOut().println(element);
			for (Element e : elements) {
				loadFile.getParent().getOut().println(" > Potential match: " + e);
			}
		}
	}
}
