package com.example.weatherapp.ui.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.navigation.fragment.findNavController
import com.example.weatherapp.ui.WeatherActivity
import com.example.weatherapp.ui.WeatherViewModel
import com.example.weathersapp.R

class LaunchFragment : Fragment() {
    lateinit var viewModel: WeatherViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_launch, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = (activity as WeatherActivity).viewModel

        view.findViewById<com.airbnb.lottie.LottieAnimationView>(R.id.animationView).apply {
            setAnimation(R.raw.raw1)
            playAnimation()
            repeatCount = com.airbnb.lottie.LottieDrawable.INFINITE
        }

        Handler(Looper.getMainLooper()).postDelayed({
            if (isAdded) {
                findNavController().navigate(R.id.homeFragment)
//                parentFragmentManager.commit {
//                    replace(R.id.weatherNavFragment, HomeFragment())
//                    addToBackStack(null)
//                }
            }
        }, 3000)
    }
}
