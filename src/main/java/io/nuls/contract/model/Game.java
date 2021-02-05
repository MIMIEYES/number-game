package io.nuls.contract.model;

import io.nuls.contract.sdk.Address;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: PierreLuo
 * @date: 2020-04-07
 */
public class Game {
    /**
     * 游戏ID
     */
    private Long id;
    /**
     * 开始高度
     */
    private Long startHeight;
    /**
     * 截止高度
     */
    private Long endHeight;
    // 延迟开奖区块数
    private Integer gameLotteryDelay;
    /**
     * 每个号码的参与者
     */
    private Map<Integer, Address> participants;
    /**
     * 所有参与者
     */
    private Map<Address, Integer> users;
    /**
     * 中奖号码
     */
    private Integer lotteryNumber;

    public Game(Long id, Long startHeight, Integer gameLotteryDelay) {
        this.id = id;
        this.startHeight = startHeight;
        this.gameLotteryDelay = gameLotteryDelay;
        this.participants = new HashMap<Integer, Address>();
        this.users = new HashMap<Address, Integer>();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getStartHeight() {
        return startHeight;
    }

    public void setStartHeight(Long startHeight) {
        this.startHeight = startHeight;
    }

    public Long getEndHeight() {
        return endHeight;
    }

    public void setEndHeight(Long endHeight) {
        this.endHeight = endHeight;
    }

    public Integer getGameLotteryDelay() {
        return gameLotteryDelay;
    }

    public void setGameLotteryDelay(Integer gameLotteryDelay) {
        this.gameLotteryDelay = gameLotteryDelay;
    }

    public Integer getLotteryNumber() {
        return lotteryNumber;
    }

    public void setLotteryNumber(Integer lotteryNumber) {
        this.lotteryNumber = lotteryNumber;
    }

    public Map<Integer, Address> getParticipants() {
        return participants;
    }

    public void setParticipants(Map<Integer, Address> participants) {
        this.participants = participants;
    }

    public Map<Address, Integer> getUsers() {
        return users;
    }

    public void setUsers(Map<Address, Integer> users) {
        this.users = users;
    }
}
