package app.instructions

import com.sanda.truckdoc.client.api.v3.sync.instructions.model.*
import java.io.File
import javax.inject.Inject

class InstructionsHelper @Inject constructor(
        private val dao: InstructionsDao,
        private val rootDir: File
) {
    fun processIncomingSet(set: InstructionSet) {
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

    private fun mapNodes(node: InstructionSetNode, parent: InstructionSetNode?): List<InstructionDb> {
        if (node is InstructionSetLeaf)
            return listOf(InstructionDb(node.id, node.icon, node.displayName, parent?.id, node.file.toFileDesc()))
        else
            return (node as InstructionSetBranch).entries.flatMap { mapNodes(it, node) } + InstructionDb(node.id, node.icon, node.displayName, parent?.id, null)
    }

    private fun InstructionFileInfo.toFileDesc(): FileDesc {
        return FileDesc(fileId, fileName, mimeType, timestamp, null)
    }

    fun exists(f: FileDesc?) = f?.let { File(rootDir, f.fileName).let { it.exists() && it.isFile } } ?: false
}
