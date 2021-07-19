package ru.randgor.dromtz.activities

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import org.json.JSONException
import ru.randgor.dromtz.helpers.RepoListAdapter
import ru.randgor.dromtz.helpers.PaginationListener
import ru.randgor.dromtz.R
import ru.randgor.dromtz.data.ElementRepoList
import ru.randgor.dromtz.databinding.ActivityMainBinding
import java.util.*
import kotlin.math.ceil


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val gson = Gson()

    private var recyclerAdapter: RepoListAdapter? = null
    private var itemClickListener: RepoListAdapter.OnItemClickListener? = null
    private var paginationListener: PaginationListener? = null
    private var data: ArrayList<ElementRepoList>? = null

    private var mRequestQueue: RequestQueue? = null

    private var currentQuery: String = PaginationListener.PAGE_DEFAULT
    private var currentPage: Int = PaginationListener.PAGE_START
    private var isNowLastPage = false
    private var totalPage: Int = PaginationListener.PAGE_SIZE
    private var isNowLoading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        mRequestQueue = Volley.newRequestQueue(applicationContext)

        val recyclerLayoutManager = LinearLayoutManager(applicationContext)

        itemClickListener = RepoListAdapter.OnItemClickListener { position ->
            val item = recyclerAdapter!!.getItem(position)
            when (item.type) {
                RepoListAdapter.VIEW_TYPE_LOADING -> {
                }
                RepoListAdapter.VIEW_TYPE_NORMAL -> {
                    val intent = Intent(this, RepoActivity::class.java)
                    intent.putExtra(RepoListAdapter.ENTITY_NAME, gson.toJson(item.repository))
                    startActivity(intent)
                }
                RepoListAdapter.VIEW_TYPE_RETRY -> {
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

        data = ArrayList<ElementRepoList>()

        recyclerAdapter = RepoListAdapter(
            data!!,
            itemClickListener!!,
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

        Toast.makeText(applicationContext, "ТЗ для Drom. Автор @RandGor", Toast.LENGTH_LONG).show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        val manager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView
        val searchListener = object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                mRequestQueue!!.cancelAll { it.tag != currentQuery }
                data?.clear()

                recyclerAdapter?.notifyDataSetChanged()
                recyclerAdapter?.changeRetryToLoading()
                recyclerAdapter?.isLoaderVisible = false

                currentQuery = if(newText.isNullOrEmpty()) PaginationListener.PAGE_DEFAULT else newText
                currentPage = PaginationListener.PAGE_START
                isNowLastPage = false
                totalPage = PaginationListener.PAGE_SIZE
                isNowLoading = false

                doApiCall()

                return true
            }
        }

        searchView.apply {
            setSearchableInfo(manager.getSearchableInfo(componentName))
            setOnQueryTextListener(searchListener)
            maxWidth = Int.MAX_VALUE
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_search -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun doApiCall() {
        if (currentPage == PaginationListener.PAGE_START)
            recyclerAdapter!!.addLoading()

        val currentTag = currentQuery

        mRequestQueue!!.cancelAll { it.tag != currentTag }

        val url = "https://api.github.com/search/repositories?per_page=${PaginationListener.PAGE_SIZE}&q=$currentQuery in:name&page=$currentPage"
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
                        val row = ElementRepoList(article)
                        data!!.add(row)
                        recyclerAdapter!!.notifyItemInserted((currentPage - 1) * PaginationListener.PAGE_SIZE + i)
                    }
                    totalPage = ceil(1.0 * response.getInt("total_count") / PaginationListener.PAGE_SIZE).toInt()

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
}