package io.nuls.contract;

import io.nuls.contract.constant.Constant;
import io.nuls.contract.event.*;
import io.nuls.contract.model.Compensation;
import io.nuls.contract.model.Game;
import io.nuls.contract.model.Lottery;
import io.nuls.contract.owner.Ownable;
import io.nuls.contract.sdk.*;
import io.nuls.contract.sdk.annotation.JSONSerializable;
import io.nuls.contract.sdk.annotation.Payable;
import io.nuls.contract.sdk.annotation.View;

import java.math.BigInteger;
import java.util.*;

import static io.nuls.contract.constant.Constant.NULS_0_dot_01;
import static io.nuls.contract.constant.Constant.PARTICIPANTS_AMOUNT;
import static io.nuls.contract.sdk.Utils.require;

/**
 * 猜不中数字游戏
 * 一局最多十人参与
 * 每个号码不能被重复投注
 * 每人不可以加倍投注
 *
 */
public class NumberGame extends Ownable implements Contract {

    private Map<Long, Game> gameMap = new HashMap<Long, Game>();
    private int size;
    private BigInteger publicBenefit;
    private Game last;
    // 两局游戏之间隔的区块数量
    private int gameInterval;
    // 游戏结束后延迟开奖的区块数量
    private int gameLotteryDelay;

    /**
     * 初始化
     */
    public NumberGame() {
        size = 0;
        gameInterval = 10;
        gameLotteryDelay = 5;
        publicBenefit = BigInteger.ZERO;
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
        require(number != null && number >= 0 && number <= 9, "数字应选择0到9之间");
        BigInteger senderValue = Msg.value();
        require(senderValue.compareTo(Constant.PARTICIPANTS_AMOUNT) >= 0, "最低参与金额100NULS");
        Address sender = Msg.sender();
        Map<Integer, Address> participants = game.getParticipants();
        Map<Address, Integer> users = game.getUsers();
        Address user = participants.get(number);
        require(user == null, "此号码已被占用");
        require(!users.containsKey(sender), "不能重复参与游戏");
        participants.put(number, sender);
        users.put(sender, number);
        publicBenefit = publicBenefit.add(senderValue.subtract(PARTICIPANTS_AMOUNT));
        Utils.emit(new ParticipantEvent(gameId, sender, number));
        // 当参与者达到10人，设置游戏结束高度
        if (participants.size() == 10) {
            game.setEndHeight(Block.number());
            Utils.emit(new GameEndEvent(gameId, game.getEndHeight()));
        }
    }

    /**
     * 开奖
     */
    public void lottery(Long gameId) {
        Game game = gameMap.get(gameId);
        require(game != null, "游戏已开奖或不存在");
        Long end = game.getEndHeight();
        require(end != null, "游戏没有结束");
        long current = Block.number();
        Integer lotteryDelay = game.getGameLotteryDelay();
        require(end + lotteryDelay < current, "开奖高度为" + (end + lotteryDelay + 1));

        // 计算中奖号码
        Lottery lottery = null;
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
        lottery = this.prize(game);

        Utils.emit(new LotteryEvent(game.getId(), game.getLotteryNumber(), lottery.getWinner(), lottery.getCompensations()));

        // 结束游戏
        gameMap.remove(gameId);
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
    public void giveUpWholeGame(Long id) {
        onlyOwner();
        Game game = gameMap.get(id);
        require(game != null, "游戏已开奖或不存在");
        Map<Integer, Address> participants = game.getParticipants();
        if(participants.size() > 0) {
            Collection<Address> list = participants.values();
            for(Address user : list) {
                user.transfer(PARTICIPANTS_AMOUNT);
            }
        }
        Utils.emit(new LotteryEvent(id, null, null, null));
        // 结束游戏
        gameMap.remove(id);
    }

    /**
     * 退出一轮未开奖的游戏，归还参与金额
     */
    public void quitGameByUser(Long id) {
        Game game = gameMap.get(id);
        require(game != null, "游戏已开奖或不存在");
        require(game.getEndHeight() == null, "游戏已结束");
        Address user = Msg.sender();
        Map<Address, Integer> users = game.getUsers();
        Integer number = users.get(user);
        require(number != null, "此用户没有参与游戏");
        user.transfer(PARTICIPANTS_AMOUNT);
        // 清理游戏参与者
        users.remove(user);
        game.getParticipants().remove(number);

        Utils.emit(new QuitGameEvent(id, user.toString()));
    }

    @View
    public int getGameInterval() {
        return gameInterval;
    }

    @View
    public int getGameLotteryDelay() {
        return gameLotteryDelay;
    }

    @View
    public String getPublicBenefit() {
        return publicBenefit.toString();
    }

    @View
    @JSONSerializable
    public Game getRunningGame(Long id) {
        Game game = gameMap.get(id);
        return game;
    }

    @View
    @JSONSerializable
    public Game getLastGame() {
        return last;
    }

    private void checkGame(Game game) {
        Long end = game.getEndHeight();
        // 结束高度为空时，当局游戏正在运行
        if(end == null) {
            return;
        }
        long current = Block.number();
        // 截止高度判断
        require(current <= end, "游戏已结束，结束高度: " + end);
    }

    // 发奖
    private Lottery prize(Game game) {
        Integer lotteryNumber = game.getLotteryNumber();
        Map<Integer, Address> participants = game.getParticipants();
        Address user = participants.get(lotteryNumber);
        String winner = user.toString();
        Map<Address, Integer> users = game.getUsers();
        users.remove(user);

        Set<Map.Entry<Address, Integer>> entries = users.entrySet();

        List<Compensation> list = new ArrayList<Compensation>();
        Address _user;
        Integer _number;
        BigInteger compensation;
        BigInteger totalCompensation = BigInteger.ZERO;
        for (Map.Entry<Address, Integer> entry : entries) {
            _user = entry.getKey();
            _number = entry.getValue();
            compensation = BigInteger.valueOf((((_number + lotteryNumber) % 9) + 1) * 200000000);
            list.add(new Compensation(_user.toString(), compensation));
            totalCompensation = totalCompensation.add(compensation);
            _user.transfer(PARTICIPANTS_AMOUNT.add(compensation));
        }
        publicBenefit = publicBenefit.add(PARTICIPANTS_AMOUNT.subtract(totalCompensation));
        return new Lottery(game.getId(), lotteryNumber, winner, list);
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
        game = new Game(gameId, start, gameLotteryDelay);
        gameMap.put(gameId, game);
        last = game;
        Utils.emit(new CreateGameEvent(game.getId(), game.getStartHeight(), game.getGameLotteryDelay()));
    }

}