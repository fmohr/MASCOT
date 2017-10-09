package de.upb.crc901.mascot;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.junit.Test;

import de.upb.crc901.configurationsetting.serialization.SerializationWrapper;
import de.upb.crc901.configurationsetting.serialization.SettingIORegistry;
import de.upb.crc901.configurationsetting.serialization.util.LiteralConverterUtil;
import de.upb.crc901.mascot.io.TemplateReaderImpl;
import de.upb.crc901.mascot.structure.Template;
import de.upb.crc901.mascot.template.benchmark.OnDemandInstantiatorBenchmarker;
import jaicore.logic.fol.structure.LiteralSet;
import jaicore.logic.fol.structure.TypeModule;
import jaicore.logic.fol.util.TypeUtil;

public class TemplateInstantiatorTest {
	private static final int TIMEOUT = 60000;

	private static final int NUMBER_OF_LEVELS_MIN = 1;
	private static final int NUMBER_OF_LEVELS_MAX = 50;
	private static final int NUMBER_OF_RUNS = 10;

	private static final int MAX_LENGTH = 4;
	private static final boolean ALLOW_DISJUNCTIONS = true;

	private final static String FILE_DIR = "testrsc/";
	private final static String statsFilename = "peak-" + (ALLOW_DISJUNCTIONS ? "cnf" : "monom") + "-" + MAX_LENGTH + "-4-notfound.txt";

	@Test
	public void testTemplateInstantiation() throws InterruptedException, ExecutionException, IOException {
		/* read operations, knowledge, and types */
		FileReader fileReader = new FileReader(FILE_DIR + "template.testset");
		SerializationWrapper parsedData = SettingIORegistry.getInstance().getParser("testset").parse(fileReader);
		
		TypeModule typeMod = parsedData.getCompositionDomain().getTypeModule();
		TypeUtil.setTypeModule(typeMod);
		Set<String> evaluablePredicates = parsedData.getCompositionDomain().getInterpretedLiteralNames();
		
		/* read templates */
		List<Template> templates = TemplateReaderImpl.readInFile(parsedData, FILE_DIR + "templates.txt");

		/* read query */
		LiteralSet query = null;
		try {
			BufferedReader queryReader = new BufferedReader(new FileReader(FILE_DIR + "query.txt"));
			String strLine = queryReader.readLine();
			query = LiteralConverterUtil.convertStringToLiteralSetWithConst(strLine, evaluablePredicates);
			TypeUtil.defineGodfatherDataTypes(query);
			queryReader.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		/* run benchmark */
		new OnDemandInstantiatorBenchmarker(parsedData, templates, query).runBenchmark(MAX_LENGTH, ALLOW_DISJUNCTIONS, NUMBER_OF_LEVELS_MIN, NUMBER_OF_LEVELS_MAX, NUMBER_OF_RUNS, TIMEOUT, statsFilename);
	}
}
