package ua.kpi.transformermonitoringsystem.ui.navigation

sealed class NavScreen(val route: String) {
    object MainMenu : NavScreen("main_menu")
    object List : NavScreen("transformer_list")
    object Form : NavScreen("transformer_form")
    object Details : NavScreen("transformer_details/{transformerId}") {
        fun createRoute(transformerId: Int) = "transformer_details/$transformerId"
    }
}
