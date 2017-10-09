package de.upb.crc901.mascot.template.instantiator.searches;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.ISolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.upb.crc901.mascot.logic.DimacsCNF;
import de.upb.crc901.mascot.logic.DimacsFormulaStream;
import de.upb.crc901.mascot.structure.Template;
import de.upb.crc901.mascot.template.instantiation.InstantiationUtil;
import gnu.trove.set.hash.TIntHashSet;
import jaicore.basic.PerformanceLogger;
import jaicore.basic.SetUtil;
import jaicore.logic.fol.structure.HornRule;
import jaicore.logic.fol.structure.Literal;

public class FormulaSearch {

	private static final Logger logger = LoggerFactory.getLogger(FormulaSearch.class);
	private final Template t;
	private final Set<VecInt> domainKnowledgeInPropLogic;
	private final TIntHashSet propositionsThatOccurInDomainKnowledge;
	private final TIntHashSet evaluablePredicates;
	private final ISolver solver = SolverFactory.newLight();
	private final Set<String> namesOfPredicatesOccuringInTemplateConstraints;
	private final Set<String> namesOfPredicatesNotBoundInPsi1;
	private final List<String> namesOfPredicatesToBind;
	private final Set<String> namesOfGenericBooleanExpressions;
	private final Set<String> namesOfGenericServiceDescriptions;
	private final Map<String,DimacsFormulaStream> streamsForPossibleFormulasOverConcretePredicateNames = new HashMap<>();

	/**
	 * This queue contains all possible (remaining) combinations of range sizes for the formulas bound to the abstract predicates.
	 */
	private final Queue<List<Integer>> rangeSizeCombinationQueue;

	/**
	 * This queue contains all possible (remaining) mappings for the currently considered range size combination
	 */
	private final Queue<PsiProp> range = new LinkedList<>();

	/**
	 * propositional logical mapping of psi1 (every vecint should contain 1 element)
	 */
	private final PsiProp psi1prop;

	/**
	 * cache of solutions, which is important to prune!
	 */
	private final Set<PsiProp> solutionsFound = new HashSet<>();

	public FormulaSearch(Template t, Set<VecInt> domainKnowledgeInPropLogic, TIntHashSet pEvaluablePredicates, PsiProp psi1prop, int maxLength, boolean allowDisjunctions, boolean allowNegations) throws InterruptedException {
		super();
		this.t = t;
		this.domainKnowledgeInPropLogic = domainKnowledgeInPropLogic;
		this.evaluablePredicates = pEvaluablePredicates;
		this.psi1prop = psi1prop;
		this.namesOfPredicatesNotBoundInPsi1 = new HashSet<>(SetUtil.difference(t.getGenericLiterals().stream().map(l -> l.getPropertyName()).collect(Collectors.toSet()), psi1prop
				.keySet()));
		this.namesOfPredicatesOccuringInTemplateConstraints = InstantiationUtil.getNamesOfAbstractPredicatesInPositiveConsistencyRules(t); 
		this.propositionsThatOccurInDomainKnowledge = new TIntHashSet();
		for (VecInt clause : domainKnowledgeInPropLogic) {
			for (int i = 0; i < clause.size(); i++) {
				this.propositionsThatOccurInDomainKnowledge.add(clause.get(i));
			}
		}
		this.namesOfPredicatesToBind = new ArrayList<>(SetUtil.intersection(namesOfPredicatesOccuringInTemplateConstraints, namesOfPredicatesNotBoundInPsi1));
		this.namesOfGenericBooleanExpressions = InstantiationUtil.getNamesOfGenericExpressions(t);
		this.namesOfGenericServiceDescriptions = InstantiationUtil.getNamesOfEffectPredicates(t);
		
		/* invoke preprocessing */
		preprocessing(maxLength, allowDisjunctions, allowNegations);

		/* create range size combination queue */
		Set<Integer> sizes = new HashSet<>();
		for (int i = 1; i <= maxLength; i++)
			sizes.add(i);
		rangeSizeCombinationQueue = new LinkedList<>(SetUtil.cartesianProduct(sizes, namesOfPredicatesToBind.size()).stream()
				.sorted((v1, v2) -> v1.stream().mapToInt(Integer::intValue).sum() - v2.stream().mapToInt(Integer::intValue).sum()).collect(Collectors.toList()));
	}
	
