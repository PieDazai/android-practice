package ci.nsu.mobile.main.deposit

sealed class BottomScreen {

    object Users : BottomScreen()

    object Deposits : BottomScreen()

    object NewDeposit : BottomScreen()
}