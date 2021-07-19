package ru.randgor.dromtz.data


import com.google.gson.Gson
import org.json.JSONException
import org.json.JSONObject
import ru.randgor.dromtz.helpers.RepoListAdapter


class ElementRepoList {
    var type = 0
    var repository: Repository? = null

    constructor() : this(RepoListAdapter.VIEW_TYPE_LOADING) {}
    constructor(type: Int) {
        this.type = type
    }

    constructor(json: JSONObject) {
        type = RepoListAdapter.VIEW_TYPE_NORMAL
        try {
            val gson = Gson()
            repository = gson.fromJson(json.toString(), Repository::class.java)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }
}