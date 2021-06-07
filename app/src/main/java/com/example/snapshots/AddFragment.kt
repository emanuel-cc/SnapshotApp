package com.example.snapshots

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.snapshots.databinding.FragmentAddBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class AddFragment : Fragment() {

    private val RC_GALLERY = 18
    private val PATH_SNAPSHOT = "snapshots"

    private lateinit var mBinding:FragmentAddBinding
    private lateinit var mStorageReference:StorageReference
    private lateinit var mDataBaseReference:DatabaseReference

    private var mPhotoSelectedUri:Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mBinding = FragmentAddBinding.inflate(inflater,container, false)

        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mBinding.btnPost.setOnClickListener {
            postSnapshot()
        }
        mBinding.btnSelect.setOnClickListener {
            openGallery()
        }

        // Se realiza la configuración para usar el storage y el database realtime
        mStorageReference = FirebaseStorage.getInstance().reference
        // Lo inicializamos con el path creado
        mDataBaseReference = FirebaseDatabase.getInstance().reference.child(PATH_SNAPSHOT)
    }

    private fun openGallery() {
        // Se lanza la actividad para abrir la galería
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, RC_GALLERY)
    }

    //Proceso de subida en el storage
    private fun postSnapshot() {
        mBinding.progressBar.visibility = View.VISIBLE
        //Se obtiene la key
        val key = mDataBaseReference.push().key!!
        // Se pone la referencia de storage
        //mStorageReference.child(PATH_SNAPSHOT).child("my_photo")
        val storageRef = mStorageReference.child(PATH_SNAPSHOT)
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
            .child(key)
        if(mPhotoSelectedUri != null) {
            storageRef.putFile(mPhotoSelectedUri!!)
                // Cuando está en progreso de subida
                .addOnProgressListener {
                    // Se calcula el porcentaje de bytes transferidos con forme al total
                    val progress = (100 * it.bytesTransferred/it.totalByteCount).toDouble()
                    mBinding.progressBar.progress = progress.toInt()
                    mBinding.txtMessage.text = "$progress%"
                }
                // Cuando esté completado el proceso
                .addOnCompleteListener {
                    mBinding.progressBar.visibility = View.INVISIBLE
                }
                // Cuando el proceso fue exitoso
                .addOnSuccessListener {
                    Snackbar.make(
                        mBinding.root,
                        "Instantánea publicada",
                        Snackbar.LENGTH_SHORT)
                        .show()
                    // Se extrae la url de la imagen cuando sea exitoso
                    it.storage.downloadUrl.addOnSuccessListener {
                        saveSnapshot(key,it.toString(), mBinding.editTitle.text.toString().trim())
                        mBinding.tilTitle.visibility = View.GONE
                        mBinding.txtMessage.text = getString(R.string.post_message_title)
                    }
                }
                // Cuando hubo un error en el proceso de subida
                .addOnFailureListener {
                    Snackbar.make(
                        mBinding.root,
                        "No se pudo subir, intente más tarde",
                        Snackbar.LENGTH_SHORT)
                        .show()
                }
        }
    }

    // Se guarda en realtimedatabase
    private fun saveSnapshot(key:String, url:String, title:String){
        // Se crea una instancia de dataclass
        val snapshot = Snapshot(title = title, photoUrl = url)
        mDataBaseReference.child(key).setValue(snapshot)
    }

    //Recibe la respuesta de la galeria
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK){
            if(requestCode == RC_GALLERY){
                mPhotoSelectedUri = data?.data
                //Le asignamos la imagen seleccionada a nuestra imageview
                mBinding.imgPhoto.setImageURI(mPhotoSelectedUri)
                mBinding.tilTitle.visibility = View.VISIBLE
                //Se agrega un mensaje
                mBinding.txtMessage.text = getString(R.string.post_message_valid_title)
            }
        }
    }
}