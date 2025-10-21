package tooling.leyden.commands;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import tooling.leyden.aotcache.Warning;
import tooling.leyden.commands.logparser.AOTMapParser;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class WarningCommandTest extends DefaultTest {

	@Test
	void checkUsedAndNotTrained() {
		final var loadFile = new LoadFileCommand();
		loadFile.setParent(getDefaultCommand());
		AOTMapParser aotCacheParser = new AOTMapParser(loadFile);

		aotCacheParser.accept("0x0000000801711128: @@ Class             624 org.infinispan.xsite.NoOpBackupSender");

		//Method not called
		aotCacheParser.accept("0x00000008017116c0: @@ Method            88 org.infinispan.xsite.NoOpBackupSender org.infinispan.xsite.NoOpBackupSender.getInstance()");

		//Method with no compiledTrainingData
		aotCacheParser.accept("0x0000000801711610: @@ Method            88 void org.infinispan.xsite.NoOpBackupSender.<init>()");
		aotCacheParser.accept("0x0000000801b3d568: @@ MethodTrainingData 96 org.infinispan.xsite.NoOpBackupSender org.infinispan.xsite.NoOpBackupSender.getInstance()");
		aotCacheParser.accept("0x0000000801b3d5f0: @@ MethodData        240 org.infinispan.xsite.NoOpBackupSender org.infinispan.xsite.NoOpBackupSender.getInstance()");
		aotCacheParser.accept("0x0000000801b3d6e0: @@ MethodCounters    64 org.infinispan.xsite.NoOpBackupSender org.infinispan.xsite.NoOpBackupSender.getInstance()");
		aotCacheParser.accept("0x000000080429e780: @@ ConstMethod       64 org.infinispan.xsite.NoOpBackupSender org.infinispan.xsite.NoOpBackupSender.getInstance()");

		WarningCommand warningCommand = new WarningCommand();
		warningCommand.parent = getDefaultCommand();
		List<Warning> warningList = warningCommand.getTopPackagesUsedAndNotTrained(100);
		assertEquals(1, warningList.size());

		//Now we add the compiledTrainingData
		aotCacheParser.accept("0x0000000801cd5648: @@ CompileTrainingData 80 1 org.infinispan.xsite.NoOpBackupSender org.infinispan.xsite.NoOpBackupSender.getInstance()");

		assertTrue(warningCommand.getTopPackagesUsedAndNotTrained(100).isEmpty());
	}
}