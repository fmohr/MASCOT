package de.upb.crc901.mascot.template.instantiator.searches;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import jaicore.basic.SetUtil;
import jaicore.logic.fol.structure.LiteralParam;


public class BindingSearchNode {

	/** set of abstract predicates that have completely been bound */
	private final Set<String> completedAbstractPredicates = new HashSet<>();

	/**
	 * maps every abstract predicate to the set of concrete predicates that have been bound completely
	 */
	private final Map<String, Set<String>> completedConcretePredicates = new HashMap<>();

	/**
	 * maps each combination of abstract predicate, concrete predicate, and place of the concrete predicate to a literal parameter
	 */
	private final Map<String, Map<String, Map<Integer, LiteralParam>>> mapping = new HashMap<>();

	/**
	 * encapsulates the abstract predicate name, concrete predicate name, and place of the concrete predicate for which a decision is made in this node
	 */
	private BindingSearchAction action;

	public BindingSearchNode(Map<String, Integer> concretePredicatesAndTheirArities, Map<String, List<String>> concretePredicatesAssociatedWithAbstractPredicates) {

		/*
		 * define the abstract predicates and the concrete predicates in it (with empty mappings)
		 */
		for (String abstractPredicate : concretePredicatesAssociatedWithAbstractPredicates.keySet()) {
			Map<String, Map<Integer, LiteralParam>> definition = new HashMap<>();
			for (String concretePredicate : concretePredicatesAssociatedWithAbstractPredicates.get(abstractPredicate)) {
				Map<Integer, LiteralParam> argmap = new HashMap<>();
				if (!concretePredicatesAndTheirArities.containsKey(concretePredicate))
					System.err.println(concretePredicatesAndTheirArities + "not defined for " + concretePredicate);
				for (int i = 0; i < concretePredicatesAndTheirArities.get(concretePredicate); i++) {
					argmap.put(i, null);
				}
				definition.put(concretePredicate, argmap);
			}
			this.mapping.put(abstractPredicate, definition);
			this.completedConcretePredicates.put(abstractPredicate, new HashSet<>());
		}

		/* determine first action */
		String firstPredicateInPsi2prop = concretePredicatesAssociatedWithAbstractPredicates.keySet().stream().collect(Collectors.toList()).get(0);
		String firstConcretePredicateForTheAbstractPredicate = concretePredicatesAssociatedWithAbstractPredicates.get(firstPredicateInPsi2prop).get(0);
		this.action = new BindingSearchAction(firstPredicateInPsi2prop, firstConcretePredicateForTheAbstractPredicate, 0);
	}

	public BindingSearchNode(BindingSearchNode parent, String abstractPredicate, String concretePredicate, int from, LiteralParam to) {

		/*
		 * make a deep copy of the mapping and the completion information of this node
		 */
		Map<String, Map<String, Map<Integer, LiteralParam>>> parentMap = parent.getMapping();
		Map<String, Set<String>> parentCompletion = parent.getCompletedConcretePredicates();
		completedAbstractPredicates.addAll(parent.getCompletedAbstractPredicates());
		for (String abstractPredicateToDuplicate : parentMap.keySet()) {
			Map<String, Map<Integer, LiteralParam>> definitionForAbstractPredicateToDuplicate = parentMap.get(abstractPredicateToDuplicate);
			Map<String, Map<Integer, LiteralParam>> duplicatedDefinition = new HashMap<>();
			this.mapping.put(abstractPredicateToDuplicate, duplicatedDefinition);
			this.completedConcretePredicates.put(abstractPredicateToDuplicate, new HashSet<>(parentCompletion.get(abstractPredicateToDuplicate)));
			for (String concretePredicateToDuplicate : definitionForAbstractPredicateToDuplicate.keySet()) {
				Map<Integer, LiteralParam> currentMapping = definitionForAbstractPredicateToDuplicate.get(concretePredicateToDuplicate);
				Map<Integer, LiteralParam> extendedMapping = new HashMap<>(currentMapping);
				duplicatedDefinition.put(concretePredicateToDuplicate, extendedMapping);
				if (abstractPredicate.equals(abstractPredicateToDuplicate) && concretePredicate.equals(concretePredicateToDuplicate)) {
					extendedMapping.put(from, to);
				}
			}
		}

		/* if there are arguments of this concrete predicate to be mapped */
		if (this.mapping.get(abstractPredicate).get(concretePredicate).size() > from + 1) {
			this.completedConcretePredicates.get(abstractPredicate).add(concretePredicate);
			this.action = new BindingSearchAction(abstractPredicate, concretePredicate, from + 1);
		}

		/* otherwise switch either concrete or abstract predicates */
		else {

			/*
			 * if there are concrete predicates to map for this abstract predicate, switch concrete predicate
			 */
			List<String> unboundConcretePredicates = new LinkedList<>(SetUtil.difference(this.mapping.get(abstractPredicate).keySet(),
					this.completedConcretePredicates.get(abstractPredicate)));
			if (!unboundConcretePredicates.isEmpty()) {
				String concretePredicateToMapNext = unboundConcretePredicates.get(0);
				this.action = new BindingSearchAction(abstractPredicate, concretePredicateToMapNext, 0);
			}

			/* otherwise, switch to next abstract predicate */
			else {
				this.completedAbstractPredicates.add(abstractPredicate);
				Optional<String> nextAbstractPredicate = this.completedConcretePredicates.keySet().stream()
						.filter(ap -> !completedConcretePredicates.get(ap).equals(mapping.get(ap).keySet())).findAny();
				if (nextAbstractPredicate.isPresent()) {
					String nameOfNextAbstractPredicate = nextAbstractPredicate.get();
					unboundConcretePredicates = new LinkedList<>(SetUtil.difference(this.mapping.get(nameOfNextAbstractPredicate).keySet(),
							this.completedConcretePredicates.get(nameOfNextAbstractPredicate)));
					this.action = new BindingSearchAction(nameOfNextAbstractPredicate, unboundConcretePredicates.get(0), 0);
				}
			}
		}
	}

	public Map<String, Map<String, Map<Integer, LiteralParam>>> getMapping() {
		return mapping;
	}

	public Set<String> getCompletedAbstractPredicates() {
		return this.completedAbstractPredicates;
	}

	public Map<String, Set<String>> getCompletedConcretePredicates() {
		return this.completedConcretePredicates;
	}

	public BindingSearchAction getAction() {
		return action;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((mapping == null) ? 0 : mapping.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BindingSearchNode other = (BindingSearchNode) obj;
		if (mapping == null) {
			if (other.mapping != null)
				return false;
		} else if (!mapping.equals(other.mapping))
			return false;
		return true;
	}
}