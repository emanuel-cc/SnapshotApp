package com.example.snapshots

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.snapshots.databinding.ActivityMainBinding
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import java.util.*

class MainActivity : AppCompatActivity() {
    private val RC_SIGN_IN = 20
    private lateinit var mBinding:ActivityMainBinding
    private lateinit var mActiveFragment:Fragment
    private lateinit var mFragmentManager:FragmentManager

    private lateinit var mAuthListener : FirebaseAuth.AuthStateListener
    private var mFirebaseAuth:FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(mBinding.root)

        //Se llama para iniciar sesión
        setupAuth()
        // Se llama al método que configura los fragmentos a mostrar en pantalla
        //setupBottomNav()
    }

    private fun setupAuth() {
        mFirebaseAuth = FirebaseAuth.getInstance()
        mAuthListener = FirebaseAuth.AuthStateListener { it ->
            if (it.currentUser == null) {
                startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder()
                    .setIsSmartLockEnabled(false)
                    .setAvailableProviders(
                        listOf(AuthUI.IdpConfig.EmailBuilder().build(),
                            AuthUI.IdpConfig.GoogleBuilder().build())
                    )
                    .build(), SnapshotsApplication.RC_SIGN_IN)
            } else {
                SnapshotsApplication.currentUser = it.currentUser!!

                val fragmentProfile = mFragmentManager?.findFragmentByTag(ProfileFragment::class.java.name)
                fragmentProfile?.let {
                    (it as FragmentAux).refresh()
                }

                if (mFragmentManager == null) {
                    mFragmentManager = supportFragmentManager
                    setupBottomNav(mFragmentManager!!)
                }
            }
        }
        /*mAuthListener = FirebaseAuth.AuthStateListener {
            var user = it.currentUser
            // Se valida si el usuario no se ha logueado
            if(user == null){
                startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder()
                    // Cuando no nos pregunte si queremos elegir las otras cuantas
                    // logueadas
                    //.setIsSmartLockEnabled(false)
                    // Se puede agregar todos los proveedores de inicio de sesión
                    // que necesitemos
                    .setAvailableProviders(
                        listOf(AuthUI.IdpConfig.EmailBuilder().build(),
                            AuthUI.IdpConfig.GoogleBuilder().build())
                    )
                    .build(),
                    SnapshotsApplication.RC_SIGN_IN
                )
            }else{
                SnapshotsApplication.currentUser = it.currentUser!!

                val fragmentProfile = mFragmentManager?.findFragmentByTag(ProfileFragment::class.java.name)
                fragmentProfile?.let {
                    (it as FragmentAux).refresh()
                }

                if (mFragmentManager == null) {
                    mFragmentManager = supportFragmentManager
                    setupBottomNav(mFragmentManager!!)
                }
            }
        }*/
    }

    // Se encarga de la configuración del los fragmentos
    private fun setupBottomNav(fragmentManager: FragmentManager){
        //mFragmentManager = supportFragmentManager

        // Se instancian todos los fragmentos disponibles en el proyecto
        val homeFragment = HomeFragment()
        val addFragment = AddFragment()
        val profileFragment = ProfileFragment()

        // Se configura el fragment inicial que aparecerá en pantalla
        mActiveFragment = homeFragment
        // Se define el host que va a contener el fragment y el fragment
        fragmentManager.beginTransaction()
            .add(R.id.hostFragment,profileFragment, ProfileFragment::class.java.name)
            //Se oculta el fragmento
            .hide(profileFragment)
            .commit()
        fragmentManager.beginTransaction()
            .add(R.id.hostFragment,addFragment, AddFragment::class.java.name)
            .hide(addFragment)
            .commit()
        fragmentManager.beginTransaction()
            .add(R.id.hostFragment,homeFragment, HomeFragment::class.java.name)
            .commit()

        mBinding.bottomNav.setOnNavigationItemSelectedListener {
            when(it.itemId){
                R.id.action_home -> {
                    fragmentManager.beginTransaction()
                        //Se oculta el fragment activo
                        .hide(mActiveFragment)
                        // Se muestra el nuevo fragment en pantalla
                        .show(homeFragment)
                        .commit()
                    // Le asignamos el nuevo fragmento activo
                    mActiveFragment = homeFragment
                    true
                }
                R.id.action_add -> {
                    fragmentManager.beginTransaction()
                        //Se oculta el fragment activo
                        .hide(mActiveFragment)
                        // Se muestra el nuevo fragment en pantalla
                        .show(addFragment)
                        .commit()
                    // Le asignamos el nuevo fragmento activo
                    mActiveFragment = addFragment
                    true
                }
                R.id.action_profile -> {
                    fragmentManager.beginTransaction()
                        //Se oculta el fragment activo
                        .hide(mActiveFragment)
                        // Se muestra el nuevo fragment en pantalla
                        .show(profileFragment)
                        .commit()
                    // Le asignamos el nuevo fragmento activo
                    mActiveFragment = profileFragment
                    true
                }
                else -> false
            }
        }

        // Permite regresar arriba de la lista del registro
        mBinding.bottomNav.setOnNavigationItemReselectedListener {
            when(it.itemId){
                R.id.action_home -> {
                    (homeFragment as HomeAuxInterface).goToTop()
                }
            }
        }
    }


    override fun onResume() {
        super.onResume()
        mFirebaseAuth?.addAuthStateListener {
            mAuthListener
        }
    }

    override fun onPause() {
        super.onPause()
        mFirebaseAuth?.removeAuthStateListener {
            mAuthListener
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Se comprueba si salió bien la autenticación
        if(requestCode == RC_SIGN_IN){
            if(resultCode == RESULT_OK){
                Toast.makeText(this, "Bienvenido...", Toast.LENGTH_SHORT).show()
            }else{
                // Se verifica si el botón es igual al backpressed
                if(IdpResponse.fromResultIntent(data) == null){
                    // Significa que se canceló el firebaseUi
                    // Se finaliza la activity
                    finish()
                }
            }
        }
    }
}