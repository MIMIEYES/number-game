package io.nuls.contract.model;

import java.math.BigInteger;

/**
 * @author: PierreLuo
 * @date: 2020-04-15
 */
public class Compensation {
    /**
     * 中奖人
     */
    private String user;
    /**
     * 补偿金额
     */
    private BigInteger compensation;

    public Compensation(String user, BigInteger compensation) {
        this.user = user;
        this.compensation = compensation;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public BigInteger getCompensation() {
        return compensation;
    }

    public void setCompensation(BigInteger compensation) {
        this.compensation = compensation;
    }
}
