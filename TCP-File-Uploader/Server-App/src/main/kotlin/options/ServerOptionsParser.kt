package options

import org.apache.commons.cli.*


object ServerOptionsParser {
    @JvmStatic
    fun parse(args: Array<String>): ServerOptions {
        val cmd: CommandLine
        try {
            cmd = DefaultParser().parse(options(), args)
        } catch (e: ParseException) {
            throw IllegalArgumentException(e.message)
        }
        val port = cmd.getOptionValue("port")
        if (port.toIntOrNull() == null || port.toInt() !in 0..65535) {
            throw IllegalArgumentException("Port must be integer value between 0 and 65535, but was \"$port\".")
        }

        return if (cmd.hasOption("upload-dir")) {
            ServerOptions(
                cmd.getOptionValue("port").toInt(),
                cmd.getOptionValue("upload-dir")
            )
        } else
            ServerOptions(
                cmd.getOptionValue("port").toInt()
            )
    }


    @JvmStatic
    private fun options(): Options {
        val options = Options()

        options.addOption(
            Option(
                "p", "port", true, "server port"
            ).apply { isRequired = true })

        options.addOption(
            Option(
                "ud", "upload-dir", true, "upload directory"
            )
        )

        return options
    }
}