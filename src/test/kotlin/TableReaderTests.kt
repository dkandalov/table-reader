import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class AcceptanceTests {
    @Test
    fun `read csv`() {
        val csv = """
            column0,column1,column2
            0,cell01,cell02
            1,cell11,cell12
        """.trimIndent()
        val lines = csv.split('\n')

        val expected = listOf(
            MyData(0, "cell01", "cell02"),
            MyData(1, "cell11", "cell12")
        )

        assertEquals(expected, readTableWithHeader(lines.first(), lines.drop(1)).map { it.toMyData() })
    }
}

data class MyData(val first: Int, val second: String, val third: String)

private fun Map<String, String>.toMyData(): MyData {
    return MyData(
        first = this["column0"]!!.toInt(),
        second = this["column1"]!!,
        third = this["column2"]!!
    )
}

class TableReaderTests {
    @Test
    fun `empty list`() {
        assertEquals(
            emptyList<Map<String, String>>(),
            readTable(lines = emptyList())
        )
    }

    @Test
    fun `one line without header`() {
        assertEquals(
            listOf(
                mapOf(
                    "0" to "item0",
                    "1" to "item1"
                )
            ),
            readTable(lines = listOf("item0,item1"))
        )
    }

    @Test
    fun `empty line`() {
        assertEquals(
            listOf(emptyMap<String, String>()),
            readTable(lines = listOf(""))
        )
    }

    @Test
    fun `table with two lines`() {
        assertEquals(
            listOf(
                mapOf("0" to "item00", "1" to "item01"),
                mapOf("0" to "item10", "1" to "item11"),
            ),
            readTable(
                lines = listOf(
                    "item00,item01",
                    "item10,item11"
                )
            )
        )
    }

    @Test
    fun `one line with header`() {
        assertEquals(
            listOf(
                mapOf(
                    "column0" to "item0",
                    "column1" to "item1"
                )
            ),
            readTableWithHeader(
                headerLine = "column0,column1",
                lines = listOf("item0,item1")
            )
        )
    }

    @Test
    fun `two lines with header`() {
        assertEquals(
            listOf(
                mapOf("column0" to "item00", "column1" to "item01"),
                mapOf("column0" to "item10", "column1" to "item11")
            ),
            readTableWithHeader(
                headerLine = "column0,column1",
                lines = listOf(
                    "item00,item01",
                    "item10,item11",
                )
            )
        )
    }

    @Test
    fun `specify custom column names`() {
        assertEquals(
            listOf(
                mapOf("column0" to "item00", "column1" to "item01"),
                mapOf("column0" to "item10", "column1" to "item11")
            ),
            readTable(
                lines = listOf(
                    "item00,item01",
                    "item10,item11",
                ),
                headerProvider = listOf("column0", "column1").toHeaderProvider()
            )
        )
    }

    @Test
    fun `one line with header split on tab`() {
        assertEquals(
            listOf(
                mapOf(
                    "column0" to "item0",
                    "column1" to "item1"
                )
            ),
            readTableWithHeader(
                headerLine = "column0\tcolumn1",
                lines = listOf("item0\titem1"),
                splitter = splitOnTab
            )
        )
    }
}

val splitOnComma = splitter(",")
val splitOnTab = splitter("\t")

fun splitter(delimiter: String) = { line: String -> line.splitFields(delimiter) }

fun readTable(
    lines: List<String>,
    headerProvider: (Int) -> String = Int::toString,
    splitter: (String) -> List<String> = splitOnComma
): List<Map<String, String>> =
    lines.map {
        parseLine(it, headerProvider, splitter)
    }

fun readTableWithHeader(
    headerLine: String,
    lines: List<String>,
    splitter: (String) -> List<String> = splitOnComma
): List<Map<String, String>> =
    readTable(lines, headerProviderFor(headerLine, splitter), splitter)

fun List<String>.toHeaderProvider(): (Int) -> String = this::get
fun Map<Int, String>.toHeaderProvider(): (Int) -> String = this::getValue

private fun headerProviderFor(
    headerLine: String,
    splitter: (String) -> List<String>
): (Int) -> String {
    val fields = splitter(headerLine)
    return { fields[it] }
}

private fun parseLine(
    line: String,
    headerProvider: (Int) -> String,
    splitter: (String) -> List<String>
): Map<String, String> {
    val values = splitter(line)
    val header = values.indices.map(headerProvider)
    return header.zip(values).toMap()
}

private fun String.splitFields(delimiter: String) =
    if (this == "") emptyList() else split(delimiter)
