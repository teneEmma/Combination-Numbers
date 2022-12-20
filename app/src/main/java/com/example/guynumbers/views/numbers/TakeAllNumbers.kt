package com.example.guynumbers.views.numbers

import com.example.guynumbers.common.Constants
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.guynumbers.R
import com.example.guynumbers.common.Constants.STORAGE_PERMISSION_CODE
import com.example.guynumbers.databinding.DialogCreteFileBinding
import com.example.guynumbers.databinding.FragmentTakeAllNumbersBinding
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.Calendar

class TakeAllNumbers : AppCompatActivity() {
    private lateinit var viewModel: TakeAllNumbersViewModel
    private lateinit var binding: FragmentTakeAllNumbersBinding
    private var argument = 0
    private lateinit var dialog: Dialog

    private val requestPermissionActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()
    ) { activityResult ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager())
                showDialog()
            else {
                showSnackWithAction(binding.btnAdd, resources.getString(R.string.permission_needed),
                    resources.getString(R.string.grant_permission)) { requestPermission() }
            }
        }
    }
    private var listElements = mutableListOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentTakeAllNumbersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        argument = retrieveElement()
        viewModel = ViewModelProvider(this)[TakeAllNumbersViewModel::class.java]

        var chosenNumbers = ""
        viewModel.liveNumbersTaken.observe(this){number->
            if(argument >= number){
                binding.btnAdd.isEnabled = true
                binding.btnCreateExcel.isEnabled = false
                binding.tvNthElement.text = formatNumberStr(number)
                chosenNumbers += "${binding.tvTakeNumber.text}-"
                binding.tvTakeNumber.text?.clear()
                binding.chosenNumbers.text = chosenNumbers
            }
            if(number >= argument){
                binding.btnAdd.isEnabled = false
                binding.btnCreateExcel.isEnabled = true
            }
        }

        viewModel.successful.observe(this){isSuccessful->
            if(isSuccessful){
                toast(resources.getString(R.string.saved))
                dialog.dismiss()
                finish()
            }else{
                viewModel.message.value?.let { msg-> toast(msg) }
            }
        }
        viewModel.message.observe(this){
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        }

        binding.btnAdd.setOnClickListener {
            val inputValue = binding.tvTakeNumber.text
            if(inputValue.isNullOrEmpty().not() && inputValue?.contains(".*\\d.*".toRegex()) == true){
                viewModel.increment()
            }
        }

        binding.btnCreateExcel.setOnClickListener {
            if(checkPermissions()){
                val listElementsStr = binding.chosenNumbers.text.split('-')
                listElementsStr.forEachIndexed { index, s -> s.toIntOrNull()?.let { listElements.add(it)} }
                showDialog()
            }else
                requestPermission()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == STORAGE_PERMISSION_CODE && grantResults.isNotEmpty()){
            val write = grantResults[0] == PackageManager.PERMISSION_GRANTED
            val read = grantResults[1] == PackageManager.PERMISSION_GRANTED

            if(read && write)
                showDialog()
            else{
                val writeRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                val readRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)

                if(readRationale.not() || writeRationale.not())
                    showSnackWithAction(binding.btnAdd,
                        resources.getString(R.string.go_seetings_permission),
                        resources.getString(R.string.go)) { rationalePermission() }
                else
                    showSnackWithAction(binding.btnAdd,  resources.getString(R.string.permission_needed),
                        resources.getString(R.string.grant_permission)){ requestPermission() }
            }
        }
    }

    private fun rationalePermission(){
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        requestPermissionActivity.launch(intent)
    }
    private fun showSnackWithAction(view: View, msg: String, actionMsg: String, exe: ()-> Unit){
        Snackbar.make(view, msg, Snackbar.LENGTH_SHORT).setAction(actionMsg) {
                exe()
            }.show()
    }
    private fun retrieveElement(): Int  = intent.getIntExtra(Constants.numberElementsStr, 0)

    private fun setNewDialogParams(window: Window): WindowManager.LayoutParams{
        val layoutParams = window.attributes
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
        return layoutParams
    }

    private fun showDialog() {
        val dialogBinding = DialogCreteFileBinding.inflate(layoutInflater)
        dialog = Dialog(this)
        dialog.setContentView(dialogBinding.root)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val layoutParams = dialog.window?.let {
            setNewDialogParams(it)
        }
        dialog.show()
        dialog.window?.attributes = layoutParams

        dialogBinding.btnCreate.setOnClickListener {
            val sheetName = dialogBinding.tvFilename.text.toString()
            createFile(sheetName)
        }

        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }
    }

    private fun createFile2() {

    }

    private fun formatNumberStr(number: Int): String {
        return when (number + 1) {
            1 -> "${number}ier numéro"
            else -> "${number}ieme numéro"
        }
    }

    private fun requestPermission(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            val intent = Intent()
            try{
                intent.action = Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
                val uri = Uri.fromParts("package", this.packageName, null)
                intent.data = uri
                requestPermissionActivity.launch(intent)
            }catch(e: Exception){
                toast("${resources.getString(R.string.permission_problem)} ${e.message}")
                intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                requestPermissionActivity.launch(intent)
            }
        }else{
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.READ_EXTERNAL_STORAGE), STORAGE_PERMISSION_CODE)
        }
    }

    private fun checkPermissions(): Boolean{
        return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            Environment.isExternalStorageManager()
        }else{
            val write = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            val read = ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)

            return write == PackageManager.PERMISSION_GRANTED && read == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun createFile(name: String?){
        var fileName = name
        if(fileName.isNullOrEmpty()){
            val dateFormat = SimpleDateFormat("MM_dd_yyyy_HH_mm")
            val date = dateFormat.format(Calendar.getInstance().time)
            fileName = date
        }
        fileName?.apply {
            viewModel.createExcelFile(this, listElements)
        }
    }

    private fun toast(msg: String){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

}