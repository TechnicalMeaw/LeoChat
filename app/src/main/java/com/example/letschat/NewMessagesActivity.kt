package com.example.letschat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toolbar
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.letschat.models.User
import com.example.letschat.models.getDate
import com.example.letschat.models.getTime
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_new_messages.*
import kotlinx.android.synthetic.main.user_row_new_messages.view.*

class NewMessagesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_messages)

        // Setting Up The Action Bar
        setSupportActionBar(toolbar_newMessages)
        supportActionBar?.title = "Select User"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        // Fetching Users
        val adapter = GroupAdapter<GroupieViewHolder>()
        newMessageView.adapter = adapter
        fetchUsers(adapter)
        progressBar.visibility = View.VISIBLE

        // Thin Line Between Users
        newMessageView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))


        // Updating Last Seen Status
        updateStatus("online")
    }

    companion object{
        const val USER_KEY = "USER_KEY"
        const val CURRENT_USER ="CURRENT_USER"
    }

    var currentUser: User? = null
    fun fetchUsers(adapter: GroupAdapter<GroupieViewHolder>){
        val ref = FirebaseDatabase.getInstance().getReference("/users")
        ref.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach{
                    Log.d("NewMessageActivity", it.toString())

                    val user = it.getValue(User::class.java)

                    if (user?.uid == FirebaseAuth.getInstance().uid){
                        currentUser = user
                    }else{
                        adapter.add(UserItem(user!!))
                    }

                }
                progressBar.visibility = View.GONE

                adapter.setOnItemClickListener{item, view ->

                    val itemUser = item as UserItem

                    val intent = Intent(view.context, chatLogActivity::class.java)
                    intent.putExtra(USER_KEY, itemUser.user)
                    intent.putExtra(CURRENT_USER, currentUser)
                    startActivity(intent)
                    finish()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    // Updating Status
    override fun onPause() {
        updateStatus("Last Seen on ${getDate() + " at " + getTime()}")
        super.onPause()
    }

    override fun onResume() {
        updateStatus("online")
        super.onResume()
    }

    private fun updateStatus(string:String){
        val currentUserReference = FirebaseDatabase.getInstance().getReference("/users/${FirebaseAuth.getInstance().uid}")
        currentUserReference.child("status").setValue(string)
    }
}




class UserItem(val user: User): Item<GroupieViewHolder>(){
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.newMessageUsername.text = user.username
        Picasso.get().load(user.thumb).into(viewHolder.itemView.newMessageProfileImageView)
    }
    override fun getLayout(): Int {

        return R.layout.user_row_new_messages
    }


}