package com.madrat.diabeteshelper.logic.util

import android.content.Context
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.observe
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.google.gson.Gson
import com.madrat.diabeteshelper.R
import com.thoughtworks.xstream.XStream

import java.io.File
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty


// Activity
inline fun <T : ViewBinding> AppCompatActivity.viewBinding(
    crossinline bindingInflater: (LayoutInflater) -> T) =
    lazy(LazyThreadSafetyMode.NONE) {
        bindingInflater.invoke(layoutInflater)
    }

// Fragment
class FragmentViewBindingDelegate<T : ViewBinding>(
    val fragment: Fragment,
    val viewBindingFactory: (View) -> T
) : ReadOnlyProperty<Fragment, T> {
    private var binding: T? = null

    init {
        fragment.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onCreate(owner: LifecycleOwner) {
                fragment.viewLifecycleOwnerLiveData.observe(fragment) { viewLifecycleOwner ->
                    viewLifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
                        override fun onDestroy(owner: LifecycleOwner) {
                            binding = null
                        }
                    })
                }
            }
        })
    }

    override fun getValue(thisRef: Fragment, property: KProperty<*>): T {
        val binding = binding
        if (binding != null) {
            return binding
        }

        val lifecycle = fragment.viewLifecycleOwner.lifecycle
        if (!lifecycle.currentState.isAtLeast(Lifecycle.State.INITIALIZED)) {
            throw IllegalStateException("Should not attempt to get bindings when Fragment views are destroyed.")
        }

        return viewBindingFactory(thisRef.requireView()).also { this.binding = it }
    }
}

fun <T : ViewBinding> Fragment.viewBinding(viewBindingFactory: (View) -> T) =
    FragmentViewBindingDelegate(
        this,
        viewBindingFactory
    )

// ViewGroup
fun ViewGroup.inflate(layoutRes: Int): View
        = LayoutInflater.from(context).inflate(layoutRes, this, false)

// RecyclerView
fun RecyclerView.linearManager() {
    this.layoutManager = LinearLayoutManager(context)
}
fun RecyclerView.gridManager(spanCount: Int) {
    this.layoutManager = GridLayoutManager(context, spanCount)
}

// EditText
inline fun EditText.hideKeyboardAndClearFocus(crossinline function: () -> Unit) {
    this.setOnEditorActionListener { textView, actionId, event ->
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            val inputMethodManager = this.context.getSystemService(
                Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(textView.windowToken, 0)
            this.isFocusable = false
            this.isFocusableInTouchMode = true
            function()
            return@setOnEditorActionListener true
        }
        false
    }
}

fun getPathToFile(fileNameWithExtension: String): String
    = Environment
    .getExternalStorageDirectory()
    .toString() + "/Apps/DiabetesHelper" + fileNameWithExtension

fun createFileWithExtension(fileNameWithExtension: String): File
    = File(getPathToFile(fileNameWithExtension))

fun Fragment.createFileWithExtensionAndWriteContent(
    fileName: String, @StringRes extensionId: Int,
    content: String): File {
    val fileNameWithExtension = context?.getString(
        extensionId, fileName
    )

    val file = createFileWithExtension(fileNameWithExtension!!)

    file.writeText(content)

    println(file.path)

    return file
}

fun Fragment.createCsvFile(content: String): File {
    val fileNameWithExtension = requireContext().getString(
        R.string.pattern_csv, "example"
    )
    val pathToFile = getPathToFile(fileNameWithExtension)

    val file = File(pathToFile)
    file.writeText(content)

    return file
}

// Serializers
// JSON
fun serializeListIntoJSON(srcObject: Any): String
    = Gson().toJson(srcObject)
// XML
fun serializeListIntoXML(srcObject: Any): String
    = XStream().toXML(srcObject)
// CSV
fun serializeListIntoCSV(srcObject: Any): String
    = ""

/*fun getCellProcessors(): Array<CellProcessor> {
    return arrayOf(
        NotNull(),  // author
        NotNull()  // value
    )
}*/

