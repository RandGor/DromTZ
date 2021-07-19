package ru.randgor.dromtz.activities

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import org.json.JSONException
import ru.randgor.dromtz.data.ElementIssuesList
import ru.randgor.dromtz.data.Repository
import ru.randgor.dromtz.databinding.ActivityRepoBinding
import ru.randgor.dromtz.helpers.IssuesListAdapter
import ru.randgor.dromtz.helpers.PaginationListener
import ru.randgor.dromtz.helpers.RepoListAdapter
import java.util.*


class RepoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRepoBinding
    private val gson = Gson()

    private var mRequestQueue: RequestQueue? = null

    private var recyclerAdapter: IssuesListAdapter? = null
    private var itemClickListener: IssuesListAdapter.OnItemClickListener? = null
    private var paginationListener: PaginationListener? = null
    private var data: ArrayList<ElementIssuesList>? = null

    private var currentQuery: String? = null
    private var currentPage: Int = PaginationListener.PAGE_START
    private var isNowLastPage = false
    private var totalPage: Int = PaginationListener.PAGE_START
    private var isNowLoading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val repo = gson.fromJson(intent.getStringExtra(RepoListAdapter.ENTITY_NAME), Repository::class.java)

        currentQuery = repo.fullName

        title = currentQuery

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding = ActivityRepoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mRequestQueue = Volley.newRequestQueue(applicationContext)

        val recyclerLayoutManager = LinearLayoutManager(applicationContext)

        itemClickListener = IssuesListAdapter.OnItemClickListener { position ->
            val item = recyclerAdapter!!.getItem(position)
            when (item.type) {
                IssuesListAdapter.VIEW_TYPE_LOADING -> {
                }
                IssuesListAdapter.VIEW_TYPE_HEAD -> {
                }
                IssuesListAdapter.VIEW_TYPE_NORMAL -> {
                }
                IssuesListAdapter.VIEW_TYPE_RETRY -> {
                    onRetry()
                }
            }
        }

        paginationListener = object : PaginationListener(recyclerLayoutManager) {
            override fun loadMoreItems() {
                if (isNowLoading || isNowLastPage)
                    return

                isNowLoading = true
                currentPage++
                doApiCall()
            }

            override fun isLastPage(): Boolean {
                return isNowLastPage
            }

            override fun isLoading(): Boolean {
                return isNowLoading
            }
        }

        data = ArrayList<ElementIssuesList>()

        data?.add(ElementIssuesList(repo))

        recyclerAdapter = IssuesListAdapter(
            data,
            itemClickListener,
            applicationContext
        )

        binding.repositoryList.apply {
            adapter = recyclerAdapter
            addOnScrollListener(paginationListener!!)

            setHasFixedSize(true)

            layoutManager = recyclerLayoutManager

            setItemViewCacheSize(20)
        }

        doApiCall()
    }

    private fun doApiCall() {
        if (currentPage == PaginationListener.PAGE_START)
            recyclerAdapter!!.addLoading()

        val currentTag = currentQuery

        mRequestQueue!!.cancelAll { it.tag != currentTag }

        val url = "https://api.github.com/search/issues?per_page=${PaginationListener.PAGE_SIZE}&q=type:issue repo:$currentTag"
        val request = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                if (currentTag != currentQuery)
                    return@JsonObjectRequest

                try {
                    val jsonArray = response.getJSONArray("items")

                    if (jsonArray.length() == 0)
                        recyclerAdapter!!.changeLoadingToRetry(0)

                    recyclerAdapter!!.removeLoading()

                    for (i in 0 until jsonArray.length()) {
                        val article = jsonArray.getJSONObject(i)
                        val row = ElementIssuesList(article, IssuesListAdapter.VIEW_TYPE_NORMAL)
                        data!!.add(row)
                        recyclerAdapter!!.notifyItemInserted(data!!.size)
                    }

                    if (currentPage < totalPage)
                        recyclerAdapter!!.addLoading()
                    else
                        isNowLastPage = true

                    isNowLoading = false
                } catch (e: JSONException) {
                    e.printStackTrace()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, { error ->
                if (currentTag != currentQuery)
                    return@JsonObjectRequest

                val status = if (error.networkResponse==null) -1 else error.networkResponse.statusCode

                recyclerAdapter!!.changeLoadingToRetry(status)
                error.printStackTrace()
            })
        request.tag = currentTag
        mRequestQueue?.add(request)
    }

    private fun onRetry() {
        mRequestQueue!!.cancelAll { it.tag != currentQuery }
        isNowLoading = false
        isNowLastPage = false
        recyclerAdapter?.changeRetryToLoading()
        doApiCall()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return true
    }
}