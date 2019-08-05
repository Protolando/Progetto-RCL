package share;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class NetworkIntefrace {

  private ByteBuffer buffer;
  private static final int BUFFER_SIZE = 1024;
  private Charset charset;

  public NetworkIntefrace() {
    this.buffer = ByteBuffer.allocate(BUFFER_SIZE);
    charset = StandardCharsets.UTF_8;
  }

  public void write(SocketChannel connection, String msg) throws IOException {
    buffer.clear();
    buffer.put(charset.encode(String.valueOf(msg.length())));
    /*E lo scrivo*/
    buffer.flip();
    connection.write(buffer);

    ByteBuffer src = charset.encode(msg);
    /*Riempio il buffer fino a capacita`, lo scrivo e ripeto finche` non ho mandato tutti i dati*/
    int i = 0;
    while (i < src.limit()) {
      /*Riempio il buffer*/
      buffer.clear();
      buffer.put(src.array(), i, Math.min(src.limit() - i, BUFFER_SIZE));
      /*E lo scrivo*/
      buffer.flip();
      i += connection.write(buffer);
    }
  }

  public String read(SocketChannel connection) throws IOException {
    /*Leggo il messaggio*/
    buffer.clear();
    connection.read(buffer);
    buffer.flip();

    /*Leggo la dimensione del messaggio*/
    CharBuffer msg = charset.decode(buffer);
    char read = msg.get();
    StringBuilder builder = new StringBuilder();
    while (Character.isDigit(read)) {
      msg.mark();
      builder.append(read);
      if (msg.hasRemaining()) {
        read = msg.get();
      } else {
        break;
      }
    }
    int nchars = Integer.parseInt(builder.toString());
    /*Il mark e` sul primo carattere non cifra*/
    msg.reset();

    /*Leggo tutti i dati inviati e li salvo in una stringa (assumo che le comunicazioni siano sempre stringhe JSON)*/
    builder = new StringBuilder();
    builder.append(msg);
    /*finche` posso ancora leggere*/
    while (builder.toString().length() < nchars) {
      /*Leggo*/
      buffer.clear();
      connection.read(buffer);
      /*Svuoto il buffer*/
      buffer.flip();
      builder.append(charset.decode(buffer));
    }

    return builder.toString();
  }
}
