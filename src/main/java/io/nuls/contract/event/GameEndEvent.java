package io.nuls.contract.event;

import io.nuls.contract.sdk.Event;

/**
 * @author: PierreLuo
 * @date: 2020-04-07
 */
public class GameEndEvent implements Event {
    private long id;
    private long endHeight;

    public GameEndEvent(long id, long endHeight) {
        this.id = id;
        this.endHeight = endHeight;
    }
}
