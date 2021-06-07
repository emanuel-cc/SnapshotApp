package com.example.snapshots

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.snapshots.databinding.FragmentHomeBinding
import com.example.snapshots.databinding.ItemSnapshotBinding
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.firebase.ui.database.SnapshotParser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase

class HomeFragment : Fragment(), HomeAuxInterface {

    private lateinit var mBinding: FragmentHomeBinding
    private  lateinit var mFirebaseAdapter:FirebaseRecyclerAdapter<Snapshot, SnapshotHolder>
    private lateinit var mLayoutManager: RecyclerView.LayoutManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mBinding = FragmentHomeBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val query = FirebaseDatabase.getInstance().reference.child("snapshots")
        val options = FirebaseRecyclerOptions.Builder<Snapshot>()
        .setQuery(query, SnapshotParser {
                val snapshot = it.getValue(Snapshot::class.java)
                //Le asignamos el nombre de la rama
                snapshot!!.id = it.key!!
                snapshot
            })
            //.setQuery(query, Snapshot::class.java)
            .build()

        // Se inicializa el adaptador
        mFirebaseAdapter = object : FirebaseRecyclerAdapter<Snapshot, SnapshotHolder>(options){
            private lateinit var mContext:Context

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SnapshotHolder {
                mContext = parent.context

                // Se crea la vista
                val view = LayoutInflater.from(mContext)
                    .inflate(R.layout.item_snapshot, parent, false)
                return SnapshotHolder(view)
            }

            override fun onBindViewHolder(holder: SnapshotHolder, position: Int, model: Snapshot) {
                val snapshot = getItem(position)

                with(holder){
                    setListener(snapshot)
                    binding.txtTitle.text = snapshot.title
                    binding.cbLike.text = snapshot.likeList.keys.size.toString()
                    FirebaseAuth.getInstance().currentUser?.let {
                        binding.cbLike.isChecked = snapshot.likeList
                            .containsKey(it.uid)
                    }
                    Glide.with(mContext)
                        .load(snapshot.photoUrl)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .centerCrop()
                        .into(binding.imgPhoto)
                }
            }

            override fun onDataChanged() {
                super.onDataChanged()
                // Podemos ponerle stop al progressbar
                mBinding.progressBar.visibility = View.GONE
            }

            // Se muestran los posibles errores de visualización
            override fun onError(error: DatabaseError) {
                super.onError(error)
                Toast.makeText(mContext, error.message, Toast.LENGTH_SHORT).show()
            }
        }

        // Se configura el layoutmanager
        mLayoutManager = LinearLayoutManager(context)
        mBinding.recView.apply {
            setHasFixedSize(true)
            layoutManager = mLayoutManager
            adapter = mFirebaseAdapter
        }
    }

    // Se le indica cuando va a empezar a consumir los datos
    override fun onStart() {
        super.onStart()
        mFirebaseAdapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        mFirebaseAdapter.stopListening()
    }

    //HomeAuxInterface
    override fun goToTop() {
        // Permite scrollear arriba de la lista, es decir, al principio de la lista
        mBinding.recView.smoothScrollToPosition(0)
    }

    //Elimina un elemento de la lista
    private fun deleteSnapshot(snapshot: Snapshot){
        val databaseReference = FirebaseDatabase.getInstance().reference.child("snapshots")
        databaseReference.child(snapshot.id).removeValue()
    }

    //Marcar como me gusta a un elemento
    private fun setLike(snapshot: Snapshot, cheked:Boolean){
        val databaseReference = FirebaseDatabase.getInstance().reference.child("snapshots")
        if(cheked){
            databaseReference.child(snapshot.id).child("likeList")
                .child(FirebaseAuth.getInstance().currentUser!!.uid)
                .setValue(cheked)
        }else{
            databaseReference.child(snapshot.id).child("likeList")
                .child(FirebaseAuth.getInstance().currentUser!!.uid)
                .setValue(null)
        }
    }

    // Se crea el adaptador del recyclerview
    inner class SnapshotHolder(view: View):RecyclerView.ViewHolder(view){
        val binding = ItemSnapshotBinding.bind(view)

        fun setListener(snapshot: Snapshot){
            //snapshot.id = 1.toString()
            binding.btnDelete.setOnClickListener {
                deleteSnapshot(snapshot)
            }
            binding.cbLike.setOnCheckedChangeListener { buttonView, isChecked ->
                setLike(snapshot, isChecked)
            }
        }

    }

}