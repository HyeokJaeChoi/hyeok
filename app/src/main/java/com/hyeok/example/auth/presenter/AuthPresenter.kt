package com.hyeok.example.auth.presenter

import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.*
import com.hyeok.example.R
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers


class AuthPresenter : AuthContract.Presenter {
    lateinit var mAuth : FirebaseAuth
    lateinit override var authView : AuthContract.View
    lateinit var authViewActivity : AppCompatActivity
    lateinit var mCallbackManager : CallbackManager
    lateinit var mGoogleSignInClient: GoogleSignInClient
    private val compositeDisposable : CompositeDisposable = CompositeDisposable()

    override fun start() {

    }

    fun facebookAuthTask(mCallbackManager: CallbackManager){
        LoginManager.getInstance().registerCallback(mCallbackManager, object : FacebookCallback<LoginResult>{
            override fun onSuccess(result: LoginResult?) {
                Log.d("accesstoken", result!!.accessToken.token)
                this@AuthPresenter.setCredential(result.accessToken)
            }

            override fun onCancel() {

            }

            override fun onError(error: FacebookException?) {

            }
        })
    }

    fun googleAuthTask(){
        authViewActivity.startActivityForResult(mGoogleSignInClient.signInIntent, 9001)
    }

    override fun executeAuthTask(v : View) {
        when (v.id) {
            R.id.fb_login_button -> {
                Log.d("buttonID", v.id.toString() + " " + R.id.fb_login_button.toString())
                facebookAuthTask(mCallbackManager)
            }
            R.id.google_login_button -> {
                Log.d("buttonID", v.id.toString() + " " + R.id.google_login_button.toString())
                googleAuthTask()
            }
            R.id.rxtest -> {
                val rxtestObservable : Observable<String> = Observable.just("test", "rx", "button")
                rxtestObservable
                        .subscribeOn(Schedulers.computation())
                        .flatMap({it -> Observable.just(it + " " + Thread.currentThread().name)})
                        .concatMap({it -> Observable.just(it + " " + Thread.currentThread().name)})
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({Log.d("currentThread2", Thread.currentThread().name + it)})
            }
        }
    }

    override fun <T>setCredential(authToken : T) {
        lateinit var credential: AuthCredential
             if(authToken is AccessToken){
                credential = FacebookAuthProvider.getCredential((authToken as AccessToken).token)
                this.mAuth.signInWithCredential(credential)
                        .addOnCompleteListener(authViewActivity) { it: Task<AuthResult> ->
                            if(it.isSuccessful) {
                                Log.d("FirebaseFacebookAuth", "FirebaseAuth Credential completed!" + " " + Thread.currentThread().name)
                                //authView.startMain(Intent(authView as AppCompatActivity, MainActivity::class.java))
                            }
                            else{
                                Log.e("FirebaseFacebookAuth", it.exception?.message)
                            }
                        }
            }
             else if(authToken is GoogleSignInAccount){
                credential = GoogleAuthProvider.getCredential((authToken as GoogleSignInAccount).idToken, null)
                this.mAuth.signInWithCredential(credential)
                        .addOnCompleteListener(authViewActivity){ it : Task<AuthResult> ->
                            if(it.isSuccessful){
                                Log.d("FirebaseGoogleAuth", "FirebaseGoogleAuth Credential completed!")
                            }
                            else{
                                Log.d("FirebaseGoogleAuth", it.exception?.message)
                            }
                        }
            }
    }

    override fun disposeAll() {
        if(!compositeDisposable.isDisposed)
            compositeDisposable.dispose()
    }
}