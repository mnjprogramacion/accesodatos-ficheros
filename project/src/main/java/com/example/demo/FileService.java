package com.example.demo;

import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class FileService {

    // Método que recibe la ruta de la carpeta y devuelve la lista de archivos
    public List<FileInfo> listarArchivos(String folderPath) {
        File folder = new File(folderPath);
        List<FileInfo> archivos = new ArrayList<>();

        if (folder.exists() && folder.isDirectory()) {
            for (File file : folder.listFiles()) {
                String extension = "";
                String fileName = file.getName();
                int i = fileName.lastIndexOf('.');
                if (i > 0 && i < fileName.length() - 1) {
                    extension = fileName.substring(i + 1);
                }

                archivos.add(new FileInfo(
                        fileName,
                        file.getAbsolutePath(),
                        file.length(),
                        new Date(file.lastModified()),
                        file.isDirectory(),
                        extension
                ));
            }
        }

        return archivos;
    }
}
