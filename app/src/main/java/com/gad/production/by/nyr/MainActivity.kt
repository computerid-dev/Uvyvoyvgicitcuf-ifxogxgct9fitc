package com.gad.production.by.nyr

import android.app.*
import android.os.Bundle
import android.content.*
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.print.*
import android.view.*
import android.widget.*
import androidx.core.content.ContextCompat
import org.json.JSONArray
import org.json.JSONObject
import java.text.NumberFormat
import java.util.*

class MainActivity: Activity(){
 private val p by lazy{getSharedPreferences("gad",0)}
 private val nf=NumberFormat.getCurrencyInstance(Locale("id","ID"))
 private lateinit var date:EditText; private lateinit var loc:EditText; private lateinit var veh:EditText; private lateinit var plate:EditText
 private lateinit var op:EditText; private lateinit var fuel:EditText; private lateinit var toll:EditText; private lateinit var park:EditText; private lateinit var sec:EditText; private lateinit var meal:EditText
 private lateinit var total:TextView; private lateinit var remain:TextView
 private var editing=-1

 override fun onCreate(b:Bundle?){super.onCreate(b); ui()}

 private fun dp(v:Int):Int=(v*resources.displayMetrics.density).toInt()
 private fun col(res:Int):Int=ContextCompat.getColor(this,res)
 private fun lp(top:Int=0,bottom:Int=16)=LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT).apply{topMargin=dp(top);bottomMargin=dp(bottom)}

 private fun labeledInput(container:LinearLayout,label:String):EditText{
  val wrap=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL}
  val lbl=TextView(this).apply{text=label.uppercase();setTextColor(col(R.color.neo_text_secondary));textSize=12f;setTypeface(typeface,Typeface.BOLD);setPadding(dp(2),0,0,dp(6))}
  val input=EditText(this).apply{hint=label;setTextColor(col(R.color.neo_text_primary));setHintTextColor(col(R.color.neo_text_secondary));background=ContextCompat.getDrawable(this@MainActivity,R.drawable.neo_input_bg);setPadding(dp(14),dp(14),dp(14),dp(14));textSize=15f}
  wrap.addView(lbl);wrap.addView(input);container.addView(wrap,lp(bottom=18));return input
 }
 private fun cardText(container:LinearLayout,text:String,bg:Int):TextView{
  val t=TextView(this).apply{this.text=text;setTextColor(col(R.color.neo_text_on_accent));textSize=18f;setTypeface(typeface,Typeface.BOLD);setPadding(dp(16),dp(16),dp(16),dp(16));background=ContextCompat.getDrawable(this@MainActivity,bg)}
  container.addView(t,lp(bottom=16));return t
 }
 private fun neoButton(container:LinearLayout,text:String,bg:Int):Button{
  val b=Button(this).apply{this.text=text;setAllCaps(true);setTextColor(col(R.color.neo_text_on_accent));setTypeface(typeface,Typeface.BOLD);background=ContextCompat.getDrawable(this@MainActivity,bg);setPadding(dp(12),dp(18),dp(12),dp(18));stateListAnimator=null;elevation=0f;textSize=15f}
  container.addView(b,lp(bottom=16));return b
 }

 private fun ui(){
  val s=ScrollView(this).apply{setBackgroundColor(col(R.color.neo_background))}
  val l=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(20),dp(24),dp(20),dp(40))}

  val header=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(20),dp(20),dp(20),dp(20));background=ContextCompat.getDrawable(this@MainActivity,R.drawable.neo_box_yellow)}
  val h=TextView(this).apply{text="GAD PRODUCTION";textSize=28f;setTypeface(typeface,Typeface.BOLD);setTextColor(col(R.color.neo_text_on_accent))}
  val sub=TextView(this).apply{text="LAPORAN OPERASIONAL KENDARAAN";textSize=13f;setTypeface(typeface,Typeface.BOLD);setTextColor(col(R.color.neo_text_on_accent));setPadding(0,dp(6),0,0)}
  header.addView(h);header.addView(sub)
  l.addView(header,lp(bottom=26))

  date=labeledInput(l,"Tanggal Event"); loc=labeledInput(l,"Lokasi Event"); veh=labeledInput(l,"Jenis Kendaraan"); plate=labeledInput(l,"Nomor Polisi")
  op=labeledInput(l,"Uang Operasional"); fuel=labeledInput(l,"Solar / Bensin"); toll=labeledInput(l,"Tol"); park=labeledInput(l,"Parkir"); sec=labeledInput(l,"Uang Security"); meal=labeledInput(l,"Uang Makan")

  total=cardText(l,"Total Pengeluaran: Rp 0",R.drawable.neo_box_cyan)
  remain=cardText(l,"Sisa Uang: Rp 0",R.drawable.neo_box_green)

  neoButton(l,"HITUNG OTOMATIS",R.drawable.neo_box_pink).setOnClickListener{calc()}
  neoButton(l,"SIMPAN LAPORAN",R.drawable.neo_box_yellow).setOnClickListener{save()}
  neoButton(l,"RIWAYAT LAPORAN",R.drawable.neo_box_cyan).setOnClickListener{history()}
  neoButton(l,"PRINT / PDF",R.drawable.neo_box_red).setOnClickListener{print()}

  s.addView(l); setContentView(s)
 }
 private fun num(e:EditText)=e.text.toString().replace("[^0-9]".toRegex(),"").toLongOrNull()?:0
 private fun calc():Long{val x=num(fuel)+num(toll)+num(park)+num(sec)+num(meal); total.text="Total Pengeluaran: ${nf.format(x)}";remain.text="Sisa Uang: ${nf.format(num(op)-x)}";return x}
 private fun save(){
  val a=JSONArray(p.getString("reports","[]")); val o=JSONObject()
  o.put("date",date.text);o.put("loc",loc.text);o.put("veh",veh.text);o.put("plate",plate.text);o.put("op",num(op));o.put("fuel",num(fuel));o.put("toll",num(toll));o.put("park",num(park));o.put("sec",num(sec));o.put("meal",num(meal));o.put("total",calc());o.put("remain",num(op)-o.getLong("total"))
  if(editing>=0){a.put(editing,o);editing=-1}else a.put(o);p.edit().putString("reports",a.toString()).apply()
  Toast.makeText(this,"Laporan tersimpan",Toast.LENGTH_SHORT).show()
 }
 private fun history(){
  val a=JSONArray(p.getString("reports","[]")); val items=ArrayList<String>()
  for(i in 0 until a.length()){val o=a.getJSONObject(i);items.add("${i+1}. ${o.getString("date")} | ${o.getString("loc")}\nSisa: ${nf.format(o.getLong("remain"))}")}
  if(items.isEmpty()){AlertDialog.Builder(this).setTitle("Riwayat").setMessage("Belum ada laporan.").setPositiveButton("OK",null).show();return}
  AlertDialog.Builder(this).setTitle("Riwayat Laporan").setItems(items.toTypedArray()){_,which->
   val o=a.getJSONObject(which); AlertDialog.Builder(this).setTitle("Laporan ${which+1}").setMessage(
    "Tanggal: ${o.getString("date")}\nLokasi: ${o.getString("loc")}\nKendaraan: ${o.getString("veh")}\nNo Polisi: ${o.getString("plate")}\n\nOperasional: ${nf.format(o.getLong("op"))}\nSolar/Bensin: ${nf.format(o.getLong("fuel"))}\nTol: ${nf.format(o.getLong("toll"))}\nParkir: ${nf.format(o.getLong("park"))}\nSecurity: ${nf.format(o.getLong("sec"))}\nMakan: ${nf.format(o.getLong("meal"))}\n\nTotal: ${nf.format(o.getLong("total"))}\nSisa: ${nf.format(o.getLong("remain"))}"
   ).setNegativeButton("Hapus"){_,_->a.remove(which);p.edit().putString("reports",a.toString()).apply()}
    .setNeutralButton("Edit"){_,_->load(o,which)}.setPositiveButton("Tutup",null).show()
  }.show()
 }
 private fun load(o:JSONObject,i:Int){editing=i;date.setText(o.getString("date"));loc.setText(o.getString("loc"));veh.setText(o.getString("veh"));plate.setText(o.getString("plate"));op.setText(o.getLong("op").toString());fuel.setText(o.getLong("fuel").toString());toll.setText(o.getLong("toll").toString());park.setText(o.getLong("park").toString());sec.setText(o.getLong("sec").toString());meal.setText(o.getLong("meal").toString());calc()}
 private fun print(){
  val text="""GAD PRODUCTION
LAPORAN OPERASIONAL KENDARAAN

Tanggal Event : ${date.text}
Lokasi Event  : ${loc.text}
Kendaraan     : ${veh.text}
No. Polisi    : ${plate.text}

Uang Operasional : ${nf.format(num(op))}
Solar / Bensin   : ${nf.format(num(fuel))}
Tol              : ${nf.format(num(toll))}
Parkir            : ${nf.format(num(park))}
Security          : ${nf.format(num(sec))}
Makan             : ${nf.format(num(meal))}

Total Pengeluaran: ${nf.format(calc())}
Sisa Uang        : ${nf.format(num(op)-calc())}
"""
  val pm=getSystemService(PRINT_SERVICE) as PrintManager
  pm.print("GAD_OPERASIONAL",object:PrintDocumentAdapter(){
   var pdf:PdfDocument?=null
   override fun onLayout(o:PrintAttributes?,n:PrintAttributes?,c:android.os.CancellationSignal?,cb:LayoutResultCallback?,e:Bundle?){pdf=PdfDocument();cb?.onLayoutFinished(PrintDocumentInfo.Builder("GAD_OPERASIONAL.pdf").setPageCount(1).build(),true)}
   override fun onWrite(r:Array<PageRange>,d:android.os.ParcelFileDescriptor?,c:android.os.CancellationSignal?,cb:WriteResultCallback?){
    val pg=pdf!!.startPage(PdfDocument.PageInfo.Builder(595,842,1).create());val paint=Paint();paint.textSize=13f
    var y=55f;text.lines().forEach{pg.canvas.drawText(it,40f,y,paint);y+=20};pdf!!.finishPage(pg);pdf!!.writeTo(java.io.FileOutputStream(d!!.fileDescriptor));pdf!!.close();cb?.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
   }
  },null)
 }
}