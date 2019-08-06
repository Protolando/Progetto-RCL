package server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.stream.Stream;

class FilesManager {

  private static int BUFFER_SIZE = 1024;

  static void writeToFile(String filepath, String data) throws IOException {
    Path path = Paths.get(filepath);
    /*Creo tutte le cartelle per il percorso (-1 perche` non voglio creare una cartella con il nome del file)*/
    Files.createDirectories(path.subpath(0, path.getNameCount() - 1));
    /*Se non esiste creo anche il file*/
    if (!Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
      Files.createFile(path);
    }

    /*Apro il canale*/
    FileChannel out = FileChannel.open(path, StandardOpenOption.WRITE);

    ByteBuffer buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
    Charset charset = StandardCharsets.UTF_8;

    ByteBuffer src = charset.encode(data);
    /*Riempio il buffer fino a capacita`, lo scrivo e ripeto finche` non ho mandato tutti i dati*/
    int i = 0;
    while (i < src.limit()) {
      /*Riempio il buffer*/
      buffer.clear();
      buffer.put(src.array(), i, Math.min(src.limit() - i, BUFFER_SIZE));
      /*E lo scrivo*/
      buffer.flip();
      i += out.write(buffer);
    }

    /*Chiudo il canale*/
    out.close();
  }

  static String readFromFile(String path) throws IOException {
    FileChannel in = FileChannel.open(Paths.get(path), StandardOpenOption.READ);

    ByteBuffer buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
    Charset charset = StandardCharsets.UTF_8;

    /*Leggo tutti i dati inviati e li salvo in una stringa (assumo che le comunicazioni siano sempre stringhe JSON)*/
    StringBuilder builder = new StringBuilder();
    /*finche` posso ancora leggere*/
    /*Leggo*/
    buffer.clear();
    while (in.read(buffer) != -1) {
      /*Svuoto il buffer*/
      buffer.flip();
      builder.append(charset.decode(buffer));
      buffer.clear();
    }
    return builder.toString();
  }

  static String[] getFilesInDir(String dirpath) { ;
    try (Stream<Path> stream = Files.walk(Paths.get(dirpath+"."), 0)) {
      return stream
          .filter(file -> Files.isDirectory(file) && !file.getFileName().toString().equals("."))
          .map(Path::getFileName)
          .map(Path::toString)
          .distinct()
          .toArray(String[]::new);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }
}
