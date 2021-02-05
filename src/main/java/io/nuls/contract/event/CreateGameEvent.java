package io.nuls.contract.event;

import io.nuls.contract.sdk.Event;

/**
 * @author: PierreLuo
 * @date: 2020-04-07
 */
public class CreateGameEvent implements Event {
    private long id;
    private long startHeight;
    private int gameLotteryDelay;

    public CreateGameEvent(long id, long startHeight, int gameLotteryDelay) {
        this.id = id;
        this.startHeight = startHeight;
        this.gameLotteryDelay = gameLotteryDelay;
    }
}
