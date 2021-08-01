package com.example.letschat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.GridLayoutManager
import com.example.letschat.backgroundWallpaper.backgroundImage
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_background_wallpaper.*
import kotlinx.android.synthetic.main.activity_user_info.*
import kotlinx.android.synthetic.main.background_image_cardview.view.*
import java.net.URL
import java.util.*
import kotlin.random.Random

class BackgroundWallpaperActivity : AppCompatActivity() {

    private val adapter = GroupAdapter<GroupieViewHolder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_background_wallpaper)

        setSupportActionBar(toolbar_BackgroundImage)
        supportActionBar?.title = "Choose Background"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        backgroundChooserRecyclerView.adapter = this.adapter
        val gridLayoutManager = GridLayoutManager(this, 2)
        backgroundChooserRecyclerView.layoutManager = gridLayoutManager



        adapter.setOnItemClickListener { item, view ->
            val image = item as imageItem
            backgroundImage.setImageUrl(image.url)
            finish()
        }



        loadWallpapers()
    }

    private fun loadWallpapers() {
        var i = 10
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)

        val height = displayMetrics.heightPixels
        val width = displayMetrics.widthPixels
        val arrayList = ArrayList<Int>()
        while (i>0){
            arrayList.add(Random.nextInt(0,500))
//            adapter.add(imageItem("https://picsum.photos/id/$i/$width/$height/"))
            i--
        }
        adapter.clear()
        arrayList.forEach {
            adapter.add(imageItem("https://picsum.photos/id/$it/$width/$height/"))
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.background_image_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.refreshImageBtn -> {
                loadWallpapers()
            }}
        return super.onOptionsItemSelected(item)
    }
}


class imageItem(private val Url: String): Item<GroupieViewHolder>() {
    val url = Url
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        Picasso.get().load(url).into(viewHolder.itemView.imageCardView.backgroundCardImageView)
    }

    override fun getLayout(): Int {
        return R.layout.background_image_cardview
    }

}