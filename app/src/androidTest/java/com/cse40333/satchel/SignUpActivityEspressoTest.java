package com.cse40333.satchel;


import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.UUID;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

@RunWith(AndroidJUnit4.class)
public class SignUpActivityEspressoTest {

    String emailDomain = "@gmail.com";

    @Rule
    public ActivityTestRule<SignUpActivity> mActivityRule = new ActivityTestRule<>(SignUpActivity.class);


    public void fillOutForm(String email, String pwd1, String pwd2) {
        // Type text and then press the button.
        onView(withId(R.id.email))
                .perform(typeText(email));
        onView(withId(R.id.password))
                .perform(typeText(pwd1));
        onView(withId(R.id.password2))
                .perform(typeText(pwd2), closeSoftKeyboard());
    }

    @Test
    public void signupInvalidEmail() {
        String invalidEmail = UUID.randomUUID().toString();
        String pwd = UUID.randomUUID().toString();
        // Fill out & submit form
        fillOutForm(invalidEmail, pwd, pwd);
        onView(withId(R.id.email_sign_up_button)).perform(click());
        // Check that the text was changed.
        onView(withId(R.id.activity_signup)).check(matches(isDisplayed()));
    }

    @Test
    public void signupPasswordsDontMatch() {
        String email = UUID.randomUUID().toString() + emailDomain;
        String pwd1 = UUID.randomUUID().toString();
        String pwd2 = UUID.randomUUID().toString();
        // Fill out & submit form
        fillOutForm(email, pwd1, pwd2);
        onView(withId(R.id.email_sign_up_button)).perform(click());
        // Check that the text was changed.
        onView(withId(R.id.activity_signup)).check(matches(isDisplayed()));
    }

    @Test
    public void signupEmailInUse() {
        /*
        Successful sign up
         */
        String email = "zzzzz" + UUID.randomUUID().toString() + emailDomain;
        String pwd = UUID.randomUUID().toString();
        // Fill out & submit form
        fillOutForm(email, pwd, pwd);
        onView(withId(R.id.email_sign_up_button)).perform(click());
        // Check that the text was changed.
        onView(withId(R.id.activity_main)).check(matches(isDisplayed()));
        /*
        return to sign up page
         */
        pressBack(); // TODO: this kills the app. we need to logout, not press back
        /*
        Unsuccessful sign up
         */
        String pwd2 = UUID.randomUUID().toString();
        // Fill out & submit form
        fillOutForm(email, pwd2, pwd2);
        onView(withId(R.id.email_sign_up_button)).perform(click());
        // Check that the text was changed.
        onView(withId(R.id.activity_signup)).check(matches(isDisplayed()));
    }

    @Test
    public void signupSuccess() {
        String email = UUID.randomUUID().toString() + emailDomain;
        String pwd = UUID.randomUUID().toString();
        // Fill out & submit form
        fillOutForm(email, pwd, pwd);
        onView(withId(R.id.email_sign_up_button)).perform(click());
        // Check that the text was changed.
        onView(withId(R.id.activity_main)).check(matches(isDisplayed()));
    }
}
