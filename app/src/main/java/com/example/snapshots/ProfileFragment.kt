package com.example.snapshots

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.snapshots.databinding.FragmentProfileBinding
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth

class ProfileFragment : Fragment() {
    private lateinit var mBinding:FragmentProfileBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mBinding = FragmentProfileBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding.txtName.text = FirebaseAuth.getInstance().currentUser?.displayName
        mBinding.txtEmail.text = FirebaseAuth.getInstance().currentUser?.email

        mBinding.btnLogout.setOnClickListener {
            signOut()
        }
    }

    private fun signOut() {
        //Ejecuta un bloque de código cuando no es nulo
        // si el objeto no es nulo, dejalo ejecutar este código
        context?.let {
            AuthUI.getInstance().signOut(it)
                // Mostrarle al usuario cuando ya cerró sesión
                .addOnCanceledListener {
                    Toast.makeText(context, "Hasta pronto...", Toast.LENGTH_SHORT).show()
                }
        }
    }
}