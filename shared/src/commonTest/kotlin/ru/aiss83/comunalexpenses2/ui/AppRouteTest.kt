package ru.aiss83.comunalexpenses2.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import ru.aiss83.comunalexpenses2.data.ResourceData

/**
 * Tests for the route persistence used by `rememberSaveable`: the current screen must survive
 * activity recreation (rotation, process death) instead of falling back to the home screen.
 */
@OptIn(ExperimentalUuidApi::class)
class AppRouteTest {

    private fun roundTrip(route: AppRoute): AppRoute = decodeRoute(route.encodeRoute())

    @Test
    fun `plain routes round trip`() {
        assertEquals(AppRoute.Home, roundTrip(AppRoute.Home))
        assertEquals(AppRoute.Settings, roundTrip(AppRoute.Settings))
        assertEquals(AppRoute.Stats, roundTrip(AppRoute.Stats))
        assertEquals(AppRoute.EditResources(), roundTrip(AppRoute.EditResources()))
    }

    @Test
    fun `edit route preserves the whole record`() {
        val record = ResourceData(
            id = Uuid.random(),
            date = 1719000000000L,
            hotWater = 101,
            coldWater = 202,
            dayElectricity = 303,
            nightElectricity = 404,
            shared = true
        )
        assertEquals(AppRoute.EditResources(record), roundTrip(AppRoute.EditResources(record)))
    }

    @Test
    fun `unknown encoded value falls back to home`() {
        assertEquals(AppRoute.Home, decodeRoute("garbage"))
        assertEquals(AppRoute.Home, decodeRoute(""))
    }

    @Test
    fun `truncated edit payload falls back to a new record`() {
        assertEquals(AppRoute.EditResources(), decodeRoute("edit|abc"))
    }
}
