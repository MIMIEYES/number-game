package io.nuls.contract;

import io.nuls.contract.constant.Constant;
import io.nuls.contract.event.CreateGameEvent;
import io.nuls.contract.event.InitialGameEvent;
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
import java.util.*;

import static io.nuls.contract.constant.Constant.*;
import static io.nuls.contract.sdk.Utils.require;

/**
 * the sample class that can be changed or removed
 */
public class NumberGame extends Ownable implements Contract {

    private Map<Long, Game> gameMap = new HashMap<Long, Game>();
    private Map<Long, Game> gameHistoryMap = new HashMap<Long, Game>();
    private int size;
    private BigInteger publicBenefit;
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
        publicBenefit = BigInteger.ZERO;
        prizePool = BigInteger.ZERO;
    }

    /**
     * 创建一局新游戏
     */
    public void createGame() {
        onlyOwner();
        createGames();
    }

    /**
     * 参与游戏
     */
    @Payable
    public void join(Long gameId, Integer number) {
        Game game = gameMap.get(gameId);
        require(game != null, "游戏已结束或不存在");
        checkGame(game);
        BigInteger senderValue = Msg.value();
        require(senderValue.compareTo(Constant.MIN_AMOUNT) >= 0, "最低参与金额2.01NULS");
        Address sender = Msg.sender();
        Map<Integer, List<User>> participants = game.getParticipants();
        List<User> users;
        if (participants == null) {
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
        users.add(new User(Msg.sender(), number, senderValue));
        prizePool = prizePool.add(PARTICIPANTS_AMOUNT);
        publicBenefit = publicBenefit.add(senderValue.subtract(PARTICIPANTS_AMOUNT));
        Utils.emit(new ParticipantEvent(gameId, Msg.sender(), number));
    }

    /**
     * 开奖
     */
    public void lottery(Long gameId) {
        Game game = gameMap.get(gameId);
        require(game != null, "游戏已开奖或不存在");
        Long end = game.getEndHeight();
        require(end != null, "游戏尚未初始化");
        long current = Block.number();
        Integer lotteryDelay = game.getGameLotteryDelay();
        require(end + lotteryDelay < current, "开奖高度为" + (end + lotteryDelay + 1));

        Lottery lottery = null;
        Map<Integer, List<User>> participants = game.getParticipants();
        // 当有参与者时，才计算中奖号码
        if (participants != null) {
            // 获取随机数
            BigInteger orginSeed = Utils.getRandomSeed(end + lotteryDelay, lotteryDelay, "sha3");
            String seedStr = orginSeed.toString();
            // 计算中奖号码
            int lotteryNumber;
            int seedLength = seedStr.length();
            if (seedLength == 1) {
                lotteryNumber = Integer.parseInt(seedStr);
            } else {
                int subStart = seedLength / 2;
                lotteryNumber = Integer.parseInt(seedStr.substring(subStart, subStart + 1));
            }
            game.setLotteryNumber(lotteryNumber);
            // 发奖
            lottery = prize(game);
        }
        if (lottery != null) {
            Utils.emit(new LotteryEvent(game.getId(), game.getLotteryNumber(), lottery.getWinners(), lottery.getPerPrize()));
        } else {
            Utils.emit(new LotteryEvent(game.getId(), game.getLotteryNumber(), null, null));
        }
        // 结束游戏
        gameMap.remove(gameId);
        gameHistoryMap.put(gameId, game);
        // 创建一局新游戏
        createGames();
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

    public void receivePublicBenefit() {
        onlyOwner();
        require(publicBenefit.compareTo(NULS_0_dot_01) >= 0, "金额不足");
        this.owner.transfer(publicBenefit);
    }

    /**
     * 放弃一轮未开奖的游戏，归还参与金额
     */
    public void giveUpGame(Long id) {
        onlyOwner();
        Game game = gameMap.get(id);
        require(game != null, "游戏已开奖或不存在");
        Map<Integer, List<User>> participants = game.getParticipants();
        int k = 0;
        if(participants != null && participants.size() > 0) {
            Collection<List<User>> lists = participants.values();
            for(List<User> list : lists) {
                for(User user : list) {
                    user.getUser().transfer(PARTICIPANTS_AMOUNT);
                    k++;
                }
            }
        }
        if(k >0) {
            prizePool = prizePool.subtract(PARTICIPANTS_AMOUNT.multiply(BigInteger.valueOf(k)));
        }
        Utils.emit(new LotteryEvent(id, null, null, null));
        // 结束游戏
        gameMap.remove(id);
        gameHistoryMap.put(id, game);

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
    public String getPublicBenefit() {
        return publicBenefit.toString();
    }

    @View
    @JSONSerializable
    public Game getGame(Long id) {
        Game game = gameMap.get(id);
        if (game == null) {
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
        Long end = game.getEndHeight();
        // 结束高度为空时，初始化游戏
        if(end == null) {
            initGames(game);
            return;
        }
        long current = Block.number();
        // 截止高度判断
        require(current <= end, "游戏已结束，结束高度: " + end);
    }

    // 发奖
    private Lottery prize(Game game) {
        Integer lotteryNumber = game.getLotteryNumber();
        Map<Integer, List<User>> participants = game.getParticipants();
        if (participants == null) {
            // 无参与者，跳过发奖
            return null;
        }
        List<User> users = participants.get(lotteryNumber);
        if (users == null || users.size() == 0) {
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
        int percent = 0;
        if(prizePool.compareTo(NULS_10) <= 0) {
            percent = 20;
        } else if(prizePool.compareTo(NULS_50) <= 0) {
            percent = 25;
        } else if(prizePool.compareTo(NULS_100) <= 0) {
            percent = 30;
        } else if(prizePool.compareTo(NULS_200) <= 0) {
            percent = 35;
        } else if(prizePool.compareTo(NULS_500) <= 0) {
            percent = 40;
        } else if(prizePool.compareTo(NULS_1000) <= 0) {
            percent = 45;
        } else {
            percent = 50;
        }
        BigDecimal availablePool = new BigDecimal(prizePool).multiply(BigDecimal.valueOf(100 - percent)).divide(BigDecimal.valueOf(100));
        BigInteger perPrize = availablePool.divide(BigDecimal.valueOf(size), 0, BigDecimal.ROUND_DOWN).toBigInteger();
        return perPrize;
    }

    private void createGames() {
        long currentHeight = Block.number();
        Game game;
        long start;
        if (last == null) {
            start = currentHeight;
        } else {
            require(last.getEndHeight() != null, "当前游戏还未结束");
            start = last.getEndHeight() + gameInterval;
            if (start < currentHeight) {
                start = currentHeight;
            }
        }
        long gameId = (long) (++size);
        game = new Game(gameId, start, null, gameLotteryDelay);
        gameMap.put(gameId, game);
        last = game;
        Utils.emit(new CreateGameEvent(game.getId(), game.getStartHeight(), game.getGameLotteryDelay()));
    }

    private void initGames(Game game) {
        long currentHeight = Block.number();
        long end = currentHeight + gameHeightRange;
        game.setEndHeight(end);
        Utils.emit(new InitialGameEvent(game.getId(), game.getEndHeight()));
    }

}