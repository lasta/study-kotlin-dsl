package lasta.page.service.workflow

fun main() {
    val workflow = Sequential(
        createTask("1"),
        Parallel(
            Sequential(
                createTask("2"),
                createTask("3"),
            ),
            Sequential(
                createTask(4),
                Parallel(
                    createTask(5),
                    createTask(6),
                ),
            ),
            Parallel(
                (7..20).map { createTask(it) },
                maxConcurrency = 3
            ),
        ),
        createTask("finalize")
    )

    workflow.run()
}

private fun createTask(suffix: String) = Task { println("Task $suffix") }
private fun createTask(suffix: Int) = createTask(suffix.toString())