	private void preprocessing(int maxlength, boolean disjunctionsAllowed, boolean negationAllowed) {
		
		/* preliminaries */
		Set<String> boundPredicates = psi1prop.keySet();
		
		/* compute set of all propositions in domain knowledge */
		Collection<TIntHashSet> background = new ArrayList<>();
		background.add(propositionsThatOccurInDomainKnowledge);
		
		/* now compute relevant concrete predicates for each rule */
		AdmissiblePredicateSearch predicateSearch = new AdmissiblePredicateSearch();
		Map<HornRule,Collection<TIntHashSet>> literalsPerRuleThatMayBeBeneficial = new HashMap<>();
		Map<HornRule,Set<String>> literalNamesOccurringInRules = new HashMap<>();
		for (HornRule r : t.getPositiveConsistencyRules()) {
			
			/* is the conclusion of this rule already bound? */
			if (boundPredicates.contains(r.getConclusion().getPropertyName())) {
				
				int conclusion = psi1prop.get(r.getConclusion().getPropertyName()).iterator().next().get(0);
				
				/* then compute other knowledge already available */
				DimacsCNF premise = new DimacsCNF();
				for (Literal l : r.getPremise()) {
					String name = l.getPropertyName();
					if (boundPredicates.contains(name))
						premise.addAll(psi1prop.get(name));
				}
				literalsPerRuleThatMayBeBeneficial.put(r, predicateSearch.getMinimalSetsOfPredicatesNecessaryToCompleteRule(premise, conclusion, new DimacsCNF(domainKnowledgeInPropLogic)));
			}
			else {
				literalsPerRuleThatMayBeBeneficial.put(r, background);
			}
			
			/* add literal names to the map */
			Set<String> literals = new HashSet<>();
			literalNamesOccurringInRules.put(r, literals);
			literals.addAll(r.getPremise().stream().map(l -> l.getPropertyName()).collect(Collectors.toList()));
			literals.add(r.getConclusion().getPropertyName());
		}
		
		/* now compute relevant concrete predicates for each abstract predicate */
		for (String unboundAbstractPredicate : namesOfPredicatesToBind) {
			boolean isDescriptor = namesOfGenericServiceDescriptions.contains(unboundAbstractPredicate);
			boolean isBooleanExpr = namesOfGenericBooleanExpressions.contains(unboundAbstractPredicate);
			List<Integer> relevantpredicates = new ArrayList<>();
			for (HornRule r : t.getPositiveConsistencyRules()) {
				if (literalNamesOccurringInRules.get(r).contains(unboundAbstractPredicate)) {
					for (TIntHashSet minimalSet : literalsPerRuleThatMayBeBeneficial.get(r)) {
						
						/* add this minimal set for all non-boolean expression generic predicates or if the minimal set contains only evaluable predicates */
						minimalSet.forEach(i -> {
							if (!isBooleanExpr || evaluablePredicates.contains(i))
								relevantpredicates.add(i);
							return true;
						});
					}
				}
			}
			streamsForPossibleFormulasOverConcretePredicateNames.put(unboundAbstractPredicate, new DimacsFormulaStream(relevantpredicates, maxlength, isDescriptor ? false : disjunctionsAllowed, false));
		}
	}

	private boolean isGoal(PsiProp mapping) {

		/* cancel if there is one unbound literal */
		if (!SetUtil.difference(new HashSet<>(namesOfPredicatesToBind), mapping.keySet()).isEmpty())
			return false;

		/* announce the check of this psi2prop */
		logger.debug("Goal Check - Start - Formed psi2prop {} from the above mapping.", mapping);

		/* now check if psi2 is a good binding on the propositional level */
		if (!doesPsi2SatisfyPositiveConsistencyRules(mapping)) {
			logger.debug("Skipping psi2prop {}, because it does not satisfy at least one postive consistency rule.", mapping);
			return false;
		}

		/* notify that we found a solution */
		// logger.info("Found solution psi2prop {}! {} nodes created at this point of time.", psi2prop, this.getCreatedCounter());
		// this.printPathFromRootToNodeAsGraphViz(node, "formularsearch");
		solutionsFound.add(mapping);
		return true;
	}

