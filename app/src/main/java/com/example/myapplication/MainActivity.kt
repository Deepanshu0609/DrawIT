package com.example.myapplication

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.app.Instrumentation
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.media.MediaScannerConnection
import android.media.tv.TvContract.AUTHORITY
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.get
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream


class MainActivity : AppCompatActivity() {
    private var drawingView: DrawingView? = null
    private var mImageButtoncurrentcolor : ImageButton? =null

    var customdialog: Dialog?=null
    val opengallery : ActivityResultLauncher<Intent> =
           registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
               result->
               if(result.resultCode== RESULT_OK&&result.data!=null)
               {
                   val imgview : ImageView= findViewById(R.id.iv_frame)
                   imgview.setImageURI(result.data?.data)
               }
           }

    val requestPermission: ActivityResultLauncher<Array<String>> =
         registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){
             permissions ->
             permissions.entries.forEach{
                  val permissionName = it.key
                 val isGranted = it.value
                 if(isGranted)
                 {
                     val pickIntent = Intent(Intent.ACTION_PICK,
                         MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                         opengallery.launch(pickIntent)

                 }else{
                     if(permissionName== Manifest.permission.READ_EXTERNAL_STORAGE){
                         Toast.makeText(this,"Oops you denied",Toast.LENGTH_LONG).show()
                     }
                 }
             }
         }


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        drawingView = findViewById(R.id.drawing_view)
        drawingView?.setSizeforbrush(20.toFloat())
        val linearLayout =findViewById<LinearLayout>(R.id.ll_color)
       mImageButtoncurrentcolor = linearLayout[1] as ImageButton
        val curr = mImageButtoncurrentcolor!!.tag.toString()
        drawingView?.setColor(curr)

        mImageButtoncurrentcolor!!.setImageDrawable(
            ContextCompat.getDrawable(this,R.drawable.pressed)
        )
        val brushbtn = findViewById<ImageButton>(R.id.ib_brush)
        brushbtn.setOnClickListener {
            showbrushsizeSelectordialog()
        }
        val ibgallery =findViewById<ImageButton>(R.id.ib_gallery)
        ibgallery.setOnClickListener{
                requestStoragePermission()
        }
      val btnundo: ImageButton=findViewById(R.id.ib_undo)
         btnundo.setOnClickListener{
             drawingView?.OnclickUndo()
         }
        val btnredo: ImageButton = findViewById(R.id.ib_redo)
         btnredo.setOnClickListener{
             drawingView?.Onclickredo()
         }
        val eraser: ImageButton = findViewById(R.id.ib_eraser)
        eraser.setOnClickListener{
            val erase = eraser.tag.toString()
             drawingView?.setColor(erase)
        }
        val btnsave : ImageButton=findViewById(R.id.ib_save)
        btnsave.setOnClickListener{
           if(isReadStorageAllowed()){
               Showprogressdialog()
               lifecycleScope.launch{
                   val frame:FrameLayout=findViewById(R.id.frame)
                   saveBitmapfile(getBitmanFromView(frame))
               }
           }
        }
    }
    fun showbrushsizeSelectordialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.brush_size)
        dialog.setTitle("Brush size:")

        val smallbtn :ImageButton = dialog.findViewById(R.id.ib_small_brush)
        smallbtn.setOnClickListener {
            drawingView?.setSizeforbrush(10.toFloat())
            dialog.dismiss()
        }
        val mediumbtn :ImageButton = dialog.findViewById(R.id.ib_medium_brush)
        mediumbtn.setOnClickListener {
            drawingView?.setSizeforbrush(20.toFloat())
            dialog.dismiss()
        }
        val largebtn :ImageButton = dialog.findViewById(R.id.ib_large_brush)
        largebtn.setOnClickListener {
            drawingView?.setSizeforbrush(30.toFloat())
            dialog.dismiss()
        }

        dialog.show()
        dialog.setCancelable(false)
    }
    fun paintClicked(view: View){

         if(view!==mImageButtoncurrentcolor)
         {
             var imagebutton = view as ImageButton
             val colortag = imagebutton.tag.toString()
             drawingView?.setColor(colortag)
             imagebutton.setImageDrawable(
                 ContextCompat.getDrawable(this,R.drawable.pressed)


             )
             mImageButtoncurrentcolor!!.setImageDrawable(
                 ContextCompat.getDrawable(this,R.drawable.normal)
             )
             mImageButtoncurrentcolor= view
         }
    }
    private fun isReadStorageAllowed(): Boolean{
        val result=ContextCompat.checkSelfPermission(this,
        Manifest.permission.WRITE_EXTERNAL_STORAGE)
        return result ==PackageManager.PERMISSION_GRANTED
    }
    private fun requestStoragePermission(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
        ){
            showRationalDialog("kids Drawing app","Kids Drawing App needs to access your External Storage")
        }else{
            requestPermission.launch(arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE

            ))
        }
    }
    private fun getBitmanFromView(view: View): Bitmap{
        val returnedbitmap= Bitmap.createBitmap(view.width,view.height,Bitmap.Config.ARGB_8888)
        val canvas = Canvas(returnedbitmap)
         if(view.background!=null){
             view.background.draw(canvas)

         }else{
             canvas.drawColor(Color.WHITE)
         }
             view.draw(canvas)

        return returnedbitmap
    }
    private suspend fun saveBitmapfile(mBitmap: Bitmap): String{
        var result=" "
        withContext(Dispatchers.IO) {
            if (mBitmap != null) {
                try {

                } catch (e: Exception) {
                    result = ""
                    e.printStackTrace()
                }
            }
        }
        return result
        }
    private fun showRationalDialog(title: String, message: String)
    {
        val builder =AlertDialog.Builder(this)
        builder.setTitle(title)
            .setMessage(message)
            .setPositiveButton("Cancel"){
                dialog,_ ->
                dialog.dismiss()

            }
        builder.create().show()
    }
     private fun Showprogressdialog(){
         customdialog =Dialog(this)
         customdialog?.setContentView(R.layout.custom_progress)
         customdialog?.show()
     }
    private fun closeprogressdialog(){
        if(customdialog!=null)
        {
            customdialog?.dismiss()
            customdialog = null
        }
    }

}