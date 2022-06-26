package blackjack.viewmodel

import blackjack.domain.BlackjackGameResult
import blackjack.domain.BlackjackGameTurn
import blackjack.domain.CardDeck
import blackjack.domain.Dealer
import blackjack.domain.HIT_CARD_COUNT
import blackjack.domain.Hands
import blackjack.domain.Observable
import blackjack.domain.Participants
import blackjack.domain.Player
import blackjack.domain.PlayerInfo
import blackjack.domain.PlayerName
import blackjack.domain.PlayingCard
import blackjack.domain.START_CARD_COUNT
import blackjack.domain.cardrule.DistinctRule
import blackjack.domain.cardrule.ShuffleRule

class BlackjackViewModel private constructor(
    val participants: Participants,
    private val cardDeck: CardDeck,
    private val isPlayerWannaHit: (Player) -> Boolean
) {
    val currentTurn: Observable<BlackjackGameTurn> = Observable(BlackjackGameTurn.from(participants))

    fun hit() {
        val currentPlayer = currentTurn.value.participant
        currentPlayer.receive(cardDeck.draw(HIT_CARD_COUNT))
    }

    fun stay() {
        val currentPlayer = currentTurn.value.participant as? Player
        currentPlayer?.stay()
    }

    fun nextTurn() {
        val nextTurn = BlackjackGameTurn.from(participants)
        if (nextTurn.participant is Dealer && nextTurn.isTurnEnd()) return // Todo : 수정

        currentTurn.value = nextTurn
    }

    fun getBlackjackGameResult(): BlackjackGameResult {
        return BlackjackGameResult.from(participants)
    }

    companion object {
        fun from(
            dealerName: PlayerName,
            playerInfos: List<PlayerInfo>,
            isPlayerWannaHit: (Player) -> Boolean
        ): BlackjackViewModel {
            val cardDeck = CardDeck.of(
                PlayingCard.all(),
                DistinctRule, ShuffleRule
            )
            val participants = Participants(
                dealer = dealerName.toDealer(cardDeck),
                players = playerInfos.toPlayers(cardDeck)
            )

            return BlackjackViewModel(
                participants,
                cardDeck,
                isPlayerWannaHit
            )
        }

        fun of(dealerName: PlayerName, playerInfos: List<PlayerInfo>, cardDeck: CardDeck): BlackjackViewModel {
            val participants = Participants(
                dealer = dealerName.toDealer(cardDeck),
                players = playerInfos.toPlayers(cardDeck)
            )
            return BlackjackViewModel(participants, cardDeck) { false }
        }

        private fun PlayerName.toDealer(cardDeck: CardDeck): Dealer {
            return Dealer(this, cardDeck.initialHands())
        }

        private fun CardDeck.initialHands(): Hands {
            return Hands.from(draw(START_CARD_COUNT))
        }

        private fun List<PlayerInfo>.toPlayers(cardDeck: CardDeck): List<Player> {
            return map { playerInfo ->
                Player(
                    name = playerInfo.name,
                    betAmount = playerInfo.betAmount,
                    hands = cardDeck.initialHands()
                )
            }
        }
    }
}
