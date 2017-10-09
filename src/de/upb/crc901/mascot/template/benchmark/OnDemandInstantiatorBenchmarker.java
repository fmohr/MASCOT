package de.upb.crc901.mascot.template.benchmark;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.upb.crc901.configurationsetting.discovery.OperationRepository;
import de.upb.crc901.configurationsetting.serialization.SerializationWrapper;
import de.upb.crc901.configurationsetting.serialization.util.LiteralConverterUtil;
import de.upb.crc901.mascot.structure.Template;
import de.upb.crc901.mascot.template.instantiation.InstantiationEnvironment;
import de.upb.crc901.mascot.template.instantiation.TemplateInstantiation;
import de.upb.crc901.mascot.template.instantiator.OnDemandInstantiator;
import jaicore.basic.PerformanceLogger;
import jaicore.basic.StringUtil;
import jaicore.basic.PerformanceLogger.PerformanceMeasure;
import jaicore.logic.fol.structure.HornFormula;
import jaicore.logic.fol.structure.HornRule;
import jaicore.logic.fol.structure.Literal;
import jaicore.logic.fol.structure.LiteralSet;
import jaicore.logic.fol.structure.Monom;
import jaicore.logic.fol.structure.TypeModule;
import jaicore.logic.fol.util.TypeUtil;

public class OnDemandInstantiatorBenchmarker {

	private static final Logger l = LoggerFactory.getLogger(OnDemandInstantiatorBenchmarker.class);
	private OperationRepository repository;
	private TypeModule typeMod;
	private Set<String> evaluablePredicates;
	private List<Template> templates;
	private LiteralSet query;
	private HornFormula knowledge;
	private final ExecutorService pool = Executors.newSingleThreadExecutor();

	public OnDemandInstantiatorBenchmarker(SerializationWrapper parsedSetting, List<Template> pTemplates, LiteralSet pQuery) {

		repository = parsedSetting.getOperationRepository();
		typeMod = parsedSetting.getCompositionDomain().getTypeModule();
		knowledge = parsedSetting.getCompositionDomain().getKnowledgeModule().asHornFormula();
		TypeUtil.setTypeModule(typeMod);

		/* read evaluable predicates */
		evaluablePredicates = parsedSetting.getCompositionDomain().getInterpretedLiteralNames();

		templates = pTemplates;

		/* read query */
		query = pQuery;
	}

	private class BenchmarkRun implements Callable<TemplateInstantiation> {

		private final int maxlength;
		private final boolean allowdisjunctions;
		private final boolean allownegations;
		private final int level;
		private final char[] chars;

		public BenchmarkRun(int maxlength, boolean allowdisjunctions, boolean allownegations, int level, char[] chars) {
			super();
			this.maxlength = maxlength;
			this.allowdisjunctions = allowdisjunctions;
			this.allownegations = allownegations;
			this.level = level;
			this.chars = chars;
		}

		public TemplateInstantiation call() {

			HornFormula extendedKnowledgeBase = new HornFormula();
			extendedKnowledgeBase.addAll(knowledge);

			/* now add dummy rules to the knowledge base */
			for (int j = 0; j < level * 10; j++) {
				Monom premise = new Monom();
				String name = StringUtil.getRandomString(8, chars);
				premise.add(LiteralConverterUtil.convertStringToLiteralWithConst(name + j + "(x,y)", evaluablePredicates));
				TypeUtil.defineGodfatherDataTypes(premise);

				Literal conclusion = LiteralConverterUtil.convertStringToLiteralWithConst(name + j + "(y,x)", evaluablePredicates);
				TypeUtil.defineGodfatherDataTypes(conclusion);
				extendedKnowledgeBase.add(new HornRule(premise, conclusion));

			}
			InstantiationEnvironment env = new InstantiationEnvironment(templates, repository, extendedKnowledgeBase, typeMod);

			PerformanceLogger.logStart("Level " + level);
			OnDemandInstantiator instantiator = new OnDemandInstantiator(env, evaluablePredicates, maxlength, allowdisjunctions, allownegations);
			try {
				Collection<TemplateInstantiation> results = instantiator.instantiate(query);
				PerformanceLogger.logEnd("Level " + level);
				return results != null ? results.iterator().next() : null;
			} catch (InterruptedException e) {
				PerformanceLogger.logEnd("Level " + level);
				return null;
			}
		}
	}
	
	private void runPreparation(int maxLength, boolean pAllowDisjunctions, int level, int timeout) {
		char[] chars = StringUtil.getCommonChars(false);
		Future<TemplateInstantiation> result = pool.submit(new BenchmarkRun(maxLength, pAllowDisjunctions, false, level + 1, chars));
		try {
			l.info("Finished initial run with result: {}", result.get(timeout, TimeUnit.MILLISECONDS));
		} catch (InterruptedException | TimeoutException e) {
			result.cancel(true);
		} catch (ExecutionException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public void runBenchmark(int maxlength, boolean pAllowDisjunctions, int minlevels, int maxlevels, int runs, int timeout, String statsFilename) {
		char[] chars = StringUtil.getCommonChars(false);
		PerformanceLogger.clearStatsFile(statsFilename);
		runPreparation(maxlength, pAllowDisjunctions, maxlevels, timeout);
		for (int level = maxlevels; level >= minlevels; level--) {
			for (int run = 0; run < runs; run++) {

				Future<TemplateInstantiation> result = pool.submit(new BenchmarkRun(maxlength, pAllowDisjunctions, false, level, chars));
				try {
					l.info("Found results: {}", result.get(timeout, TimeUnit.MILLISECONDS));
				} catch (InterruptedException | TimeoutException e) {
					result.cancel(true);
				} catch (ExecutionException e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
			PerformanceLogger.getInstance().printStats("Level " + level, PerformanceMeasure.TIME);
			PerformanceLogger.getInstance().writeStatsOfTagToFile("Level " + level, statsFilename, true);
			PerformanceLogger.clearLog();
		}
		pool.shutdown();
	}
}
