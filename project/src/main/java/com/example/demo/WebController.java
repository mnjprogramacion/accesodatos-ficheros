package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.net.URLEncoder;



@Controller
public class WebController {

    @Autowired
    private RandomAccessFileService rafService;

    private final List<String> camposFAA = List.of("DNI", "NOMBRE", "DIRECCION", "CP");
    private final List<Integer> camposLengthFAA = List.of(9, 32, 32, 5);



    @Autowired
    private FileService fileService;

    // Carpeta base
    private final String baseFolderPath = "C:\\Users\\matti\\Desktop\\test";

    @GetMapping("/verCarpeta")
    public String listarArchivos(Model model, @RequestParam(required = false) String path) {
        String folderPath = (path != null && !path.isEmpty()) ? path : baseFolderPath;

        List<FileInfo> archivos = fileService.listarArchivos(folderPath);
        model.addAttribute("archivos", archivos);
        model.addAttribute("currentPath", folderPath);

        // Carpeta padre para el botón de retroceder
        File currentFolder = new File(folderPath);
        model.addAttribute("parentPath", currentFolder.getParent());

        return "verCarpeta";
    }

    @GetMapping("/editarArchivo")
    public String editarArchivo(@RequestParam("path") String path, Model model) throws IOException {
        File file = new File(path);
        if (!file.exists() || file.isDirectory() || !file.getName().endsWith(".txt")) {
            return "redirect:/verCarpeta"; 
        }

        String contenido = Files.readString(file.toPath());
        model.addAttribute("archivoPath", path);
        model.addAttribute("contenido", contenido);
        model.addAttribute("archivoName", file.getName());

        // Añadimos parentPath para el botón de volver
        model.addAttribute("parentPath", file.getParent());

        return "editarArchivo";
    }

    // WebController.java
    @GetMapping("/editarRegistroFAA")
    public String editarRegistroFAA(@RequestParam("path") String path,
                                @RequestParam("pos") long pos,
                                Model model) throws IOException {
    Map<String,String> registro = rafService.leerRegistro(path, camposFAA, camposLengthFAA, pos);
    model.addAttribute("registro", registro);
    model.addAttribute("campos", camposFAA);
    model.addAttribute("camposLength", camposLengthFAA);
    model.addAttribute("pos", pos);
    model.addAttribute("archivoPath", path); // 👈 ahora dinámico
    return "editarRegistroFAA";
}


    @GetMapping("/verRegistrosFAA")
    public String verRegistrosFAA(@RequestParam("path") String path, Model model) throws IOException {
        File file = new File(path);
        long longReg = camposLengthFAA.stream().mapToInt(Integer::intValue).sum();
        long numReg = file.exists() ? file.length() / longReg : 0;
        List<Long> posiciones = new ArrayList<>();
        for (long i = 0; i < numReg; i++) posiciones.add(i);
        model.addAttribute("posiciones", posiciones);
        model.addAttribute("archivoPath", path); // 👈 muy importante
        return "verRegistrosFAA";
    }



    @GetMapping("/editarArchivoFAA")
    public String editarArchivoFAA(@RequestParam("path") String path,
                                @RequestParam(defaultValue = "0") long pos,
                                Model model) throws IOException {
        File file = new File(path);
        if (!file.exists() || file.isDirectory()) {
            return "redirect:/verCarpeta";
        }

        long longReg = camposLengthFAA.stream().mapToInt(Integer::intValue).sum();
        long numReg = file.length() / longReg;
        if (pos >= numReg) pos = 0;

        Map<String,String> registro = rafService.leerRegistro(path, camposFAA, camposLengthFAA, pos);
        registro.put("ID", String.valueOf(pos)); // ID = posición del registro


        // --- NUEVO: lista de posiciones existentes ---
        List<Long> posiciones = new ArrayList<>();
        for (long i = 0; i < numReg; i++) posiciones.add(i);

        model.addAttribute("registro", registro);
        model.addAttribute("campos", camposFAA);
        model.addAttribute("camposLength", camposLengthFAA);
        model.addAttribute("pos", pos);
        model.addAttribute("archivoPath", path);
        model.addAttribute("numRegistros", numReg);
        model.addAttribute("posiciones", posiciones); // 👈 añadimos
        return "editarRegistroFAA";
    }


    @GetMapping("/nuevoRegistroFAA")
    public String nuevoRegistroFAA(@RequestParam("path") String path, Model model) throws IOException {
    long nextId = rafService.getNextId(path, camposLengthFAA);

    Map<String,String> registro = new HashMap<>();
    for (String campo : camposFAA) {
        registro.put(campo, "");
    }

    model.addAttribute("registro", registro);
    model.addAttribute("campos", camposFAA);
    model.addAttribute("camposLength", camposLengthFAA);
    model.addAttribute("pos", ""); // posición vacía = nuevo registro
    model.addAttribute("archivoPath", path);
    return "editarRegistroFAA";
}


    @PostMapping("/guardarArchivoTxt")
    public String guardarArchivoTxt(@RequestParam("path") String path,
                                    @RequestParam("contenido") String contenido) throws IOException {
        File file = new File(path);
        if (file.exists() && !file.isDirectory()) {
            Files.writeString(file.toPath(), contenido);
        }
        // Redirigir a la carpeta padre con URL encode
        String encodedPath = URLEncoder.encode(file.getParent(), StandardCharsets.UTF_8);
        return "redirect:/verCarpeta?path=" + encodedPath;
    }

    @PostMapping("/subirArchivo")
    public String subirArchivo(@RequestParam("file") MultipartFile file,
                            @RequestParam("currentPath") String currentPath) throws IOException {
        fileService.guardarArchivo(currentPath, file);

        // Codificar la ruta antes de redirigir
        String encodedPath = URLEncoder.encode(currentPath, StandardCharsets.UTF_8);
        return "redirect:/verCarpeta?path=" + encodedPath;
    }


    @PostMapping("/eliminar")
    public String eliminar(@RequestParam("path") String path,
                        @RequestParam("currentPath") String currentPath) {
        fileService.eliminar(path);
        String encodedPath = URLEncoder.encode(currentPath, StandardCharsets.UTF_8);
        return "redirect:/verCarpeta?path=" + encodedPath;
    }

    @PostMapping("/guardarRegistroFAA")
    public String guardarRegistroFAA(@RequestParam Map<String,String> params) throws IOException {
        String path = params.get("archivoPath");
        long pos;

        File file = new File(path);
        long longReg = camposLengthFAA.stream().mapToInt(Integer::intValue).sum();
        long numReg = file.exists() ? file.length() / longReg : 0;

        // Si es nuevo registro, usar la posición al final
        if(params.get("pos") == null || params.get("pos").isEmpty() || Long.parseLong(params.get("pos")) >= numReg) {
            pos = numReg;
        } else {
            pos = Long.parseLong(params.get("pos"));
        }

        Map<String,String> registro = new HashMap<>();
        for(String campo : camposFAA){
            registro.put(campo, params.getOrDefault(campo,""));
        }

        rafService.guardarRegistro(path, camposFAA, camposLengthFAA, pos, registro);

        System.out.println(registro);

        return "redirect:/editarArchivoFAA?path=" + URLEncoder.encode(path, StandardCharsets.UTF_8) + "&pos=" + pos;
    }

}


