package hu.bme.aut.android.befitt.ui.statistics

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import android.widget.PopupMenu
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.ViewModelProvider
import hu.bme.aut.android.befitt.R
import hu.bme.aut.android.befitt.StatDetailActivity
import hu.bme.aut.android.befitt.adapter.StatItemRecyclerViewAdapter
import hu.bme.aut.android.befitt.databinding.FragmentStatListBinding
import hu.bme.aut.android.befitt.model.Statistics


class StatListFragment : Fragment(), StatItemRecyclerViewAdapter.StatItemClickListener {
    private lateinit var binding: FragmentStatListBinding
    private lateinit var statItemRecyclerViewAdapter: StatItemRecyclerViewAdapter
    private lateinit var statViewModel: StatViewModel
    private lateinit var mContext: Context
    private var twoPane = false

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStatListBinding.inflate(layoutInflater)

        statViewModel = ViewModelProvider(this)[StatViewModel::class.java]
        statViewModel.allStat.observe(viewLifecycleOwner) { stats ->
            statItemRecyclerViewAdapter.submitList(stats)
        }

        if (view?.findViewById<NestedScrollView>(R.id.stat_detail_container) != null){
            twoPane = true
        }

        setupRecyclerView()
        return binding.root
    }

    private fun setupRecyclerView() {
        statItemRecyclerViewAdapter = StatItemRecyclerViewAdapter()
        statItemRecyclerViewAdapter.itemClickListener = this
        binding.root.findViewById<RecyclerView>(R.id.statList).adapter = statItemRecyclerViewAdapter
    }

    override fun onItemClick(statistics: Statistics) {
        if (twoPane){
            val fragment = StatDetailFragment.newInstance(statistics)
            childFragmentManager
                .beginTransaction()
                .replace(R.id.stat_detail_container, fragment)
                .commit()
        } else {
            val intent = Intent(mContext, StatDetailActivity::class.java)
            intent.putExtra(StatDetailActivity.KEY_DESC, statistics)
            startActivity(intent)
        }
    }

    override fun onItemLongClick(position: Int, view: View, statistics: Statistics): Boolean {
        val popup = PopupMenu(context, view)
        popup.inflate(R.menu.menu_stat)
        popup.setOnMenuItemClickListener { item ->
            when(item.itemId) {
                R.id.delete -> {
                    statViewModel.deleteStat(statistics)
                    return@setOnMenuItemClickListener true
                }
            }
            false
        }
        popup.show()
        return false
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        val menuInflater = MenuInflater(mContext)
        menuInflater.inflate(R.menu.menu_list, menu)
        return super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.itemDeleteAll){
            statViewModel.deleteAllStat()
        }
        return super.onOptionsItemSelected(item)
    }
}