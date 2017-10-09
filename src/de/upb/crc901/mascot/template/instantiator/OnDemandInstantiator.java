package de.upb.crc901.mascot.template.instantiator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.sat4j.core.VecInt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.upb.crc901.configurationsetting.operation.Operation;
import de.upb.crc901.configurationsetting.operation.OperationInvocation;
import de.upb.crc901.mascot.logic.DimacsCNF;
import de.upb.crc901.mascot.structure.GenericAbstractLiteral;
import de.upb.crc901.mascot.structure.GenericBooleanExpression;
import de.upb.crc901.mascot.structure.GenericLiteral;
import de.upb.crc901.mascot.structure.GenericOperationCall;
import de.upb.crc901.mascot.structure.Template;
import de.upb.crc901.mascot.template.instantiation.GenericAbstractLiteralInstantiation;
import de.upb.crc901.mascot.template.instantiation.GenericBooleanExpressionInstantiation;
import de.upb.crc901.mascot.template.instantiation.GenericOperationCallInstantiation;
import de.upb.crc901.mascot.template.instantiation.InstantiationEnvironment;
import de.upb.crc901.mascot.template.instantiation.InstantiationUtil;
import de.upb.crc901.mascot.template.instantiation.TemplateInstantiation;
import de.upb.crc901.mascot.template.instantiator.searches.BindingSearch;
import de.upb.crc901.mascot.template.instantiator.searches.FormulaSearch;
import de.upb.crc901.mascot.template.instantiator.searches.PsiProp;
import gnu.trove.set.hash.TIntHashSet;
import jaicore.basic.PerformanceLogger;
import jaicore.basic.SetUtil;
import jaicore.logic.fol.structure.CNFFormula;
import jaicore.logic.fol.structure.Clause;
import jaicore.logic.fol.structure.HornRule;
import jaicore.logic.fol.structure.Literal;
import jaicore.logic.fol.structure.LiteralParam;
import jaicore.logic.fol.structure.LiteralSet;
import jaicore.logic.fol.structure.Monom;
import jaicore.logic.fol.structure.VariableParam;

public class OnDemandInstantiator {

	private static final Logger logger = LoggerFactory.getLogger(OnDemandInstantiator.class);
	private InstantiationEnvironment environment;
	private Map<String, Integer> predicatesOccuringInKnowledgeBase = new HashMap<>();

	/* SAT solving related variables */
	private final Map<String, Integer> dimacsEncode;
	private final Map<Integer, String> dimacsDecode;
	private final CNFFormula domainKnowledge;
	private final Set<VecInt> domainKnowledgeInPropLogic;
	private final Set<Integer> computablePredicates;
	private final int maxLength;
	private final boolean allowDisjunctions;
	private final boolean allowNegations;

