package tooling.leyden.commands.logparser;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import tooling.leyden.commands.DefaultTest;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class LogParserTest extends DefaultTest {

	@ParameterizedTest
	@ValueSource(strings = {"aot.log", "aot.log.0", "aot.log.1"})
	void accept(String file) throws Exception {
		File f = new File(getClass().getResource(file).getPath());
		getSystemRegistry().execute("load log " + f.getAbsolutePath());
		assertTrue(getDefaultCommand().getAotCache().getAll().size() > 0);
	}
}