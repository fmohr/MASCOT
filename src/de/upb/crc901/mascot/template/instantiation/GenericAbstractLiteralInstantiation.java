package de.upb.crc901.mascot.template.instantiation;

import de.upb.crc901.mascot.logic.Rule;
import de.upb.crc901.mascot.logic.RuleSet;
import de.upb.crc901.mascot.structure.GenericAbstractLiteral;
import jaicore.logic.fol.structure.CNFFormula;
import jaicore.logic.fol.structure.Clause;

public class GenericAbstractLiteralInstantiation {
	private GenericAbstractLiteral genericLiteral;
	private CNFFormula formula;

	public GenericAbstractLiteralInstantiation(GenericAbstractLiteral genericLiteral, CNFFormula formula) {
		super();
		this.genericLiteral = genericLiteral;
		this.formula = formula;
	}

	public GenericAbstractLiteral getGenericLiteral() {
		return genericLiteral;
	}

	public CNFFormula getFormula() {
		return formula;
	}

	public RuleSet getImpliedRules() {

		/*
		 * get literal used to represent preconditions and postconditions of
		 * component call
		 */
		RuleSet equivalences = new RuleSet();
		equivalences.add(new Rule(new CNFFormula(new Clause(genericLiteral)), formula));
		equivalences.add(new Rule(formula, new CNFFormula(new Clause(genericLiteral))));
		return equivalences;
	}

	@Override
	public String toString() {
		return "GenericHelperLiteralInstantiation [" + genericLiteral + "/" + formula + "]";
	}
}
