package de.upb.crc901.mascot.template.completeInstantiator;

/**
 * @author Felix
 *
 */
public class InstantiationRestProblem {
	// private static int counter = 0;
	// private int id = counter++;
	// private InstantiationRestProblem parent;
	// private GenericOperationCallInstantiation
	// performedComponentCallInstantiation;
	// private GenericBooleanExpressionInstantiation
	// performedLiteralInstantiation;
	// private GenericAbstractLiteralInstantiation
	// performedHelperLiteralInstantiation;
	// private Template template;
	//
	// public InstantiationRestProblem(
	// InstantiationRestProblem parent,
	// GenericBooleanExpressionInstantiation performedLiteralInstantiation,
	// Template restProblem) {
	// super();
	// this.parent = parent;
	// this.performedLiteralInstantiation = performedLiteralInstantiation;
	// this.template = restProblem;
	// }
	//
	// public InstantiationRestProblem(
	// InstantiationRestProblem parent,
	// GenericOperationCallInstantiation performedComponentCallInstantiation,
	// Template restProblem) {
	// super();
	// this.parent = parent;
	// this.performedComponentCallInstantiation =
	// performedComponentCallInstantiation;
	// this.template = restProblem;
	// }
	//
	// public InstantiationRestProblem(
	// InstantiationRestProblem parent,
	// GenericAbstractLiteralInstantiation performedHelperLiteralInstantiation,
	// Template restProblem) {
	// super();
	// this.parent = parent;
	// this.performedHelperLiteralInstantiation =
	// performedHelperLiteralInstantiation;
	// this.template = restProblem;
	// }
	//
	// public InstantiationRestProblem(Template restProblem) {
	// super();
	// this.template = restProblem;
	// }
	//
	// public InstantiationRestProblem getParent() {
	// return parent;
	// }
	//
	// public GenericOperationCallInstantiation
	// getPerformedComponentCallInstantiation() {
	// return performedComponentCallInstantiation;
	// }
	//
	// public GenericBooleanExpressionInstantiation
	// getPerformedLiteralInstantiation() {
	// return performedLiteralInstantiation;
	// }
	//
	// public GenericAbstractLiteralInstantiation
	// getPerformedHelperLiteralInstantiation() {
	// return performedHelperLiteralInstantiation;
	// }
	//
	// public Template getTemplate() {
	// return template;
	// }
	//
	// public RuleSet getImpliedRules() {
	// if (this.parent == null)
	// return new RuleSet();
	// if (this.performedComponentCallInstantiation != null) {
	// RuleSet answer = this.parent.getImpliedRules();
	// answer.add(this.performedComponentCallInstantiation.getImpliedRules());
	// return answer;
	// }
	// else if (this.performedLiteralInstantiation != null) {
	// RuleSet answer = this.parent.getImpliedRules();
	// answer.add(this.performedLiteralInstantiation.getImpliedRules());
	// return answer;
	// }
	// else if (this.performedHelperLiteralInstantiation != null) {
	// RuleSet answer = this.parent.getImpliedRules();
	// answer.add(this.performedHelperLiteralInstantiation.getImpliedRules());
	// return answer;
	// }
	// System.out.println("This should not happen!");
	// return null;
	// }
	//
	// @Override
	// public String toString() {
	// String action = "";
	// if (performedComponentCallInstantiation != null)
	// action = ", action = " + performedComponentCallInstantiation;
	// if (performedLiteralInstantiation != null)
	// action = ", action = " + performedLiteralInstantiation;
	// if (performedHelperLiteralInstantiation != null)
	// action = ", action = " + performedHelperLiteralInstantiation;
	// return "IRP " + id + "[parent=" + parent
	// + action + "]";
	// }
	//
	// public InstantiationRestProblem
	// calculateRestProblemForComponentInstantiation
	// (GenericOperationCallInstantiation instantiation, RuleSet
	// domainKnowledge) {
	// LiteralSet[][] newRules = calculateNewConsistencyRules(template,
	// domainKnowledge, this.parent != null ? this.parent.getImpliedRules() :
	// null, instantiation.getImpliedRules());
	//
	// /* add subproblem to open list */
	// List<GenericOperationCall> missingComponents = template.getComponents();
	// List<GenericOperationCall> reducedListOfMissingComponents = new
	// LinkedList<GenericOperationCall>();
	// for (int i = 1; i < missingComponents.size(); i++)
	// if (missingComponents.get(i) != instantiation.getGenericComponentCall())
	// reducedListOfMissingComponents.add(missingComponents.get(i));
	//
	// /* replace abstract precondition and postcondition predicate of the
	// generic component call by the concrete predicate in the preconditions,
	// the postconditions, and the rules of the template */
	// LiteralSet newPreconditions =
	// concretizeAbstractPreconditionsAndPostconditionsOfComponentInstantiation(instantiation,
	// template.getPreconditions());
	// LiteralSet newPostconditions =
	// concretizeAbstractPreconditionsAndPostconditionsOfComponentInstantiation(instantiation,
	// template.getEffects());
	// for (int i = 0; i < Math.max(newRules[0].length, newRules[2].length);
	// i++) {
	// if (newRules[0].length > i) {
	// newRules[0][i] =
	// concretizeAbstractPreconditionsAndPostconditionsOfComponentInstantiation(instantiation,
	// newRules[0][i]);
	// newRules[1][i] =
	// concretizeAbstractPreconditionsAndPostconditionsOfComponentInstantiation(instantiation,
	// newRules[1][i]);
	// }
	// if (newRules[2].length > i) {
	// newRules[2][i] =
	// concretizeAbstractPreconditionsAndPostconditionsOfComponentInstantiation(instantiation,
	// newRules[2][i]);
	// newRules[3][i] =
	// concretizeAbstractPreconditionsAndPostconditionsOfComponentInstantiation(instantiation,
	// newRules[3][i]);
	// }
	// }
	//
	// /* update rules again (necessary in addition to the one at the beginning
	// of the method, because the concretization of the component has
	// concequences for the rules) */
	// LiteralSet[][] fbtmp1 = {newRules[0], newRules[1]};
	// LiteralSet[][] fbtmp2 = {newRules[2], newRules[3]};
	// newRules = calculateNewConsistencyRules(fbtmp1, fbtmp2, domainKnowledge,
	// this.parent != null ? this.parent.getImpliedRules() : null,
	// instantiation.getImpliedRules());
	//
	// /* create new template with one placeholder less */
	// Template templateOfRestProblem = new Template(template.getInputs(),
	// template.getOutputs(), newPreconditions, newPostconditions,
	// reducedListOfMissingComponents, template.getBooleanExpressions(),
	// template.getHelperPredicates(), newRules[0], newRules[1], newRules[2],
	// newRules[3]);
	// return new InstantiationRestProblem(this, instantiation,
	// templateOfRestProblem);
	// }
	//
	// public InstantiationRestProblem
	// calculateRestProblemForBooleanExpressionInstantiation
	// (GenericBooleanExpressionInstantiation toReplace, RuleSet
	// domainKnowledge) {
	//
	// /* determine reduced consistency rules */
	// LiteralSet[][] newRules = calculateNewConsistencyRules(template,
	// domainKnowledge, this.parent != null ? this.parent.getImpliedRules() :
	// null, toReplace.getImpliedRules());
	//
	// /* add subproblem to open list */
	// List<GenericBooleanExpression> missingBooleanExpressions =
	// template.getBooleanExpressions();
	// List<GenericBooleanExpression> reducedListOfMissingBooleanExpressions =
	// new LinkedList<GenericBooleanExpression>();
	// for (int i = 1; i < missingBooleanExpressions.size(); i++)
	// if (missingBooleanExpressions.get(i) != toReplace.getGenericLiteral())
	// reducedListOfMissingBooleanExpressions.add(missingBooleanExpressions.get(i));
	// Template templateOfRestProblem = new Template(template.getInputs(),
	// template.getOutputs(), template.getPreconditions(),
	// template.getEffects(), template.getComponents(),
	// reducedListOfMissingBooleanExpressions, template.getHelperPredicates(),
	// newRules[0], newRules[1], newRules[2], newRules[3]);
	// return new InstantiationRestProblem(this, toReplace,
	// templateOfRestProblem);
	// }
	//
	// public InstantiationRestProblem
	// calculateRestProblemForHelperPredicateInstantiation
	// (GenericAbstractLiteralInstantiation toReplace, RuleSet domainKnowledge)
	// {
	//
	// /* determine reduced consistency rules */
	// LiteralSet[][] newRules = calculateNewConsistencyRules(template,
	// domainKnowledge, this.parent != null ? this.parent.getImpliedRules() :
	// null, toReplace.getImpliedRules());
	//
	// /* add subproblem to open list */
	// List<GenericAbstractLiteral> missingHelperPredicates =
	// template.getHelperPredicates();
	// List<GenericAbstractLiteral> reducedListOfMissingHelperPredicates = new
	// LinkedList<GenericAbstractLiteral>();
	// for (int i = 1; i < reducedListOfMissingHelperPredicates.size(); i++)
	// if (missingHelperPredicates.get(i) != toReplace.getGenericLiteral())
	// reducedListOfMissingHelperPredicates.add(missingHelperPredicates.get(i));
	// Template templateOfRestProblem = new Template(template.getInputs(),
	// template.getOutputs(), template.getPreconditions(),
	// template.getEffects(), template.getComponents(),
	// template.getBooleanExpressions(), reducedListOfMissingHelperPredicates,
	// newRules[0], newRules[1], newRules[2], newRules[3]);
	// return new InstantiationRestProblem(this, toReplace,
	// templateOfRestProblem);
	// }
	//
	// private static LiteralSet
	// concretizeAbstractPreconditionsAndPostconditionsOfComponentInstantiation(GenericOperationCallInstantiation
	// instantiation, LiteralSet fb) {
	//
	// GenericOperationCall genericComponentCall =
	// instantiation.getGenericComponentCall();
	// OperationCall componentCall = instantiation.getComponentCall();
	// LiteralSet fbConcretized = new LiteralSet();
	//
	// String predicateNameForPrecondition = "PRE_" +
	// genericComponentCall.getName() + "_" +
	// componentCall.getOperation().getName();
	// String predicateNameForPostcondition = "POST_" +
	// genericComponentCall.getName() + "_" +
	// componentCall.getOperation().getName();
	//
	// /* recalculate preconditions, effects, and rules by replacing the
	// placeholder of the precondition/effect of the component by the concrete
	// predicate */
	// for (Literal l : fb.getFacts()) {
	//
	// /* if the literal matches the predicate for the precondition of the
	// substituted generic component call, replace it */
	// if (l.getPredicate() ==
	// genericComponentCall.getPreconditions().getPredicate()) {
	// String variables = "";
	// for (String var : l.getVariables()) {
	// if (var.startsWith("'<") && var.endsWith(">'")) {
	// String varCoreName = var.substring(2, var.length() - 2);
	// for (int i = 0; i <
	// instantiation.getNumberOfVariableInputsThatMustBeIntroduced(); i++)
	// variables += (variables.length() > 0 ? ", " : "") + "'" + varCoreName +
	// (i+1) + "'";
	// }
	// else
	// variables += (variables.length() > 0 ? ", " : "") + var;
	// }
	// try {
	// fbConcretized.add(Literal.get(predicateNameForPrecondition + "(" +
	// variables + ")"));
	// }
	// catch (Exception e){e.printStackTrace();}
	// }
	//
	// /* if the literal matches the predicate for the postcondition of the
	// substituted generic component call, replace it */
	// else if (l.getPredicate() ==
	// genericComponentCall.getEffects().getPredicate()) {
	// String variables = "";
	// boolean scannedVariableInputs = false;
	// for (String var : l.getVariables()) {
	// if (var.startsWith("'<") && var.endsWith(">'")) {
	// String varCoreName = var.substring(2, var.length() - 2);
	//
	// /* first, replace the variable inputs by the concrete further inputs */
	// if (!scannedVariableInputs) {
	// scannedVariableInputs = true;
	// for (int i = 0; i <
	// instantiation.getNumberOfVariableInputsThatMustBeIntroduced(); i++)
	// variables += (variables.length() > 0 ? ", " : "") + "'" + varCoreName +
	// (i+1) + "'";
	// }
	//
	// /* second, replace the variable outputs by the concrete further outputs
	// */
	// else {
	// for (int i = 0; i <
	// instantiation.getNumberOfVariableOutputsThatMustBeIntroduced(); i++)
	// variables += (variables.length() > 0 ? ", " : "") + "'" + varCoreName +
	// (i+1) + "'";
	// }
	// }
	// else
	// variables += (variables.length() > 0 ? ", " : "") + var;
	// }
	// try {
	// fbConcretized.add(Literal.get(predicateNameForPostcondition + "(" +
	// variables + ")"));
	// }
	// catch (Exception e){e.printStackTrace();}
	// }
	//
	// /* otherwise, just copy the literal to the new precondition */
	// else {
	// fbConcretized.add(l);
	// }
	// }
	// return fbConcretized;
	// }
	//
	// private static LiteralSet[][] calculateNewConsistencyRules(Template
	// template, RuleSet domainKnowledge, RuleSet rulesImpliedByParents, RuleSet
	// rulesImpliedByInstantiation) {
	// LiteralSet[] currentPositivePreconditions =
	// template.getPreconditionsOfPositiveConsistencyRules();
	// LiteralSet[] currentPositivePostconditions =
	// template.getPostconditionsOfPositiveConsistencyRules();
	// LiteralSet[] currentNegativePreconditions =
	// template.getPreconditionsOfNegativeConsistencyRules();;
	// LiteralSet[] currentNegativePostconditions =
	// template.getPostconditionsOfNegativeConsistencyRules();
	//
	// LiteralSet[][] currentPositiveRules = new
	// LiteralSet[2][currentPositivePreconditions.length];
	// currentPositiveRules[0] = currentPositivePreconditions;
	// currentPositiveRules[1] = currentPositivePostconditions;
	// LiteralSet[][] currentNegativeRules = new
	// LiteralSet[2][currentNegativePreconditions.length];
	// currentNegativeRules[0] = currentNegativePreconditions;
	// currentNegativeRules[1] = currentNegativePostconditions;
	// return calculateNewConsistencyRules(currentPositiveRules,
	// currentNegativeRules, domainKnowledge, rulesImpliedByParents,
	// rulesImpliedByInstantiation);
	// }
	//
	// private static LiteralSet[][] calculateNewConsistencyRules(LiteralSet[][]
	// currentPositiveRules, LiteralSet[][] currentNegativeRules, RuleSet
	// domainKnowledge, RuleSet rulesImpliedByParents, RuleSet
	// rulesImpliedByInstantiation) {
	//
	// LiteralSet[] currentPositivePreconditions = currentPositiveRules[0];
	// LiteralSet[] currentPositivePostconditions = currentPositiveRules[1];
	// LiteralSet[] currentNegativePreconditions = currentNegativeRules[0];
	// LiteralSet[] currentNegativePostconditions = currentNegativeRules[1];
	//
	// /* determine reduced consistency rules */
	// Reasoner reasoner = new Reasoner();
	// reasoner.addRuleBase(domainKnowledge);
	// if (rulesImpliedByParents != null)
	// reasoner.addRuleBase(rulesImpliedByParents);
	// reasoner.addRuleBase(rulesImpliedByInstantiation);
	//
	// /* create fact bases for preconditions and effects of positive
	// consistency rules */
	// LiteralSet[] newPositivePreconditionOfRules = new
	// LiteralSet[currentPositivePreconditions.length];
	// LiteralSet[] newPositivePostconditionOfRules = new
	// LiteralSet[currentPositivePostconditions.length];
	// if (newPositivePostconditionOfRules.length !=
	// newPositivePostconditionOfRules.length) {
	// try { throw new Exception(); }
	// catch (Exception e) { e.printStackTrace(); System.exit(1); }
	// }
	// for (int i = 0; i < currentPositivePreconditions.length; i++) {
	// LiteralSet starting = currentPositivePreconditions[i];
	// reasoner.addFactBase(starting);
	// newPositivePreconditionOfRules[i] = reasoner.forwardChaining();
	// newPositivePostconditionOfRules[i] = new LiteralSet();
	// for (Literal fact : currentPositivePostconditions[i].getFacts()) {
	// if (!newPositivePreconditionOfRules[i].contains(fact) &&
	// !newPositivePreconditionOfRules[i].contains(fact.getNegatedClone()))
	// newPositivePostconditionOfRules[i].add(fact);
	// }
	// reasoner.rmFactBase(starting);
	// }
	//
	// /* create fact bases for preconditions and effects of negative
	// consistency rules */
	// LiteralSet[] newNegativePreconditionOfRules = new
	// LiteralSet[currentNegativePreconditions.length];
	// LiteralSet[] newNegativePostconditionOfRules = new
	// LiteralSet[currentNegativePostconditions.length];
	// if (newNegativePostconditionOfRules.length !=
	// newNegativePostconditionOfRules.length) {
	// try { throw new Exception(); }
	// catch (Exception e) { e.printStackTrace(); System.exit(1); }
	// }
	// for (int i = 0; i < currentNegativePreconditions.length; i++) {
	// LiteralSet starting = currentNegativePreconditions[i];
	// reasoner.addFactBase(starting);
	// newNegativePreconditionOfRules[i] = reasoner.forwardChaining();
	// newNegativePostconditionOfRules[i] = new LiteralSet();
	// for (Literal fact : currentNegativePostconditions[i].getFacts()) {
	// if (!newNegativePreconditionOfRules[i].contains(fact) &&
	// !newNegativePreconditionOfRules[i].contains(fact.getNegatedClone()))
	// newNegativePostconditionOfRules[i].add(fact);
	// }
	// reasoner.rmFactBase(starting);
	// }
	//
	// LiteralSet[][] newRules = new
	// LiteralSet[4][Math.max(newPositivePreconditionOfRules.length,
	// newNegativePostconditionOfRules.length)];
	// newRules[0] = newPositivePreconditionOfRules;
	// newRules[1] = newPositivePostconditionOfRules;
	// newRules[2] = newNegativePreconditionOfRules;
	// newRules[3] = newNegativePostconditionOfRules;
	// return newRules;
	// }
	//
	// public int getId() {
	// return id;
	// }
	//
	// public boolean isPredicateUnboundInRestproblem(Predicate p) {
	// return this.template.isPredicateUnbound(p);
	// }
	//
	// public boolean hasUnsatisfiedPositiveConsistencyRules() {
	// for (LiteralSet effect :
	// this.template.getPostconditionsOfPositiveConsistencyRules())
	// if (!effect.isEmpty())
	// return true;
	// return false;
	// }
	//
	// /**
	// * A consistency rule is unsatisfiable if all domain-UNspecific predicates
	// have been interpreted,
	// * but the effect has not been infered.
	// * @return
	// */
	// public boolean hasUnsatisfiablePositiveConsistencyRule() {
	// LiteralSet[] preconditions =
	// this.template.getPreconditionsOfPositiveConsistencyRules();
	// LiteralSet[] effects =
	// this.template.getPostconditionsOfPositiveConsistencyRules();
	//
	// /* we try to find a rule with non-empty effect but completely interpreted
	// precondition */
	// for (int i = 0; i < preconditions.length; i++) {
	// if (effects[i].isEmpty())
	// continue;
	// boolean allAbstractPredicatesAreBound = true;
	// for (Literal l : preconditions[i].getFacts()) {
	// Predicate p = l.getPredicate();
	// if (p.isDomainDescribingPredicate())
	// continue;
	// if (this.isPredicateUnboundInRestproblem(p)) {
	// allAbstractPredicatesAreBound = false;
	// break;
	// }
	// }
	// for (Literal l : effects[i].getFacts()) {
	// Predicate p = l.getPredicate();
	// if (p.isDomainDescribingPredicate())
	// continue;
	// if (this.isPredicateUnboundInRestproblem(p)) {
	// allAbstractPredicatesAreBound = false;
	// break;
	// }
	// }
	// if (allAbstractPredicatesAreBound)
	// return true;
	// }
	// return false;
	// }
	//
	// public boolean hasSatisfiedNegativeConsistencyRules() {
	// for (LiteralSet effect :
	// this.template.getPostconditionsOfNegativeConsistencyRules())
	// if (effect.isEmpty())
	// return true;
	// return false;
	// }
	//
	// public boolean hasContradictoryPreconditionInPositiveConsistencyRule() {
	// for (LiteralSet precondition :
	// this.template.getPreconditionsOfPositiveConsistencyRules())
	// if (precondition.isContradictory())
	// return true;
	// return false;
	// }
}
