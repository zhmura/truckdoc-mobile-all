package app.instructions

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.sanda.truckdoc.client.api.v3.sync.instructions.model.InstructionSet
import kotlinx.android.synthetic.main.instructions_activity.*
import javax.inject.Inject

class InstructionsActivity : AppCompatActivity(R.layout.instructions_activity) {

    companion object {
        fun start(c: Context, parent: InstructionDb?) {
            c.startActivity(Intent(c, InstructionsActivity::class.java).putExtra("parent", parent))
        }
    }

    @Inject
    lateinit var helper: InstructionsHelper

    @Inject
    lateinit var dao: InstructionsDao

    private val parent by lazy { intent.getSerializableExtra("parent") as? InstructionDb }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (applicationContext as InstructionsInjectorProvider).appComponent().inject(this)

        val adapter = InstructionsAdapter(helper)
        recyclerView.adapter = adapter
        if (parent == null)
            dao.findRoot().observe(this, Observer {
                adapter.submitList(it)
            })
        else {
            supportActionBar?.title = parent!!.displayName
            dao.findEntries(parent!!.id).observe(this, Observer {
                adapter.submitList(it)
            })
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}

interface InstructionsProvider {
    fun getInstructions(): InstructionSet?
}

