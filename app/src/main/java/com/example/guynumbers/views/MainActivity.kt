package com.example.guynumbers.views

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.guynumbers.R
import com.example.guynumbers.common.Constants
import com.example.guynumbers.databinding.ActivityMainBinding
import com.example.guynumbers.views.numbers.TakeAllNumbers

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnEnterElements.setOnClickListener {
            val numberElements = binding.combinationNumberPicker.text?.toString()?.toIntOrNull()
            if(numberElements != null && numberElements >= 2){
                val intent = Intent(this, TakeAllNumbers::class.java)
                intent.apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    putExtra(Constants.numberElementsStr, numberElements)
                }
                startActivity(intent)
            }else
                binding.ilNumberPicker.helperText = resources.getString(R.string.number_not_valide)
        }

    }
}