/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.onboarding

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.view.animation.TranslateAnimation
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.messaging.FirebaseMessaging
import org.mozilla.fenix.BrowserDirection
import org.mozilla.fenix.HomeActivity
import org.mozilla.fenix.NavHostActivity
import org.mozilla.fenix.R
import org.mozilla.fenix.ext.nav
import org.mozilla.fenix.ext.requireComponents


// The Donation Reminder onboarding journey

class OnboardingFragment : Fragment(R.layout.onboarding_donationreminder_demo) {

    private var page = 0
    private lateinit var imgAnim1: ImageView
    private lateinit var imgAnim2: ImageView
    private lateinit var handlerAnimation: Handler
    private lateinit var nextStep: Button
    private lateinit var title: TextView
    private lateinit var desc: TextView


    private val requestNotificationPermission =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { _: Boolean ->
            finishOnboarding()
        }


    // Produce pulse animation
    private var runnableAnim: Runnable = Runnable {

        run {

            imgAnim1.animate().scaleX(75f).scaleY(75f).alpha(0f).setDuration(1000).withEndAction {
                run {
                    imgAnim1.scaleX = 1f
                    imgAnim1.scaleY = 1f
                    imgAnim1.alpha = 1f

                }
            }

            imgAnim2.animate().scaleX(75f).scaleY(75f).alpha(0f).setDuration(700).withEndAction {
                run {
                    imgAnim2.scaleX = 1f
                    imgAnim2.scaleY = 1f
                    imgAnim2.alpha = 1f

                }
            }

            handlerAnimation.postDelayed(runnableAnim, 1500)
        }

    }


    /**
     * Create the page
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setHasOptionsMenu(false)
    }

    /**
     * Modify the page layout to match the requirements
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        // Only show the title on the action bar
        val actionBar = (activity as NavHostActivity).getSupportActionBarAndInflateIfNecessary()
        actionBar.setDisplayHomeAsUpEnabled(false)
        actionBar.show()


        // Get the handle to the title, desc and next step button
        val activity = activity as AppCompatActivity
        title = activity.findViewById(R.id.onboarding_dr_title)
        desc = activity.findViewById(R.id.onboarding_dr_desc)
        nextStep = activity.findViewById(R.id.onboarding_dr_next_step_button)

        desc.movementMethod = ScrollingMovementMethod()

        // Now - present the first step
        page = 0
        nextStep()


        // Wait for the next step button to be clicked
        nextStep.setOnClickListener {

            // Display the next page
            nextStep()
        }

        // Define Pulse Animation Handler
        handlerAnimation = Handler(Looper.getMainLooper())

    }

    /**
     * Enable or disable the next step button
     */
    private fun nextStepButton(enabled: Boolean) = when (enabled) {
        false -> {
            nextStep.alpha = 0.7f
            nextStep.isEnabled = false
        }
        else -> {
            nextStep.alpha = 1f
            nextStep.isEnabled = true
        }
    }

