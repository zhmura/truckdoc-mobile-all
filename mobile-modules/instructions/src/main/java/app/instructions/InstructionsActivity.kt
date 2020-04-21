package app.instructions

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.fasterxml.jackson.databind.ObjectMapper
import com.sanda.truckdoc.client.api.v3.sync.instructions.model.InstructionSet
import kotlinx.android.synthetic.main.instructions_activity.*
import javax.inject.Inject

private val json = """
    {
      "version": 1,
      "entries": [
        {
          "type": "branch",
          "id": "aps_safe",
          "displayName": "APS-Safe",
          "icon": "menu/aps_safe.png",
          "entries": [
            {
              "type": "leaf",
              "id": "by_stops",
              "displayName": "Беларусь",
              "icon": "countries/by.png",
              "file": {
                "fileId": 1233,
                "fileName": "belarus_stops.pdf",
                "mimeType": "application/pdf",
                "timestamp": 123232324
              }
            },
            {
              "type": "leaf",
              "id": "pl_stops",
              "displayName": "Poland",
              "icon": "countries/pl.png",
              "file": {
                "fileId": 1234,
                "fileName": "poland_stops.pdf",
                "mimeType": "application/pdf",
                "timestamp": 123232324
              }
            }
          ]
        },
        {
          "type": "branch",
          "id": "instructions",
          "displayName": "Instructions",
          "icon": "menu/instructions.png",
          "entries": [
            {
              "type": "leaf",
              "id": "road_accidents",
              "displayName": "Действия при ДТП",
              "icon": "instructions/ra.png",
              "file": {
                "fileId": 1235,
                "fileName": "ra.pdf",
                "mimeType": "application/pdf",
                "timestamp": 123232324
              }
            },
            {
              "type": "leaf",
              "id": "health_issues",
              "displayName": "Действия при расстройстве здоровья, травме",
              "icon": "instructions/hi.png",
              "file": {
                "fileId": 1236,
                "fileName": "hi.pdf",
                "mimeType": "application/pdf",
                "timestamp": 123232324
              }
            }
          ]
        },
        {
          "type": "branch",
          "id": "country_info",
          "displayName": "Инфо страны",
          "icon": "menu/country_info.png",
          "entries": [
            {
              "type": "leaf",
              "id": "info_by",
              "displayName": "Беларусь",
              "icon": "countries/by.png",
              "file": {
                "fileId": 1237,
                "fileName": "by_info.pdf",
                "mimeType": "application/pdf",
                "timestamp": 123232324
              }
            },
            {
              "type": "leaf",
              "id": "info_ge",
              "displayName": "Германия",
              "icon": "countries/ge.png",
              "file": {
                "fileId": 1238,
                "fileName": "ge_info.pdf",
                "mimeType": "application/pdf",
                "timestamp": 123232324
              }
            }
          ]
        }
      ]
    }
""".trimIndent()

private val mock: InstructionsProvider by lazy {
    object : InstructionsProvider {
        override fun getInstructions(): InstructionSet? {
            return ObjectMapper().readValue(json, InstructionSet::class.java)
        }
    }
}

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

    private val instructionsProvider by lazy { mock }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (applicationContext as InstructionsActivityInjectorProvider).appComponent().inject(this)
        val set = instructionsProvider.getInstructions()

        helper.processIncomingSet(set!!)

        val adapter = InstructionsAdapter()
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

