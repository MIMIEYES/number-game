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
package io.nuls.contract.event;

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
     * 中奖人们
     */
    private List<String> winners;
    /**
     * 平分金额
     */
    private BigInteger perPrize;

    public LotteryEvent(Long gameId, Integer number, List<String> winners, BigInteger perPrize) {
        this.gameId = gameId;
        this.number = number;
        this.winners = winners;
        this.perPrize = perPrize;
    }
}