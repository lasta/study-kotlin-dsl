package lasta.page.service.workflow

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking

// cf. https://speakerdeck.com/kohii00/kotlinwoyong-itadslde-nashe-ji-shou-fa-toshi-yong-shang-nozhu-yi?slide=19

sealed interface WorkflowNode {
    fun run()
}

class Task(private val action: () -> Unit) : WorkflowNode {
    override fun run() = action()
}

class Sequential(
    private val nodes: List<WorkflowNode>
) : WorkflowNode {
    constructor(vararg nodes: WorkflowNode) : this(nodes.toList())

    override fun run() = nodes.forEach(WorkflowNode::run)
}

class Parallel(
    private val nodes: List<WorkflowNode>,
    private val maxConcurrency: Int?,
) : WorkflowNode {
    constructor(vararg nodes: WorkflowNode, maxConcurrency: Int? = null) : this(nodes.toList(), maxConcurrency)

    override fun run() = runBlocking {
        nodes.chunked(maxConcurrency ?: nodes.size).forEach { chunked ->
            coroutineScope {
                chunked.map { job ->
                    async { job.run() }
                }.awaitAll()
            }
        }
    }
}
