package com.example.apicliente1
import android.util.Log
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.apicliente1.ui.theme.ApiCliente1Theme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import org.json.JSONArray

class MainActivity : ComponentActivity() {
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        setContent {
            ApiCliente1Theme {
                Surface(
                    modifier = androidx.compose.ui.Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Observe changes in state and display the result
                    val state = viewModel.state
                    DisplayJsonResult(state.jsonResult)
                }
            }
        }

        // Use LaunchedEffect to perform the network request
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val jsonString = fetchDataFromEndpoint("http://192.168.254.48:4000")
                viewModel.updateState(jsonString)
            } catch (e: Exception) {
                // Handle error
                e.printStackTrace()
            }
        }
    }
}

// ...

@Composable
fun DisplayJsonResult(jsonResult: String) {
    var displayText = "Unable to parse JSON data"

    if (jsonResult.isNotEmpty()) {
        try {
            // Attempt to parse JSON array
            val jsonArray = JSONArray(jsonResult)
            val resultsList = mutableListOf<String>()

            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val codigo = jsonObject.getInt("codigo")
                val nome = jsonObject.getString("nome")

                resultsList.add("Código: $codigo\nNome: $nome")
            }

            if (resultsList.isNotEmpty()) {
                displayText = resultsList.joinToString("\n\n")
            }
        } catch (e: JSONException) {
            // Handle JSON parsing error
            e.printStackTrace()
        }
    } else {
        println("entrou no else")
    }

    Text(
        text = displayText,
        modifier = Modifier.fillMaxSize()
    )
}

// ...



class MainViewModel : ViewModel() {
    private val _state = mutableStateOf(AppState())
    val state get() = _state.value

    fun updateState(jsonResult: String) {
        _state.value = AppState(jsonResult)
    }
}

data class AppState(val jsonResult: String = "")

// Function to fetch JSON data from the specified endpoint
fun fetchDataFromEndpoint(endpointUrl: String): String {
    val url = URL(endpointUrl)
    val connection = url.openConnection() as HttpURLConnection

    return try {
        val reader = BufferedReader(InputStreamReader(connection.inputStream))
        val result = StringBuilder()
        var line: String?

        while (reader.readLine().also { line = it } != null) {
            result.append(line)
        }

        result.toString()
    } finally {
        connection.disconnect()
    }
}
