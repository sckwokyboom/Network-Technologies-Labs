package models

import java.io.Serializable

class UploadInfo(val fileName: String, val length: Long) : Serializable {
    override fun toString(): String {
        return "models.UploadInfo(name='$fileName', length=$length)"
    }
}