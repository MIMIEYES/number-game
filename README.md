## Guess not right - Guess the number game

* A maximum of ten people can participate in a game
* Each person bets 100 NULS
* Each number cannot be bet repeatedly
* Each person cannot double the bet
* A loser will be drawn, and the remaining 9 people will randomly allocate the loser’s 100 NULS bets

Methods:

- Only the owner of the contract can execute it.

    - createGame: Create a game, when a game ends, the owner executes the createGame method to start a new round of games.

    - giveUpWholeGame: Give up a round of undrawn games and return the participation amount

    - receivePublicBenefit: receive platform benefits

- Everyone can execute it.

    - join: Participate in the game, bet 100 NULS and choose a number from 0-9

    - lottery: draw, a loser will be drawn, and the remaining 9 people will randomly allocate the loser's 100 NULS bets

    - setGameInterval: Set the number of blocks between each game

    - setGameLotteryDelay: Set the number of blocks to delay the lottery after the game ends

    - quitGameByUser: Quit a round of games that have not been drawn and return the participation amount

    - getLastGame: get the latest game