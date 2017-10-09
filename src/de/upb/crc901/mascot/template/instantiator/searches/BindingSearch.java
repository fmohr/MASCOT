package de.upb.crc901.mascot.template.instantiator.searches;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.sat4j.core.VecInt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.upb.crc901.mascot.structure.GenericLiteral;
import de.upb.crc901.mascot.structure.Template;
import de.upb.crc901.mascot.template.instantiation.InstantiationUtil;
import jaicore.basic.PerformanceLogger;
import jaicore.basic.SetUtil;
import jaicore.logic.fol.algorithms.resolution.Solver;
import jaicore.logic.fol.algorithms.resolution.SolverFactory;
import jaicore.logic.fol.structure.CNFFormula;
import jaicore.logic.fol.structure.Clause;
import jaicore.logic.fol.structure.ConstantParam;
import jaicore.logic.fol.structure.HornRule;
import jaicore.logic.fol.structure.Literal;
import jaicore.logic.fol.structure.LiteralParam;
import jaicore.logic.fol.structure.Monom;
import jaicore.logic.fol.structure.TypeModule;
import jaicore.logic.fol.structure.VariableParam;

public class BindingSearch {

	private static final Logger logger = LoggerFactory.getLogger(BindingSearch.class);

	private final TypeModule typeModule;
	private final CNFFormula backgroundKnowledge;
//	private final Map<String, Integer> dimacsEncode;
	private final Map<Integer, String> dimacsDecode;
	private final Template t; // the template is required to check the consistency rules
//	private Map<String, Integer> concretePredicatesAndTheirArities; // names and arities
	private Map<String, Integer> abstractPredicatesAndTheirArities; // names and arities
	private Map<GenericLiteral, Monom> psi1;
	private PsiProp psi2prop; // binding of abstract
	private final Map<String, Set<String>> concretePredicatesOccurringInDefinitionOfAbstractPedicates = new HashMap<>();
	private final Set<String> namesOfEffectPredicates;
	// predicate names
	private Map<String, Map<String, int[]>> parameterMappingForPsi1;
	private Solver solver;

	/*
	 * structures necessary to iterate over the possible bindings. All candidates are stored in the first var, the state of the traversal in the pathPointers, and the map from the state to the
	 * candidates are stored in the paths
	 */
	private final Map<Integer, List<Map<Integer, LiteralParam>>> possibleMappingsPerPointer = new HashMap<>();
	private final int[] pathPointers;
	private final Map<Integer, String> pathToAbstractPredicates = new HashMap<>();
	private final Map<Integer, String> pathToConcretePredicates = new HashMap<>();

