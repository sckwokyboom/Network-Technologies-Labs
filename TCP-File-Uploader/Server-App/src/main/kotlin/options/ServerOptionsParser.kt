package options

import org.apache.commons.cli.*


class ServerOptionsParser {
    fun parse(args: Array<String>): ServerOptions {
        val cmd: CommandLine
        try {
            cmd = DefaultParser().parse(options(), args)
        } catch (e: ParseException) {
            throw IllegalArgumentException(e.message)
        }
        val port = cmd.getOptionValue("port")
        if (port.toIntOrNull() == null || port.toInt() !in 0..65535) {
            println(port)
            throw IllegalArgumentException("Port must be integer value between 0 and 65535")
        }

        return if (cmd.hasOption("destination")) {
            ServerOptions(
                cmd.getOptionValue("port").toInt(),
                cmd.getOptionValue("destination")
            )
        } else
            ServerOptions(
                cmd.getOptionValue("port").toInt()
            )
    }

    companion object FileUploaderServerOptions {

        @JvmStatic
        private fun options(): Options {
            val options = Options()

            options.addOption(
                Option(
                    "p", "port", true, "server port"
                ).apply { isRequired = true })

            options.addOption(
                Option(
                    "d", "destination", true, "upload directory"
                )
            )

            return options
        }
    }
}