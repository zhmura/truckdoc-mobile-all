package app.instructions

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import com.sanda.truckdoc.client.api.v3.sync.instructions.model.*
import com.sanda.truckdoc.client.api.v3.sync.instructions.model.InstructionSetNode.Type.BRANCH
import com.sanda.truckdoc.client.api.v3.sync.instructions.model.InstructionSetNode.Type.LEAF
import java.io.File
import javax.inject.Inject
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

class InstructionsHelper @Inject constructor(
        private val dao: InstructionsDao,
        val rootDir: File,
        private val context: Context,
        private val instructionsPrefs: InstructionsPrefs
) {
    fun processIncomingSet(setWithVersion: InstructionSetWithVersion) {
        if (setWithVersion.version > instructionsPrefs.lastKnownInstructionsVersion()) {
            Log.i("InstructionsHelper", "New config version found. Old ${setWithVersion.version} new ${instructionsPrefs.lastKnownInstructionsVersion()}")
            dao.removeAll()
            rootDir.delete()
        }
        instructionsPrefs.lastKnownInstructionsVersion(setWithVersion.version)

        val set = setWithVersion.instructionSet
        val configNodes = set.entries.flatMap { mapNodes(it, null) }
        dao.removeMissingNodes(configNodes.map { it.id })

        configNodes.filter { it.file != null }.forEach { server ->
            val db = dao.find(server.id)
            if (db == null) {
                dao.insert(server.copy(file = server.file!!.copy(pending = server.file.timestamp)))
            } else {
                if (db.file?.timestamp == server.file!!.timestamp)
                    dao.update(server.copy(file = server.file.copy(pending = db.file.pending)))
                else
                    dao.update(server.copy(file = server.file.copy(pending = server.file.timestamp)))
            }
        }
        configNodes.filter { it.file == null }.forEach {
            val db = dao.find(it.id)
            if (db == null) {
                dao.insert(it)
            } else {
                dao.update(it)
            }
        }
    }

    private fun mapNodes(node: InstructionSetNode, parent: InstructionSetNode?): List<InstructionDb> =
            if (node is InstructionSetLeaf)
                listOf(InstructionDb(node.id, LEAF, node.icon, node.displayName, parent?.id, node.file?.toFileDesc()))
            else
                (node as InstructionSetBranch).entries.flatMap { mapNodes(it, node) } + InstructionDb(node.id, BRANCH, node.icon, node.displayName, parent?.id, null)

    private fun InstructionFileInfo.toFileDesc(): FileDesc = FileDesc(fileId, fileName, mimeType, timestamp, null)

    fun getUri(f: FileDesc): Uri = FileProvider.getUriForFile(context, "com.sanda.truckdoc.client.provider", File(rootDir, f.fileName));
}

@OptIn(ExperimentalContracts::class)
fun InstructionsHelper.exists(f: FileDesc?): Boolean {
    contract {
        returns(true) implies (f != null)
    }
    return f?.let { File(this.rootDir, f.fileName).let { it.exists() && it.isFile } } ?: false
}
