package jp.techacademy.hiromu.naitou.qa_app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.google.firebase.auth.FirebaseAuth
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream
import java.util.Base64

import jp.techacademy.hiromu.naitou.qa_app.databinding.ListAnswerBinding
import jp.techacademy.hiromu.naitou.qa_app.databinding.ListQuestionDetailBinding

class QuestionDetailListAdapter(context: Context, private val question: Question) : BaseAdapter() {
    companion object {
        private const val TYPE_QUESTION = 0
        private const val TYPE_ANSWER = 1
    }
    private lateinit var databaseReference: DatabaseReference
    private var layoutInflater: LayoutInflater

    init {
        layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    override fun getCount(): Int {
        return 1 + question.answers.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            TYPE_QUESTION
        } else {
            TYPE_ANSWER
        }
    }

    override fun getViewTypeCount(): Int {
        return 2
    }

    override fun getItem(position: Int): Any {
        return question
    }

    override fun getItemId(position: Int): Long {
        return 0
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        databaseReference = FirebaseDatabase.getInstance().reference

        val user = FirebaseAuth.getInstance().currentUser
        var isFavorite : String? = null
        val favorite = question.questionUid
        //Log.d("user",user.toString())

        if (getItemViewType(position) == TYPE_QUESTION) {
            // ViewBindingを使うための設定
            val binding = if (convertView == null) {
                ListQuestionDetailBinding.inflate(layoutInflater, parent, false)
            } else {
                ListQuestionDetailBinding.bind(convertView)
            }
            val view: View = convertView ?: binding.root

            binding.bodyTextView.text = question.body
            binding.nameTextView.text = question.name
            //お気に入りの表示
            //val isFavorite = Fire

            //val favorite = question.questionUid
            //val userFavoriteRef = databaseReference.child(UsersPATH).child(user!!.uid).child("favorite").child(favorite)

            //var isFavorite : String
            val childUpdates = hashMapOf<String,Any>()

            //binding.favoriteImageView.isVisible = user != null
            if(user != null) {
                binding.favoriteImageView.isVisible = true
                val userFavoriteRef = databaseReference.child(UsersPATH).child(user!!.uid).child("favorite").child(favorite)
                binding.favoriteImageView.apply {
                    userFavoriteRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val data = snapshot.value as Map<*, *>?
                            //Log.d("test",data!!["name"] as String)
                            try {
                                data!!["body"] as String
                                isFavorite = "1"
                                setImageResource(if (isFavorite == "1") R.drawable.ic_star else R.drawable.ic_star_border)
                            } catch (e: Exception) {
                                isFavorite = "0"
                                setImageResource(if (isFavorite == "1") R.drawable.ic_star else R.drawable.ic_star_border)
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                        }
                    })
                    //isFavorite?.let { Log.d("test", it) }
                    setOnClickListener {
                        if ("0" == isFavorite) {
                            childUpdates["body"] = question.body
                            childUpdates["image"] =
                                Base64.getEncoder().encodeToString(question.imageBytes)
                            childUpdates["name"] = question.name
                            childUpdates["title"] = question.title
                            childUpdates["uid"] = question.uid
                            isFavorite = "1"
                            setImageResource(if (isFavorite == "1") R.drawable.ic_star else R.drawable.ic_star_border)
                            userFavoriteRef.updateChildren(childUpdates)
                        } else if ("1" == isFavorite) {
                            isFavorite = "0"
                            setImageResource(if (isFavorite == "1") R.drawable.ic_star else R.drawable.ic_star_border)
                            userFavoriteRef.removeValue()
                        }
                        //val userRefF = databaseReference.child(UsersPATH).child(user!!.uid).equalTo(favorite)
                        //val map = dataSnapshot.value as Map<*, *>
                        //val user_name = map["title"] as? String ?: ""
                        isFavorite?.let { it1 -> Log.d("test", it1) }
                    }
                }
            }
            else
                binding.favoriteImageView.isVisible = false

            val bytes = question.imageBytes
            if (bytes.isNotEmpty()) {
                val image = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    .copy(Bitmap.Config.ARGB_8888, true)
                binding.imageView.setImageBitmap(image)
            }

            return view
        } else {
            // ViewBindingを使うための設定
            val binding = if (convertView == null) {
                ListAnswerBinding.inflate(layoutInflater, parent, false)
            } else {
                ListAnswerBinding.bind(convertView)
            }
            val view: View = convertView ?: binding.root

            binding.bodyTextView.text = question.answers[position - 1].body
            binding.nameTextView.text = question.answers[position - 1].name

            return view
        }
    }
}