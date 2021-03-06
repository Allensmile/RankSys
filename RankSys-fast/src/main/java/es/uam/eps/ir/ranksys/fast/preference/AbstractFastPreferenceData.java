/* 
 * Copyright (C) 2015 Information Retrieval Group at Universidad Autónoma
 * de Madrid, http://ir.ii.uam.es
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.ranksys.fast.preference;

import es.uam.eps.ir.ranksys.core.preference.IdPref;
import es.uam.eps.ir.ranksys.fast.index.FastItemIndex;
import es.uam.eps.ir.ranksys.fast.index.FastUserIndex;
import java.io.Serializable;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Abstract FastFeatureData, implementing the interfaces of FastUserIndex and FastItemIndex by delegating to implementations of these.
 *
 * @author Saúl Vargas (saul.vargas@uam.es)
 *
 * @param <U> type of the users
 * @param <I> type of the items
 */
public abstract class AbstractFastPreferenceData<U, I> implements FastPreferenceData<U, I>, Serializable {

    /**
     * user index.
     */
    protected final FastUserIndex<U> ui;

    /**
     * item index.
     */
    protected final FastItemIndex<I> ii;

    /**
     * Converter from IdxPref to IdPref (preference for item).
     */
    protected final Function<IdxPref, IdPref<I>> uPrefFun;

    /**
     * Converter from IdxPref to IdPref (preference from user).
     */
    protected final Function<IdxPref, IdPref<U>> iPrefFun;

    /**
     * Constructor.
     *
     * @param users user index
     * @param items item index
     */
    public AbstractFastPreferenceData(FastUserIndex<U> users, FastItemIndex<I> items) {
        this(users, items,
                (Function<IdxPref, IdPref<I>> & Serializable) p -> new IdPref<>(items.iidx2item(p)),
                (Function<IdxPref, IdPref<U>> & Serializable) p -> new IdPref<>(users.uidx2user(p)));
    }

    /**
     * Constructor.
     *
     * @param userIndex user index
     * @param itemIndex item index
     * @param uPrefFun converter from IdxPref to IdPref (preference for item).
     * @param iPrefFun converter from IdxPref to IdPref (preference from user).
     */
    public AbstractFastPreferenceData(FastUserIndex<U> userIndex, FastItemIndex<I> itemIndex, Function<IdxPref, IdPref<I>> uPrefFun, Function<IdxPref, IdPref<U>> iPrefFun) {
        this.ui = userIndex;
        this.ii = itemIndex;
        this.uPrefFun = uPrefFun;
        this.iPrefFun = iPrefFun;
    }

    @Override
    public int user2uidx(U u) {
        return ui.user2uidx(u);
    }

    @Override
    public U uidx2user(int i) {
        return ui.uidx2user(i);
    }

    @Override
    public boolean containsUser(U u) {
        return ui.containsUser(u);
    }

    @Override
    public int numUsers() {
        return ui.numUsers();
    }

    @Override
    public Stream<U> getAllUsers() {
        return ui.getAllUsers();
    }

    @Override
    public int item2iidx(I i) {
        return ii.item2iidx(i);
    }

    @Override
    public I iidx2item(int i) {
        return ii.iidx2item(i);
    }

    @Override
    public boolean containsItem(I i) {
        return ii.containsItem(i);
    }

    @Override
    public int numItems() {
        return ii.numItems();
    }

    @Override
    public Stream<I> getAllItems() {
        return ii.getAllItems();
    }

    @Override
    public int numUsers(I i) {
        return numUsers(item2iidx(i));
    }

    @Override
    public int numItems(U u) {
        return numItems(user2uidx(u));
    }

    @Override
    public Stream<? extends IdPref<I>> getUserPreferences(final U u) {
        return getUidxPreferences(user2uidx(u)).map(uPrefFun);
    }

    @Override
    public Stream<? extends IdPref<U>> getItemPreferences(final I i) {
        return getIidxPreferences(item2iidx(i)).map(iPrefFun);
    }

    @Override
    public Stream<U> getUsersWithPreferences() {
        return getUidxWithPreferences().mapToObj(this::uidx2user);
    }

    @Override
    public Stream<I> getItemsWithPreferences() {
        return getIidxWithPreferences().mapToObj(this::iidx2item);
    }

    @Override
    public int numUsersWithPreferences() {
        return (int) getUidxWithPreferences().count();
    }

    @Override
    public int numItemsWithPreferences() {
        return (int) getIidxWithPreferences().count();
    }

}
