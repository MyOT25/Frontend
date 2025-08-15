package com.example.myot.profile

import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myot.feed.adapter.FeedAdapter
import com.example.myot.feed.model.FeedItem
import com.example.myot.profile.data.ProfileFeedRepository
import com.example.myot.retrofit2.RetrofitClient
import kotlinx.coroutines.launch

class ProfileTabFragment : Fragment() {

    companion object {
        private const val ARG_TAB = "arg_tab"
        private const val ARG_USER_ID = "arg_user_id"

        fun newInstance(tab: ProfileFeedTab, userId: Long) = ProfileTabFragment().apply {
            arguments = bundleOf(ARG_TAB to tab.name, ARG_USER_ID to userId)
        }
    }

    private val tab: ProfileFeedTab by lazy { ProfileFeedTab.valueOf(requireArguments().getString(ARG_TAB)!!) }
    private val userId: Long by lazy { requireArguments().getLong(ARG_USER_ID) }

    private lateinit var recyclerView: RecyclerView
    private lateinit var progress: ProgressBar
    private lateinit var emptyView: TextView
    private val repo by lazy { ProfileFeedRepository(RetrofitClient.profileFeedService) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val context = requireContext()

        val topSpacer = View(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                (34 * context.resources.displayMetrics.density).toInt()
            )
            visibility = View.INVISIBLE
        }

        progress = ProgressBar(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).also { it.topMargin = (8 * context.resources.displayMetrics.density).toInt() }
        }

        emptyView = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            text = "게시글이 없습니다."
            visibility = View.GONE
            gravity = android.view.Gravity.CENTER_HORIZONTAL
            setPadding(0, (12 * context.resources.displayMetrics.density).toInt(), 0, 0)
        }

        recyclerView = RecyclerView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            layoutManager = LinearLayoutManager(context)
            adapter = FeedAdapter(emptyList())
        }

        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            addView(topSpacer)
            addView(progress)
            addView(emptyView)
            addView(recyclerView)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progress.visibility = View.VISIBLE
        emptyView.visibility = View.GONE

        lifecycleScope.launch {
            val items: List<FeedItem> = try {
                val token: String? = null
                when (tab) {
                    ProfileFeedTab.ALL    -> repo.getAll(userId, page = 1, pageSize = 20, auth = token)
                    ProfileFeedTab.REPOST -> repo.getReposts(userId, page = 1, pageSize = 20, auth = token)
                    ProfileFeedTab.QUOTE  -> repo.getQuotes(userId, page = 1, pageSize = 20, auth = token)
                    ProfileFeedTab.MEDIA  -> repo.getMedia(userId, page = 1, pageSize = 20, auth = token)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }

            progress.visibility = View.GONE
            if (items.isEmpty()) {
                emptyView.visibility = View.VISIBLE
            } else {
                recyclerView.adapter = FeedAdapter(items)
                emptyView.visibility = View.GONE
            }
        }
    }
}