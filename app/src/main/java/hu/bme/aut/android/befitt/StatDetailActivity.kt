package hu.bme.aut.android.befitt

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import hu.bme.aut.android.befitt.model.Statistics
import hu.bme.aut.android.befitt.ui.statistics.StatDetailFragment
import hu.bme.aut.android.befitt.ui.statistics.StatListFragment

class StatDetailActivity : AppCompatActivity() {
    companion object {
        const val KEY_DESC = "KEY_DESC"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stat_detail)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (savedInstanceState == null) {
            val fragment = StatDetailFragment.newInstance(intent.getSerializableExtra(KEY_DESC)!! as Statistics)

            supportFragmentManager.beginTransaction()
                .add(R.id.stat_detail_container, fragment)
                .commit()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem) =
        when(item.itemId){
            android.R.id.home -> {
                navigateUpTo(Intent(this, StatListFragment::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
}