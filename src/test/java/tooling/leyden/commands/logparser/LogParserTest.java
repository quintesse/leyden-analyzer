package tooling.leyden.commands.logparser;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import tooling.leyden.aotcache.Element;
import tooling.leyden.commands.DefaultTest;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class LogParserTest extends DefaultTest {

	@ParameterizedTest
	@ValueSource(strings = {"aot.log", "aot.log.0", "aot.log.1"})
	void addElements(String file) throws Exception {
		File f = new File(getClass().getResource(file).getPath());
		getSystemRegistry().execute("load log " + f.getAbsolutePath());
		final var aotCache = getDefaultCommand().getInformation();

		//Now check individual values
		for (Element e : aotCache.getAll()) {
			assertNull(e.getAddress()); //Log doesn't provide this
			assertNotNull(e.getKey());
			assertNull(e.getSize()); //Log doesn't provide this
			assertNotNull(e.getType());
			assertNotNull(e.getWhereDoesItComeFrom());
			//Sometimes due to the order of the log,
			//we will have more than one source here
			assertTrue(e.getSources().size() > 0);
		}
	}

	@Test
	void addCreationWarnings() throws Exception {
		File f = new File(getClass().getResource("aot.log.0").getPath());
		File f2 = new File(getClass().getResource("aot.log.1").getPath());
		getSystemRegistry().execute("load log " + f.getAbsolutePath() + " " + f2.getAbsolutePath());
		final var aotCache = getDefaultCommand().getInformation();
		assertFalse(aotCache.getWarnings().isEmpty());
	}

	@Test
	void addLoadingLog() throws Exception {
		File f = new File(getClass().getResource("aot.log.loading").getPath());
		getSystemRegistry().execute("load log " + f.getAbsolutePath());

		final var aotCache = getDefaultCommand().getInformation();

		assertFalse(aotCache.getStatistics().getKeys().isEmpty());
		assertTrue(aotCache.getWarnings().isEmpty());
		assertFalse(aotCache.getAll().isEmpty());
		assertFalse(aotCache.getExternalElements().isEmpty());

		final var extClasses = Integer.valueOf(aotCache.getStatistics().getValue("[LOG] Classes not loaded from AOT Cache").toString());
		final var extMethods = Integer.valueOf(aotCache.getStatistics().getValue("[LOG] Methods not loaded from AOT Cache").toString());
		final var extLambdas = Integer.valueOf(aotCache.getStatistics().getValue("[LOG] Lambda Methods not loaded from AOT Cache").toString());
		final var classes = Integer.valueOf(aotCache.getStatistics().getValue("[LOG] Classes loaded from AOT Cache").toString());
		final var methods = Integer.valueOf(aotCache.getStatistics().getValue("[LOG] Methods loaded from AOT Cache").toString());
		final var lambdas = Integer.valueOf(aotCache.getStatistics().getValue("[LOG] Lambda Methods loaded from AOT Cache").toString());

		assertEquals(256, extClasses);
		assertEquals(1571, extMethods);
		assertEquals(1556, extLambdas);
		assertEquals(8601, classes);
		assertEquals(198, methods);
		assertEquals(197, lambdas);

		assertEquals(aotCache.getExternalElements().size(), extClasses + extMethods);
		assertEquals(aotCache.getElements(null, null, null, true, false, "Class").size(), classes);
		assertEquals(aotCache.getElements(null, null, null, true, true, "Class").size(), extClasses + classes);
//		assertEquals(aotCache.getElements(null, null, null, true, false, "Method").size(), methods);
//		assertEquals(aotCache.getElements(null, null, null, true, true, "Method").size(), extMethods + methods);
	}
}