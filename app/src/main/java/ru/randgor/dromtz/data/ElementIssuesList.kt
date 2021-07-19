package ru.randgor.dromtz.data


import com.google.gson.Gson
import org.json.JSONException
import org.json.JSONObject
import ru.randgor.dromtz.helpers.IssuesListAdapter


class ElementIssuesList {
    var type = 0
    var repository: Repository? = null
    var issue: Issue? = null

    constructor() : this(IssuesListAdapter.VIEW_TYPE_LOADING) {}
    constructor(type: Int) {
        this.type = type
    }
    constructor(repository: Repository) {
        this.type = IssuesListAdapter.VIEW_TYPE_HEAD
        this.repository = repository
    }

    constructor(json: JSONObject, type: Int) {
        this.type = type
        try {
            val gson = Gson()
            this.issue = gson.fromJson(json.toString(), Issue::class.java)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }
}