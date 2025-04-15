package com.Meditation.Sounds.frequencies.lemeor.ui.albums.tabs

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.feature.base.BaseFragment
import com.Meditation.Sounds.frequencies.lemeor.data.api.RetrofitBuilder
import com.Meditation.Sounds.frequencies.lemeor.data.database.DataBase
import com.Meditation.Sounds.frequencies.lemeor.data.model.Album
import com.Meditation.Sounds.frequencies.lemeor.data.remote.ApiHelper
import com.Meditation.Sounds.frequencies.lemeor.data.utils.ViewModelFactory
import com.Meditation.Sounds.frequencies.lemeor.ui.purchase.new_flow.NewPurchaseActivity
import com.Meditation.Sounds.frequencies.lemeor.ui.purchase.new_flow.PurchaseItemAlbumWebView
import com.Meditation.Sounds.frequencies.lemeor.ui.purchase.new_flow.PurchaseScalarWebView
import com.Meditation.Sounds.frequencies.utils.Utils
import com.Meditation.Sounds.frequencies.views.ItemOffsetDecoration
import kotlinx.android.synthetic.main.fragment_albums_category.*


class AlbumsRecyclerFragment : BaseFragment() {

    interface AlbumsRecyclerListener {
        fun onStartAlbumDetail(album: Album)
        fun onStartLongAlbumDetail(album: Album)
    }

    private var mListener: AlbumsRecyclerListener? = null
    private lateinit var mViewModel: AlbumsViewModel
    private var mAlbumAdapter: AlbumsAdapter? = null
    private var mListAlbum = arrayListOf<Album>()

    private var categoryId: Int? = null
    //var  isSoothe = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        categoryId = arguments?.getInt(ARG_SECTION_NUMBER)
    }

    override fun initLayout(): Int = R.layout.fragment_albums_category

    override fun initComponents() {
        mViewModel = ViewModelProvider(
            this,
            ViewModelFactory(
                ApiHelper(RetrofitBuilder(requireContext()).apiService),
                DataBase.getInstance(requireContext())
            )
        )[AlbumsViewModel::class.java]
        if (categoryId != null) {
            mViewModel.albums(categoryId!!)?.observe(viewLifecycleOwner) {
                mListAlbum.clear()
                mListAlbum.addAll(it.sortedBy { it.order_by })
                mAlbumAdapter?.setData(mListAlbum)
            }
        }

        getAlbumData()

        albums_recycler_view.setHasFixedSize(true)
        if (activity?.resources?.configuration?.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            albums_recycler_view.layoutManager = GridLayoutManager(context, 3)
        } else if (activity?.resources?.configuration?.orientation == Configuration.ORIENTATION_PORTRAIT) {
            albums_recycler_view.layoutManager = GridLayoutManager(context, 2)
        }
        val itemDecoration = ItemOffsetDecoration(
            requireContext(),
            if (Utils.isTablet(requireContext())) R.dimen.margin_buttons else R.dimen.item_offset
        )
        albums_recycler_view.addItemDecoration(itemDecoration)
    }

    override fun addListener() {

    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            albums_recycler_view.layoutManager = GridLayoutManager(context, 3)
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            albums_recycler_view.layoutManager = GridLayoutManager(context, 2)
        }
    }

    private fun getAlbumData() {
        mAlbumAdapter = AlbumsAdapter(
            requireContext(), mListAlbum,
            isRegenerate = false,
            isSoothe = false
        )
        albums_recycler_view.adapter = mAlbumAdapter
        mAlbumAdapter!!.setOnClickListener(object : AlbumsAdapter.Listener {
            override fun onClickItem(album: Album) {
                startAlbumDetails(album)
            }

            override fun onLongClickItem(album: Album) {
                startLongAlbumDetails(album)
            }
        })

        /*  if (Utils.isConnectedToNetwork(activity)) {
              getAlbumsShareList()
          } else {
              (activity as BaseActivity).showAlert(getString(R.string.err_network_available))
          }*/
    }

    //higher, inner, advanded quantum -> https://www.qicoil.com/pricing/?q=ascension
    //-> còn lại unlock_url khác rỗng thì mở url đó luôn
    //-> không có unlock_url -> https://qilifestore.com
    fun startAlbumDetails(album: Album) {
//        if (!album.isUnlocked && album.unlock_url != null && album.unlock_url!!.isNotEmpty()) {
//            startActivity(
//                PurchaseItemAlbumWebView.newIntent(requireContext(), album.unlock_url!!)
//            )
//        } else if (album.isUnlocked) {
//            mListener?.onStartAlbumDetail(album)
//        } else {
//            startActivity(
//                NewPurchaseActivity.newIntent(
//                    requireContext(),
//                    album.category_id,
//                    album.tier_id,
//                    album.id
//                )
//            )
//        }

        if (!album.isUnlocked) {
            // 1 quantum, 2 rife, higher 3, inner 4, special 8,  advanded quantum 10
            when (album.tier_id) {
                10, 3, 4 -> {
                    startActivity(
                        PurchaseItemAlbumWebView.newIntent(
                            requireContext(),
                            "https://www.qicoil.com/pricing/?q=ascension"
                        )
                    )
                }

                1, 2 -> {
                    startActivity(
                        PurchaseItemAlbumWebView.newIntent(
                            requireContext(),
                            "https://www.qicoil.com/pricing/?q=wellness"
                        )
                    )
                }

                else -> {
                    if (album.unlock_url != null && album.unlock_url!!.isNotEmpty()) {
                        startActivity(
                            PurchaseItemAlbumWebView.newIntent(requireContext(), album.unlock_url!!)
                        )
                    } else {
                        startActivity(
                            PurchaseItemAlbumWebView.newIntent(
                                requireActivity(),
                                "https://qilifestore.com"
                            )
                        )
                    }
                }
            }
        } else {
            mListener?.onStartAlbumDetail(album)
        }
    }

    fun startLongAlbumDetails(album: Album) {
        mListener?.onStartLongAlbumDetail(album)
    }

    companion object {
        private const val ARG_SECTION_NUMBER = "section_number"

        @JvmStatic
        fun newInstance(
            sectionNumber: Int,
            listener: AlbumsRecyclerListener
        ): AlbumsRecyclerFragment {
            return AlbumsRecyclerFragment().apply {
                mListener = listener
                arguments = Bundle().apply {
                    putInt(ARG_SECTION_NUMBER, sectionNumber)
                }
            }
        }
    }
}
