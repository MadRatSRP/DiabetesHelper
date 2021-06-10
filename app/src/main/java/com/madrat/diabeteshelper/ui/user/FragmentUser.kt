package com.madrat.diabeteshelper.ui.user

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.madrat.diabeteshelper.databinding.FragmentUserBinding
import com.madrat.diabeteshelper.getIsRegisteredFromPreferences
import com.madrat.diabeteshelper.network.NetworkClient
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
        isRegistered?.let {
            /*with(binding) {
                button.setOnClickListener {
                    if (!isRegistered) {
                        registerUser(
                            setupLogin.text.toString(),
                            setupPassword.text.toString()
                        )
                        isRegistered = true
                    } else {
                        authorizeUser(
                            setupLogin.text.toString(),
                            setupPassword.text.toString()
                        )
                    }
                }
            }*/
            with(binding) {
                registerUser(
                    setupLogin.text.toString(),
                    setupPassword.text.toString()
                )
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
                val hashCode: Int? = response.body()
                print(hashCode)
            }
            override fun onFailure(
                call: Call<Int>,
                throwable: Throwable
            ) {
                throwable.printStackTrace()
            }
        })
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
                val userBody: User? = response.body()
                print(userBody)
            }
            override fun onFailure(
                call: Call<User>,
                throwable: Throwable
            ) {
                throwable.printStackTrace()
            }
        })
    }
}