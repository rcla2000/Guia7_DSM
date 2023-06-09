package com.example.firebase_la181955

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.firebase.auth.FirebaseAuth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity() {
    private lateinit var mGoogleSignInClient : GoogleSignInClient
    val Req_Code : Int = 123
    private lateinit var callbackManager: CallbackManager
    private lateinit var auth : FirebaseAuth
    private lateinit var btnLogin : Button
    private lateinit var btnFacebook : Button
    private lateinit var btnGoogle : Button
    private lateinit var textViewRegister : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        callbackManager = CallbackManager.Factory.create()
        FirebaseApp.initializeApp(this)

        auth = FirebaseAuth.getInstance()
        btnLogin = findViewById<Button>(R.id.btnLogin)
        btnFacebook = findViewById<Button>(R.id.btnFacebook)
        btnGoogle = findViewById<Button>(R.id.btnGoogle)

        btnLogin.setOnClickListener {
            val email = findViewById<EditText>(R.id.editTextEmailAddress).text.toString()
            val password = findViewById<EditText>(R.id.txtPassword).text.toString()
            this.login(email, password)
        }
        textViewRegister = findViewById<TextView>(R.id.textViewRegister)
        textViewRegister.setOnClickListener {
            this.goToRegister()
        }
        val googleSign = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, googleSign)
        btnGoogle.setOnClickListener {
            signInGoogle()
        }

        btnFacebook.setOnClickListener {
            LoginManager.getInstance().logInWithReadPermissions(this, listOf("public_profile", "email"))
        }

        LoginManager.getInstance().registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult) {
                Log.d("TAG", "Success Login")
                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                intent.putExtra("tipoLogin", "facebook")
                startActivity(intent)
                finish()
            }

            override fun onCancel() {
                Toast.makeText(this@LoginActivity, "Login cancelado", Toast.LENGTH_LONG).show()
            }

            override fun onError(error: FacebookException) {
                Toast.makeText(this@LoginActivity, error.message, Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun signInGoogle() {
        val signInIntent : Intent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, Req_Code)
    }

    // Datos de la cuenta de Google
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == Req_Code) {
            val task : Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleResult(task)
        }
        callbackManager.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun handleResult(completedTask : Task<GoogleSignInAccount>) {
        try {
            val account : GoogleSignInAccount? = completedTask.getResult(ApiException::class.java)
            if (account != null) {
                loginGoogle(account)
            }
        } catch (e: ApiException) {
            Toast.makeText(this, "Ha ocurrido un error", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loginGoogle(account : GoogleSignInAccount) {
        val credencial = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credencial).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("tipoLogin", "google")
                startActivity(intent)
                finish()
            }
        }
    }
    private fun login(email : String, password: String) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }.addOnFailureListener { exception ->
            Toast.makeText(applicationContext, exception.localizedMessage, Toast.LENGTH_SHORT).show()
        }
    }

    private fun goToRegister() {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
    }
}