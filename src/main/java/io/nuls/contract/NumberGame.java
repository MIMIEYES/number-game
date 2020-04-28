package io.nuls.contract;

import io.nuls.contract.constant.Constant;
import io.nuls.contract.event.CreateGameEvent;
import io.nuls.contract.event.LotteryEvent;
import io.nuls.contract.event.ParticipantEvent;
import io.nuls.contract.model.Game;
import io.nuls.contract.model.Lottery;
import io.nuls.contract.model.User;
import io.nuls.contract.owner.Ownable;
import io.nuls.contract.sdk.*;
import io.nuls.contract.sdk.annotation.JSONSerializable;
import io.nuls.contract.sdk.annotation.Payable;
import io.nuls.contract.sdk.annotation.View;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.nuls.contract.sdk.Utils.require;

/**
 * the sample class that can be changed or removed
 */
public class NumberGame extends Ownable implements Contract {

    private Map<Long, Game> gameMap = new HashMap<Long, Game>();
    private Map<Long, Game> gameHistoryMap = new HashMap<Long, Game>();
    private int size;
    private BigInteger prizePool;
    private Game last;
    // 两局游戏之间隔的区块数量
    private int gameInterval;
    // 一局游戏的区块数量
    private int gameHeightRange;
    // 游戏结束后延迟开奖的区块数量
    private int gameLotteryDelay;

    /**
     * 初始化
     */
    public NumberGame() {
        size = 0;
        gameInterval = 10;
        gameHeightRange = 60;
        gameLotteryDelay = 5;
        prizePool = BigInteger.ZERO;
    }

    /**
     * 创建一局新游戏
     */
    public void createGame() {
        onlyOwner();
        createGames(1);
    }

    /**
     * 参与游戏
     */
    @Payable
    public void join(Long gameId, Integer number) {
        Game game = gameMap.get(gameId);
        require(game != null, "游戏已结束或不存在");
        checkGame(game);
        require(Msg.value().compareTo(Constant.MIN_AMOUNT) >= 0, "最低参与金额2.01NULS");
        Address sender = Msg.sender();
        Map<Integer, List<User>> participants = game.getParticipants();
        List<User> users;
        if(participants == null) {
            participants = new HashMap<Integer, List<User>>();
            users = new ArrayList<User>();
            participants.put(number, users);
            game.setParticipants(participants);
        } else {
            users = participants.get(number);
            if (users == null) {
                users = new ArrayList<User>();
                participants.put(number, users);
            }
        }
        users.add(new User(Msg.sender(), number, Msg.value()));
        prizePool = prizePool.add(Constant.PARTICIPANTS_AMOUNT);
        Utils.emit(new ParticipantEvent(gameId, Msg.sender(), number));

    }

    /**
     * 开奖
     */
    public void lottery(Long gameId) {
        Game game = gameMap.get(gameId);
        require(game != null, "游戏已开奖或不存在");
        Long end = game.getEndHeight();
        long current = Block.number();
        Integer lotteryDelay = game.getGameLotteryDelay();
        require(end + lotteryDelay < current, "开奖高度为" + (end + lotteryDelay + 1));

        Lottery lottery = null;
        Map<Integer, List<User>> participants = game.getParticipants();
        // 当有参与者时，才计算中奖号码
        if(participants != null) {
            // 获取随机数
            BigInteger orginSeed = Utils.getRandomSeed(end + lotteryDelay, lotteryDelay, "sha3");
            String seedStr = orginSeed.toString();
            // 计算中奖号码
            int lotteryNumber;
            int seedLength = seedStr.length();
            if(seedLength == 1) {
                lotteryNumber = Integer.parseInt(seedStr);
            } else {
                int subStart = seedLength / 2;
                lotteryNumber = Integer.parseInt(seedStr.substring(subStart, subStart + 1));
            }
            game.setLotteryNumber(lotteryNumber);
            // 发奖
            lottery = prize(game);
        }
        if(lottery != null) {
            Utils.emit(new LotteryEvent(game.getId(), game.getLotteryNumber(), lottery.getWinners(), lottery.getPerPrize()));
        } else {
            Utils.emit(new LotteryEvent(game.getId(), game.getLotteryNumber(), null, null));
        }
        // 结束游戏
        gameMap.remove(gameId);
        gameHistoryMap.put(gameId, game);
        // 创建一局新游戏
        createGames(1);
    }

