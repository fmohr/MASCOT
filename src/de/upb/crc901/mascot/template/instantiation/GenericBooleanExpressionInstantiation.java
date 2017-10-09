package de.upb.crc901.mascot.template.instantiation;

import de.upb.crc901.mascot.logic.Rule;
import de.upb.crc901.mascot.logic.RuleSet;
import de.upb.crc901.mascot.structure.GenericBooleanExpression;
import jaicore.logic.fol.structure.CNFFormula;
import jaicore.logic.fol.structure.Clause;

public class GenericBooleanExpressionInstantiation {
	private GenericBooleanExpression genericLiteral;
	private CNFFormula formula;

	public GenericBooleanExpressionInstantiation(GenericBooleanExpression genericLiteral, CNFFormula formula) {
		super();
		this.genericLiteral = genericLiteral;
		this.formula = formula;
	}

	public GenericBooleanExpression getGenericLiteral() {
		return genericLiteral;
	}

	public CNFFormula getFormula() {
		return formula;
	}

	public RuleSet getImpliedRules() {
		RuleSet equivalences = new RuleSet();
		equivalences.add(new Rule(new CNFFormula(new Clause(genericLiteral)), formula));
		equivalences.add(new Rule(formula, new CNFFormula(new Clause(genericLiteral))));
		return equivalences;
	}

	@Override
	public String toString() {
		return "GenericLiteralInstantiation [" + genericLiteral + "/" + this.formula + "]";
	}
}
