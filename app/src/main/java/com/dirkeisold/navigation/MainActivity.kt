package com.dirkeisold.navigation

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.dirkeisold.navigation.common.OnReselectedDelegate
import com.dirkeisold.navigation.common.or
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {

    class FragmentInfo(val menuId: Int,
                       private val getFrame: Function1<Unit?, FrameLayout>,
                       private val getController: Function1<Unit?, NavController>,
                       private val getFragment: Function1<Unit?, Fragment>) {
        val fragment: Fragment by lazy { getFragment(null) }
        val controller: NavController by lazy { getController(null) }
        val wrapper: FrameLayout by lazy { getFrame(null) }
    }

    private val infoHome = FragmentInfo(
            R.id.navigation_home,
            { section_home_wrapper },
            { findNavController(R.id.section_home) },
            { section_home })

    private val infoDashboard = FragmentInfo(
            R.id.navigation_dashboard,
            { section_dashboard_wrapper },
            { findNavController(R.id.section_dashboard) },
            { section_dashboard })

    private val infoNotifications = FragmentInfo(
            R.id.navigation_notifications,
            { section_notification_wrapper },
            { findNavController(R.id.section_notification) },
            { section_notification })

    private val childFragments: Array<FragmentInfo>
        get() = arrayOf(infoHome,
                infoDashboard,
                infoNotifications)

    var currentController: NavController? = null

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        currentController = childFragments.first{it.menuId == item.itemId}.controller
        childFragments.forEach {
            it.wrapper.visibility = if(it.menuId == item.itemId) View.VISIBLE else View.INVISIBLE
        }
        onReselected(item.itemId)
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        currentController = childFragments[0].controller
        navigation.setOnNavigationItemSelectedListener(this)

        childFragments.forEach {
            it.wrapper.visibility = if(it.controller === currentController) View.VISIBLE else View.INVISIBLE
        }
    }

    override fun supportNavigateUpTo(upIntent: Intent) {
        currentController?.navigateUp()
    }

    override fun onBackPressed() {
        currentController
                ?.let { if (it.popBackStack().not()) finish() }
                .or { finish() }
    }

    private fun onReselected(itemId: Int) {
        childFragments.firstOrNull {it.menuId == itemId }?.let { fragmentInfo ->
            (fragmentInfo.fragment.childFragmentManager.fragments.first { it.isVisible } as? OnReselectedDelegate)?.onReselected()
        }
    }
}
