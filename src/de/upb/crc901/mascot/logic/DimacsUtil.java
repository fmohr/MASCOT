package de.upb.crc901.mascot.logic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sat4j.core.VecInt;

import gnu.trove.list.array.TIntArrayList;
import jaicore.basic.SetUtil;
import jaicore.logic.fol.structure.Literal;

public class DimacsUtil {

	public static Map<Integer, Collection<DimacsCNF>> createPossibleCNFsOrderedBySize(Collection<Integer> predicates, int maxLength, boolean allowDisjunctions, boolean allowNegations) throws InterruptedException {
		List<Set<Integer>> possibleClauses = getPossibleClauses(predicates, allowDisjunctions ? maxLength : 1, allowNegations);
		Map<Integer,Collection<DimacsCNF>> dimacsCNFBySize = new HashMap<>();
		Map<Integer,Set<Set<Set<Integer>>>> cnfsBySize = getPossibleCNFs(groupClausesBySize(possibleClauses), maxLength);
		
		/* convert the CNFs into dimacs format that can be used with SAT4J */
		for (int size : cnfsBySize.keySet()) {
			ArrayList<DimacsCNF> convertedCNFs = new ArrayList<>();
			for (Set<Set<Integer>> cnf : cnfsBySize.get(size)) {
				convertedCNFs.add(convertIntCNFToDimacsCNF(cnf));
			}
			dimacsCNFBySize.put(size, convertedCNFs);
		}
		return dimacsCNFBySize;
	}
	
	public static Set<Set<Set<Integer>>> getPossibleCNFs(Map<Integer,List<Set<Integer>>> clauses, int length, Map<Integer,Set<Set<Set<Integer>>>> cnfsOfLessSizes) {
		
		Set<Set<Set<Integer>>> cnfsOfSizei = new HashSet<>();
		for (int j = 0; j < length; j++) {
			
			/* consider all cnfs of size j */
			for (Set<Set<Integer>> cnf : cnfsOfLessSizes.get(j)) {
				
				/* consider all clauses of size i - j, if these exist, which could be used to obtain a new cnf of size exactly i */
				if (!clauses.containsKey(length - j))
					continue;
				for (Set<Integer> clause : clauses.get(length - j)) {
					Set<Set<Integer>> newCNF = new HashSet<>(cnf);
					newCNF.add(clause);
					if (newCNF.size() > cnf.size())
						cnfsOfSizei.add(newCNF);
				}
			}
		}
		return cnfsOfSizei;
	}
	
	/**
	 * expects clauses accessible by size 
	 * 
	 * @param clauses
	 * @param maxlength
	 * @return
	 */
	public static Map<Integer, Set<Set<Set<Integer>>>> getPossibleCNFs(Map<Integer,List<Set<Integer>>> clauses, int maxlength) {
		
		Map<Integer,Set<Set<Set<Integer>>>> cnfs = new HashMap<>();
		
		/* initialize dynamic programming with cnf of size zero */
		Set<Set<Integer>> emptyCNF = new HashSet<>();
		Set<Set<Set<Integer>>> cnfsOfSizeZero = new HashSet<>();
		cnfsOfSizeZero.add(emptyCNF);
		cnfs.put(0, cnfsOfSizeZero);
		
		for (int i = 1; i <= maxlength; i++) {
			
			/* now compute the cnfs that have EXACTLY size i */
			cnfs.put(i, getPossibleCNFs(clauses, i, cnfs));
		}
		cnfs.remove(0);
		return cnfs;
	}
	
	public static Map<Integer,List<Set<Integer>>> groupClausesBySize(List<Set<Integer>> clauses) {
		Map<Integer,List<Set<Integer>>> map = new HashMap<>();
		for (Set<Integer> clause : clauses) {
			int size = clause.size();
			if (!map.containsKey(size))
				map.put(size, new ArrayList<>());
			map.get(size).add(clause);
		}
		return map;
	}
	
	public static List<Set<Integer>> getPossibleClauses(Collection<Integer> predicates, int maxlength, boolean allowNegations) throws InterruptedException {
		List<Set<Integer>> newClauses = new ArrayList<>();
		for (int i = 1; i <= maxlength; i++)
			newClauses.addAll(getPossibleClausesOfSize(predicates, i, allowNegations));
		return newClauses;
	}
	
	public static Collection<Set<Integer>> getPossibleClausesOfSize(Collection<Integer> predicates, int length, boolean allowNegations) throws InterruptedException {
		Collection<Integer> literals = new ArrayList<>(predicates);
		if (allowNegations) {
			for (int predicate : predicates)
				literals.add(predicate * -1);
		}
		return SetUtil.subsetsOfSize(literals, length);
	}
	
	public static VecInt addLiteralToClause(VecInt clause, int literal) {
		int[] lits = new int[clause.size() + 1];
		for (int j = 0; j < clause.size(); j++) {
			lits[j] = clause.get(j);
		}
		lits[clause.size()] = literal;
		return new VecInt(lits);
	}

	public static DimacsCNF convertIntLiteralToDimacsCNF(int literal) {
		DimacsCNF cnfForSAT4J = new DimacsCNF();
		cnfForSAT4J.add(new VecInt(new int[] { literal }));
		return cnfForSAT4J;
	}
	
	public static DimacsCNF convertTIntCNFToDimacsCNF(Collection<TIntArrayList> cnf) {
		DimacsCNF cnfForSAT4J = new DimacsCNF();
		for (TIntArrayList clause : cnf) {
			int[] clauseAsArray = new int[clause.size()];
			for (int i = 0; i < clause.size(); i++) {
				clauseAsArray[i] = clause.get(i);
			}
			cnfForSAT4J.add(new VecInt(clauseAsArray));
		}
		return cnfForSAT4J;
	}

	public static DimacsCNF convertIntCNFToDimacsCNF(Collection<? extends Collection<Integer>> cnf) {
		DimacsCNF cnfForSAT4J = new DimacsCNF();
		for (Collection<Integer> clause : cnf) {
			int[] clauseAsArray = new int[clause.size()];
			int i = 0;
			for (Integer literal : clause) {
				clauseAsArray[i++] = literal;
			}
			cnfForSAT4J.add(new VecInt(clauseAsArray));
		}
		return cnfForSAT4J;
	}

	public static List<List<Literal>> decodeDimacs(DimacsCNF cnf, Map<Integer,String> dimacsDecode) {
		List<List<Literal>> out = new ArrayList<>();
		for (VecInt dimClause : cnf) {
			List<Literal> intClause = new ArrayList<>();
			for (int i = 0; i < dimClause.size(); i++)
				intClause.add(new Literal(dimacsDecode.get(dimClause.get(i))));
			out.add(intClause);
		}
		return out;
	}
}