	public OnDemandInstantiator(InstantiationEnvironment environment, Set<String> namesOfEvaluablePredicates, int pMaxLength, boolean pAllowDisjunction, boolean pAllowNegations) {
		super();
		this.environment = environment;
		this.computablePredicates = new HashSet<>();

		/* identify all domain predicates and write domain knowledge in CNF */
		this.domainKnowledge = new CNFFormula();
		for (HornRule r : environment.getKnowledgeBase()) {
			Clause c = new Clause();
			for (Literal l : r.getPremise()) {
				predicatesOccuringInKnowledgeBase.put(l.getPropertyName(), l.getParameters().size());
				c.add(new Literal(l.getPropertyName(), l.getParameters(), !l.isPositive()));
			}
			Literal conclusionLiteral = r.getConclusion();
			c.add(conclusionLiteral);
			predicatesOccuringInKnowledgeBase.put(conclusionLiteral.getPropertyName(), conclusionLiteral.getParameters().size());
			domainKnowledge.add(c);
		}

		/* create dimacs encoding for predicates */
		int i = 1;
		dimacsEncode = new HashMap<>();
		dimacsDecode = new HashMap<>();
		for (String property : predicatesOccuringInKnowledgeBase.keySet()) {
			if (namesOfEvaluablePredicates.contains(property))
				computablePredicates.add(i);
			dimacsEncode.put(property, i);
			dimacsDecode.put(i, property);
			i++;
		}

		/* compute domain knowledge as dimacs encoding */
		domainKnowledgeInPropLogic = new HashSet<>();
		Set<Set<Integer>> cnfprop = new HashSet<>();
		for (HornRule r : environment.getKnowledgeBase()) {
			Monom premise = r.getPremise();
			int conclusionAsDimacs = dimacsEncode.get(r.getConclusion().getPropertyName()) * (r.getConclusion().isPositive() ? 1 : -1);
			Set<Integer> clauseAsSet = new HashSet<>();
			boolean isTautology = false;
			for (Literal l : premise) {
				int lit = dimacsEncode.get(l.getPropertyName()) * (l.isPositive() ? -1 : 1);
				if (lit == conclusionAsDimacs * -1) {
					isTautology = true;
					break;
				}
				clauseAsSet.add(lit);
			}

			/* only add if clause is no tautology */
			if (isTautology)
				continue;
			clauseAsSet.add(conclusionAsDimacs);
			cnfprop.add(clauseAsSet);
		}
		for (Set<Integer> clauseAsSet : cnfprop) {
			int[] clause = new int[clauseAsSet.size()];
			i = 0;
			for (int lit : clauseAsSet) {
				clause[i++] = lit;
			}
			domainKnowledgeInPropLogic.add(new VecInt(clause));
		}
		
		/* generate streams for formulas */
		maxLength = pMaxLength;
		allowDisjunctions = pAllowDisjunction;
		allowNegations = pAllowNegations;
	}

