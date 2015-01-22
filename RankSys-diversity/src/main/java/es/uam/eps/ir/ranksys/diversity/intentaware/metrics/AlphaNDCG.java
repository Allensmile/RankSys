/* 
 * Copyright (C) 2014 Information Retrieval Group at Universidad Autonoma
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
package es.uam.eps.ir.ranksys.diversity.intentaware.metrics;

import es.uam.eps.ir.ranksys.core.IdDouble;
import es.uam.eps.ir.ranksys.core.feature.FeatureData;
import es.uam.eps.ir.ranksys.core.Recommendation;
import es.uam.eps.ir.ranksys.core.model.PersonalizableModel;
import es.uam.eps.ir.ranksys.core.model.PersonalizableModel.UserModel;
import es.uam.eps.ir.ranksys.metrics.AbstractRecommendationMetric;
import es.uam.eps.ir.ranksys.metrics.rank.LogarithmicDiscountModel;
import es.uam.eps.ir.ranksys.metrics.rank.RankingDiscountModel;
import es.uam.eps.ir.ranksys.metrics.rel.BinaryRelevanceModel;
import es.uam.eps.ir.ranksys.metrics.rel.IdealRelevanceModel;
import es.uam.eps.ir.ranksys.metrics.rel.IdealRelevanceModel.UserIdealRelevanceModel;
import es.uam.eps.ir.ranksys.metrics.rel.RelevanceModel.UserRelevanceModel;
import gnu.trove.impl.Constants;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

/**
 *
 * @author Saúl Vargas (saul.vargas@uam.es)
 * @author Pablo Castells (pablo.castells@uam.es)
 */
public class AlphaNDCG<U, I, F> extends AbstractRecommendationMetric<U, I> {

    private final int cutoff;
    private final double alpha;
    private final IdealRelevanceModel<U, I> relModel;
    private final FeatureData<I, F, ?> featureData;
    private final AlphaNDCGIdeal idcg;
    private final RankingDiscountModel disc = new LogarithmicDiscountModel();

    public AlphaNDCG(int cutoff, double alpha, FeatureData<I, F, ?> featureData, BinaryRelevanceModel<U, I> relModel) {
        super();
        this.cutoff = cutoff;
        this.alpha = alpha;
        this.relModel = relModel;
        this.featureData = featureData;

        this.idcg = new AlphaNDCGIdeal(relModel.isCaching(), relModel.getModeledUsers().stream());
    }

    @Override
    public double evaluate(Recommendation<U, I> recommendation) {
        UserRelevanceModel<U, I> urm = relModel.getUserModel(recommendation.getUser());

        double ndcg = 0.0;
        int rank = 0;
        TObjectIntMap<F> redundancy = new TObjectIntHashMap<>(Constants.DEFAULT_CAPACITY, Constants.DEFAULT_LOAD_FACTOR, 0);

        for (IdDouble<I> pair : recommendation.getItems()) {
            if (urm.isRelevant(pair.id)) {
                double gain = featureData.getItemFeatures(pair.id).sequential()
                        .map(fv -> fv.id)
                        .mapToDouble(f -> {
                            int r = redundancy.adjustOrPutValue(f, 1, 1) - 1;
                            return Math.pow(1 - alpha, r);
                        }).sum();
                ndcg += gain * disc.disc(rank);
            }

            rank++;
            if (rank >= cutoff) {
                break;
            }
        }
        if (ndcg > 0) {
            ndcg /= idcg.getUserModel(recommendation.getUser()).ideal;
        }

        return ndcg;
    }

    private double idcg(UserIdealRelevanceModel<U, I> urm) {
        double ideal = 0;

        TObjectIntMap<F> redundancy = new TObjectIntHashMap<>(Constants.DEFAULT_CAPACITY, Constants.DEFAULT_LOAD_FACTOR, 0);
        Set<I> candidates = new HashSet<>(urm.getRelevantItems());
        int rank = 0;

        while (rank <= cutoff && !candidates.isEmpty()) {
            I bi = null;
            double bg = Double.NEGATIVE_INFINITY;
            for (I i : candidates) {
                double gain = featureData.getItemFeatures(i)
                        .map(fv -> fv.id)
                        .mapToDouble(f -> {
                            return Math.pow(1 - alpha, redundancy.get(f));
                        }).sum();
                if (gain > bg) {
                    bg = gain;
                    bi = i;
                }
            }
            candidates.remove(bi);
            featureData.getItemFeatures(bi).sequential()
                    .map(fv -> fv.id)
                    .forEach(f -> redundancy.adjustOrPutValue(f, 1, 1));
            ideal += bg * disc.disc(rank);
            rank++;
        }

        return ideal;
    }

    private class AlphaNDCGIdeal extends PersonalizableModel<U> {

        public AlphaNDCGIdeal(boolean caching, Stream<U> users) {
            super(caching, users);
        }

        @Override
        protected UserAlphaNDCGIdeal get(U u) {
            return new UserAlphaNDCGIdeal(idcg(relModel.getUserModel(u)));
        }

        @Override
        public UserAlphaNDCGIdeal getUserModel(U u) {
            return (UserAlphaNDCGIdeal) super.getUserModel(u);
        }

        public class UserAlphaNDCGIdeal implements UserModel<U> {

            public final double ideal;

            public UserAlphaNDCGIdeal(double ideal) {
                this.ideal = ideal;
            }

        }
    }
}
