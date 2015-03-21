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
package es.uam.eps.ir.ranksys.novelty.temporal.reranking;

import es.uam.eps.ir.ranksys.novdiv.itemnovelty.ItemNovelty;
import es.uam.eps.ir.ranksys.novdiv.itemnovelty.reranking.ItemNoveltyReranker;

/**
 *
 * @author Saúl Vargas (saul.vargas@uam.es)
 */
public class TemporalItemNoveltyReranker<U, I> extends ItemNoveltyReranker<U, I> {

    public TemporalItemNoveltyReranker(double lambda, ItemNovelty<U, I> novelty, boolean norm) {
        super(lambda, novelty, norm);
    }

    public TemporalItemNoveltyReranker(double lambda, ItemNovelty<U, I> novelty, int cutoff, boolean norm) {
        super(lambda, novelty, cutoff, norm);
    }

}
