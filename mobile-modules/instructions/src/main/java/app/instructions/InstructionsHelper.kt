package app.instructions

import com.sanda.truckdoc.client.api.v3.sync.instructions.model.*
import javax.inject.Inject

class InstructionsHelper @Inject constructor(
        private val dao: InstructionsDao
) {
    fun processIncomingSet(set: InstructionSet) {
        val configNodes = set.entries.flatMap { mapNodes(it, null) }
        dao.removeMissingNodes(configNodes.map { it.id })

        configNodes.filter { it.file != null }.forEach {
            val db = dao.find(it.id)
            if (db == null) {
                dao.insert(it.copy(file = it.file!!.copy(pending = it.file.timestamp)))
            } else {
                if (db.file?.timestamp == it.file!!.timestamp)
                    dao.update(it)
                else
                    dao.update(it.copy(file = it.file.copy(pending = it.file.timestamp)))
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
}
