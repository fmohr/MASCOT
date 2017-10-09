package de.upb.crc901.mascot.template.instantiation;

import java.util.List;

import de.upb.crc901.mascot.structure.Template;
import jaicore.logic.fol.structure.LiteralSet;
import jaicore.logic.fol.structure.Monom;

/**
 * @author Felix
 *
 */
public class TemplateInstantiation {

	private Template template;
	private List<GenericOperationCallInstantiation> componentCalls;
	private List<GenericBooleanExpressionInstantiation> booleanExpressions;
	private List<GenericAbstractLiteralInstantiation> helperPredicates;
	private Monom precondition, effect;

	public TemplateInstantiation(Template template, List<GenericOperationCallInstantiation> componentCalls,
			List<GenericBooleanExpressionInstantiation> booleanExpressions,
			List<GenericAbstractLiteralInstantiation> helperPredicates) {
		super();
		this.template = template;
		this.componentCalls = componentCalls;
		this.booleanExpressions = booleanExpressions;
		this.helperPredicates = helperPredicates;
	}

	public Template getTemplate() {
		return template;
	}

	public List<GenericOperationCallInstantiation> getComponentCalls() {
		return componentCalls;
	}

	public List<GenericBooleanExpressionInstantiation> getBooleanExpressions() {
		return booleanExpressions;
	}

	public List<GenericAbstractLiteralInstantiation> getHelperPredicates() {
		return helperPredicates;
	}

	public LiteralSet getPrecondition() {
		if (this.precondition == null) {
			this.precondition = new Monom(this.template.getPreconditions());

			/* replace preconditions of generic components */
			for (GenericOperationCallInstantiation inst : this.componentCalls) {
				InstantiationUtil.replacePredicateInMonomIfOccurs(this.precondition,
						inst.getGenericOperationCall().getPreconditions(),
						new Monom(inst.getOperationCall().getOperation().getPrecondition().getCondition()));
			}

			/* replace boolean expressions */
			for (GenericBooleanExpressionInstantiation inst : this.booleanExpressions) {
				InstantiationUtil.replacePredicateInMonomIfOccurs(this.precondition, inst.getGenericLiteral(),
						Monom.fromCNFFormula(inst.getFormula()));
			}

			/* replace helper predicates */
			for (GenericAbstractLiteralInstantiation inst : this.helperPredicates) {
				InstantiationUtil.replacePredicateInMonomIfOccurs(this.precondition, inst.getGenericLiteral(),
						Monom.fromCNFFormula(inst.getFormula()));
			}
		}
		return this.precondition;
	}

	public LiteralSet getEffect() {
		if (this.effect == null) {
			this.effect = new Monom(this.template.getEffects());

			/* replace preconditions and effects of generic components */
			for (GenericOperationCallInstantiation inst : this.componentCalls) {
				InstantiationUtil.replacePredicateInMonomIfOccurs(this.effect,
						inst.getGenericOperationCall().getPreconditions(),
						new Monom(inst.getOperationCall().getOperation().getPrecondition().getCondition()));
				InstantiationUtil.replacePredicateInMonomIfOccurs(this.effect,
						inst.getGenericOperationCall().getEffects(),
						new Monom(inst.getOperationCall().getOperation().getEffect().getCondition()));
			}

			/* replace boolean expressions */
			for (GenericBooleanExpressionInstantiation inst : this.booleanExpressions) {
				InstantiationUtil.replacePredicateInMonomIfOccurs(this.effect, inst.getGenericLiteral(),
						Monom.fromCNFFormula(inst.getFormula()));
			}

			/* replace helper predicates */
			for (GenericAbstractLiteralInstantiation inst : this.helperPredicates) {
				InstantiationUtil.replacePredicateInMonomIfOccurs(this.effect, inst.getGenericLiteral(),
						Monom.fromCNFFormula(inst.getFormula()));
			}
		}
		return this.effect;
	}

	@Override
	public String toString() {
		return "TemplateInstantiation [\n\t" + "componentCalls=" + componentCalls + ",\n\tbooleanExpressions="
				+ booleanExpressions + ",\n\thelperPredicates=" + helperPredicates + "\n]";
	}
}