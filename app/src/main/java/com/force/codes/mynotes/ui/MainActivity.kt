package com.force.codes.mynotes.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import androidx.lifecycle.lifecycleScope
import com.force.codes.R
import com.force.codes.databinding.FragmentSplashScreenBinding
import com.force.codes.mynotes.core.base.BaseFragment
import com.force.codes.mynotes.core.base.BindingInflater
import com.force.codes.mynotes.core.extensions.navigateTo
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    class SplashScreenFragment : BaseFragment<FragmentSplashScreenBinding>() {

        override val bindingInflater: BindingInflater<FragmentSplashScreenBinding>
            get() = FragmentSplashScreenBinding::inflate

        override fun setupView() {
            viewLifecycleOwner.lifecycleScope.launch {
                delay(2000L)
                navigateTo(R.id.action_splashScreenFragment_to_fragmentMyNotes)
            }
        }
    }
}