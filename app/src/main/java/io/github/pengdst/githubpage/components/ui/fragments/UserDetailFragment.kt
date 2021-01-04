package io.github.pengdst.githubpage.components.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import io.github.pengdst.githubpage.R
import io.github.pengdst.githubpage.components.adapters.pagers.UserPagerAdapter
import io.github.pengdst.githubpage.components.ui.base.BindingFragment
import io.github.pengdst.githubpage.components.viewmodels.UserViewModel
import io.github.pengdst.githubpage.databinding.FragmentHomeBinding
import io.github.pengdst.githubpage.databinding.FragmentUserDetailBinding
import io.github.pengdst.githubpage.datas.domain.models.UserDetail
import io.github.pengdst.githubpage.util.extensions.asFormattedDecimals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class UserDetailFragment : BindingFragment<FragmentUserDetailBinding>() {

    private val args: UserDetailFragmentArgs by navArgs()
    private val userDetail: UserDetail get() = args.user

    private val userViewModel: UserViewModel by viewModels()

    @Inject lateinit var userPagerAdapter: UserPagerAdapter
    @Inject lateinit var glide: RequestManager

    override fun getViewBinding(inflater: LayoutInflater) =
        FragmentUserDetailBinding.inflate(inflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupTabLayoutAndViewPager()
        updateUi(userDetail)
        getDetailUser()

    }

    private fun setupTabLayoutAndViewPager() {
        userPagerAdapter.apply {
            addFragment(
                Pair(
                    getString(R.string.text_followers),
                    FollowersFragment.newInstance(userDetail.username.toString())
                )
            )
            addFragment(
                Pair(
                    getString(R.string.text_followings),
                    FollowingsFragment.newInstance(userDetail.username.toString())
                )
            )
        }
        binding.viewpaggerFollowers.adapter = userPagerAdapter
        binding.tabLayoutFollowers.setupWithViewPager(binding.viewpaggerFollowers)
    }

    private fun getDetailUser() {

        lifecycleScope.launchWhenCreated {

            try {
                val users = userViewModel.getUserDetail(userDetail.username.toString())

                withContext(Dispatchers.Main) {
                    users.body()?.let { updateUi(it) }
                }

            } catch (e: Exception) {
                Timber.e("Error ${e.stackTraceToString()}")
            }

        }
    }

    private fun updateUi(userDetail: UserDetail) {
        with(binding) {
            requireAppCompatActivity().supportActionBar?.title = userDetail.name
            textViewFullname.text = userDetail.name
            textViewUsername.text = userDetail.username

            textViewFollowers.text = userDetail.followers?.asFormattedDecimals()
            textViewFollowings.text = userDetail.following?.asFormattedDecimals()
            textViewRepositories.text = userDetail.publicRepos?.asFormattedDecimals()

            glide.load(userDetail.avatarUrl)
                .into(imageProfile)
        }
    }
}