package de.upb.crc901.mascot.template.instantiator.searches;

import gnu.trove.set.hash.TIntHashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;

import org.sat4j.core.VecInt;

import de.upb.crc901.mascot.logic.DimacsCNF;


public class AdmissiblePredicateSearch {
	
	public Collection<TIntHashSet> getMinimalSetsOfPredicatesNecessaryToCompleteRule(DimacsCNF premise, int conclusion, DimacsCNF knowledge) {
		Queue<TIntHashSet> open = new LinkedList<TIntHashSet>();
		TIntHashSet initialRestProblem = new TIntHashSet(new int[]{conclusion});
		open.add(initialRestProblem);
		
		Collection<TIntHashSet> solutions = new ArrayList<>();
		
		while (!open.isEmpty()) {
			TIntHashSet restProblem = open.poll();
			
			/* does this clause has exactly one literal that occurs in the rest problem? */
			for (VecInt clause : knowledge) {
				
				/* compute index of only literal in common between the clause and the rest problem. If there is none or more than one, indexOfClause retains -1 */
				int indexOfLiteral = -1;
				for (int i = 0; i < clause.size(); i++) {
					if (restProblem.contains(clause.get(i))) {
						if (indexOfLiteral < 0)
							indexOfLiteral = i;
						else {
							indexOfLiteral = -1;
							break;
						}
					}
				}
				
				/* if there is no unique index of a literal, go to next clause */
				if (indexOfLiteral < 0)
					continue;
				
				/* otherwise create new rest problem that contains the negation of all the other literals */
				TIntHashSet newRestProblem = new TIntHashSet(restProblem);
				newRestProblem.remove(clause.get(indexOfLiteral));
				for (int i = 0; i < clause.size(); i++) {
					if (i != indexOfLiteral) {
						newRestProblem.add(clause.get(i) * -1);
					}
				}
				solutions.add(newRestProblem);
				open.add(newRestProblem);
			}
		}
		return solutions;
	}

}