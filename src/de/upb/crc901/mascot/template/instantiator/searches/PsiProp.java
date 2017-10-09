package de.upb.crc901.mascot.template.instantiator.searches;

import java.util.HashMap;
import java.util.Map;

import de.upb.crc901.mascot.logic.DimacsCNF;

@SuppressWarnings("serial")
public class PsiProp extends HashMap<String, DimacsCNF> {

	public PsiProp() {
		super();
	}
	
	public PsiProp(Map<String,DimacsCNF> map) {
		super(map);
	}

	public PsiProp(PsiProp clone) {
		super(clone);
	}
}