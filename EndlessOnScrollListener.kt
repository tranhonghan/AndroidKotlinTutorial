package vision.com.vws.utils

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager

abstract class EndlessOnScrollListener : RecyclerView.OnScrollListener {

    // The minimum amount of items to have below your current scroll position
    // before loading more.
    private var visibleThreshold = 5
    // The current offset index of data you have loaded
    private var currentPage = 0
    // The total number of items in the dataset after the last load
    private var previousTotalItemCount = 0
    // Sets the starting page index
    private val startingPageIndex = 0

    private var mLayoutManager: RecyclerView.LayoutManager

    constructor(layoutManager: LinearLayoutManager) {
        this.mLayoutManager = layoutManager
    }


    constructor(layoutManager: LinearLayoutManager, visibleThreshold: Int) {
        this.mLayoutManager = layoutManager
        this.visibleThreshold = visibleThreshold
    }

    constructor(layoutManager: GridLayoutManager) {
        this.mLayoutManager = layoutManager
        this.visibleThreshold *= layoutManager.spanCount
    }

    constructor(layoutManager: StaggeredGridLayoutManager) {
        this.mLayoutManager = layoutManager
        this.visibleThreshold *= layoutManager.spanCount
    }

    private fun getLastVisibleItem(lastVisibleItemPositions: IntArray): Int {
        var maxSize = 0
        for (i in lastVisibleItemPositions.indices) {
            if (i == 0) {
                maxSize = lastVisibleItemPositions[i]
            } else if (lastVisibleItemPositions[i] > maxSize) {
                maxSize = lastVisibleItemPositions[i]
            }
        }
        return maxSize
    }

    // This happens many times a second during a scroll, so be wary of the code you place here.
    // We are given a few useful parameters to help us work out if we need to load some more data,
    // but first we check if we are waiting for the previous load to finish.
    override fun onScrolled(view: RecyclerView, dx: Int, dy: Int) {
        var lastVisibleItemPosition = 0
        val totalItemCount = mLayoutManager.itemCount

        when (mLayoutManager) {
            is StaggeredGridLayoutManager -> {
                val lastVisibleItemPositions = (mLayoutManager as StaggeredGridLayoutManager).findLastVisibleItemPositions(null)
                // get maximum element within the list
                lastVisibleItemPosition = getLastVisibleItem(lastVisibleItemPositions)
            }
            is LinearLayoutManager -> lastVisibleItemPosition = (mLayoutManager as LinearLayoutManager).findLastVisibleItemPosition()
            is GridLayoutManager -> lastVisibleItemPosition = (mLayoutManager as GridLayoutManager).findLastVisibleItemPosition()
        }

        // If the total item count is zero and the previous isn't, assume the
        // list is invalidated and should be reset back to initial state
        // If it’s still loading, we check to see if the dataset count has
        // changed, if so we conclude it has finished loading and update the current page
        // number and total item count.

        // If it isn’t currently loading, we check to see if we have breached
        // the visibleThreshold and need to reload more data.
        // If we do need to reload some more data, we execute onLoadMore to fetch the data.
        // threshold should reflect how many total columns there are too

        // If the total item count is zero and the previous isn't, assume the
        // list is invalidated and should be reset back to initial state
        if (totalItemCount < previousTotalItemCount) {
            this.currentPage = this.startingPageIndex
            this.previousTotalItemCount = totalItemCount
        }
        // If it’s still loading, we check to see if the dataset count has
        // changed, if so we conclude it has finished loading and update the current page
        // number and total item count.
        if (totalItemCount > previousTotalItemCount) {
            previousTotalItemCount = totalItemCount
        }

        // If it isn’t currently loading, we check to see if we have breached
        // the visibleThreshold and need to reload more data.
        // If we do need to reload some more data, we execute onLoadMore to fetch the data.
        // threshold should reflect how many total columns there are too
        if (lastVisibleItemPosition + visibleThreshold > totalItemCount) {
            currentPage++
            loadMoreItems(currentPage, totalItemCount)
        }
    }
    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        when (newState) {
            RecyclerView.SCROLL_STATE_IDLE -> {}
            RecyclerView.SCROLL_STATE_DRAGGING -> {}
            RecyclerView.SCROLL_STATE_SETTLING -> {
            }
        }

    }


    // Defines the process for actually loading more data based on page
    abstract fun loadMoreItems(page: Int, totalItemsCount: Int)
}
