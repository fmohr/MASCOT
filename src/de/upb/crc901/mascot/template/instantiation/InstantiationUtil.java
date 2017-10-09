package de.upb.crc901.mascot.template.instantiation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.upb.crc901.mascot.logic.DimacsUtil;
import de.upb.crc901.mascot.structure.GenericLiteral;
import de.upb.crc901.mascot.structure.Template;
import de.upb.crc901.mascot.template.instantiator.searches.PsiProp;
import jaicore.logic.fol.structure.CNFFormula;
import jaicore.logic.fol.structure.Clause;
import jaicore.logic.fol.structure.HornRule;
import jaicore.logic.fol.structure.Literal;
import jaicore.logic.fol.structure.LiteralParam;
import jaicore.logic.fol.structure.Monom;
import jaicore.logic.fol.structure.VariableParam;



public abstract class InstantiationUtil {

	private static final Logger logger = LoggerFactory.getLogger(InstantiationUtil.class);

	public static void replacePredicateInMonomIfOccurs(Monom monom, Literal from, Monom to) {
		if (!monom.contains(from)) {
			return;
		}
		monom.remove(from);
		monom.addAll(to);
	}

	public static CNFFormula rewriteLiteralAccordingToPsi1AndPsi2(Literal literalToRewrite, Map<GenericLiteral, Monom> psi1,
			Map<String, Map<String, int[]>> parameterMappingForPsi1, Map<String, List<List<Literal>>> psi2prop,
			Map<String, Map<String, Map<Integer, LiteralParam>>> mappingForItemsInPsi2) {
		Monom psi1replacement = rewriteLiteralAccordingToPsi1(literalToRewrite, psi1, parameterMappingForPsi1);
		if (psi1replacement != null)
			return psi1replacement.asCNF();
		CNFFormula result = rewriteLiteralAccordingToPsi2(literalToRewrite, psi2prop, mappingForItemsInPsi2);
		if (result != null)
			return result;
		logger.debug("Literal {} is neither bound in psi1 nor in psi2!", literalToRewrite);
		return null;
	}

	public static Monom rewriteLiteralAccordingToPsi1(Literal literalToRewrite, Map<GenericLiteral, Monom> psi1, Map<String, Map<String, int[]>> parameterMappingForPsi1) {

		/* announce activity */
		logger.debug("Rewriting abstract literal {}", literalToRewrite);

		/*
		 * create some important information about the literal to be rewritten
		 */
		String nameOfLiteralToBeRewritten = literalToRewrite.getProperty();
		List<LiteralParam> paramsOfLiteralToBeRewritten = literalToRewrite.getParameters();

		/* create formula that will replace this literal */
		Monom formula = new Monom();

		/*
		 * if this literal is defined in psi1, perform the rather simple rewriting (because it is a monom)
		 */
		Optional<GenericLiteral> occurrenceInPsi1 = psi1.keySet().stream().filter(gp -> gp.getProperty().equals(literalToRewrite.getProperty())).findAny();
		if (!occurrenceInPsi1.isPresent()) {
			return null;
		}

		Monom mappedMonom = psi1.get(occurrenceInPsi1.get());
		for (Literal concretePredicate : mappedMonom) {

			/*
			 * for this particular concrete predicate, compute the parameters of the literal in terms of the parameters used in the literal that is to be rewritten
			 */
			List<LiteralParam> mappedParams = new LinkedList<>();
			int[] parameterMapping = parameterMappingForPsi1.get(nameOfLiteralToBeRewritten).get(concretePredicate.getProperty());
			for (int i : parameterMapping) {
				mappedParams.add(paramsOfLiteralToBeRewritten.get(i));
			}

			/*
			 * create a new literal with the respective variables and add it to the clause
			 */
			formula.add(new Literal(concretePredicate.getProperty(), mappedParams));
		}
		return formula;
	}

