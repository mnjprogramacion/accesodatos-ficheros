package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class WebController {

    @Autowired
    private FileService fileService;

    @GetMapping("/verCarpeta")
    public String listarArchivos(Model model) {
        String folderPath = "C:\\Users\\matti\\Desktop\\test"; // Cambia por tu ruta

        // Llamamos al servicio que devuelve la lista de archivos
        List<FileInfo> archivos = fileService.listarArchivos(folderPath);

        model.addAttribute("archivos", archivos);
        return "verCarpeta";
    }
}
