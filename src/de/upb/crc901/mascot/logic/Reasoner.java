package de.upb.crc901.mascot.logic;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import jaicore.logic.fol.structure.ConstantParam;
import jaicore.logic.fol.structure.HornFormula;
import jaicore.logic.fol.structure.HornRule;
import jaicore.logic.fol.structure.Literal;
import jaicore.logic.fol.structure.LiteralParam;
import jaicore.logic.fol.structure.Monom;
import jaicore.logic.fol.structure.VariableParam;

public class Reasoner {

	private Monom usedFacts = new Monom();
	private List<Monom> factBases = new LinkedList<Monom>();
	private List<HornFormula> ruleBases = new LinkedList<>();

	public void addFactBase(Monom fb) {
		if (fb.hasVariables()) {
			System.out.println(
					"Fact Bases of a reasoner may not contain variables but only constants!\nThe error occured with\n"
							+ fb);
			System.exit(1);
		}
		this.factBases.add(fb);
		this.usedFacts.addAll(fb);
	}

	public void rmFactBase(Monom fb) {
		this.factBases.remove(fb);
		this.usedFacts = new Monom();
		for (Monom remainingfb : this.factBases)
			this.usedFacts.addAll(remainingfb);
	}

	public void addRuleBase(HornFormula rb) {
		this.ruleBases.add(rb);
	}

	public void rmRuleBase(HornFormula rb) {
		this.ruleBases.remove(rb);
	}

	public boolean forwardTest(Literal test) {
		Monom deducedFacts = this.usedFacts;
		Monom tmpFacts;

		/* perform recognize act cycle */
		do {
			tmpFacts = new Monom(deducedFacts);
			this.performRecognizeAct(deducedFacts);
		} while (!deducedFacts.equals(tmpFacts) && !deducedFacts.contains(test));

		/* return true or false */
		return deducedFacts.contains(test);
	}

	public Monom forwardChaining() {
		Monom deducedFacts = new Monom(this.usedFacts);
		Monom tmpFacts;

		/* perform recognize act cycle */
		do {
			tmpFacts = new Monom(deducedFacts);
			this.performRecognizeAct(deducedFacts);
			if (deducedFacts.isContradictory())
				return deducedFacts;
		} while (!deducedFacts.equals(tmpFacts));
		return deducedFacts;
	}

	public void performRecognizeAct(Monom facts) {
		for (HornFormula ruleBase : this.ruleBases)
			for (HornRule rule : ruleBase)
				facts.addAll(getFactsByRule(rule, facts));
	}

	private Set<Literal> getFactsByRule(HornRule rule, Monom facts) {

		/*
		 * cancel if the rule conclusion is a fact and if that fact is already
		 * in the fact base
		 */
		Literal conclusion = rule.getConclusion();
		if (conclusion.isGround() && facts.contains(conclusion))
			return null;

		/*
		 * cancel if the rule premise contains predicates that are not part of
		 * the fact base
		 */
		List<Literal> premise = new LinkedList<>(rule.getPremise());
		for (Literal literal : premise)
			if (!facts.containsLiteralWithPredicatename(literal.getProperty()))
				return null;

		Set<Literal> r = this.getFactsByRule(premise, conclusion, facts, new Hashtable<>(), 0);
		return r;
	}

