package tooling.leyden.commands.logparser;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import tooling.leyden.aotcache.ClassObject;
import tooling.leyden.aotcache.Element;
import tooling.leyden.aotcache.MethodObject;
import tooling.leyden.aotcache.ReferencingElement;
import tooling.leyden.commands.DefaultTest;
import tooling.leyden.commands.LoadFileCommand;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class AOTCacheParserTest extends DefaultTest {

	@Test
	void accept() throws Exception {
		File file = new File(getClass().getResource("aot.map").getPath());
		final var aotCache = getDefaultCommand().getAotCache();
		getSystemRegistry().execute("load aotCache " + file.getAbsolutePath());
		assertTrue(aotCache.getAll().size() > 0);
		assertEquals(0, aotCache.getWarnings().size());

		//Now check individual values
		//Skip classes because they may have been indirectly generated
		aotCache.getAll().parallelStream().filter(e -> !e.getType().equalsIgnoreCase("Class")).forEach(e -> {
			assertNotNull(e.getAddress(), "Address of " + e + " shouldn't be null.");
			assertNotNull(e.getKey(), "Key of " + e + " shouldn't be null.");
			assertNotNull(e.getSize(), "Size of " + e + " shouldn't be null.");
			assertNotNull(e.getType(), "Type of " + e + " shouldn't be null.");
			assertEquals(1, e.getSources().size(), "We shouldn't have more than one source here " + e.getSources().stream().reduce((s, s2) -> s + ", " + s2));
		});

		assertEquals(655, aotCache.getElements(null, null, null, true, "Symbol").size());
		assertEquals(114, aotCache.getElements(null, null, null, true, "ConstantPool").size());
		assertEquals(192, aotCache.getElements(null, null, null, true, "ConstantPoolCache").size());
		assertEquals(494 + 5, aotCache.getElements(null, null, null, true, "Class").size());
		assertEquals(5927, aotCache.getElements(null, null, null, true, "Method").size());
		assertEquals(1385, aotCache.getElements(null, null, null, true, "ConstMethod").size());
	}

	@Test
	void acceptObjectsWithReferences() throws Exception {
		final var loadFile = new LoadFileCommand();
		loadFile.setParent(getDefaultCommand());
		final var aotCache = loadFile.getParent().getAotCache();
		AOTMapParser aotCacheParser = new AOTMapParser(loadFile);

		var classObject = new ClassObject("java.lang.Float");
		aotCache.addElement(classObject, "test");

		classObject = new ClassObject("java.lang.String$CaseInsensitiveComparator");
		aotCache.addElement(classObject, "test");

		aotCacheParser.accept("0x00000000fff63458: @@ Object (0xfff63458) java.lang.String$CaseInsensitiveComparator");
		aotCacheParser.accept("0x00000000fff632f0: @@ Object (0xfff632f0) [I length: 0");
		aotCacheParser.accept("0x00000000fff62900: @@ Object (0xfff62900) java.lang.Float");

		assertEquals(5, aotCache.getAll().size());
		final var objects = aotCache.getElements(null, null, null, true, "Object");
		assertEquals(3, objects.size());
		for (Element e : objects) {
			assertTrue(e instanceof ReferencingElement);
			ReferencingElement re = (ReferencingElement) e;
			if (!re.getKey().equals("(0xfff632f0) [I length: 0")) {
				assertTrue(!re.getReferences().isEmpty());
				assertTrue(re.getKey().contains(re.getReferences().getFirst().getKey()));
			}
		}

	}

	@Test
	void acceptSymbol() throws Exception {
		final var loadFile = new LoadFileCommand();
		loadFile.setParent(getDefaultCommand());
		final var aotCache = loadFile.getParent().getAotCache();
		aotCache.clear();
		AOTMapParser aotCacheParser = new AOTMapParser(loadFile);

		var classObject = new ClassObject("java.security.InvalidAlgorithmParameterException");
		aotCache.addElement(classObject, "test");

		aotCacheParser.accept("0x00000008028dbaf0: @@ Symbol            48 InvalidAlgorithmParameterException.java");

		assertEquals(2, aotCache.getAll().size());
		assertEquals(1, aotCache.getElements(null, null, null, true, "Symbol").size());
		assertEquals(1, aotCache.getElements(null, null, null, true, "Class").size());
		for (Element e : aotCache.getElements(null, null, null, true, "Symbol")) {
			assertTrue(e instanceof ReferencingElement);
			ReferencingElement re = (ReferencingElement) e;
			assertNotEquals(0, re.getReferences().size());
		}

	}


	@Test
	void acceptObjectsWithExplicitReference() throws Exception {
		final var loadFile = new LoadFileCommand();
		loadFile.setParent(getDefaultCommand());
		final var aotCache = loadFile.getParent().getAotCache();
		aotCache.clear();
		AOTMapParser aotCacheParser = new AOTMapParser(loadFile);

		var classObject = new ClassObject("java.lang.String");
		aotCache.addElement(classObject, "test");


		aotCacheParser.accept("0x0000000800a8efe8: @@ Class             536 sun.util.locale.BaseLocale");
		aotCacheParser.accept("0x0000000800a8f258: @@ ConstantPoolCache 64 sun.util.locale.BaseLocale");

		aotCacheParser.accept("0x0000000800a8f3c8: @@ Class             512 [Lsun.util.locale.BaseLocale;");
		aotCacheParser.accept("0x0000000800a98270: @@ Class             568 sun.util.locale.BaseLocale$1");

		aotCacheParser.accept("0x0000000800a98500: @@ ConstantPoolCache 64 sun.util.locale.BaseLocale$1");

		aotCacheParser.accept("0x0000000801f74b70: @@ Symbol            32 sun/util/locale/BaseLocale");
		aotCacheParser.accept("0x0000000801f74bb0: @@ Symbol            40 [Lsun/util/locale/BaseLocale;");
		aotCacheParser.accept("0x0000000801f74bd8: @@ Symbol            72 (Lsun/util/locale/BaseLocale;Lsun/util/locale/LocaleExtensions;)V");
		aotCacheParser.accept("0x0000000801f74c78: @@ Symbol            40 Lsun/util/locale/BaseLocale;");
		aotCacheParser.accept("0x0000000801f74cf8: @@ Symbol            112 (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Lsun/util/locale/BaseLocale;");
		aotCacheParser.accept("0x0000000801f74e88: @@ Symbol            88 (Lsun/util/locale/BaseLocale;Lsun/util/locale/LocaleExtensions;)Ljava/util/Locale;");
		aotCacheParser.accept("0x0000000801f754d0: @@ Symbol            104 (Lsun/util/locale/BaseLocale;Lsun/util/locale/LocaleExtensions;)Lsun/util/locale/LanguageTag;");
		aotCacheParser.accept("0x0000000801f75708: @@ Symbol            40 ()Lsun/util/locale/BaseLocale;");
		aotCacheParser.accept("0x0000000801f76418: @@ Symbol            72 Ljava/util/Map<Lsun/util/locale/BaseLocale;Ljava/util/Locale;>;");
		aotCacheParser.accept("0x0000000801f77410: @@ Symbol            72 (Ljava/lang/String;Ljava/lang/String;)Lsun/util/locale/BaseLocale;");
		aotCacheParser.accept("0x0000000801f774b0: @@ Symbol            40 sun/util/locale/BaseLocale$1");
		aotCacheParser.accept("0x0000000801f77580: @@ Symbol            104 Ljava/util/function/Supplier<Ljdk/internal/util/ReferencedKeySet<Lsun/util/locale/BaseLocale;>;>;");
		aotCacheParser.accept("0x0000000801f77658: @@ Symbol            40 Lsun/util/locale/BaseLocale$1;");
		aotCacheParser.accept("0x0000000801f77680: @@ Symbol            80 ()Ljdk/internal/util/ReferencedKeySet<Lsun/util/locale/BaseLocale;>;");
		aotCacheParser.accept("0x0000000801f776d0: @@ Symbol            128 Ljava/lang/Object;Ljava/util/function/Supplier<Ljdk/internal/util/ReferencedKeySet<Lsun/util/locale/BaseLocale;>;>;");
		aotCacheParser.accept("0x000000080208db10: @@ Symbol            112 (Lsun/util/locale/BaseLocale;Lsun/util/locale/LocaleExtensions;)Lsun/util/locale/InternalLocaleBuilder;");
		aotCacheParser.accept("0x0000000802093c48: @@ Symbol            112 Ljdk/internal/util/ReferencedKeyMap<Lsun/util/locale/BaseLocale;Ljava/util/List<Ljava/util/Locale;>;>;");
		aotCacheParser.accept("0x0000000802093d78: @@ Symbol            56 (Lsun/util/locale/BaseLocale;)Ljava/util/List;");
		aotCacheParser.accept("0x0000000802093e38: @@ Symbol            72 (Lsun/util/locale/BaseLocale;)Ljava/util/List<Ljava/util/Locale;>;");

		aotCacheParser.accept("0x0000000802ff83e8: @@ ConstantPool      2456 sun.util.locale.BaseLocale");

		aotCacheParser.accept("0x00000000ffe06dc0: @@ Object (0xffe06dc0) [Lsun.util.locale.BaseLocale; length: 19");
		aotCacheParser.accept("0x00000000ffe06e20: @@ Object (0xffe06e20) sun.util.locale.BaseLocale");
		aotCacheParser.accept("0x00000000ffe06e58: @@ Object (0xffe06e58) sun.util.locale.BaseLocale");
		aotCacheParser.accept("0x00000000ffe06e90: @@ Object (0xffe06e90) sun.util.locale.BaseLocale");
		aotCacheParser.accept("0x00000000ffe94558: @@ Object (0xffe94558) java.lang.String \"sun.util.locale.BaseLocale\"");
		aotCacheParser.accept("0x00000000ffef4720: @@ Object (0xffef4720) java.lang.Class Lsun/util/locale/BaseLocale$1;");
		aotCacheParser.accept("0x00000000ffefd1e8: @@ Object (0xffefd1e8) java.lang.Class Lsun/util/locale/BaseLocale;");
		aotCacheParser.accept("0x00000000ffefd288: @@ Object (0xffefd288) java.lang.Class [Lsun/util/locale/BaseLocale;");

		assertEquals(1, aotCache.getElements(null, null, null, true, "ConstantPool").size());
		assertEquals(2, aotCache.getElements(null, null, null, true, "ConstantPoolCache").size());
		assertEquals(19, aotCache.getElements(null, null, null, true, "Symbol").size());
		assertEquals(8, aotCache.getElements(null, null, null, true, "Object").size());

		for (Element e : aotCache.getElements(null, null, null, true,
				"Object", "ConstantPoolCache", "ConstantPool")) {
			assertTrue(e instanceof ReferencingElement);
			ReferencingElement re = (ReferencingElement) e;
			assertNotEquals(0, re.getReferences().size(), e + " should have a reference");
		}

		assertEquals(3 + 1, aotCache.getElements(null, null, null, true, "Class").size());

		assertEquals(1 + 2 + 19 + 8 + 3 + 1, aotCache.getAll().size());

	}


	@Test
	void acceptMethodDataAndMethodCounters() throws Exception {
		final var loadFile = new LoadFileCommand();
		loadFile.setParent(getDefaultCommand());
		final var aotCache = loadFile.getParent().getAotCache();
		aotCache.clear();
		AOTMapParser aotCacheParser = new AOTMapParser(loadFile);

		aotCacheParser.accept("0x0000000800772d58: @@ Class             528 java.lang.Object");
		aotCacheParser.accept("0x0000000800799620: @@ Class             528 jdk.internal.misc.CDS");
		aotCacheParser.accept("0x0000000801d92878: @@ Method            88 void jdk.internal.misc.CDS.keepAlive(java.lang.Object)");
		aotCacheParser.accept("0x0000000801d928d0: @@ MethodData        568 void jdk.internal.misc.CDS.keepAlive(java.lang.Object)");
		aotCacheParser.accept("0x0000000801d92b08: @@ MethodCounters    64 void jdk.internal.misc.CDS.keepAlive(java.lang.Object)");
		aotCacheParser.accept("0x00000008048c5c60: @@ ConstMethod       152 void jdk.internal.misc.CDS.keepAlive(java.lang.Object)");

		aotCacheParser.accept("0x0000000801f5fb68: @@ MethodData        296 void java.lang.Long.<init>(long)");
		aotCacheParser.accept("0x0000000801f5fd90: @@ MethodData        288 void java.lang.Short.<init>(short)");
		aotCacheParser.accept("0x0000000801f6a670: @@ MethodData        672 int jdk.internal.util.ArraysSupport.hugeLength(int, int)");
		aotCacheParser.accept("0x0000000801f6a968: @@ MethodData        408 int jdk.internal.util.ArraysSupport.utf16hashCode(int, byte[], int, int)");
		aotCacheParser.accept("0x0000000801f6b188: @@ MethodData        328 int jdk.internal.util.ArraysSupport.hashCode(int, int[], int, int)");
		aotCacheParser.accept("0x0000000801f6b328: @@ MethodData        328 int jdk.internal.util.ArraysSupport.hashCode(int, short[], int, int)");
		aotCacheParser.accept("0x0000000801f6b4c8: @@ MethodData        328 int jdk.internal.util.ArraysSupport.hashCode(int, char[], int, int)");
		aotCacheParser.accept("0x0000000801f6f848: @@ MethodData        584 void java.util.ImmutableCollections$Set12.<init>(java.lang.Object, java.lang.Object)");
		aotCacheParser.accept("0x0000000801f895e0: @@ MethodCounters    64 java.util.Optional java.lang.VersionProps.optional()");
		aotCacheParser.accept("0x0000000801f89678: @@ MethodCounters    64 java.util.Optional java.lang.VersionProps.build()");
		aotCacheParser.accept("0x0000000801f89710: @@ MethodCounters    64 java.util.Optional java.lang.VersionProps.pre()");
		aotCacheParser.accept("0x0000000801f897a8: @@ MethodCounters    64 java.util.List java.lang.VersionProps.versionNumbers()");
		aotCacheParser.accept("0x0000000801f89840: @@ MethodCounters    64 java.util.Optional java.lang.VersionProps.optionalOf(java.lang.String)");
		aotCacheParser.accept("0x0000000801f898d8: @@ MethodCounters    64 java.util.List java.lang.VersionProps.parseVersionNumbers(java.lang.String)");

		var elements = aotCache.getElements(null, null, null, true, "MethodData");
		assertEquals(9, elements.size());
		for (Element e : elements) {
			assertTrue(((ReferencingElement) e).getReferences().size() > 0);
		}

		elements = aotCache.getElements(null, null, null, true, "MethodCounters");
		assertEquals(7, elements.size());
		for (Element e : elements) {
			assertTrue(((ReferencingElement) e).getReferences().size() > 0);
		}

		elements = aotCache.getElements("void jdk.internal.misc.CDS.keepAlive(java.lang.Object)",
				null, null, true, "Method");
		var method = elements.getFirst();
		assertNotNull(method.getClass());
		assertEquals("jdk.internal.misc.CDS", ((MethodObject)method).getClassObject().getKey());
		elements = aotCache.getElements("void jdk.internal.misc.CDS.keepAlive(java.lang.Object)",
				null, null, true, "ConstMethod", "MethodData", "MethodCounters");

		assertEquals(3, elements.size());
		for (Element e : elements) {
			assertTrue(((ReferencingElement) e).getReferences().contains(method));
		}
	}

}