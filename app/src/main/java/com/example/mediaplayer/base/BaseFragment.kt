package com.example.mediaplayer.base

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.example.mediaplayer.tools.SnackBar
import com.example.mediaplayer.tools.toast

abstract class BaseFragment<B: ViewBinding> : Fragment() {

    protected lateinit var binding : B

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = getFragmentBinding(inflater,container)
        return binding.root
    }

    abstract fun getFragmentBinding(inflater: LayoutInflater, container: ViewGroup?) : B

    fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }

    fun snackBar(text: String) {
        view?.let {
            SnackBar(requireView() ,text)
        }
    }

    fun toast(message: String) {
        requireContext().toast(message)
    }

}