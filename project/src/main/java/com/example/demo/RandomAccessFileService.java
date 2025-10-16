package com.example.demo;

import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RandomAccessFileService {

    public Map<String, String> leerRegistro(String filePath, List<String> campos, List<Integer> camposLength, long pos) throws IOException {
        Map<String, String> registro = new HashMap<>();
        File file = new File(filePath);
        long longReg = camposLength.stream().mapToInt(Integer::intValue).sum();

        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            raf.seek(pos * longReg);
            for (int i = 0; i < campos.size(); i++) {
                byte[] buffer = new byte[camposLength.get(i)];
                raf.read(buffer);
                String valor = new String(buffer, "ISO-8859-1").trim();
                registro.put(campos.get(i), valor);
            }
        }
        return registro;
    }

    public void guardarRegistro(String filePath, List<String> campos, List<Integer> camposLength, long pos, Map<String,String> registro) throws IOException {
        File file = new File(filePath);
        long longReg = camposLength.stream().mapToInt(Integer::intValue).sum();

        try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
            long expectedPos = pos * longReg;
            if (expectedPos > raf.length()) {
                // Mover al final del archivo si pos está más allá
                raf.seek(raf.length());
            } else {
                raf.seek(expectedPos);
            }

            for (int i = 0; i < campos.size(); i++) {
                String campo = campos.get(i);
                int len = camposLength.get(i);
                String valor = registro.getOrDefault(campo, "");
                String valorFormateado = String.format("%1$-" + len + "s", valor != null ? valor : "");
                raf.write(valorFormateado.getBytes("ISO-8859-1"), 0, len);
            }
        }
    }


        /**
     * 1) Devuelve el valor de un campo específico de un registro
     */
    public String selectCampo(String filePath, List<String> campos, List<Integer> camposLength,
                              int numRegistro, String nomColumna) throws IOException {

        Map<String, String> registro = leerRegistro(filePath, campos, camposLength, numRegistro);
        return registro.getOrDefault(nomColumna, "");
    }

    /**
     * 2) Devuelve todos los valores de una columna
     */
    public List<String> selectColumna(String filePath, List<String> campos, List<Integer> camposLength,
                                      String nomColumna) throws IOException {

        List<String> valores = new java.util.ArrayList<>();
        File file = new File(filePath);
        long longReg = camposLength.stream().mapToInt(Integer::intValue).sum();
        long numRegistros = file.length() / longReg;

        for (int i = 0; i < numRegistros; i++) {
            String valor = selectCampo(filePath, campos, camposLength, i, nomColumna);
            valores.add(valor);
        }
        return valores;
    }

    /**
     * 3) Devuelve una lista con todos los campos de un registro
     */
    public List<String> selectRowList(String filePath, List<String> campos, List<Integer> camposLength,
                                      int numRegistro) throws IOException {

        Map<String, String> registro = leerRegistro(filePath, campos, camposLength, numRegistro);
        List<String> lista = new java.util.ArrayList<>();
        for (String campo : campos) {
            lista.add(registro.getOrDefault(campo, ""));
        }
        return lista;
    }

    /**
     * 4) Devuelve un HashMap con los datos de un registro
     */
    public Map<String, String> selectRowMap(String filePath, List<String> campos, List<Integer> camposLength,
                                            int numRegistro) throws IOException {
        return leerRegistro(filePath, campos, camposLength, numRegistro);
    }

    /**
     * 5.1) Modifica en el fichero todos los campos del registro indicado
     */
    public void update(String filePath, List<String> campos, List<Integer> camposLength,
                       int row, Map<String, String> nuevosValores) throws IOException {

        guardarRegistro(filePath, campos, camposLength, row, nuevosValores);
    }

    public long getNextId(String filePath, List<Integer> camposLength) throws IOException {
        File file = new File(filePath);
        long longReg = camposLength.stream().mapToInt(Integer::intValue).sum();
        long numRegistros = file.exists() ? file.length() / longReg : 0;
        return numRegistros + 1;
}


}