	/**
	 * Determines the set of (ground) predicates that can be derived with the
	 * rule; that is, we calculate the ground conclusions (may be multiple)
	 * 
	 * @param premise
	 * @param conclusion
	 * @param facts
	 * @param mapping
	 * @param depth
	 * @return
	 */
	private Set<Literal> getFactsByRule(List<Literal> premise, Literal conclusion, Monom facts,
			Hashtable<VariableParam, ConstantParam> mapping, int depth) {

		/* get premise and cancel if length is reached */
		if (depth >= premise.size()) {

			Set<Literal> fb = new HashSet<Literal>();

			/* now add all literals of the conclusion to the fact base */
			List<LiteralParam> conclusionParams = conclusion.getParameters();
			List<LiteralParam> groundParams = new LinkedList<>();
			for (LiteralParam param : conclusionParams) {
				if (param instanceof ConstantParam) {
					groundParams.add(param);
				} else {
					if (mapping.containsKey(param))
						groundParams.add(mapping.get(param));
					else {

						/*
						 * here, we have an unmapped variable that may be mapped
						 * to any constant
						 */
						for (ConstantParam constant : this.getConstants()) {
							Hashtable<VariableParam, ConstantParam> mapping2 = new Hashtable<>();
							mapping2.putAll(mapping);
							mapping2.put((VariableParam) param, constant);
							fb.addAll(this.getFactsByRule(premise, conclusion, facts, mapping2, depth));
						}
						return fb;
					}
				}
			}
			fb.add(new Literal(conclusion.getProperty(), groundParams));
			return fb;
		}

		/*
		 * determine the literal at position $depth in the rule and its partial
		 * grounding by $mapping
		 */
		Literal ruleLiteral = premise.get(depth);
		List<LiteralParam> params = ruleLiteral.getParameters(); // contains the
																	// arguments
																	// of the
																	// literal
		List<ConstantParam> grounding = new LinkedList<>(); // will contain the
															// constant value
															// for each argument
		for (LiteralParam param : params) {
			if (param instanceof ConstantParam)
				grounding.add((ConstantParam) param);
			else if (mapping.containsKey(param))
				grounding.add(mapping.get(param));
			else
				grounding.add(null);
		}

		/*
		 * determine literals that match the predicate in premise position
		 * $depth
		 */
		List<Literal> matchingLiterals = new LinkedList<Literal>();
		for (Literal knownLiteral : facts) {
			if (knownLiteral.getProperty().equals(ruleLiteral.getProperty())
					&& knownLiteral.isPositive() == ruleLiteral.isPositive())
				matchingLiterals.add(knownLiteral);
		}

		/*
		 * for each matching literal, try to ground the variables consistently
		 */
		Set<Literal> fb = new HashSet<Literal>();
		for (Literal candidateLiteral : matchingLiterals) {

			/*
			 * check whether the ruleLiteral can be ground to the
			 * candidateLiteral
			 */
			List<ConstantParam> constantsOfLiteralInState = candidateLiteral.getConstantParams(); // Facts
																									// should
																									// not
																									// contain
																									// any
																									// variables,
																									// so
																									// these
																									// are
																									// ALL
																									// the
																									// parameters
																									// of
																									// the
																									// literal
			boolean groundingPossible = true;
			for (int i = 0; i < constantsOfLiteralInState.size(); i++) {
				if (grounding.get(i) != null && !grounding.get(i).equals(constantsOfLiteralInState.get(i))) {
					groundingPossible = false;
					break;
				}
			}
			if (!groundingPossible)
				continue;

			/*
			 * since a grounding is possible, add the mappings to $mapping that
			 * are required for that step
			 */
			Hashtable<VariableParam, ConstantParam> mapping2 = new Hashtable<>();
			mapping2.putAll(mapping);
			boolean validGrounding = true;
			for (int i = 0; i < grounding.size(); i++) {
				if (grounding.get(i) != null) // ignore parameters that have
												// already been ground
					continue;
				VariableParam var = (VariableParam) params.get(i);
				if (mapping2.containsKey(var)) { // this step is entered if
													// during grounding the
													// literal, one variable is
													// bound more than once.
													// This is only possible if
													// the used constants are
													// equal
					if (!mapping2.get(var).equals(constantsOfLiteralInState.get(i))) { // here,
																						// the
																						// same
																						// variable
																						// is
																						// ground
																						// to
																						// different
																						// constants
						validGrounding = false;
						break;
					} else
						; // here, everything is finde, because the variable was
							// ground twice
				} else
					mapping2.put(var, constantsOfLiteralInState.get(i));
			}
			if (!validGrounding)
				continue;

			/*
			 * calculate recursively all knowledge that can be derived from that
			 */
			fb.addAll(getFactsByRule(premise, conclusion, facts, mapping2, depth + 1));
		}

		/* return the gathered knowledge */
		return fb;
	}

	private List<ConstantParam> getConstants() {
		List<ConstantParam> constants = new LinkedList<>();
		for (HornFormula rb : this.ruleBases)
			constants.addAll(rb.getConstantParams());
		for (Monom fb : this.factBases)
			constants.addAll(fb.getConstantParams());
		return constants;
	}

	@Override
	public String toString() {
		return "Reasoner [usedFacts=" + usedFacts + ", ruleBases=" + ruleBases + "]";
	}

}