	public Collection<TemplateInstantiation> instantiate(LiteralSet effectsToShow) throws InterruptedException {

		/* announce activity */
		logger.info("Start on-demand-instantiation with goal {}", effectsToShow);
		List<TemplateInstantiation> solutions = new LinkedList<>();

		/* compute some basic information for this request */
		Set<String> predicateNamesOccuringInDesiredEffects = effectsToShow.toPropositionalSet();

		/* process every template */
		for (Template t : environment.getTemplates()) {
			if (Thread.interrupted())
				throw new InterruptedException();
			logger.info("Considering template {}", t);

			/* compute some basic information for this template */
			Set<String> predicateNamesOccuringInTemplateEffects = t.getEffects().stream().map(l -> l.getProperty()).collect(Collectors.toSet());
			Map<String, Integer> abstractPredicatesAndTheirArities = new HashMap<>();
			for (GenericLiteral l : t.getGenericLiterals()) {
				abstractPredicatesAndTheirArities.put(l.getProperty(), l.getParameters().size());
			}

			/* step 1: create input and output mappings */
			Collection<Map<VariableParam, VariableParam>> varMappings = SetUtil.allPartialMappings(effectsToShow.getVariableParams(), t.getEffects().getVariableParams());

			for (Map<VariableParam, VariableParam> varMapping : varMappings) {
				if (Thread.interrupted())
					throw new InterruptedException();
				logger.info("Entering Step 1: Considering variable mappings between goal and variables in postcondition of template. Mapping is {}", varMapping);
				logger.debug("Considering variable mapping {}", varMapping);

				/* compute the mapped effects */
				Monom mappedDesiredEffects = new Monom(effectsToShow, varMapping);
				logger.debug("Mapped desired effects are {}", mappedDesiredEffects);

				/*
				 * step 2: compute coverage mappings for predicates (that is, mappings that map every predicate in E_q to predicates in E_t; the arguments are already fixed by step 1)
				 */
				Collection<Map<String, String>> literalMappings = SetUtil.allTotalMappings(predicateNamesOccuringInDesiredEffects, predicateNamesOccuringInTemplateEffects);
				Collection<Map<GenericLiteral, Monom>> spaceOfPSI1 = new HashSet<>();
				for (Map<String, String> literalMapping : literalMappings) {
					if (Thread.interrupted())
						throw new InterruptedException();

					logger.info("Entering Step 2: Considered literal mapping {}.", literalMapping);

					/*
					 * create monom on the rhs of the equivalence corresponding to this particular mapping
					 */
					Map<GenericLiteral, Monom> psi1 = new HashMap<>();
					for (String nameOfPredicateInDesiredEffect : literalMapping.keySet()) {
						String nameOfPredicateInTemplateEffect = literalMapping.get(nameOfPredicateInDesiredEffect);
						GenericLiteral literalInTemplateEffect = getLiteralInTemplateEffectWithPropertyName(t, nameOfPredicateInTemplateEffect);
						if (!psi1.containsKey(literalInTemplateEffect))
							psi1.put(literalInTemplateEffect, new Monom());
						Monom m = psi1.get(literalInTemplateEffect);
						m.addAll(mappedDesiredEffects.getLiteralsWithPropertyName(nameOfPredicateInDesiredEffect));
					}

					/* check validity of this choice */
					boolean valid = true;
					for (GenericLiteral abstractPredicate : psi1.keySet()) {
						if (!new HashSet<>(abstractPredicate.getVariableParams()).equals(psi1.get(abstractPredicate).getVariableParams())) {
							logger.debug("Reject psi {} because abstract predicate {} and its monom {} have not the same variables.", psi1, abstractPredicate,
									psi1.get(abstractPredicate));
							valid = false;
							break;
						}
					}

					if (!valid) {
						continue;
					}

					/*
					 * now check if the template effect mapped to the domain entails the desired effect
					 */
					Monom mappedTemplateEffectTmp = new Monom(t.getEffects());
					for (GenericLiteral l : psi1.keySet()) {
						logger.trace("Replacing {} with {}", l, psi1.get(l));
						InstantiationUtil.replacePredicateInMonomIfOccurs(mappedTemplateEffectTmp, new Literal(l.getProperty(), l.getParameters(), l.isPositive()), psi1.get(l));
					}
					logger.debug("Mapped template effects with psi1 {} from {} to {}", psi1, t.getEffects(), mappedTemplateEffectTmp);
					Monom mappedTemplateEffect = new Monom(mappedTemplateEffectTmp, varMapping);
					if (!mappedTemplateEffect.containsAll(mappedDesiredEffects)) {
						logger.debug("Reject psi {} because it does not make the template effects {} entail the desired effects {}.", psi1, mappedTemplateEffect,
								mappedDesiredEffects);
						continue;
					}

					logger.debug("Computed valid equivalence {}", psi1);
					spaceOfPSI1.add(psi1);
				}

				/*
				 * summarize current achievements and go to next variable mapping, if this mapping does not allow to deduce the desired effects
				 */
				logger.info("Step 2 identified {} valid set(s) Psi1 ", spaceOfPSI1.size());
				if (spaceOfPSI1.isEmpty())
					continue;
				
				/*
				 * step 3: compute bindings for the remaining abstract predicates in the constraints of the template that make the constraints ok
				 */
				for (Map<GenericLiteral, Monom> psi1 : spaceOfPSI1) {
					if (Thread.interrupted())
						throw new InterruptedException();
					
					/* store the set of predicates bound by psi1 */
					//Set<String> boundPredicates = psi1.keySet().stream().map(l -> l.getPropertyName()).collect(Collectors.toSet());

					/* compute the set of unbound predicates (Gamma) */
					logger.info("Entering Step 3: Compute bindings for the remaining abstract predicates in template constraints. Considering psi1 {}", psi1);
					Map<String, Map<String, int[]>> parameterMappingForPsi1 = InstantiationUtil.getParameterMappingForPsi1(psi1);

					/*
					 * create a prop logic version of psi1 and encode it directly in dimacs. Since psi1 maps to monoms, each vecint has only length 1
					 */
					logger.info("Entering Step 3.1: Compute propositional versions of psi1 (knowledge and constraints have been computed before).");
					PsiProp psi1prop = new PsiProp();
					for (GenericLiteral abstractLiteral : psi1.keySet()) {
						DimacsCNF clauses = new DimacsCNF();
						for (Literal concreteLiteralInMappedMonom : psi1.get(abstractLiteral)) {
							int[] clauseInDimacs = new int[1];
							clauseInDimacs[0] = dimacsEncode.get(concreteLiteralInMappedMonom.getProperty());
							clauses.add(new VecInt(clauseInDimacs));
						}
						psi1prop.put(abstractLiteral.getPropertyName(), clauses);
					}
					
					/* now run A* algorithm in order to find psi2 */
					FormulaSearch fs = new FormulaSearch(t, domainKnowledgeInPropLogic, new TIntHashSet(computablePredicates), psi1prop, maxLength, allowDisjunctions, allowNegations);
					PsiProp psi2prop;
					logger.info("Entering Step 3.2 and 3.3: Search for propositional version of prop2 such that the template constraints are satisfied on the propositional logic level.");
					List<PsiProp> seen = new ArrayList<>();
					while ((psi2prop = fs.getNextPsi2prop()) != null) {
						if (Thread.interrupted())
							throw new InterruptedException();
						if (seen.contains(psi2prop)) {
							System.err.println("Sehe das selbe psi2prop zum zweiten Mal! " + psi2prop + ", d.h. " + printMapping(psi2prop));
							System.exit(1);
						}
						seen.add(psi2prop);

						/*
						 * create a table of concrete predicates occuring in psi2prop with their arities
						 */
						Map<String, List<List<Literal>>> prop2asStringMap = new HashMap<>();
						for (String abstractPredicate : psi2prop.keySet()) {
							List<List<Literal>> cnf = new LinkedList<>();
							for (VecInt clause : psi2prop.get(abstractPredicate)) {
								List<Literal> literalClause = new LinkedList<>();
								for (int i = 0; i < clause.size(); i++) {
									int literal = clause.get(i);
									int atom = Math.max(literal, literal * -1);
									String property = dimacsDecode.get(atom);
									
									literalClause.add(new Literal(property, new LinkedList<>(), literal > 0));
								}
								cnf.add(literalClause);
							}
							prop2asStringMap.put(abstractPredicate, cnf);
						}
						logger.info(
								"Entering Step 3.4: Search for binding of variables of concrete predicates to variables of abstract predicates or constants. Considered psi2prop is {}",
								prop2asStringMap);
						
						/* create variable mappings for the predicates */
						BindingSearch bs = new BindingSearch(environment.getTypeModule(), domainKnowledge, t, predicatesOccuringInKnowledgeBase, abstractPredicatesAndTheirArities,
								psi1, parameterMappingForPsi1, psi2prop, dimacsEncode, dimacsDecode);
						Map<String, Map<String, Map<Integer, LiteralParam>>> binding = bs.nextBinding();
						if (binding == null) {
							logger.info("Could not find any binding for psi2prop {}, so searching for another psi2prop.", prop2asStringMap);
							continue;
						}
						
						PerformanceLogger.logStart("Service Discovery");
						/* step 4: discover services */
						logger.info("Entering Step 4: Try to discover an operation for psi1 {} with binding {} and psi2 {} with binding {}", psi1, parameterMappingForPsi1, prop2asStringMap, binding);
						Map<GenericOperationCall, OperationInvocation> discoveryResults = new HashMap<>();
						for (GenericOperationCall op : t.getComponents()) {
							CNFFormula postMapping = InstantiationUtil.rewriteLiteralAccordingToPsi1AndPsi2(op.getEffects(), psi1, parameterMappingForPsi1, prop2asStringMap,
									binding);
							
							/* outsort this request if the postcondition is not a monom */
							if (postMapping.stream().filter(c -> c.size() > 1).findAny().isPresent()) {
								logger.info("Ignoring request for operation {}, since postcondition is not a monom", op);
								continue;
							}
							Monom post = postMapping != null ? Monom.fromCNFFormula(postMapping) : new Monom();
							logger.info("Entering Step 4: Try to discover an operation with I = {}, O = {}, and E = {}", op.getInputs(), op.getOutputs(), post);
							Set<LiteralParam> inputs = new HashSet<>();
							inputs.addAll(op.getInputs());
							OperationInvocation discoveryResult = discover(inputs, op.getOutputs(), post);
							discoveryResults.put(op, discoveryResult);
							if (discoveryResult == null) {
								logger.info("Could not find any result for query {}");
								break;
							}
						}
						PerformanceLogger.logEnd("Service Discovery");

						if (!discoveryResults.containsValue(null)) {
							PerformanceLogger.logStart("Solution computation");
							/*
							 * create instantiations for generic service calls, boolean expressions, and auxilliary predicates
							 */
							List<GenericOperationCallInstantiation> operationCallInstantiations = new LinkedList<>();
							for (GenericOperationCall c : t.getComponents()) {
								operationCallInstantiations.add(new GenericOperationCallInstantiation(c, discoveryResults.get(c)));
							}
							List<GenericBooleanExpressionInstantiation> booleanExpressionInstantiations = new LinkedList<>();
							for (GenericBooleanExpression b : t.getBooleanExpressions()) {
								booleanExpressionInstantiations.add(new GenericBooleanExpressionInstantiation(b, InstantiationUtil.rewriteLiteralAccordingToPsi1AndPsi2(b, psi1,
										parameterMappingForPsi1, prop2asStringMap, binding)));
							}
							List<GenericAbstractLiteralInstantiation> helperPredicateInstantiations = new LinkedList<>();
							for (GenericAbstractLiteral l : t.getHelperPredicates()) {
								helperPredicateInstantiations.add(new GenericAbstractLiteralInstantiation(l, InstantiationUtil.rewriteLiteralAccordingToPsi1AndPsi2(l, psi1,
										parameterMappingForPsi1, prop2asStringMap, binding)));
							}

							/* now create full template instantiation */
							TemplateInstantiation solution = new TemplateInstantiation(t, operationCallInstantiations, booleanExpressionInstantiations,
									helperPredicateInstantiations);
							logger.info("Found template instantiation that is a solution: {}", solution);
							solutions.add(solution);
							PerformanceLogger.logEnd("Solution computation");
							return solutions;
						}
					}
				}
			}
		}
		return null;
	}

