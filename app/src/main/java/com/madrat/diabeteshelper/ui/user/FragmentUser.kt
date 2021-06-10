package com.madrat.diabeteshelper.ui.user

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.madrat.diabeteshelper.*
import com.madrat.diabeteshelper.databinding.FragmentUserBinding
import com.madrat.diabeteshelper.network.NetworkClient
import com.madrat.diabeteshelper.ui.mainactivity.MainActivity
import com.madrat.diabeteshelper.ui.user.model.RequestAuthorizeUser
import com.madrat.diabeteshelper.ui.user.model.RequestRegisterUser
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FragmentUser: Fragment() {
    private var nullableBinding: FragmentUserBinding? = null
    private val binding get() = nullableBinding!!
    private var networkService: UserNetworkInterface? = null
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        nullableBinding = FragmentUserBinding.inflate(
            inflater,
            container,
            false
        )
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeDependencies(view.context)
        val isRegistered = getIsRegisteredFromPreferences()
        val userHashcode = getHashcodeFromPreferences()
        isRegistered?.let {
            if (!isRegistered) {
                with(binding) {
                    registrationLayout.visibility = View.VISIBLE
                    registerUser(
                        setupRegistrationLogin.text.toString(),
                        setupRegistrationPassword.text.toString()
                    )
                }
            } else {
                if (userHashcode == null) {
                    updateAuthorizationLayout()
                } else {
                
                }
            }
        }
    }
    fun initializeDependencies(context: Context) {
        networkService = NetworkClient
                .getRetrofit(context)
                .create(UserNetworkInterface::class.java)
    }
    private fun registerUser(
        emailOrUserPassword: String,
        password: String
    ) {
        val response = context?.let {
            networkService?.registerUser(
                RequestRegisterUser(
                    emailOrUserPassword,
                    password
                )
            )
        }
        response?.enqueue(object : Callback<Int> {
            override fun onResponse(
                call: Call<Int>,
                response: Response<Int>
            ) {
                if (response.isSuccessful) {
                    doOnUserRegisteredSuccess()
                } else {
                    doOnUserRegisteredError()
                }
            }
            override fun onFailure(
                call: Call<Int>,
                throwable: Throwable
            ) {
                throwable.printStackTrace()
            }
        })
    }
    fun doOnUserRegisteredSuccess() {
        showStatusMessage(R.string.message_successful_user_registered)
        
        binding.registrationLayout.visibility = View.GONE
        
        updateAuthorizationLayout()
        
        saveIsRegisteredToPreferences(true)
    }
    fun doOnUserRegisteredError() {
        showStatusMessage(
            R.string.message_error_user_registered,
            false
        )
    }
    
    private fun updateAuthorizationLayout() {
        with(binding) {
            authorizationLayout.visibility = View.VISIBLE
    
            buttonAuthorizeUser.setOnClickListener {
                authorizeUser(
                    setupAuthorizationLogin.text.toString(),
                    setupAuthorizationPassword.text.toString()
                )
            }
        }
    }
    
    private fun authorizeUser(
        emailOrUserPassword: String,
        password: String
    ) {
        val response = context?.let {
            networkService?.authorizeUser(
                RequestAuthorizeUser(
                    emailOrUserPassword,
                    password
                )
            )
        }
        response?.enqueue(object : Callback<User> {
            override fun onResponse(
                call: Call<User>,
                response: Response<User>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        doOnUserAuthorizationSuccess(
                            it
                        )
                    }
                } else {
                    doOnUserAuthorizationError()
                }
            }
            override fun onFailure(
                call: Call<User>,
                throwable: Throwable
            ) {
                throwable.printStackTrace()
            }
        })
    }
    
    fun doOnUserAuthorizationSuccess(user: User) {
        showStatusMessage(R.string.message_successful_user_authorized)
    
        binding.authorizationLayout.visibility = View.GONE
    
        updateUserAuthorizedLayout()
    
        saveHashcodeToPreferences(user.hashCode().toString())
    }
    
    private fun updateUserAuthorizedLayout() {
        with(binding) {
            userAuthorizedLayout.visibility = View.VISIBLE
            
            buttonExitAccount.setOnClickListener {
                leaveAccount()
            }
        }
    }
    
    fun leaveAccount() {
        binding.userAuthorizedLayout.visibility = View.GONE
        
        saveHashcodeToPreferences(null)
        
        updateAuthorizationLayout()
    }
    
    fun doOnUserAuthorizationError() {
        showStatusMessage(
            R.string.message_error_user_authorized,
            false
        )
    }
    
    private fun showStatusMessage(@StringRes messageRes: Int, isSuccess: Boolean = true) {
        (activity as? MainActivity)?.showMessage(
            messageRes, isSuccess
        )
    }
}