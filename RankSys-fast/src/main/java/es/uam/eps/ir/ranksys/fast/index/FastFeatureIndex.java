/* 
 * Copyright (C) 2015 Information Retrieval Group at Universidad Autonoma
 * de Madrid, http://ir.ii.uam.es
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package es.uam.eps.ir.ranksys.fast.index;

import es.uam.eps.ir.ranksys.core.index.FeatureIndex;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Fast version of FeatureIndex, where features are internally represented with 
 * numerical indices from 0 (inclusive) to the number of indexed features
 * (exclusive).
 *
 * @author Saúl Vargas (saul.vargas@uam.es)
 * 
 * @param <F> type of the features
 */
public interface FastFeatureIndex<F> extends FeatureIndex<F> {

    @Override
    public default boolean containsFeature(F f) {
        return feature2fidx(f) >= 0;
    }

    @Override
    public default Stream<F> getAllFeatures() {
        return IntStream.range(0, numFeatures()).mapToObj(fidx -> fidx2feature(fidx));
    }

    /**
     * Returns the index assigned to the feature.
     *
     * @param f feature
     * @return the index of the feature, or -1 if the feature does not exist
     */
    public int feature2fidx(F f);

    /**
     * Returns the feature represented with the index.
     *
     * @param fidx feature index
     * @return the feature whose index is fidx
     */
    public F fidx2feature(int fidx);

}