	public BindingSearch(TypeModule pTypeModule, CNFFormula backgroundKnowledge, Template t, Map<String, Integer> concretePredicatesAndTheirArities,
			Map<String, Integer> abstractPredicatesAndTheirArities, Map<GenericLiteral, Monom> psi1, Map<String, Map<String, int[]>> parameterMappingForPsi1, PsiProp psi2prop,
			Map<String, Integer> dimacsEncode, Map<Integer, String> dimacsDecode) throws InterruptedException {
		super();
		typeModule = pTypeModule;
		this.t = t;
//		this.concretePredicatesAndTheirArities = concretePredicatesAndTheirArities;
		this.psi1 = psi1;
		this.psi2prop = psi2prop;
		this.abstractPredicatesAndTheirArities = abstractPredicatesAndTheirArities;
		this.parameterMappingForPsi1 = parameterMappingForPsi1;
		this.backgroundKnowledge = backgroundKnowledge;
		this.solver = SolverFactory.getInstance().getSolver(backgroundKnowledge);
		this.dimacsDecode = dimacsDecode;
//		this.dimacsEncode = dimacsEncode;
		this.namesOfEffectPredicates = InstantiationUtil.getNamesOfEffectPredicates(t);

		for (String abstractPred : psi2prop.keySet()) {
			Set<String> occurring = new HashSet<>();
			concretePredicatesOccurringInDefinitionOfAbstractPedicates.put(abstractPred, occurring);
			for (VecInt clause : psi2prop.get(abstractPred)) {
				for (int i = 0; i < clause.size(); i++) {
					occurring.add(dimacsDecode.get(clause.get(i)));
				}
			}
		}

		/* compute all variable bindings per abstract predicate */
		int pointers = 0;
		for (String abstractPred : psi2prop.keySet()) {
			int arityOfAbstractPredicate = abstractPredicatesAndTheirArities.get(abstractPred);
			for (String concretePredicate : concretePredicatesOccurringInDefinitionOfAbstractPedicates.get(abstractPred)) {
				List<Map<Integer, LiteralParam>> mappingsForThisPredicateCombo = new ArrayList<>();
				int arityOfConcretePredicate = concretePredicatesAndTheirArities.get(concretePredicate);
				
				/* only consider mappings if the abstract predicate and concrete predicate have the same arity */
				Collection<Integer> domain = new ArrayList<>();
				Collection<LiteralParam> range = new ArrayList<>();
				for (int i = 0; i < arityOfAbstractPredicate; i++)
					range.add(new VariableParam("" + i));
				
				/* consider also constants but not for concrete predicates occurring in the binding of an abstract predicate that is an effect predicate of an operation */
				if (!namesOfEffectPredicates.contains(abstractPred)) {
					for (ConstantParam p : backgroundKnowledge.getConstantParams())
						range.add(p);
				}
				
				/* add every place of the concrete predicate to the domain */
				for (int i = 0; i < arityOfConcretePredicate; i++) {
					domain.add(i);
				}
				
				for (Map<Integer, LiteralParam> map : SetUtil.allTotalMappings(domain, range)) {
					
					/* only consider mappings where every variable of the abstract literal occurs exactly once */
					List<Object> targets = map.values().stream().filter(p -> p instanceof VariableParam).collect(Collectors.toList());
					if ((targets.size() == arityOfAbstractPredicate) && (new HashSet<>(targets).size() == targets.size()))
						mappingsForThisPredicateCombo.add(map);
				}

				/* assign list of candidates to this pointer */
				possibleMappingsPerPointer.put(pointers, mappingsForThisPredicateCombo);

				/* set pointer information and increase pointer */
				pathToAbstractPredicates.put(pointers, abstractPred);
				pathToConcretePredicates.put(pointers, concretePredicate);
				pointers++;
			}
		}
		pathPointers = new int[pointers]; // initialize pointer state;
	}

	private VariableBinding getCurrentBinding() {
		VariableBinding binding = new VariableBinding();
		for (int pointer = 0; pointer < pathPointers.length; pointer++) {
			
			/* if there is one list that has no candidate, then return 0 */
			if (possibleMappingsPerPointer.get(pointer).isEmpty())
				return null;
			
			/* otherwise complete mapping */
			Map<Integer, LiteralParam> map = possibleMappingsPerPointer.get(pointer).get(pathPointers[pointer]);
			String abstractPredicate = pathToAbstractPredicates.get(pointer);
			String concretePredicate = pathToConcretePredicates.get(pointer);
			if (!binding.containsKey(abstractPredicate))
				binding.put(abstractPredicate, new HashMap<>());
			binding.get(abstractPredicate).put(concretePredicate, map);
		}
		return binding;
	}

	private boolean increaseState() {
		int pointerToIncrease = 0;
		boolean pointerChanged = false;
		do {
			pathPointers[pointerToIncrease]++;
			if (pathPointers[pointerToIncrease] >= possibleMappingsPerPointer.get(pointerToIncrease).size()) {
				pathPointers[pointerToIncrease] = 0;
				pointerToIncrease++;
				pointerChanged = true;
			} else
				pointerChanged = false;
		} while (pointerChanged && pathPointers.length > pointerToIncrease);
		return !pointerChanged;
	}