	private boolean isPrunable(PsiProp node) {
		
		/* check if there is a solution that has been found and that is a subset of this one */
		for (PsiProp solution : solutionsFound) {
			boolean allDefsAreSubsets = true;
			for (String nameOfAbstractPredicate : solution.keySet()) {
				DimacsCNF clausesOfSolutionForThisPredicate = solution.get(nameOfAbstractPredicate);
				DimacsCNF clausesOfCandidateForThisPredicate = node.get(nameOfAbstractPredicate);
				if (clausesOfCandidateForThisPredicate == null || !clausesOfCandidateForThisPredicate.containsAll(clausesOfSolutionForThisPredicate)) {
					allDefsAreSubsets = false;
					break;
				}
			}

			/* now we found a solution such that this candidate here contains all clauses of the solution and additional (irrelevant) facts */
			if (allDefsAreSubsets)
				return true;
		}
		return false;
	}

	public PsiProp getNextPsi2prop() throws InterruptedException {
		PerformanceLogger.logStart("nextPsi2prop");
		Set<PsiProp> seen = new HashSet<>();
		while (!rangeSizeCombinationQueue.isEmpty()) {

			/* if there is still something in the range, try that */
			List<Integer> combination = rangeSizeCombinationQueue.peek();
			
			/* if we enter this combination for the first time (range is empty), compute all possible mappings for this combination */
			if (range.isEmpty()) {

				/* compute list of mappings for each abstract predicate */
				List<Collection<DimacsCNF>> candidates = new ArrayList<>();
				for (int i = 0; i < namesOfPredicatesToBind.size(); i++) {
					String abstractPredicate = namesOfPredicatesToBind.get(i);
					int formulaSize = combination.get(i);
					DimacsFormulaStream stream = streamsForPossibleFormulasOverConcretePredicateNames.get(abstractPredicate);
					stream.reset();
					Collection<DimacsCNF> candidatesForThisAbstractPredicate = new ArrayList<>();
					DimacsCNF cnf;
					while ((cnf = stream.nextOfExactLength(formulaSize)) != null) {
						candidatesForThisAbstractPredicate.add(cnf);
					}
					candidates.add(candidatesForThisAbstractPredicate);
				}

				/* for each of these mappings, create a psi2prop object */
				List<PsiProp> listToShuffle = new ArrayList<>();
				for (List<DimacsCNF> mapping : SetUtil.cartesianProduct(candidates)) {
					PsiProp psi2prop = new PsiProp();
					for (int i = 0; i < namesOfPredicatesToBind.size(); i++) {
						psi2prop.put(namesOfPredicatesToBind.get(i), mapping.get(i));
					}
					listToShuffle.add(psi2prop);
				}
				Collections.shuffle(listToShuffle);
				range.addAll(listToShuffle);
			}
			
			/* now check all the possible mappings */
			while (!range.isEmpty()) {
				PsiProp nextMapping = range.poll();
//				System.out.println("Check " + nextMapping);
				if (seen.contains(nextMapping)){
					System.err.println("Already seen " + nextMapping);
				}
				seen.add(nextMapping);
				if (!isPrunable(nextMapping) && isGoal(nextMapping)) {
					PerformanceLogger.logEnd("nextPsi2prop");
					if (range.isEmpty()) {
						rangeSizeCombinationQueue.poll();
					}
					return nextMapping;
				}
			}

			/* if this range size combination has no more mappings to offer, remove it from the queue */
			rangeSizeCombinationQueue.poll();
		}
		PerformanceLogger.logEnd("nextPsi2prop");
		return null;
	}

	private boolean doesPsi2SatisfyPositiveConsistencyRules(PsiProp psi2prop) {
		for (HornRule r : t.getPositiveConsistencyRules()) {
			DimacsCNF clausesForSAT4J = new DimacsCNF(domainKnowledgeInPropLogic);
			clausesForSAT4J.addAll(this.getNegatedHornRuleAsCNF(r, psi2prop));

			/*
			 * check satisfiability (should not be satisfiable, because we check on semantic consequence by contradiction)
			 */
			if (isSatisfiable(clausesForSAT4J)) {
				logger.trace("Instantiation with psi1 {} and psi2 {} does not satisfy positive consistency rule {}, which is encoded as {}", psi1prop, psi2prop, r,
						this.getNegatedHornRuleAsCNF(r, psi2prop));
				return false;
			}
		}
		return true;
	}

