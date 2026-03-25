package com.apiscall.skeletoncode.workpermitmodule.presentation.approvals

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class ApprovalsPagerAdapter(
    fragment: Fragment,
    private val fragments: List<Fragment>,
    private val titles: List<String>
) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = fragments.size

    override fun createFragment(position: Int): Fragment = fragments[position]

    fun getPageTitle(position: Int): String = titles[position]
}