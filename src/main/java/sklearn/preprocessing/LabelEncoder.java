/*
 * Copyright (c) 2015 Villu Ruusmann
 *
 * This file is part of JPMML-SkLearn
 *
 * JPMML-SkLearn is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JPMML-SkLearn is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with JPMML-SkLearn.  If not, see <http://www.gnu.org/licenses/>.
 */
package sklearn.preprocessing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import org.dmg.pmml.DataType;
import org.dmg.pmml.DerivedField;
import org.dmg.pmml.MapValues;
import org.dmg.pmml.OpType;
import org.jpmml.converter.CategoricalFeature;
import org.jpmml.converter.ContinuousFeature;
import org.jpmml.converter.Feature;
import org.jpmml.converter.FeatureUtil;
import org.jpmml.converter.PMMLUtil;
import org.jpmml.converter.TypeUtil;
import org.jpmml.sklearn.ClassDictUtil;
import org.jpmml.sklearn.SkLearnEncoder;
import sklearn.Transformer;

public class LabelEncoder extends Transformer {

	public LabelEncoder(String module, String name){
		super(module, name);
	}

	@Override
	public OpType getOpType(){
		return OpType.CATEGORICAL;
	}

	@Override
	public DataType getDataType(){
		List<?> classes = getClasses();

		return TypeUtil.getDataType(classes, DataType.STRING);
	}

	@Override
	public List<Feature> encodeFeatures(List<Feature> features, SkLearnEncoder encoder){
		List<?> classes = getClasses();

		ClassDictUtil.checkSize(1, features);

		Feature feature = features.get(0);

		List<Object> inputCategories = new ArrayList<>();
		List<Integer> outputCategories = new ArrayList<>();

		for(int i = 0; i < classes.size(); i++){
			inputCategories.add(classes.get(i));
			outputCategories.add(i);
		}

		Supplier<MapValues> mapValuesSupplier = () -> {
			encoder.toCategorical(feature.getName(), inputCategories);

			return PMMLUtil.createMapValues(feature.getName(), inputCategories, outputCategories);
		};

		DerivedField derivedField = encoder.ensureDerivedField(FeatureUtil.createName("label_encoder", feature), OpType.CATEGORICAL, DataType.INTEGER, mapValuesSupplier);

		Feature encodedFeature = new CategoricalFeature(encoder, derivedField, outputCategories);

		Feature result = new CategoricalFeature(encoder, feature, inputCategories){

			@Override
			public ContinuousFeature toContinuousFeature(){
				return encodedFeature.toContinuousFeature();
			}
		};

		return Collections.singletonList(result);
	}

	public List<?> getClasses(){
		return getArray("classes_");
	}
}
