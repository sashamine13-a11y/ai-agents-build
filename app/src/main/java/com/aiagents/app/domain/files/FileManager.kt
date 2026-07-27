package com.aiagents.app.domain.files

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import java.io.BufferedReader
import java.io.InputStreamReader

class FileManager(private val context: Context) {

    fun readFile(uri: Uri): String {
        return context.contentResolver.openInputStream(uri)?.use { stream ->
            BufferedReader(InputStreamReader(stream)).readText()
        } ?: throw Exception("Failed to read file")
    }

    fun writeFile(uri: Uri, content: String) {
        context.contentResolver.openOutputStream(uri, "wt")?.use { stream ->
            stream.write(content.toByteArray())
        } ?: throw Exception("Failed to write file")
    }

    fun createFile(parentUri: Uri, name: String, content: String): Uri? {
        val parent = DocumentFile.fromTreeUri(context, parentUri) ?: return null
        val file = parent.createFile("text/plain", name) ?: return null
        context.contentResolver.openOutputStream(file.uri, "wt")?.use {
            it.write(content.toByteArray())
        }
        return file.uri
    }

    fun listFiles(uri: Uri): List<FileInfo> {
        val dir = DocumentFile.fromTreeUri(context, uri) ?: return emptyList()
        return dir.listFiles().map {
            FileInfo(it.name ?: "unknown", it.uri.toString(), it.isDirectory)
        }
    }

    fun getFileName(uri: Uri): String? {
        return DocumentFile.fromSingleUri(context, uri)?.name
    }

    data class FileInfo(val name: String, val uri: String, val isDirectory: Boolean)
}
