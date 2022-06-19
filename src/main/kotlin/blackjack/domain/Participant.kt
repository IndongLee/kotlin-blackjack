package blackjack.domain

sealed class Participant(
    val name: PlayerName,
    protected var hands: Hands
) {
    val cardsOfHands: PlayingCards
        get() = hands.cards
    private val playerState: PlayerState
        get() = hands.state
    val score: Score
        get() = playerState.score

    fun receive(playingCards: PlayingCards) {
        hands += playingCards
    }

    abstract fun isReceivable(): Boolean

    class Player(
        name: PlayerName,
        hands: Hands
    ) : Participant(name, hands) {
        constructor(name: String, vararg initialCards: PlayingCard) : this(
            PlayerName(name),
            Hands.from(PlayingCards.from(initialCards.toList()))
        )

        fun stay() {
            hands = hands.stay()
        }

        override fun isReceivable(): Boolean = hands.isReceivable()
    }

    class Dealer(
        name: PlayerName,
        hands: Hands
    ) : Participant(name, hands) {
        constructor(name: String, vararg initialCards: PlayingCard) : this(
            PlayerName(name),
            Hands.from(PlayingCards.from(initialCards.toList()))
        )

        override fun isReceivable(): Boolean = score.value <= SHOULD_HIT_MAX_SCORE

        companion object {
            const val SHOULD_HIT_MAX_SCORE = 16
        }
    }
}