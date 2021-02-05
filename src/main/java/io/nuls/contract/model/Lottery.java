package io.nuls.contract.model;

import java.util.List;

/**
 * @author: PierreLuo
 * @date: 2020-04-15
 */
public class Lottery {
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

    public Lottery(Long gameId, Integer number, String winner, List<Compensation> compensations) {
        this.gameId = gameId;
        this.number = number;
        this.winner = winner;
        this.compensations = compensations;
    }

    public Long getGameId() {
        return gameId;
    }

    public void setGameId(Long gameId) {
        this.gameId = gameId;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public String getWinner() {
        return winner;
    }

    public void setWinner(String winner) {
        this.winner = winner;
    }

    public List<Compensation> getCompensations() {
        return compensations;
    }

    public void setCompensations(List<Compensation> compensations) {
        this.compensations = compensations;
    }
}
