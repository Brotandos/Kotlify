package com.brotandos.kotlify

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class TestingScreenTest {

    private lateinit var helloWorldString: String

    @get:Rule
    var activityRule: ActivityTestRule<TestingActivity> =
        ActivityTestRule(TestingActivity::class.java, false, true)

    @Before
    fun initValidString() {
        helloWorldString = "Hello, World!"
    }

    @Test
    fun checkTextHelloWorld() {
        onView(withId(R.id.hello_world_text_view))
            .check(matches(withText(helloWorldString)))
    }
}