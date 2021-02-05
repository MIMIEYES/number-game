package io.nuls.contract.event;

import io.nuls.contract.model.Compensation;
import io.nuls.contract.sdk.Address;
import io.nuls.contract.sdk.Event;

import java.math.BigInteger;
import java.util.List;

/**
 * @author: PierreLuo
 * @date: 2020-04-07
 */
public class LotteryEvent implements Event {
    private Long gameId;
    /**
     * 中奖号码
     */
    private Integer number;
    /**
     * 中奖人
     */
    private String winner;
    private List<Compensation> compensations;

    public LotteryEvent(Long gameId, Integer number, String winner, List<Compensation> compensations) {
        this.gameId = gameId;
        this.number = number;
        this.winner = winner;
        this.compensations = compensations;
    }

}
