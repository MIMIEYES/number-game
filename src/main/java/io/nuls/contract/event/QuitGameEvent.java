package io.nuls.contract.event;

import io.nuls.contract.sdk.Event;

/**
 * @author: PierreLuo
 * @date: 2020-04-07
 */
public class QuitGameEvent implements Event {
    private Long gameId;
    /**
     * 参与者地址
     */
    private String address;

    public QuitGameEvent(Long gameId, String address) {
        this.gameId = gameId;
        this.address = address;
    }
}