	public static CNFFormula rewriteLiteralAccordingToPsi2(Literal literalToRewrite, Map<String, List<List<Literal>>> psi2prop,
			Map<String, Map<String, Map<Integer, LiteralParam>>> mappingForItemsInPsi2) {

		/* announce activity */
		logger.debug("Rewriting abstract literal {}", literalToRewrite);

		/*
		 * create some important information about the literal to be rewritten
		 */
		String nameOfLiteralToBeRewritten = literalToRewrite.getProperty();
		List<LiteralParam> paramsOfLiteralToBeRewritten = literalToRewrite.getParameters();
		
		/* create formula that will replace this literal */
		CNFFormula formula = new CNFFormula();

		/*
		 * if this literal is not defined in psi1 but in psi2, perform the cnf base rewriting
		 */
		Optional<String> occurrenceInPsi2 = psi2prop.keySet().stream().filter(gp -> gp.equals(literalToRewrite.getProperty())).findAny();
		if (!occurrenceInPsi2.isPresent()) {
			return null;
		}

		Map<String, Map<Integer, LiteralParam>> mappingOfParametersForConcretePredicates = mappingForItemsInPsi2.get(nameOfLiteralToBeRewritten);
		List<List<Literal>> mappedPropositionalBasedCNF = psi2prop.get(occurrenceInPsi2.get());

		/* now consider each clause and each of its literals in particular */
		for (List<Literal> clause : mappedPropositionalBasedCNF) {
			Clause rewrittenClause = new Clause();
			for (Literal concretePredicate : clause) {

				/*
				 * for this particular concrete predicate, compute the parameters of the literal in terms of the parameters used in the literal that is to be rewritten
				 */
				List<LiteralParam> mappedParams = new LinkedList<>();
				Map<Integer, LiteralParam> parameterMapping = mappingOfParametersForConcretePredicates.get(concretePredicate.getPropertyName());
				for (int i : parameterMapping.keySet()) {
					LiteralParam param = parameterMapping.get(i);
					if (param instanceof VariableParam) {
						/* the variable of the concrete predicate is a number corresponding to the position of the abstract predicate replaced by it */
						mappedParams.add(paramsOfLiteralToBeRewritten.get(Integer.parseInt(param.getName())));
					} else {
						mappedParams.add(param); // then this argument is mapped
													// to a constant
					}
				}

				/*
				 * create a new literal with the respective variables and add it to the clause
				 */
				rewrittenClause.add(new Literal(concretePredicate.getProperty(), mappedParams));
			}
			formula.add(rewrittenClause);
		}
		return formula;
	}

	public static Map<String, Map<String, int[]>> getParameterMappingForPsi1(Map<GenericLiteral, Monom> psi1) {
		Map<String, Map<String, int[]>> parameterMappingForPsi1 = new HashMap<>();
		for (GenericLiteral abstractLiteral : psi1.keySet()) {
			List<LiteralParam> paramsOfAbstractLiteral = abstractLiteral.getParameters();
			Map<String, int[]> mappingsAssociatedWithAbstractPredicate = new HashMap<>();
			for (Literal concreteLiteral : psi1.get(abstractLiteral)) {
				List<LiteralParam> paramsOfConcreteLiteral = concreteLiteral.getParameters();
				int[] mapping = new int[paramsOfConcreteLiteral.size()];
				for (int i = 0; i < paramsOfConcreteLiteral.size(); i++) {
					mapping[i] = paramsOfAbstractLiteral.indexOf(paramsOfConcreteLiteral.get(i));
				}
				mappingsAssociatedWithAbstractPredicate.put(concreteLiteral.getProperty(), mapping);
			}
			parameterMappingForPsi1.put(abstractLiteral.getProperty(), mappingsAssociatedWithAbstractPredicate);
		}
		return parameterMappingForPsi1;
	}
	
	public static Map<String,List<List<Literal>>> decodePsiProp(PsiProp psiprop, Map<Integer,String> dimacsDecode) {
		Map<String,List<List<Literal>>> out = new HashMap<>();
		for (String prop : psiprop.keySet()) {
			out.put(prop, DimacsUtil.decodeDimacs(psiprop.get(prop), dimacsDecode));
		}
		return out;
	}

	public static Set<String> getNamesOfAbstractPredicatesInPositiveConsistencyRules(Template t) {
		Set<String> out = new HashSet<>();
		for (HornRule r : t.getPositiveConsistencyRules()) {
			out.addAll(r.getPremise().stream().map(l -> l.getProperty()).collect(Collectors.toList()));
			out.add(r.getConclusion().getProperty());
		}
		return out;
	}

	public static Set<String> getNamesOfGenericExpressions(Template t) {
		return t.getBooleanExpressions().stream().map(b -> b.getProperty()).collect(Collectors.toSet());
	}
	
	public static Set<String> getNamesOfEffectPredicates(Template t) {
		return t.getComponents().stream().map(b -> b.getEffects().getPropertyName()).collect(Collectors.toSet());
	}
}
