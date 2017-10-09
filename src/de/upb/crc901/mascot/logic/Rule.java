package de.upb.crc901.mascot.logic;

import java.util.HashSet;
import java.util.Set;

import jaicore.logic.fol.structure.CNFFormula;
import jaicore.logic.fol.structure.ConstantParam;

/**
 * @author wever
 *
 */
public class Rule {

	private final CNFFormula premise;
	private final CNFFormula conclusion;

	public Rule(final CNFFormula pPremise, final CNFFormula pConclusion) {
		this.premise = pPremise;
		this.conclusion = pConclusion;
	}

	public CNFFormula getPremise() {
		return this.premise;
	}

	public CNFFormula getConclusion() {
		return this.conclusion;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append(premise);
		sb.append(" => ");
		sb.append(conclusion);

		return sb.toString();
	}

	public Set<ConstantParam> getConstantParams() {
		Set<ConstantParam> constants = new HashSet<>();
		constants.addAll(premise.getConstantParams());
		constants.addAll(conclusion.getConstantParams());
		return constants;
	}
}
