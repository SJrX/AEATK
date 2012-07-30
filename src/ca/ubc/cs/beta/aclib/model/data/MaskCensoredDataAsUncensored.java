package ca.ubc.cs.beta.aclib.model.data;

public class MaskCensoredDataAsUncensored extends AbstractSanitizedModelData {

	public MaskCensoredDataAsUncensored(SanitizedModelData smd) {
		super(smd);
	}


	@Override
	public boolean[] getCensoredResponses() {
		boolean[] censoredResponses = super.getCensoredResponses();
		return new boolean[censoredResponses.length];
	}
	
	
	

}
