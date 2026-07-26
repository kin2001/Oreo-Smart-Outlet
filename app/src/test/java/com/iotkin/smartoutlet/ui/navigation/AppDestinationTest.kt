package com.iotkin.smartoutlet.ui.navigation

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppDestinationTest {

    @Test
    fun bottomNavigationIsLimitedToTopLevelRoutes() {
        bottomNavigationDestinations.forEach {
            assertTrue(
                isBottomNavigationRoute(it.route)
            )
        }

        assertFalse(
            isBottomNavigationRoute(
                "outlet/{outletNumber}"
            )
        )

        assertFalse(
            isBottomNavigationRoute(
                "schedule/edit/{scheduleOutletNumber}"
            )
        )

        assertFalse(
            isBottomNavigationRoute("about")
        )

        assertFalse(
            isBottomNavigationRoute(null)
        )
    }
}
