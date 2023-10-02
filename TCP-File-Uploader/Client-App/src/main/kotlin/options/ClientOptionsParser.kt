package options

import org.apache.commons.cli.*
import kotlin.io.path.Path

object ClientOptionsParser {
    @JvmStatic
    fun parse(args: Array<String>): ClientOptions {
        val cmd: CommandLine
        try {
            cmd = DefaultParser().parse(options(), args)
        } catch (e: ParseException) {
            throw IllegalArgumentException(e.message)
        }
        val port = cmd.getOptionValue("port")
        if (port.toIntOrNull() == null || port.toInt() !in 0..65535) {
            throw IllegalArgumentException("Port must be integer value between 0 and 65535.")
        }

        return ClientOptions(
            Path(cmd.getOptionValue("source")),
            cmd.getOptionValue("host"),
            port.toInt()
        )
    }

    @JvmStatic
    private fun options(): Options {
        val options = Options()

        options.addOption(
            Option(
                "s", "source", true, "file path"
            ).apply { isRequired = true })

        options.addOption(
            Option(
                "h", "host", true, "server host"
            ).apply { isRequired = true }
        )

        options.addOption(
            Option(
                "p", "port", true, "server port"
            ).apply { isRequired = true }
        )

        return options
    }
}