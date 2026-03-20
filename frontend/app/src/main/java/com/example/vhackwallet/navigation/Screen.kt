package com.example.vhackwallet.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Scanner : Screen("scanner")
    object Activity : Screen("activity")
    object Wallet : Screen("wallet")
    object Profile : Screen("profile")
    object TransferSearch : Screen("transfer_search")
    object Loading : Screen("loading/{nextRoute}") {
        fun createRoute(nextRoute: String) = "loading/${nextRoute.replace("/", "|")}"
    }
    object TransferAmount : Screen("transfer_amount/{name}/{id}/{isNewRecipient}") {
        fun createRoute(name: String, id: String, isNewRecipient: Boolean = false) = 
            "transfer_amount/$name/$id/$isNewRecipient"
    }
    object TransferConfirm : Screen("transfer_confirm/{name}/{id}/{amount}/{details}/{isNewRecipient}") {
        fun createRoute(name: String, id: String, amount: String, details: String, isNewRecipient: Boolean = false) = 
            "transfer_confirm/$name/$id/$amount/$details/$isNewRecipient"
    }
    object TransferVerification : Screen("transfer_verification/{name}/{id}/{amount}/{details}") {
        fun createRoute(name: String, id: String, amount: String, details: String) = 
            "transfer_verification/$name/$id/$amount/$details"
    }
    object TransferSuccess : Screen("transfer_success/{name}/{id}/{amount}/{details}") {
        fun createRoute(name: String, id: String, amount: String, details: String) = 
            "transfer_success/$name/$id/$amount/$details"
    }
}
