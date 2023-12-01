package ru.aiss83.comunalexpenses2

/**
 * Navigation routes constants
 */
sealed class NavRoutes(val route: String) {

    /** Navigation route to home screen */
    data object Home: NavRoutes("main")

    /** Navigation route to settings screen */
    data object Settings: NavRoutes("settings")

    /** Navigation route to editor  */
    data object EditResources: NavRoutes("edit_resources")
}
