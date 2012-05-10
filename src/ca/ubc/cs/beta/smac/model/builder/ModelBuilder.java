package ca.ubc.cs.beta.smac.model.builder;

import ca.ubc.cs.beta.models.fastrf.RandomForest;

public interface ModelBuilder {

	RandomForest getRandomForest();

	RandomForest getPreparedRandomForest();

}
