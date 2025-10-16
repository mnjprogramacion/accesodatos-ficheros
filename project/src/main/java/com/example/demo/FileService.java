package com.example.demo;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class FileService {

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

    public void guardarArchivo(String folderPath, MultipartFile file) throws IOException {
        Path ruta = Paths.get(folderPath, file.getOriginalFilename());
        Files.write(ruta, file.getBytes());
    }

    public boolean eliminar(String path) {
        File file = new File(path);
        if (!file.exists()) return false;

        if (file.isDirectory()) {
            // Eliminar contenido recursivamente
            File[] archivos = file.listFiles();
            if (archivos != null) {
                for (File f : archivos) {
                    eliminar(f.getAbsolutePath());
                }
            }
        }
        return file.delete();
    }
}
