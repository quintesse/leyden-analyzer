package tooling.leyden.commands.logparser;

import tooling.leyden.aotcache.AOTCache;
import tooling.leyden.aotcache.ClassObject;
import tooling.leyden.aotcache.ConstantPoolCacheObject;
import tooling.leyden.aotcache.ConstantPoolObject;
import tooling.leyden.aotcache.Element;
import tooling.leyden.aotcache.MethodObject;
import tooling.leyden.aotcache.RecordObject;
import tooling.leyden.aotcache.ReferencingElement;
import tooling.leyden.aotcache.SymbolObject;
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
		if (content.indexOf(": @@") == 18) {
			if (!this.aotCache.getAll().isEmpty() && this.aotCache.getAll().size() % 5000 == 0) {
				this.loadFile.getParent().getOut().println("Already processed " + this.aotCache.getAll().size() + " " +
						"elements.");
				this.loadFile.getParent().getOut().flush();
			}
			try {
				final var thisSource = "AOT Map";
				String[] contentParts = content.split(" +");

				final var address = contentParts[0].substring(0, contentParts[0].length() - 1);
				final var type = contentParts[2];
				final var size = Integer.valueOf(contentParts[3]);
				final var identifier =
						content.substring(content.indexOf(" " + contentParts[3]) + 1 + contentParts[3].length()).trim();
				
				if (type.equalsIgnoreCase("Class")) {
					processClass(identifier, address, size, thisSource);
				} else if (type.equalsIgnoreCase("Method")) {
					processMethod(identifier, size, thisSource);
				} else if (type.equalsIgnoreCase("Symbol")) {
//					0x0000000801e3c000: @@ Symbol            40 [Ljdk/internal/vm/FillerElement;
//					0x0000000801e3c028: @@ Symbol            32 jdk/internal/event/Event
//					0x0000000801e3c048: @@ Symbol            24 jdk/jfr/Event
//					0x0000000801e3c060: @@ Symbol            8 [Z
					processReferencingElement(new SymbolObject(), identifier, address, size, thisSource);
				} else if (type.equalsIgnoreCase("ConstantPoolCache")) {
//0x0000000800ec7408: @@ ConstantPoolCache 64 javax.naming.spi.ObjectFactory
					processReferencingElement(new ConstantPoolCacheObject(), identifier, address, size, thisSource);
				} else if (type.equalsIgnoreCase("ConstantPool")) {
					processReferencingElement(new ConstantPoolObject(), identifier, address, size, thisSource);
				} else if (type.startsWith("TypeArray")) {
					// 0x0000000800001d80: @@ TypeArrayU1       600
					//TODO typeArray
//					SymbolObject symbol = new SymbolObject();
//					symbol.setName(identifier);
//					symbol.setAddress(address);
//					this.aotCache.addElement(symbol);
				} else if (type.startsWith("Object (")) {
					//TODO Object
//					SymbolObject symbol = new SymbolObject();
//					symbol.setName(identifier);
//					symbol.setAddress(address);
//					this.aotCache.addElement(symbol);
				} else if (type.equalsIgnoreCase("ConstMethod")) {
					//TODO 0x0000000804990600: @@ ConstMethod       88 void jdk.internal.access.SharedSecrets.setJavaNetHttpCookieAccess(jdk.internal.access.JavaNetHttpCookieAccess)
//					SymbolObject symbol = new SymbolObject();
//					symbol.setName(identifier);
//					symbol.setAddress(address);
//					this.aotCache.addElement(symbol);
				} else if (type.equalsIgnoreCase("Annotations")) {
					//TODO 0x0000000802ef37b8: @@ Annotations       32
//					SymbolObject symbol = new SymbolObject();
//					symbol.setName(identifier);
//					symbol.setAddress(address);
//					this.aotCache.addElement(symbol);
				} else if (type.equals("RecordComponent")) {
					//TODO 0x00000008029329e8: @@ RecordComponent   24
					RecordObject recordObject = new RecordObject();
					recordObject.setAddress(address);
					this.aotCache.addElement(recordObject, thisSource);
				} else if (type.startsWith("AdapterHandlerEnt")) {
					//TODO 0x0000000801dfbea0: @@ AdapterHandlerEntry 48
//					SymbolObject symbol = new SymbolObject();
//					symbol.setName(identifier);
//					symbol.setAddress(address);
//					this.aotCache.addElement(symbol);
				} else if (type.startsWith("AdapterFingerPrin")) {
					//TODO 0x0000000800002ba0: @@ AdapterFingerPrint 8
//					SymbolObject symbol = new SymbolObject();
//					symbol.setName(identifier);
//					symbol.setAddress(address);
//					this.aotCache.addElement(symbol);
				} else {
					loadFile.getParent().getOut().println("Unidentified: " + type);
					loadFile.getParent().getOut().println(identifier);
					loadFile.getParent().getOut().println(content);
				}
			} catch (Exception e) {
				loadFile.getParent().getOut().println("ERROR: " + e.getMessage());
				loadFile.getParent().getOut().println("ERROR: " + content);
			}
		}
	}

	private void processReferencingElement(ReferencingElement e, String identifier, String address, Integer size, String thisSource) {
		e.setName(identifier);
		e.setSize(size);
		e.setAddress(address);
		fillReferencedElement(identifier, e);
		this.aotCache.addElement(e, thisSource);
	}

	private void processMethod(String identifier, Integer size, String thisSource) {
		// 0x000000080082ac80: @@ Method            88 char example.Class.example(long)
		// 0x0000000800773ea0: @@ Method            88 boolean java.lang.Object.equals(java.lang.Object)
		MethodObject methodObject = new MethodObject();
		methodObject.setSize(size);
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
		this.aotCache.addElement(methodObject, thisSource);
	}

	private void processClass(String identifier, String address, Integer size, String thisSource) {
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
			classObject.setSize(size);
			classObject.setName(identifier.substring(identifier.lastIndexOf(".") + 1));
			if (identifier.indexOf(".") > 0) {
				classObject.setPackageName(identifier.substring(0, identifier.lastIndexOf(".")));
			}
			this.aotCache.addElement(classObject, thisSource);
		}

		classObject.setAddress(address);
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