    /**
     * 更新游戏间隔区块
     */
    public void setGameInterval(int gameInterval) {
        onlyOwner();
        this.gameInterval = gameInterval;
    }

    /**
     * 更新游戏运行的区块数量
     */
    public void setGameHeightRange(int gameHeightRange) {
        onlyOwner();
        this.gameHeightRange = gameHeightRange;
    }

    /**
     * 更新游戏结束后延迟开奖的区块数量
     */
    public void setGameLotteryDelay(int gameLotteryDelay) {
        this.gameLotteryDelay = gameLotteryDelay;
    }

    @View
    public int getGameInterval() {
        return gameInterval;
    }

    @View
    public int getGameHeightRange() {
        return gameHeightRange;
    }

    @View
    public int getGameLotteryDelay() {
        return gameLotteryDelay;
    }

    @View
    public String getPrizePool() {
        return prizePool.toString();
    }

    @View
    @JSONSerializable
    public Game getGame(Long id) {
        Game game = gameMap.get(id);
        if(game == null) {
            game = gameHistoryMap.get(id);
        }
        return game;
    }

    @View
    @JSONSerializable
    public Game getLastGame() {
        return last;
    }

    private void checkGame(Game game) {
        long current = Block.number();
        long end = game.getEndHeight();
        // 截止高度判断
        require(current <= end, "游戏已结束，结束高度: " + end);
    }

    // 发奖
    private Lottery prize(Game game) {
        Integer lotteryNumber = game.getLotteryNumber();
        Map<Integer, List<User>> participants = game.getParticipants();
        if(participants == null) {
            // 无参与者，跳过发奖
            return null;
        }
        List<User> users = participants.get(lotteryNumber);
        if(users == null || users.size() == 0) {
            // 无人中奖，跳过发奖
            return null;
        }
        int size = users.size();
        List<String> winners = new ArrayList<String>(size);
        // 平分奖池
        BigInteger perPrize = calPrize(size);
        for (User user : users) {
            winners.add(user.getUser().toString());
            user.getUser().transfer(perPrize);
        }
        prizePool = prizePool.subtract(perPrize.multiply(BigInteger.valueOf(size)));
        return new Lottery(game.getId(), lotteryNumber, winners, perPrize);
    }

    // 计算平分金额
    private BigInteger calPrize(int size) {
        if (prizePool.compareTo(BigInteger.ZERO) == 0) {
            return BigInteger.ZERO;
        }
        return new BigDecimal(prizePool).divide(BigDecimal.valueOf(size), 0, BigDecimal.ROUND_DOWN).toBigInteger();
    }

    private void createGames(int count) {
        long currentHeight = Block.number();
        Game game;
        long start;
        if (last == null) {
            start = currentHeight;
        } else {
            start = last.getEndHeight() + gameInterval;
            if(start < currentHeight) {
                start = currentHeight;
            }
        }
        long end = start + gameHeightRange;
        long gameId;
        for (int i = 0; i < count; i++) {
            gameId = (long) (++size);
            game = new Game(gameId, start, end, gameLotteryDelay);
            gameMap.put(gameId, game);
            if (i == count - 1) {
                last = game;
            }
            Utils.emit(new CreateGameEvent(game.getId(), game.getStartHeight(), game.getEndHeight(), game.getGameLotteryDelay()));
            // next
            start = end + gameInterval;
            end = start + gameHeightRange;
        }
    }

}