	private GenericLiteral getLiteralInTemplateEffectWithPropertyName(Template t, String name) {
		for (Literal l : t.getEffects()) {
			if (l.getProperty().equals(name))
				return new GenericLiteral(name, l.getParameters(), l.isPositive());
		}
		return null;
	}

	private OperationInvocation discover(Set<LiteralParam> in, Set<VariableParam> out, Monom post) throws InterruptedException {
		for (Operation o : environment.getComponents()) {
			logger.debug("Discovery - Checking operation {}", o.getName());

			/*
			 * check if the postcondition of the operation imply the required postconditions
			 */
			Collection<Map<VariableParam, VariableParam>> outputMappings = SetUtil.allPartialMappings(out, o.getOutputParameters());
			for (Map<VariableParam, VariableParam> outputMapping : outputMappings) {
				Monom mappedRequiredEffects = new Monom(post, outputMapping);
				Collection<Map<VariableParam, LiteralParam>> inputMappings = SetUtil.allPartialMappings(o.getInputParameters(), in);
				for (Map<VariableParam, LiteralParam> inputMapping : inputMappings) {
					Monom mappedProvidedEffects = new Monom(o.getEffect().getCondition(), inputMapping);
					logger.debug("Discovery - Considering input mapping {} and output mapping {}", inputMapping, outputMapping);
					if (mappedProvidedEffects.containsAll(mappedRequiredEffects)) {
						logger.info("Discovery - Success: Discovery result is operation {} with input mapping {} and output mapping {}", o.getName(), inputMapping, outputMapping);
						return new OperationInvocation(o, inputMapping, outputMapping);
					}
				}
			}
		}
		return null;
	}

	private String printMapping(PsiProp mapping) {
		StringBuilder str = new StringBuilder();
		for (String abstractPredicate : mapping.keySet()) {
			str.append(abstractPredicate + " = ");
			int i = 0;
			for (VecInt clause : mapping.get(abstractPredicate)) {
				if (clause.size() > 1)
					str.append("(");
				for (int j = 0; j < clause.size(); j++)
					str.append(dimacsDecode.get(clause.get(j)));
				if (clause.size() > 1)
					str.append(")");
				i++;
				if (i < mapping.get(abstractPredicate).size())
					str.append(" & ");
			}
			str.append("; ");
		}
		return str.toString();
	}
}