	public Map<String, Map<String, Map<Integer, LiteralParam>>> nextBinding() throws InterruptedException {

		logger.info("Starting search for parameter binding for psi2prop {}");
		PerformanceLogger.logStart("nextBinding");

		while (increaseState()) {
			VariableBinding currentBinding = getCurrentBinding();
			if (currentBinding == null)
				break;
			if (isGoal(currentBinding))
				return currentBinding;
		}

		PerformanceLogger.logEnd("nextBinding");
		logger.info("Finished search for parameter binding for psi2prop {}. No more bindings found.");
		return null;
	}

	protected boolean isGoal(VariableBinding binding) throws InterruptedException{

		logger.debug("Goal check - Start check for binding {}.", binding);
		for (HornRule r : t.getPositiveConsistencyRules()) {
			logger.debug("Goal check - Check positive consistency rule {}.", r);
			CNFFormula rewrittenPositiveRule = this.negateAndRewriteRuleAccordingToMapping(r, binding);
			logger.debug("Goal check - This consistency rule is rewrtitten and negated to a monom that must be unsatisfiable: {}.", rewrittenPositiveRule);

			Map<VariableParam, ConstantParam> grounding = new HashMap<>();
			for (VariableParam param : rewrittenPositiveRule.getVariableParams()) {
				grounding.put(param, new ConstantParam(param.getName() + "_GROUND", typeModule.getType("Thing")));
			}
			CNFFormula groundRule = new CNFFormula(rewrittenPositiveRule, grounding);

			/* now create the formula to be checked on satisfiability */
			CNFFormula formulaToBeChecked = new CNFFormula();
			formulaToBeChecked.addAll(groundRule);
			solver.addFormula(formulaToBeChecked);
			if (solver.isSatisfiable(backgroundKnowledge, formulaToBeChecked)) {
				logger.debug("Goal check - Canceled, because concistency rule {} is not valid.", r);
				return false;
			}
		}
		logger.info("Goal check - Successfully completed: Found solution binding {}", binding);
		return true;
	}

	protected boolean isPrunable(BindingSearchNode node) {

		/*
		 * check if there is a complete mapping where one of the params of the abstract predicate does not occur in the concrete predicate
		 */
		for (String completedAbstractPredicate : node.getCompletedAbstractPredicates()) {
			Set<LiteralParam> argsUncovered = new HashSet<>();
			for (int i = 0; i < abstractPredicatesAndTheirArities.get(completedAbstractPredicate); i++)
				argsUncovered.add(new VariableParam(String.valueOf(i)));
			Map<String, Map<Integer, LiteralParam>> mapForThisPredicate = node.getMapping().get(completedAbstractPredicate);
			for (String concretePredicate : mapForThisPredicate.keySet()) {
				for (int src : mapForThisPredicate.get(concretePredicate).keySet()) {
					LiteralParam target = mapForThisPredicate.get(concretePredicate).get(src);
					argsUncovered.remove(target);
					if (argsUncovered.isEmpty())
						break;
				}
				if (argsUncovered.isEmpty())
					break;
			}
			if (!argsUncovered.isEmpty()) {
				logger.trace("Pruning binding {}, because the arguments {} of the abstract predicate {} do not occur in the concrete predicates.", node.getMapping(),
						argsUncovered, completedAbstractPredicate);
				return true;
			}
		}
		return false;
	}

	private CNFFormula negateAndRewriteRuleAccordingToMapping(HornRule rule, Map<String, Map<String, Map<Integer, LiteralParam>>> mapping) {

		CNFFormula formula = new CNFFormula();

		/* create rewritten premise */
		Monom premise = rule.getPremise();
		for (Literal l : premise) {
			formula.addAll(InstantiationUtil.rewriteLiteralAccordingToPsi1AndPsi2(l, psi1, parameterMappingForPsi1, InstantiationUtil.decodePsiProp(psi2prop, dimacsDecode),
					mapping));
		}

		/* create rewritten conclusion */
		Monom conclusion = Monom.fromCNFFormula(InstantiationUtil.rewriteLiteralAccordingToPsi1AndPsi2(rule.getConclusion(), psi1, parameterMappingForPsi1,
				InstantiationUtil.decodePsiProp(psi2prop, dimacsDecode), mapping));
		formula.add(Clause.getByNegatingMonom(conclusion));
		return formula;
	}
}