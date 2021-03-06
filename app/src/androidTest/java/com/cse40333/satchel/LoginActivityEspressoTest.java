package com.cse40333.satchel;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;

import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class LoginActivityEspressoTest {

    @Rule
    public ActivityTestRule<LoginActivity> mActivityRule = new ActivityTestRule<>(LoginActivity.class);

    @Test
    public void loginTest() {
        // Type text and then press the button.
        onView(withId(R.id.email))
                .perform(typeText("pdrumm@nd.edu"));
        onView(withId(R.id.password))
                .perform(typeText("test123"), closeSoftKeyboard());
        onView(withId(R.id.email_sign_in_button)).perform(click());

        // Check that the text was changed.
        onView(withId(R.id.activity_main)).check(matches(isDisplayed()));
    }
}
