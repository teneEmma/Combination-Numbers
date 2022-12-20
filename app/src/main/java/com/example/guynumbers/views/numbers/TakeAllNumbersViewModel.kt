package com.example.guynumbers.views.numbers


import android.os.Environment
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.guynumbers.common.Combination
import com.example.guynumbers.common.Constants
import com.example.guynumbers.common.Constants.EXETENSION_EXCEL
import com.example.guynumbers.common.Constants.FOLDER_DIR
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.xssf.usermodel.XSSFRow
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream

class TakeAllNumbersViewModel() : ViewModel() {

    private var _liveNumbersTaken = MutableLiveData<Int>()
    val liveNumbersTaken: LiveData<Int>
        get() = _liveNumbersTaken
    private var _successful = MutableLiveData<Boolean>(false)
    val successful: LiveData<Boolean>
        get() = _successful
    private val _message = MutableLiveData<String>()
    val message: LiveData<String>
    get() = _message

    init{
        _liveNumbersTaken.value = 0
    }

    fun increment() = _liveNumbersTaken.postValue(_liveNumbersTaken.value?.plus(1))

    fun clear(){
        _liveNumbersTaken.value = 0
    }

    fun createExcelFile(fileName: String, array: MutableList<Int>){
        viewModelScope.launch(Dispatchers.IO) {
            val filename = "${fileName}$EXETENSION_EXCEL"
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet(Constants.SHEET_NAME)
            setHeader(sheet, array)
            val n = array.size
            val result = mutableListOf<Int>()

            val obj = Combination()
            for(i in 0..n) {
                result.add(obj.combination(array.toIntArray(), n, i))
                Log.e("VALUES", result.toString())
            }

            setBody(sheet, result)

            val directory = File("${Environment.getExternalStorageDirectory()}/$FOLDER_DIR")
            if(!directory.exists()) {
                if(directory.mkdir())
                    _message.postValue("$FOLDER_DIR Crée Avec Success")
                else {
                    _message.postValue("$FOLDER_DIR N'A PAS pu être Crée")
                    return@launch
                }
            }

            try{
                val fileOutputStream = FileOutputStream(File(directory, filename))
                workbook.write(fileOutputStream)
                fileOutputStream.close()
                workbook.close()
                _successful.postValue(true)
            }catch(e: Exception){
                Log.e("BUG", e.stackTraceToString())
                _message.postValue("UNE ERREUR C'est Produite ${e.message}")
                _successful.postValue(false)
            }
        }
    }

    private fun setHeader(sheet: XSSFSheet, header: MutableList<Int>){
        val row = sheet.createRow(0)
            val cell = row.createCell(0, CellType.STRING)
            cell.setCellValue("COMBINAISION POUR ${header.size} NOMBRES")
    }

    private fun setBody(sheet: XSSFSheet, result: MutableList<Int>){
        var row: XSSFRow
        for(r in 1 until result.size){
            row = sheet.createRow(r)
            var cell = row.createCell(0, CellType.STRING)
            cell.setCellValue("n = $r")
            cell = row.createCell(1, CellType.NUMERIC)
            cell.setCellValue(result[r].toDouble())
        }

        row = sheet.createRow(result.size)
        var totalCell = row.createCell(0, CellType.STRING)
        totalCell.setCellValue("TOTAL")
        totalCell = row.createCell(1, CellType.NUMERIC)
        totalCell.setCellValue(result.subList(1, result.size).sum().toDouble())
    }
}