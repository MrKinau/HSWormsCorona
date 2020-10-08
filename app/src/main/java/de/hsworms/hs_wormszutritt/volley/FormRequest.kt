package de.hsworms.hs_wormszutritt.volley

import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest


class FormRequest(method: Int,
                  url: String?,
                  listener: Response.Listener<String>?,
                  errorListener: Response.ErrorListener?,
                  private val room : String,
                  private val matrikel: String,
                  private val checkIn: Boolean) : StringRequest(method, url, listener, errorListener) {

    override fun getBodyContentType(): String? {
        return "application/x-www-form-urlencoded; charset=UTF-8"
    }

    @Throws(AuthFailureError::class)
    public override fun getParams(): Map<String, String>? {
        val params: MutableMap<String, String> = mutableMapOf()
        params["Raum"] = room.trim()
        params["Matrikel"] = matrikel.trim()
        params["Ipadr"] = "nope"
        if (checkIn) {
            params["Vorgang"] = "in"
        } else {
            params["Absenden"] = "out"
        }
        return params
    }

}