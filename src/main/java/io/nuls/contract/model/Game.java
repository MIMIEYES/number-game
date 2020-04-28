/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.contract.model;

import java.util.List;
import java.util.Map;
import java.util.Set;

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
     * 每个号码的参与者们
     */
    private Map<Integer, List<User>> participants;
    /**
     * 中奖号码
     */
    private Integer lotteryNumber;

    public Game(Long id, Long startHeight, Long endHeight, Integer gameLotteryDelay) {
        this.id = id;
        this.startHeight = startHeight;
        this.endHeight = endHeight;
        this.gameLotteryDelay = gameLotteryDelay;
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

    public Map<Integer, List<User>> getParticipants() {
        return participants;
    }

    public void setParticipants(Map<Integer, List<User>> participants) {
        this.participants = participants;
    }

}
