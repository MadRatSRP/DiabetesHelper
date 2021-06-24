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
import com.madrat.diabeteshelper.ui.user.model.RequestUnauthorizeUser
import okhttp3.ResponseBody
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
                updateRegistrationLayout()
            } else {
                if (userHashcode == null) {
                    updateAuthorizationLayout()
                } else {
                    updateUserAuthorizedLayout()
                }
            }
        }
    }
    
    fun initializeDependencies(context: Context) {
        networkService = NetworkClient
                .getRetrofit(context)
                .create(UserNetworkInterface::class.java)
    }
    private fun updateRegistrationLayout() {
        with(binding) {
            registrationLayout.visibility = View.VISIBLE
            
            buttonRegisterUser.setOnClickListener {
                registerUser(
                    setupRegistrationLogin.text.toString(),
                    setupRegistrationPassword.text.toString()
                )
            }
            
            buttonChangeToAuthorization.setOnClickListener {
                binding.registrationLayout.visibility = View.GONE
    
                updateAuthorizationLayout()
            }
        }
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
        response?.enqueue(object : Callback<String> {
            override fun onResponse(
                call: Call<String>,
                response: Response<String>
            ) {
                if (response.isSuccessful) {
                    doOnUserRegisteredSuccess()
                } else {
                    doOnUserRegisteredError()
                }
            }
            override fun onFailure(
                call: Call<String>,
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
    
            if (setupAuthorizationLogin.text != null) {
                setupAuthorizationLogin.setText("")
                setupAuthorizationPassword.setText("")
            }
    
            buttonAuthorizeUser.setOnClickListener {
                authorizeUser(
                    setupAuthorizationLogin.text.toString(),
                    setupAuthorizationPassword.text.toString()
                )
            }
            buttonChangeToRegistration.setOnClickListener {
                binding.authorizationLayout.visibility = View.GONE
                
                updateRegistrationLayout()
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
        response?.enqueue(object : Callback<String> {
            override fun onResponse(
                call: Call<String>,
                response: Response<String>
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
                call: Call<String>,
                throwable: Throwable
            ) {
                throwable.printStackTrace()
            }
        })
    }
    
    fun doOnUserAuthorizationSuccess(userHashcode: String) {
        showStatusMessage(R.string.message_successful_user_authorized)
    
        binding.authorizationLayout.visibility = View.GONE
    
        updateUserAuthorizedLayout()
    
        saveHashcodeToPreferences(userHashcode)
    }
    
    private fun unathorizeUser(
        userHashcode: String
    ) {
        val response = context?.let {
            networkService?.unauthorizeUser(
                RequestUnauthorizeUser(
                    userHashcode
                )
            )
        }
        response?.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>,
                                    response: Response<ResponseBody>) {
                leaveAccount()
            }
    
            override fun onFailure(call: Call<ResponseBody>,
                                   throwable: Throwable) {
                throwable.printStackTrace()
            }
        })
    }
    
    private fun updateUserAuthorizedLayout() {
        with(binding) {
            userAuthorizedLayout.visibility = View.VISIBLE
            
            buttonExitAccount.setOnClickListener {
                getHashcodeFromPreferences()?.let { it1 ->
                    unathorizeUser(
                        it1
                    )
                }
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