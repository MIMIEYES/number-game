package io.nuls.contract.event;

import io.nuls.contract.sdk.Address;
import io.nuls.contract.sdk.Event;

/**
 * @author: PierreLuo
 * @date: 2020-04-07
 */
public class ParticipantEvent implements Event {
    private long gameId;
    private Address address;
    /**
     * 游戏数字
     */
    private int number;

    public ParticipantEvent(long gameId, Address address, int number) {
        this.gameId = gameId;
        this.address = address;
        this.number = number;
    }
}
