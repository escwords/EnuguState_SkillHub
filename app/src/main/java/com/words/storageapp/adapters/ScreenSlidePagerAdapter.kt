package com.words.storageapp.adapters
//
//import androidx.core.os.bundleOf
//import androidx.fragment.app.Fragment
//import androidx.viewpager2.adapter.FragmentStateAdapter
//import com.words.storageapp.home.AboutFragment
//import com.words.storageapp.ui.detail.AlbumFragment
//import com.words.storageapp.util.USERID
//
//class ScreenSlidePagerAdapter(fa: Fragment, skillId: String) : FragmentStateAdapter(fa) {
//
//    companion object {
//        const val ALBUM_FRAGMENT_INT = 0
//        const val ABOUT_FRAGMENT_INT = 1
//    }
//
////    private val getTabFragment: Map<Int, () -> Fragment> = mapOf(
////        ALBUM_FRAGMENT_INT to {
////            AlbumFragment().apply {
////                val bundle =
////                    bundleOf(USERID to skillId.trimIndent()) //skillId was trimmed because it was indented which i don't know how
////                arguments = bundle
////            }
////        },
////        ABOUT_FRAGMENT_INT to {
////            AboutFragment().apply {
////                val bundle =
////                    bundleOf(USERID to skillId) //skillId was used here to fetch data from database
////                arguments = bundle
////            }
////        }
////    )
//
//    override fun getItemCount(): Int = getTabFragment.size
//
//    override fun createFragment(position: Int): Fragment {
//        return getTabFragment[position]?.invoke() ?: throw IndexOutOfBoundsException()
//    }
//
//
//}