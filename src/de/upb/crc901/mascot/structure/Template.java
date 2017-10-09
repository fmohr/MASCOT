package de.upb.crc901.mascot.structure;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jaicore.logic.fol.structure.HornRule;
import jaicore.logic.fol.structure.LiteralSet;
import jaicore.logic.fol.structure.Monom;
import jaicore.logic.fol.structure.VariableParam;


/**
 * @author Felix
 *
 */
public class Template {

	/* specification */
	private Set<VariableParam> inputs, outputs;
	private Monom preconditions, effects;

	/* placeholders */
	private List<GenericOperationCall> components;
	private List<GenericBooleanExpression> booleanExpressions;
	private List<GenericAbstractLiteral> helperPredicates;

	/* instantiation constraints */
	private List<HornRule> positiveConsistencyRules;
	private List<HornRule> negativeConsistencyRules;

	/* accounting variables */
	private Set<GenericLiteral> genericLiterals = null;

	public Template(Set<VariableParam> inputs, Set<VariableParam> outputs, LiteralSet preconditions, LiteralSet effects,
			List<GenericOperationCall> components, List<GenericBooleanExpression> booleanExpressions,
			List<GenericAbstractLiteral> helperPredicates, List<HornRule> positiveConsistencyRules,
			List<HornRule> negativeConsistencyRules) {
		super();
		this.inputs = inputs;
		this.outputs = outputs;
		this.preconditions = new Monom(preconditions);
		this.effects = new Monom(effects);
		this.components = components;
		this.booleanExpressions = booleanExpressions;
		this.helperPredicates = helperPredicates;
		this.positiveConsistencyRules = positiveConsistencyRules;
		this.negativeConsistencyRules = negativeConsistencyRules;
	}

	public Set<VariableParam> getInputs() {
		return inputs;
	}

	public Set<VariableParam> getOutputs() {
		return outputs;
	}

	public LiteralSet getPreconditions() {
		return preconditions;
	}

	public Monom getEffects() {
		return effects;
	}

	public List<GenericOperationCall> getComponents() {
		return components;
	}

	public List<GenericBooleanExpression> getBooleanExpressions() {
		return booleanExpressions;
	}

	public List<GenericAbstractLiteral> getHelperPredicates() {
		return helperPredicates;
	}

	public List<HornRule> getPositiveConsistencyRules() {
		return positiveConsistencyRules;
	}

	public List<HornRule> getNegativeConsistencyRules() {
		return negativeConsistencyRules;
	}

	public Set<GenericLiteral> getGenericLiterals() {
		if (genericLiterals == null) {
			genericLiterals = new HashSet<>();
			for (GenericOperationCall call : components) {
				genericLiterals.add(call.getPreconditions());
				genericLiterals.add(call.getEffects());
			}
			genericLiterals.addAll(booleanExpressions);
			genericLiterals.addAll(helperPredicates);
		}
		return genericLiterals;
	}

	@Override
	public String toString() {
		return "Template [inputs=" + inputs + ", outputs=" + outputs + ", preconditions=" + preconditions + ", effects="
				+ effects + ", components=" + components + ", booleanExpressions=" + booleanExpressions
				+ ", helperPredicates=" + helperPredicates + ", positiveConsistencyRules=" + positiveConsistencyRules
				+ ", negativeConsistencyRules=" + negativeConsistencyRules + "]";
	}
}