    /**
     * Support paging
     */
    private fun nextStep() {

        // Disable the next step button until the page is complete
        nextStepButton(false)

        page += 1

        when (page) {

            1 -> {

                // Update the views with the latest step data
                title.text = getString(R.string.onboarding_dr_title1)
                desc.text = getString(R.string.onboarding_dr_desc1)
                nextStep.text = getString(R.string.onboarding_dr_step1)

                nextStepButton(true)
            }

            2 -> {


                // Update the views with the latest step data
                title.text = getString(R.string.onboarding_dr_title2)
                desc.text = getString(R.string.onboarding_dr_desc2)
                nextStep.text = getString(R.string.onboarding_dr_step2)

                // Fade in the search page
                activity?.let {
                    fadeOut(it.findViewById(R.id.onboarding_dr_start)) {}
                    fadeIn(it.findViewById(R.id.onboarding_dr_search)) {

                        // Display the pulse over the heart cart
                        pulse(
                            it.findViewById(R.id.onboarding_dr_pulse_heart_cart),
                            it.findViewById(R.id.onboarding_dr_pulse_heart_cart1),
                            2000,
                        ) {
                            nextStepButton(true)
                        }
                    }
                }

            }
            3 -> {

                // Update the views with the latest step data
                title.text = getString(R.string.onboarding_dr_title3)
                desc.text = getString(R.string.onboarding_dr_desc3)
                nextStep.text = getString(R.string.onboarding_dr_step3)

                // Slide the sidebar in
                activity?.let {
                    show(it.findViewById(R.id.onboarding_dr_sidebar_fade))
                    slideLeft(it.findViewById(R.id.onboarding_dr_sidebar), 1000)
                }

                nextStepButton(true)
            }

            4 -> {

                // Update the views with the latest step data
                title.text = getString(R.string.onboarding_dr_title4)
                desc.text = getString(R.string.onboarding_dr_desc4)
                nextStep.text = getString(R.string.onboarding_dr_step4)

                // Pulse over John lewis
                activity?.let {
                    pulse(
                        it.findViewById(R.id.onboarding_dr_pulse_jl),
                        it.findViewById(R.id.onboarding_dr_pulse_jl),
                        2000,
                    ) {
                        // Transition to John Lewis & Fade in the thankyou popup
                        fadeOut(it.findViewById(R.id.onboarding_dr_sidebar)) {}
                        fadeOut(it.findViewById(R.id.onboarding_dr_sidebar_fade)) {}
                        fadeOut(it.findViewById(R.id.onboarding_dr_search)) {}
                        fadeIn(it.findViewById(R.id.onboarding_dr_jl)) {

                            sleep(1000) {
                                fadeIn(it.findViewById(R.id.onboarding_dr_jl_thank_you)) {
                                    nextStepButton(true)
                                }
                            }


                        }

                    }
                }
            }
            5 -> {

                // Update the views with the latest step data
                title.text = getString(R.string.onboarding_dr_title5)
                desc.text = getString(R.string.onboarding_dr_desc5)
                nextStep.text = getString(R.string.onboarding_dr_step5)
                nextStep.setBackgroundResource(R.drawable.round_shape_green_button);

                // Fade out the thank you popup and show the notification
                activity?.let {
                    fadeOut(it.findViewById(R.id.onboarding_dr_jl_thank_you)) {}
                    fadeOut(it.findViewById(R.id.onboarding_dr_jl)) {}

                    fadeIn(it.findViewById(R.id.onboarding_dr_phone_home)) {

                        sleep(1000) {
                            fadeIn(it.findViewById(R.id.onboarding_dr_notification)) {
                                nextStepButton(true)
                            }
                        }

                    }
                }

            }
            6 -> {


                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    val permission = Manifest.permission.POST_NOTIFICATIONS

                    if (activity?.let { ActivityCompat.checkSelfPermission(it, permission) } != PackageManager.PERMISSION_GRANTED){
                        requestNotificationPermission.launch(permission)
                    }

                } else {
                    finishOnboarding()
                }


            }
        }

    }

    private fun finishOnboarding() {

        // Subscribe the device to the communications topic
        FirebaseMessaging.getInstance().subscribeToTopic("communications");

        requireComponents.fenixOnboarding.finish()

        // Navigate back to the home page
        findNavController().nav(
            id = R.id.onboardingFragment,
            directions = OnboardingFragmentDirections.actionHome(),
        )

        // Show the app welcome page
        (activity as HomeActivity).openToBrowserAndLoad(
            searchTermOrURL = "https://www.giveasyoulive.com/app-welcome",
            newTab = true,
            from = BrowserDirection.FromHome,
        )



    }


    /**
     * Sleep for a specified interval
     */
    private fun sleep(interval: Long, callback: () -> Unit) {

        val handler = Handler(Looper.getMainLooper())

        handler.postDelayed(
            {

                handler.removeCallbacksAndMessages(null)

                callback()

            },
            interval,
        )
    }

    /**
     * Slide the view from right to left
     */
    private fun slideLeft(img: ImageView, interval: Long) {
        val animation = TranslateAnimation(500.0f, 0.0f, 0.0f, 0.0f)

        animation.duration = interval
        animation.repeatCount = 0
        animation.fillAfter = true
        img.startAnimation(animation);

    }

    /**
     * Pulse icon
     */
    private fun pulse(imgAnim1: ImageView, imgAnim2: ImageView, interval: Long, callback: () -> Unit) {

        this.imgAnim1 = imgAnim1
        this.imgAnim2 = imgAnim2

        // Make the animation element visible
        imgAnim1.visibility = View.VISIBLE
        imgAnim2.visibility = View.VISIBLE

        // Run the animation
        runnableAnim.run()

        sleep(interval) {

            // Hide the animation
            imgAnim1.visibility = View.INVISIBLE
            imgAnim2.visibility = View.INVISIBLE

            handlerAnimation.removeCallbacksAndMessages(null)

            callback()
        }

    }

    /**
     * Show image
     */
    private fun show(img: ImageView) {
        img.visibility = View.VISIBLE
    }

    /**
     * Hide image
     */
    private fun hide(img: ImageView) {
        img.visibility = View.INVISIBLE
    }


    /**
     * Fade in ImageView
     */
    private fun fadeIn(img: ImageView, callback: () -> Unit ) {
        val fadeIn = ObjectAnimator.ofFloat(img, "alpha", 0f, 1f)
        fadeIn.duration = 500
        fadeIn.addListener(
            object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    // We wanna set the view to VISIBLE, but with alpha 0. So it appear invisible in the layout.
                    img.visibility = View.VISIBLE
                    img.alpha = 0f

                    callback()
                }
            },
        )
        fadeIn.start()
    }

    /**
     * Fade out ImageView
     */
    private fun fadeOut(img: ImageView, callback: () -> Unit) {
        val fadeOut = ObjectAnimator.ofFloat(img, "alpha", 1f, 0f)
        fadeOut.duration = 500
        fadeOut.addListener(
            object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    // We wanna set the view to GONE, after it's fade out. so it actually disappear from the layout & don't take up space.
                    img.visibility = View.GONE

                    callback()
                }
            },
        )
        fadeOut.start()
    }

}