	private DimacsCNF getNegatedHornRuleAsCNF(HornRule r, PsiProp psi2prop) {

		/* clauses is the cnf formula */
		DimacsCNF clauses = new DimacsCNF();
		logger.trace("Start clause construction for horn rule {} with psi1prop being {} and psi2prop being {}.", r, psi1prop, psi2prop);

		/* add dimac clauses for the mapped premise of the rule */
		for (Literal abstractLiteral : r.getPremise()) {

			/* find mapping in psi 1 */
			Optional<String> literalMappedInPsi1 = psi1prop.keySet().stream().filter(l2 -> l2.equals(abstractLiteral.getPropertyName())).findFirst();
			if (literalMappedInPsi1.isPresent()) {
				logger.error("NOT IMPLEMENTED; ABORTING!");
				System.exit(1);
			}

			/* find mapping in psi 2 */
			Optional<String> literalMappedInPsi2 = psi2prop.keySet().stream().filter(l2 -> l2.equals(abstractLiteral.getProperty())).findFirst();
			if (literalMappedInPsi2.isPresent()) {
				DimacsCNF domainSpecificMappingForLiteral = psi2prop.get(literalMappedInPsi2.get());
				clauses.addAll(domainSpecificMappingForLiteral);
			} else {
				logger.trace("The abstract literal {} is not mapped to a domain specific formula neither by psi1, which is {} nor by psi2, which is {}!", abstractLiteral,
						psi1prop, psi2prop);
				continue;
			}
		}

		/* create dimacs notation for domain specific mapping of conclusion */
		Optional<String> conclusionMappedInPsi1 = psi1prop.keySet().stream().filter(l2 -> l2.equals(r.getConclusion().getPropertyName())).findFirst();
		if (conclusionMappedInPsi1.isPresent()) {
			DimacsCNF domainSpecificMappingForLiteral = psi1prop.get(conclusionMappedInPsi1.get());

			/* this monom is converted into a clause with signs negated (this monom is the beta of check DOMAIN KNOWLEDGE \and PREMISE \and not beta) */
			for (VecInt atomInConcusion : domainSpecificMappingForLiteral) {
				VecInt negatedClause = new VecInt(new int[] { atomInConcusion.get(0) * -1 });
				clauses.add(negatedClause);
			}
		} else {
			Optional<String> literalMappedInPsi2 = psi2prop.keySet().stream().filter(l2 -> l2.equals(r.getConclusion().getProperty())).findFirst();
			if (literalMappedInPsi2.isPresent()) {

				/* get the formula mapped to the abstract predicate in the conclusion. This is assumed to be a monom, e.g. with clauses of size 1 only */
				DimacsCNF mappingOfThisPredicateInPsi2prop = psi2prop.get(literalMappedInPsi2.get());
				if (!mappingOfThisPredicateInPsi2prop.isEmpty()) {
					int[] negatedConclusion = new int[mappingOfThisPredicateInPsi2prop.size()];
					int i = 0;
					for (VecInt clause : mappingOfThisPredicateInPsi2prop) {
						negatedConclusion[i++] = clause.get(0) * -1;
					}
					clauses.add(new VecInt(negatedConclusion));
				}
			} else {
				logger.error("The conclusion literal {} is mapped neither by psi1, which is {} nor by psi2, which is {}!", r.getConclusion(), psi1prop, psi2prop);
				System.exit(1);
			}
		}
		logger.trace("Finished clause construction for horn rule {} with psi1prop being{} and psi2prop being {}. Resulting clauses are: {}", r, psi1prop, psi2prop, clauses);
		return clauses;
	}

	private boolean isSatisfiable(DimacsCNF model) {

		/* estimate upper bound for model size */
		int k = 0;
		for (VecInt vec : model) {
			k += vec.size();
		}

		solver.reset();
		solver.newVar(k);
		solver.setExpectedNumberOfClauses(model.size());
		try {
			for (VecInt vec : model) {
				solver.addClause(vec);
			}
			IProblem problem = solver;
			if (problem.isSatisfiable()) {
				return true;
			}
		} catch (ContradictionException e) {
			logger.trace("Detected that formula is contradictory on construction.");
			return false;
		} catch (org.sat4j.specs.TimeoutException e) {
			e.printStackTrace();
		}
		return false;
	}

}