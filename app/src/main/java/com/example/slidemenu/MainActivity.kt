package com.example.slidemenu

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.example.slidemenu.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    companion object {
        const val TAG = "MainActivity"
    }

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.slideMenu.addMenu {
            return@addMenu initSlideArray()
        }
    }

    private fun initSlideArray(): Array<SlideMenu.SlideAttrs> {
        return arrayOf(SlideMenu.SlideAttrs(
            "置顶", Color.GRAY, Color.WHITE
        ) {
            Log.d(TAG, "initSlideArray: 1")
        }, SlideMenu.SlideAttrs(
            "已读", Color.BLUE, Color.WHITE
        ) {
            Log.d(TAG, "initSlideArray: 2")
        }, SlideMenu.SlideAttrs(
            "删除", Color.RED, Color.WHITE
        ) {
            Log.d(TAG, "initSlideArray: 3")
        })
    }
}